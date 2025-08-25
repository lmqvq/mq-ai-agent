package com.mq.mqaiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheService;
import com.mq.mqaiagent.service.TextSimilarityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MQ
 * @description 针对AI响应缓存操作的数据库操作Service实现
 * @createDate 2025-08-25 00:00:00
 */
@Service
@Slf4j
public class AiResponseCacheServiceImpl implements AiResponseCacheService {

    @Resource
    private CacheService cacheService;

    @Resource
    private TextSimilarityService textSimilarityService;

    /**
     * AI响应缓存过期时间（小时）
     */
    private static final long AI_RESPONSE_CACHE_EXPIRE_HOURS = 12;

    /**
     * 问题索引缓存过期时间（小时）
     */
    private static final long QUESTION_INDEX_CACHE_EXPIRE_HOURS = 24;

    /**
     * 问题索引缓存键
     */
    private static final String QUESTION_INDEX_KEY = "mq:ai:agent:question:index";

    /**
     * 最大缓存问题数量
     */
    private static final int MAX_CACHED_QUESTIONS = 1000;

    @Override
    public String getCachedResponse(String question, Long userId) {
        if (StrUtil.isBlank(question)) {
            return null;
        }

        try {
            // 1. 尝试精确匹配
            String exactCacheKey = generateCacheKey(question, userId);
            String exactResponse = cacheService.get(exactCacheKey, String.class);
            if (exactResponse != null) {
                log.debug("AI响应精确缓存命中，question: {}", question.substring(0, Math.min(50, question.length())));
                return exactResponse;
            }

            // 2. 尝试相似问题匹配
            String similarResponse = findSimilarResponse(question, userId);
            if (similarResponse != null) {
                log.debug("AI响应相似缓存命中，question: {}", question.substring(0, Math.min(50, question.length())));
                // 将相似问题的响应也缓存到当前问题的键下
                cacheService.set(exactCacheKey, similarResponse, AI_RESPONSE_CACHE_EXPIRE_HOURS);
                return similarResponse;
            }

            log.debug("AI响应缓存未命中，question: {}", question.substring(0, Math.min(50, question.length())));
            return null;

        } catch (Exception e) {
            log.error("获取AI响应缓存失败，question: {}, error: {}",
                    question.substring(0, Math.min(50, question.length())), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean cacheResponse(String question, String response, Long userId) {
        if (StrUtil.isBlank(question) || StrUtil.isBlank(response)) {
            return false;
        }

        try {
            // 1. 缓存AI响应
            String cacheKey = generateCacheKey(question, userId);
            boolean cacheResult = cacheService.set(cacheKey, response, AI_RESPONSE_CACHE_EXPIRE_HOURS);

            // 2. 更新问题索引
            if (cacheResult) {
                updateQuestionIndex(question, userId);
                log.debug("AI响应缓存成功，question: {}", question.substring(0, Math.min(50, question.length())));
            }

            return cacheResult;

        } catch (Exception e) {
            log.error("缓存AI响应失败，question: {}, error: {}",
                    question.substring(0, Math.min(50, question.length())), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean clearCache(String question, Long userId) {
        if (StrUtil.isBlank(question)) {
            return false;
        }

        try {
            String cacheKey = generateCacheKey(question, userId);
            boolean result = cacheService.delete(cacheKey);

            if (result) {
                // 从问题索引中移除
                removeFromQuestionIndex(question, userId);
                log.debug("AI响应缓存清除成功，question: {}", question.substring(0, Math.min(50, question.length())));
            }

            return result;

        } catch (Exception e) {
            log.error("清除AI响应缓存失败，question: {}, error: {}",
                    question.substring(0, Math.min(50, question.length())), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public AiResponseCacheService.CacheStats getCacheStats() {
        try {
            List<String> questionIndex = getQuestionIndex();
            int totalCachedQuestions = questionIndex.size();

            // 计算缓存命中率需要额外的统计数据，这里简化处理
            return new AiResponseCacheService.CacheStats(totalCachedQuestions, 0, 0.0);

        } catch (Exception e) {
            log.error("获取缓存统计信息失败: {}", e.getMessage(), e);
            return new AiResponseCacheService.CacheStats(0, 0, 0.0);
        }
    }

    /**
     * 生成缓存键
     *
     * @param question 问题
     * @param userId   用户ID（可选）
     * @return 缓存键
     */
    private String generateCacheKey(String question, Long userId) {
        String questionHash = textSimilarityService.generateTextHash(question);
        if (userId != null) {
            return cacheService.generateAiResponseKey(userId + ":" + questionHash);
        }
        return cacheService.generateAiResponseKey(questionHash);
    }

    /**
     * 查找相似问题的响应
     *
     * @param question 当前问题
     * @param userId   用户ID（可选）
     * @return 相似问题的响应，如果没有找到则返回null
     */
    @SuppressWarnings("unchecked")
    private String findSimilarResponse(String question, Long userId) {
        try {
            List<String> questionIndex = getQuestionIndex();
            if (questionIndex.isEmpty()) {
                return null;
            }

            // 计算与所有已缓存问题的相似度
            List<TextSimilarityService.SimilarityResult> similarities = textSimilarityService
                    .calculateSimilarities(question, questionIndex);

            // 查找第一个相似度超过阈值的问题
            for (TextSimilarityService.SimilarityResult result : similarities) {
                if (result.isSimilar(textSimilarityService.getSimilarityThreshold())) {
                    String similarCacheKey = generateCacheKey(result.getText(), userId);
                    String cachedResponse = cacheService.get(similarCacheKey, String.class);
                    if (cachedResponse != null) {
                        log.debug("找到相似问题缓存，原问题: {}, 相似问题: {}, 相似度: {}",
                                question.substring(0, Math.min(30, question.length())),
                                result.getText().substring(0, Math.min(30, result.getText().length())),
                                result.getSimilarity());
                        return cachedResponse;
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("查找相似问题响应失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取问题索引
     *
     * @return 问题列表
     */
    @SuppressWarnings("unchecked")
    private List<String> getQuestionIndex() {
        try {
            List<String> index = cacheService.get(QUESTION_INDEX_KEY, List.class);
            return index != null ? index : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取问题索引失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新问题索引
     *
     * @param question 新问题
     * @param userId   用户ID（可选）
     */
    private void updateQuestionIndex(String question, Long userId) {
        try {
            List<String> questionIndex = getQuestionIndex();

            // 避免重复添加
            if (!questionIndex.contains(question)) {
                questionIndex.add(question);

                // 限制索引大小，移除最旧的问题
                if (questionIndex.size() > MAX_CACHED_QUESTIONS) {
                    questionIndex = questionIndex.subList(
                            questionIndex.size() - MAX_CACHED_QUESTIONS,
                            questionIndex.size());
                }

                cacheService.set(QUESTION_INDEX_KEY, questionIndex, QUESTION_INDEX_CACHE_EXPIRE_HOURS);
            }
        } catch (Exception e) {
            log.error("更新问题索引失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从问题索引中移除问题
     *
     * @param question 要移除的问题
     * @param userId   用户ID（可选）
     */
    private void removeFromQuestionIndex(String question, Long userId) {
        try {
            List<String> questionIndex = getQuestionIndex();
            if (questionIndex.remove(question)) {
                cacheService.set(QUESTION_INDEX_KEY, questionIndex, QUESTION_INDEX_CACHE_EXPIRE_HOURS);
            }
        } catch (Exception e) {
            log.error("从问题索引移除问题失败: {}", e.getMessage(), e);
        }
    }
}
