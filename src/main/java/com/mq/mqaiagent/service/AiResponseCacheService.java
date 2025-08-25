package com.mq.mqaiagent.service;

/**
 * @author MQ
 * @description 针对AI响应缓存操作的数据库操作Service
 * @createDate 2025-08-25 00:00:00
 */
public interface AiResponseCacheService {

    /**
     * 获取缓存的AI响应
     * 首先检查是否有完全匹配的缓存，然后检查相似问题的缓存
     *
     * @param question 用户问题
     * @param userId   用户ID（可选，用于个性化缓存）
     * @return 缓存的AI响应，如果没有找到则返回null
     */
    String getCachedResponse(String question, Long userId);

    /**
     * 缓存AI响应
     *
     * @param question 用户问题
     * @param response AI响应
     * @param userId   用户ID（可选）
     * @return 是否缓存成功
     */
    boolean cacheResponse(String question, String response, Long userId);

    /**
     * 清除指定问题的缓存
     *
     * @param question 问题
     * @param userId   用户ID（可选）
     * @return 是否清除成功
     */
    boolean clearCache(String question, Long userId);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    AiResponseCacheService.CacheStats getCacheStats();

    /**
     * 缓存统计信息
     */
    class CacheStats {
        private final int totalCachedQuestions;
        private final int totalRequests;
        private final double hitRate;

        public CacheStats(int totalCachedQuestions, int totalRequests, double hitRate) {
            this.totalCachedQuestions = totalCachedQuestions;
            this.totalRequests = totalRequests;
            this.hitRate = hitRate;
        }

        public int getTotalCachedQuestions() {
            return totalCachedQuestions;
        }

        public int getTotalRequests() {
            return totalRequests;
        }

        public double getHitRate() {
            return hitRate;
        }

        @Override
        public String toString() {
            return String.format("CacheStats{totalCachedQuestions=%d, totalRequests=%d, hitRate=%.2f%%}",
                    totalCachedQuestions, totalRequests, hitRate * 100);
        }
    }
}
