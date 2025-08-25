package com.mq.mqaiagent.service.impl;

import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheMetricsService;
import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author MQ
 * @description 针对缓存性能监控操作的数据库操作Service实现
 * @createDate 2025-08-25 00:00:00
 */
@Service
@Slf4j
public class CacheMetricsServiceImpl implements CacheMetricsService {

    @Resource
    private CacheService cacheService;

    @Resource
    private AiResponseCacheService aiResponseCacheService;

    /**
     * 对话记忆缓存命中次数
     */
    private final AtomicLong chatMemoryCacheHits = new AtomicLong(0);

    /**
     * 对话记忆缓存未命中次数
     */
    private final AtomicLong chatMemoryCacheMisses = new AtomicLong(0);

    /**
     * AI响应缓存命中次数
     */
    private final AtomicLong aiResponseCacheHits = new AtomicLong(0);

    /**
     * AI响应缓存未命中次数
     */
    private final AtomicLong aiResponseCacheMisses = new AtomicLong(0);

    /**
     * 节省的API调用次数
     */
    private final AtomicLong savedApiCalls = new AtomicLong(0);

    /**
     * 缓存指标键前缀
     */
    private static final String METRICS_PREFIX = "mq:ai:agent:metrics:";

    @Override
    public void recordChatMemoryCacheHit() {
        chatMemoryCacheHits.incrementAndGet();
        log.debug("对话记忆缓存命中，总命中次数: {}", chatMemoryCacheHits.get());
    }

    @Override
    public void recordChatMemoryCacheMiss() {
        chatMemoryCacheMisses.incrementAndGet();
        log.debug("对话记忆缓存未命中，总未命中次数: {}", chatMemoryCacheMisses.get());
    }

    @Override
    public void recordAiResponseCacheHit() {
        aiResponseCacheHits.incrementAndGet();
        savedApiCalls.incrementAndGet();
        log.debug("AI响应缓存命中，总命中次数: {}, 节省API调用: {}",
                aiResponseCacheHits.get(), savedApiCalls.get());
    }

    @Override
    public void recordAiResponseCacheMiss() {
        aiResponseCacheMisses.incrementAndGet();
        log.debug("AI响应缓存未命中，总未命中次数: {}", aiResponseCacheMisses.get());
    }

    @Override
    public double getChatMemoryCacheHitRate() {
        long hits = chatMemoryCacheHits.get();
        long misses = chatMemoryCacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total;
    }

    @Override
    public double getAiResponseCacheHitRate() {
        long hits = aiResponseCacheHits.get();
        long misses = aiResponseCacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total;
    }

    @Override
    public long getSavedApiCalls() {
        return savedApiCalls.get();
    }

    @Override
    public CacheMetricsService.CachePerformanceReport getPerformanceReport() {
        // 获取AI响应缓存统计
        AiResponseCacheService.CacheStats aiCacheStats = aiResponseCacheService.getCacheStats();

        return new CacheMetricsService.CachePerformanceReport(
                // 对话记忆缓存指标
                chatMemoryCacheHits.get(),
                chatMemoryCacheMisses.get(),
                getChatMemoryCacheHitRate(),

                // AI响应缓存指标
                aiResponseCacheHits.get(),
                aiResponseCacheMisses.get(),
                getAiResponseCacheHitRate(),
                aiCacheStats.getTotalCachedQuestions(),

                // 性能节省指标
                savedApiCalls.get(),

                // Redis连接状态
                cacheService.isRedisAvailable());
    }

    @Override
    public void resetCounters() {
        chatMemoryCacheHits.set(0);
        chatMemoryCacheMisses.set(0);
        aiResponseCacheHits.set(0);
        aiResponseCacheMisses.set(0);
        savedApiCalls.set(0);

        log.info("缓存性能计数器已重置");
    }

    @Override
    public void persistMetrics() {
        try {
            if (!cacheService.isRedisAvailable()) {
                log.warn("Redis不可用，无法持久化性能指标");
                return;
            }

            CacheMetricsService.CachePerformanceReport report = getPerformanceReport();
            String metricsKey = METRICS_PREFIX + "performance_report";

            cacheService.set(metricsKey, report, 24); // 保存24小时
            log.debug("缓存性能指标已持久化到Redis");

        } catch (Exception e) {
            log.error("持久化缓存性能指标失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public CacheMetricsService.CachePerformanceReport loadPersistedMetrics() {
        try {
            if (!cacheService.isRedisAvailable()) {
                log.warn("Redis不可用，无法加载持久化的性能指标");
                return getPerformanceReport();
            }

            String metricsKey = METRICS_PREFIX + "performance_report";
            CacheMetricsService.CachePerformanceReport report = cacheService.get(metricsKey, CacheMetricsService.CachePerformanceReport.class);

            if (report != null) {
                log.debug("从Redis加载了持久化的缓存性能指标");
                return report;
            }

        } catch (Exception e) {
            log.error("加载持久化的缓存性能指标失败: {}", e.getMessage(), e);
        }

        return getPerformanceReport();
    }
}
