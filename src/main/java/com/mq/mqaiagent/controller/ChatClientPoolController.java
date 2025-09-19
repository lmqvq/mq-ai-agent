package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.annotation.AuthCheck;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.constant.UserConstant;
import com.mq.mqaiagent.pool.ChatClientPool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ChatClient 对象池监控控制器
 * 用于监控和管理 ChatClient 对象池的性能
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/1/17
 */
@RestController
@RequestMapping("/pool")
@Slf4j
public class ChatClientPoolController {

    @Resource
    private ChatClientPool chatClientPool;

    /**
     * 获取 ChatClient 对象池缓存统计信息
     * 
     * @return 缓存统计信息
     */
    @GetMapping("/stats")
    public BaseResponse<ChatClientPool.CacheStats> getCacheStats() {
        try {
            ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
            log.info("ChatClient对象池统计信息: 缓存命中: {}, 缓存未命中: {}, 命中率: {:.2f}%, 当前缓存大小: {}, 总创建数量: {}",
                    stats.cacheHits(), stats.cacheMisses(), stats.hitRate() * 100, 
                    stats.currentCacheSize(), stats.totalCreated());
            return ResultUtils.success(stats);
        } catch (Exception e) {
            log.error("获取ChatClient对象池统计信息失败", e);
            return ResultUtils.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发对象池清理
     * 
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    public BaseResponse<String> manualCleanup() {
        try {
            ChatClientPool.CacheStats beforeStats = chatClientPool.getCacheStats();
            // 执行清理
            chatClientPool.cleanupExpiredClients();
            ChatClientPool.CacheStats afterStats = chatClientPool.getCacheStats();
            int cleanedCount = beforeStats.currentCacheSize() - afterStats.currentCacheSize();
            String message = String.format("清理完成，清理前缓存大小: %d, 清理后缓存大小: %d, 清理数量: %d", 
                    beforeStats.currentCacheSize(), afterStats.currentCacheSize(), cleanedCount);
            log.info("手动清理ChatClient对象池: {}", message);
            return ResultUtils.success(message);
        } catch (Exception e) {
            log.error("手动清理ChatClient对象池失败", e);
            return ResultUtils.error(500, "清理失败: " + e.getMessage());
        }
    }

    /**
     * 清空所有缓存（谨慎使用）
     * 
     * @return 清空结果
     */
    @PostMapping("/clear-all")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> clearAll() {
        try {
            ChatClientPool.CacheStats beforeStats = chatClientPool.getCacheStats();
            // 清空所有缓存
            chatClientPool.clearAll();
            String message = String.format("已清空所有缓存，清理数量: %d", beforeStats.currentCacheSize());
            log.warn("清空所有ChatClient对象池缓存: {}", message);
            return ResultUtils.success(message);
        } catch (Exception e) {
            log.error("清空ChatClient对象池缓存失败", e);
            return ResultUtils.error(500, "清空缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取对象池性能报告
     * 
     * @return 性能报告
     */
    @GetMapping("/performance-report")
    public BaseResponse<PerformanceReport> getPerformanceReport() {
        try {
            ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
            // 计算性能指标
            double hitRate = stats.hitRate();
            long totalRequests = stats.cacheHits() + stats.cacheMisses();
            double objectReuseRate = totalRequests > 0 ? (double) stats.cacheHits() / stats.totalCreated() : 0.0;
            // 性能评估
            String performanceLevel = evaluatePerformance(hitRate);
            PerformanceReport report = new PerformanceReport(
                    stats.cacheHits(),
                    stats.cacheMisses(),
                    stats.totalCreated(),
                    stats.currentCacheSize(),
                    hitRate,
                    objectReuseRate,
                    totalRequests,
                    performanceLevel,
                    generateRecommendations(hitRate, stats.currentCacheSize())
            );
            return ResultUtils.success(report);
        } catch (Exception e) {
            log.error("获取ChatClient对象池性能报告失败", e);
            return ResultUtils.error(500, "获取性能报告失败: " + e.getMessage());
        }
    }

    /**
     * 评估性能等级
     */
    private String evaluatePerformance(double hitRate) {
        if (hitRate >= 0.9) {
            return "优秀";
        } else if (hitRate >= 0.7) {
            return "良好";
        } else if (hitRate >= 0.5) {
            return "一般";
        } else {
            return "需要优化";
        }
    }

    /**
     * 生成优化建议
     */
    private String generateRecommendations(double hitRate, int cacheSize) {
        StringBuilder recommendations = new StringBuilder();
        if (hitRate < 0.5) {
            recommendations.append("缓存命中率较低，建议检查缓存键生成策略；");
        }
        if (cacheSize > 800) {
            recommendations.append("缓存大小较大，建议调整清理策略；");
        } else if (cacheSize < 10) {
            recommendations.append("缓存大小较小，可能需要增加缓存容量；");
        }
        if (recommendations.length() == 0) {
            recommendations.append("当前性能表现良好，继续保持。");
        }
        return recommendations.toString();
    }

    /**
     * 性能报告数据结构
     */
    public record PerformanceReport(
            long cacheHits,
            long cacheMisses,
            long totalCreated,
            int currentCacheSize,
            double hitRate,
            double objectReuseRate,
            long totalRequests,
            String performanceLevel,
            String recommendations
    ) {}
}
