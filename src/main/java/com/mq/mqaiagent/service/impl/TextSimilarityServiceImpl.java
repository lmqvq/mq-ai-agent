package com.mq.mqaiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mq.mqaiagent.service.TextSimilarityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author MQ
 * @description 针对文本相似度计算的数据库操作Service实现
 * @createDate 2025-08-25 00:00:00
 */
@Service
@Slf4j
public class TextSimilarityServiceImpl implements TextSimilarityService {

    /**
     * 相似度阈值，超过此值认为两个问题相似
     */
    private static final double SIMILARITY_THRESHOLD = 0.75;

    /**
     * 最小文本长度，低于此长度的文本不进行相似度计算
     */
    private static final int MIN_TEXT_LENGTH = 5;

    @Override
    public double calculateCosineSimilarity(String text1, String text2) {
        if (StrUtil.isBlank(text1) || StrUtil.isBlank(text2)) {
            return 0.0;
        }

        // 1. 预处理文本
        String processedText1 = preprocessText(text1);
        String processedText2 = preprocessText(text2);

        if (processedText1.length() < MIN_TEXT_LENGTH || processedText2.length() < MIN_TEXT_LENGTH) {
            return 0.0;
        }

        try {
            // 2. 分词并构建词频向量
            Map<String, Integer> vector1 = buildWordFrequencyVector(processedText1);
            Map<String, Integer> vector2 = buildWordFrequencyVector(processedText2);

            // 3. 计算余弦相似度
            return computeCosineSimilarity(vector1, vector2);
        } catch (Exception e) {
            log.error("计算文本相似度失败: text1={}, text2={}, error={}", 
                     text1.substring(0, Math.min(50, text1.length())), 
                     text2.substring(0, Math.min(50, text2.length())), 
                     e.getMessage(), e);
            return 0.0;
        }
    }

    @Override
    public boolean isSimilar(String text1, String text2) {
        double similarity = calculateCosineSimilarity(text1, text2);
        boolean result = similarity >= SIMILARITY_THRESHOLD;
        
        log.debug("文本相似度计算: similarity={}, threshold={}, result={}", 
                 similarity, SIMILARITY_THRESHOLD, result);
        
        return result;
    }

    @Override
    public String generateTextHash(String text) {
        if (StrUtil.isBlank(text)) {
            return "empty";
        }
        
        // 预处理文本后计算哈希
        String processedText = preprocessText(text);
        return String.valueOf(processedText.hashCode());
    }

    @Override
    public double getSimilarityThreshold() {
        return SIMILARITY_THRESHOLD;
    }

    @Override
    public List<TextSimilarityService.SimilarityResult> calculateSimilarities(String targetText, List<String> candidates) {
        if (StrUtil.isBlank(targetText) || candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        return candidates.stream()
                .map(candidate -> new TextSimilarityService.SimilarityResult(candidate, calculateCosineSimilarity(targetText, candidate)))
                .filter(result -> result.getSimilarity() > 0.0)
                .sorted((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()))
                .collect(Collectors.toList());
    }

    /**
     * 文本预处理
     *
     * @param text 原始文本
     * @return 预处理后的文本
     */
    private String preprocessText(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }

        return text
                // 转换为小写
                .toLowerCase()
                // 移除多余的空白字符
                .replaceAll("\\s+", " ")
                // 移除标点符号（保留中文字符）
                .replaceAll("[\\p{Punct}&&[^\\u4e00-\\u9fa5]]", "")
                // 去除首尾空格
                .trim();
    }

    /**
     * 构建词频向量
     * 使用简单的字符级别分词（适合中文）
     *
     * @param text 预处理后的文本
     * @return 词频向量
     */
    private Map<String, Integer> buildWordFrequencyVector(String text) {
        Map<String, Integer> vector = new HashMap<>();
        
        // 使用字符级别的n-gram（这里使用2-gram）
        for (int i = 0; i < text.length() - 1; i++) {
            String gram = text.substring(i, i + 2);
            vector.merge(gram, 1, Integer::sum);
        }
        
        // 如果文本太短，也添加单字符
        if (text.length() <= 3) {
            for (char c : text.toCharArray()) {
                String singleChar = String.valueOf(c);
                vector.merge(singleChar, 1, Integer::sum);
            }
        }
        
        return vector;
    }

    /**
     * 计算两个词频向量的余弦相似度
     *
     * @param vector1 第一个向量
     * @param vector2 第二个向量
     * @return 余弦相似度
     */
    private double computeCosineSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        // 获取所有维度
        Set<String> allDimensions = new HashSet<>();
        allDimensions.addAll(vector1.keySet());
        allDimensions.addAll(vector2.keySet());

        // 计算点积和模长
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String dimension : allDimensions) {
            int value1 = vector1.getOrDefault(dimension, 0);
            int value2 = vector2.getOrDefault(dimension, 0);

            dotProduct += value1 * value2;
            norm1 += value1 * value1;
            norm2 += value2 * value2;
        }

        // 避免除零
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
