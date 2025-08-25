package com.mq.mqaiagent.service;

import java.util.List;

/**
 * @author MQ
 * @description 针对文本相似度计算的数据库操作Service
 * @createDate 2025-08-25 00:00:00
 */
public interface TextSimilarityService {

    /**
     * 计算两个文本的余弦相似度
     *
     * @param text1 第一个文本
     * @param text2 第二个文本
     * @return 相似度值，范围[0,1]，1表示完全相同
     */
    double calculateCosineSimilarity(String text1, String text2);

    /**
     * 判断两个文本是否相似
     *
     * @param text1 第一个文本
     * @param text2 第二个文本
     * @return 是否相似
     */
    boolean isSimilar(String text1, String text2);

    /**
     * 生成文本的哈希值，用于缓存键
     *
     * @param text 输入文本
     * @return 哈希值字符串
     */
    String generateTextHash(String text);

    /**
     * 获取相似度阈值
     *
     * @return 相似度阈值
     */
    double getSimilarityThreshold();

    /**
     * 批量计算文本与候选文本列表的相似度
     *
     * @param targetText 目标文本
     * @param candidates 候选文本列表
     * @return 相似度结果列表，按相似度降序排列
     */
    List<TextSimilarityService.SimilarityResult> calculateSimilarities(String targetText, List<String> candidates);

    /**
     * 相似度计算结果
     */
    class SimilarityResult {
        private final String text;
        private final double similarity;

        public SimilarityResult(String text, double similarity) {
            this.text = text;
            this.similarity = similarity;
        }

        public String getText() {
            return text;
        }

        public double getSimilarity() {
            return similarity;
        }

        public boolean isSimilar(double threshold) {
            return similarity >= threshold;
        }

        @Override
        public String toString() {
            return String.format("SimilarityResult{text='%s', similarity=%.3f}",
                    text.length() > 30 ? text.substring(0, 30) + "..." : text,
                    similarity);
        }
    }
}
