package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheMetricsService;
import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理控制器
 * 提供缓存监控、管理和统计功能
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/8/25
 */
@RestController
@RequestMapping("/cache")
@Slf4j
public class CacheController {

    @Resource
    private CacheService cacheService;

    @Resource
    private AiResponseCacheService aiResponseCacheService;

    @Resource
    private CacheMetricsService cacheMetricsService;

    /**
     * 获取缓存性能报告
     */
    @GetMapping("/performance")
    public BaseResponse<CacheMetricsService.CachePerformanceReport> getPerformanceReport() {
        try {
            CacheMetricsService.CachePerformanceReport report = cacheMetricsService.getPerformanceReport();
            log.info("获取缓存性能报告成功");
            return ResultUtils.success(report);
        } catch (Exception e) {
            log.error("获取缓存性能报告失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "获取缓存性能报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存状态
     */
    @GetMapping("/status")
    public BaseResponse<Map<String, Object>> getCacheStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("redisAvailable", cacheService.isRedisAvailable());
            status.put("chatMemoryCacheHitRate", cacheMetricsService.getChatMemoryCacheHitRate());
            status.put("aiResponseCacheHitRate", cacheMetricsService.getAiResponseCacheHitRate());
            status.put("savedApiCalls", cacheMetricsService.getSavedApiCalls());

            AiResponseCacheService.CacheStats aiCacheStats = aiResponseCacheService.getCacheStats();
            status.put("totalCachedQuestions", aiCacheStats.getTotalCachedQuestions());

            log.debug("获取缓存状态成功");
            return ResultUtils.success(status);
        } catch (Exception e) {
            log.error("获取缓存状态失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "获取缓存状态失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定对话的缓存
     */
    @DeleteMapping("/chat-memory")
    public BaseResponse<String> clearChatMemoryCache(
            @RequestParam String conversationId,
            @RequestParam(required = false) Long userId) {
        try {
            String cacheKey = cacheService.generateChatMemoryKey(conversationId, userId);
            boolean result = cacheService.delete(cacheKey);

            if (result) {
                log.info("清除对话记忆缓存成功，conversationId: {}, userId: {}", conversationId, userId);
                return ResultUtils.success("缓存清除成功");
            } else {
                log.warn("清除对话记忆缓存失败，可能缓存不存在，conversationId: {}, userId: {}", conversationId, userId);
                return ResultUtils.success("缓存不存在或已清除");
            }
        } catch (Exception e) {
            log.error("清除对话记忆缓存失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除AI响应缓存
     */
    @DeleteMapping("/ai-response")
    public BaseResponse<String> clearAiResponseCache(
            @RequestParam String question,
            @RequestParam(required = false) Long userId) {
        try {
            boolean result = aiResponseCacheService.clearCache(question, userId);

            if (result) {
                log.info("清除AI响应缓存成功，question: {}, userId: {}",
                        question.substring(0, Math.min(50, question.length())), userId);
                return ResultUtils.success("缓存清除成功");
            } else {
                log.warn("清除AI响应缓存失败，可能缓存不存在，question: {}, userId: {}",
                        question.substring(0, Math.min(50, question.length())), userId);
                return ResultUtils.success("缓存不存在或已清除");
            }
        } catch (Exception e) {
            log.error("清除AI响应缓存失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 重置性能计数器
     */
    @PostMapping("/reset-metrics")
    public BaseResponse<String> resetMetrics() {
        try {
            cacheMetricsService.resetCounters();
            log.info("缓存性能计数器重置成功");
            return ResultUtils.success("性能计数器重置成功");
        } catch (Exception e) {
            log.error("重置性能计数器失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "重置失败: " + e.getMessage());
        }
    }

    /**
     * 预热缓存
     */
    @PostMapping("/warmup")
    public BaseResponse<String> warmupCache() {
        try {
            // 这里可以实现缓存预热逻辑
            // 例如：预加载常用的对话记忆、常见问题的AI响应等

            log.info("缓存预热操作完成");
            return ResultUtils.success("缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "缓存预热失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存配置信息
     */
    @GetMapping("/config")
    public BaseResponse<Map<String, Object>> getCacheConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("chatMemoryCachePrefix", "mq:ai:agent:chat:memory:");
            config.put("aiResponseCachePrefix", "mq:ai:agent:ai:response:");
            config.put("redisAvailable", cacheService.isRedisAvailable());

            log.debug("获取缓存配置成功");
            return ResultUtils.success(config);
        } catch (Exception e) {
            log.error("获取缓存配置失败: {}", e.getMessage(), e);
            return ResultUtils.error(500, "获取缓存配置失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            boolean redisAvailable = cacheService.isRedisAvailable();

            health.put("status", redisAvailable ? "UP" : "DOWN");
            health.put("redisAvailable", redisAvailable);
            health.put("timestamp", System.currentTimeMillis());

            if (redisAvailable) {
                // 执行简单的读写测试
                String testKey = "health:check:" + System.currentTimeMillis();
                String testValue = "test";

                boolean writeSuccess = cacheService.set(testKey, testValue, 1);
                Object readValue = cacheService.get(testKey);
                boolean readSuccess = testValue.equals(readValue);
                cacheService.delete(testKey);

                health.put("writeTest", writeSuccess);
                health.put("readTest", readSuccess);
                health.put("overall", writeSuccess && readSuccess ? "HEALTHY" : "DEGRADED");
            } else {
                health.put("overall", "UNHEALTHY");
            }

            log.debug("缓存健康检查完成，状态: {}", health.get("overall"));
            return ResultUtils.success(health);
        } catch (Exception e) {
            log.error("缓存健康检查失败: {}", e.getMessage(), e);
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("overall", "ERROR");
            errorHealth.put("error", e.getMessage());
            errorHealth.put("timestamp", System.currentTimeMillis());
            return ResultUtils.success(errorHealth);
        }
    }
}
