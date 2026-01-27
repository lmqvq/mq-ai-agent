package com.mq.mqaiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mq.mqaiagent.ai.AiModelRouter;
import com.mq.mqaiagent.ai.AiModelType;
import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheService;
import com.mq.mqaiagent.service.TextSimilarityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 针对 AI 响应缓存操作的 Service 实现。
 * 关键点：缓存键与相似问题索引都需要按模型隔离，避免不同模型串缓存。
 */
@Service
@Slf4j
public class AiResponseCacheServiceImpl implements AiResponseCacheService {

    @Resource
    private CacheService cacheService;

    @Resource
    private TextSimilarityService textSimilarityService;

    @Resource
    private AiModelRouter aiModelRouter;

    /**
     * AI 响应缓存过期时间（小时）
     */
    private static final long AI_RESPONSE_CACHE_EXPIRE_HOURS = 12;

    /**
     * 问题索引缓存过期时间（小时）
     */
    private static final long QUESTION_INDEX_CACHE_EXPIRE_HOURS = 24;

    /**
     * 问题索引缓存键（基础前缀）
     */
    private static final String QUESTION_INDEX_KEY_BASE = "mq:ai:agent:question:index";

    /**
     * 最大缓存问题数量
     */
    private static final int MAX_CACHED_QUESTIONS = 1000;

    @Override
    public String getCachedResponse(String question, Long userId) {
        return getCachedResponse(question, userId, aiModelRouter.getDefaultModelType());
    }

    @Override
    public String getCachedResponse(String question, Long userId, AiModelType modelType) {
        if (StrUtil.isBlank(question)) {
            return null;
        }
        AiModelType resolvedModelType = resolveModelType(modelType);

        try {
            // 1. 尝试精确匹配
            String exactCacheKey = generateCacheKey(question, userId, resolvedModelType);
            String exactResponse = cacheService.get(exactCacheKey, String.class);
            if (exactResponse != null) {
                log.debug("AI 响应精确缓存命中，model: {}, question: {}",
                        resolvedModelType.getCode(), abbreviate(question, 50));
                return exactResponse;
            }

            // 2. 尝试相似问题匹配
            String similarResponse = findSimilarResponse(question, userId, resolvedModelType);
            if (similarResponse != null) {
                log.debug("AI 响应相似缓存命中，model: {}, question: {}",
                        resolvedModelType.getCode(), abbreviate(question, 50));
                // 将相似问题的响应也缓存到当前问题的键中
                cacheService.set(exactCacheKey, similarResponse, AI_RESPONSE_CACHE_EXPIRE_HOURS);
                return similarResponse;
            }

            log.debug("AI 响应缓存未命中，model: {}, question: {}",
                    resolvedModelType.getCode(), abbreviate(question, 50));
            return null;

        } catch (Exception e) {
            log.error("获取 AI 响应缓存失败，model: {}, question: {}, error: {}",
                    resolvedModelType.getCode(), abbreviate(question, 50), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean cacheResponse(String question, String response, Long userId) {
        return cacheResponse(question, response, userId, aiModelRouter.getDefaultModelType());
    }

    @Override
    public boolean cacheResponse(String question, String response, Long userId, AiModelType modelType) {
        if (StrUtil.isBlank(question) || StrUtil.isBlank(response)) {
            return false;
        }
        AiModelType resolvedModelType = resolveModelType(modelType);

        try {
            // 1. 缓存 AI 响应
            String cacheKey = generateCacheKey(question, userId, resolvedModelType);
            boolean cacheResult = cacheService.set(cacheKey, response, AI_RESPONSE_CACHE_EXPIRE_HOURS);

            // 2. 更新问题索引
            if (cacheResult) {
                updateQuestionIndex(question, resolvedModelType);
                log.debug("AI 响应缓存成功，model: {}, question: {}",
                        resolvedModelType.getCode(), abbreviate(question, 50));
            }

            return cacheResult;

        } catch (Exception e) {
            log.error("缓存 AI 响应失败，model: {}, question: {}, error: {}",
                    resolvedModelType.getCode(), abbreviate(question, 50), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean clearCache(String question, Long userId) {
        return clearCache(question, userId, aiModelRouter.getDefaultModelType());
    }

    @Override
    public boolean clearCache(String question, Long userId, AiModelType modelType) {
        if (StrUtil.isBlank(question)) {
            return false;
        }
        AiModelType resolvedModelType = resolveModelType(modelType);

        try {
            String cacheKey = generateCacheKey(question, userId, resolvedModelType);
            boolean result = cacheService.delete(cacheKey);

            if (result) {
                removeFromQuestionIndex(question, resolvedModelType);
                log.debug("AI 响应缓存清除成功，model: {}, question: {}",
                        resolvedModelType.getCode(), abbreviate(question, 50));
            }

            return result;

        } catch (Exception e) {
            log.error("清除 AI 响应缓存失败，model: {}, question: {}, error: {}",
                    resolvedModelType.getCode(), abbreviate(question, 50), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public AiResponseCacheService.CacheStats getCacheStats() {
        return getCacheStats(aiModelRouter.getDefaultModelType());
    }

    private AiResponseCacheService.CacheStats getCacheStats(AiModelType modelType) {
        try {
            List<String> questionIndex = getQuestionIndex(modelType);
            int totalCachedQuestions = questionIndex.size();
            return new AiResponseCacheService.CacheStats(totalCachedQuestions, 0, 0.0);
        } catch (Exception e) {
            log.error("获取缓存统计信息失败: {}", e.getMessage(), e);
            return new AiResponseCacheService.CacheStats(0, 0, 0.0);
        }
    }

    private AiModelType resolveModelType(AiModelType modelType) {
        return modelType == null ? aiModelRouter.getDefaultModelType() : modelType;
    }

    private boolean isLegacyModel(AiModelType modelType) {
        // 为了兼容现有缓存，qwen-plus 继续使用旧的 key 结构
        return modelType == AiModelType.QWEN_PLUS;
    }

    /**
     * 生成缓存键。
     * 对 qwen-plus 保持旧逻辑不变；其他模型增加模型前缀隔离。
     */
    private String generateCacheKey(String question, Long userId, AiModelType modelType) {
        String questionHash = textSimilarityService.generateTextHash(question);
        String modelScopedHash = isLegacyModel(modelType)
                ? questionHash
                : modelType.getCode() + ":" + questionHash;

        if (userId != null) {
            return cacheService.generateAiResponseKey(userId + ":" + modelScopedHash);
        }
        return cacheService.generateAiResponseKey(modelScopedHash);
    }

    /**
     * 查找相似问题的响应
     *
     * @param question 当前问题
     * @param userId   用户ID（可选）
     * @return 相似问题的响应，如果没有找到则返回null
     */
    @SuppressWarnings("unchecked")
    private String findSimilarResponse(String question, Long userId, AiModelType modelType) {
        try {
            List<String> questionIndex = getQuestionIndex(modelType);
            if (questionIndex.isEmpty()) {
                return null;
            }

            List<TextSimilarityService.SimilarityResult> similarities = textSimilarityService
                    .calculateSimilarities(question, questionIndex);

            for (TextSimilarityService.SimilarityResult result : similarities) {
                if (result.isSimilar(textSimilarityService.getSimilarityThreshold())) {
                    String similarCacheKey = generateCacheKey(result.getText(), userId, modelType);
                    String cachedResponse = cacheService.get(similarCacheKey, String.class);
                    if (cachedResponse != null) {
                        log.debug("找到相似问题缓存，model: {}, 原问题: {}, 相似问题: {}, 相似度: {}",
                                modelType.getCode(),
                                abbreviate(question, 30),
                                abbreviate(result.getText(), 30),
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

    private String getQuestionIndexKey(AiModelType modelType) {
        return isLegacyModel(modelType)
                ? QUESTION_INDEX_KEY_BASE
                : QUESTION_INDEX_KEY_BASE + ":" + modelType.getCode();
    }

    /**
     * 获取问题索引。
     */
    @SuppressWarnings("unchecked")
    private List<String> getQuestionIndex(AiModelType modelType) {
        try {
            String key = getQuestionIndexKey(modelType);
            List<String> index = cacheService.get(key, List.class);
            return index != null ? index : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取问题索引失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新问题索引。
     */
    private void updateQuestionIndex(String question, AiModelType modelType) {
        try {
            String key = getQuestionIndexKey(modelType);
            List<String> questionIndex = getQuestionIndex(modelType);

            if (!questionIndex.contains(question)) {
                questionIndex.add(question);

                if (questionIndex.size() > MAX_CACHED_QUESTIONS) {
                    questionIndex = questionIndex.subList(
                            questionIndex.size() - MAX_CACHED_QUESTIONS,
                            questionIndex.size());
                }

                cacheService.set(key, questionIndex, QUESTION_INDEX_CACHE_EXPIRE_HOURS);
            }
        } catch (Exception e) {
            log.error("更新问题索引失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从问题索引中移除问题。
     */
    private void removeFromQuestionIndex(String question, AiModelType modelType) {
        try {
            String key = getQuestionIndexKey(modelType);
            List<String> questionIndex = getQuestionIndex(modelType);
            if (questionIndex.remove(question)) {
                cacheService.set(key, questionIndex, QUESTION_INDEX_CACHE_EXPIRE_HOURS);
            }
        } catch (Exception e) {
            log.error("从问题索引中移除问题失败: {}", e.getMessage(), e);
        }
    }

    private String abbreviate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        return text.substring(0, Math.min(maxLen, text.length()));
    }
}
