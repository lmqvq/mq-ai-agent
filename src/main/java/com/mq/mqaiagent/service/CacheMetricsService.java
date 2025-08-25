package com.mq.mqaiagent.service;

/**
 * @author MQ
 * @description 针对缓存性能监控操作的数据库操作Service
 * @createDate 2025-08-25 00:00:00
 */
public interface CacheMetricsService {

    /**
     * 记录对话记忆缓存命中
     */
    void recordChatMemoryCacheHit();

    /**
     * 记录对话记忆缓存未命中
     */
    void recordChatMemoryCacheMiss();

    /**
     * 记录AI响应缓存命中
     */
    void recordAiResponseCacheHit();

    /**
     * 记录AI响应缓存未命中
     */
    void recordAiResponseCacheMiss();

    /**
     * 获取对话记忆缓存命中率
     *
     * @return 命中率（0.0-1.0）
     */
    double getChatMemoryCacheHitRate();

    /**
     * 获取AI响应缓存命中率
     *
     * @return 命中率（0.0-1.0）
     */
    double getAiResponseCacheHitRate();

    /**
     * 获取节省的API调用次数
     *
     * @return 节省的API调用次数
     */
    long getSavedApiCalls();

    /**
     * 获取完整的缓存性能报告
     *
     * @return 性能报告
     */
    CacheMetricsService.CachePerformanceReport getPerformanceReport();

    /**
     * 重置所有计数器
     */
    void resetCounters();

    /**
     * 将性能指标持久化到Redis
     */
    void persistMetrics();

    /**
     * 从Redis加载性能指标
     *
     * @return 性能报告
     */
    CacheMetricsService.CachePerformanceReport loadPersistedMetrics();

    /**
     * 缓存性能报告
     */
    class CachePerformanceReport {
        private final long chatMemoryCacheHits;
        private final long chatMemoryCacheMisses;
        private final double chatMemoryCacheHitRate;

        private final long aiResponseCacheHits;
        private final long aiResponseCacheMisses;
        private final double aiResponseCacheHitRate;
        private final int totalCachedQuestions;

        private final long savedApiCalls;
        private final boolean redisAvailable;

        private final long timestamp;

        public CachePerformanceReport(long chatMemoryCacheHits, long chatMemoryCacheMisses,
                double chatMemoryCacheHitRate, long aiResponseCacheHits,
                long aiResponseCacheMisses, double aiResponseCacheHitRate,
                int totalCachedQuestions, long savedApiCalls, boolean redisAvailable) {
            this.chatMemoryCacheHits = chatMemoryCacheHits;
            this.chatMemoryCacheMisses = chatMemoryCacheMisses;
            this.chatMemoryCacheHitRate = chatMemoryCacheHitRate;
            this.aiResponseCacheHits = aiResponseCacheHits;
            this.aiResponseCacheMisses = aiResponseCacheMisses;
            this.aiResponseCacheHitRate = aiResponseCacheHitRate;
            this.totalCachedQuestions = totalCachedQuestions;
            this.savedApiCalls = savedApiCalls;
            this.redisAvailable = redisAvailable;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public long getChatMemoryCacheHits() {
            return chatMemoryCacheHits;
        }

        public long getChatMemoryCacheMisses() {
            return chatMemoryCacheMisses;
        }

        public double getChatMemoryCacheHitRate() {
            return chatMemoryCacheHitRate;
        }

        public long getAiResponseCacheHits() {
            return aiResponseCacheHits;
        }

        public long getAiResponseCacheMisses() {
            return aiResponseCacheMisses;
        }

        public double getAiResponseCacheHitRate() {
            return aiResponseCacheHitRate;
        }

        public int getTotalCachedQuestions() {
            return totalCachedQuestions;
        }

        public long getSavedApiCalls() {
            return savedApiCalls;
        }

        public boolean isRedisAvailable() {
            return redisAvailable;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "CachePerformanceReport{\n" +
                            "  对话记忆缓存: 命中=%d, 未命中=%d, 命中率=%.2f%%\n" +
                            "  AI响应缓存: 命中=%d, 未命中=%d, 命中率=%.2f%%\n" +
                            "  缓存问题总数: %d\n" +
                            "  节省API调用: %d次\n" +
                            "  Redis状态: %s\n" +
                            "  统计时间: %d\n" +
                            "}",
                    chatMemoryCacheHits, chatMemoryCacheMisses, chatMemoryCacheHitRate * 100,
                    aiResponseCacheHits, aiResponseCacheMisses, aiResponseCacheHitRate * 100,
                    totalCachedQuestions, savedApiCalls,
                    redisAvailable ? "可用" : "不可用", timestamp);
        }
    }
}
