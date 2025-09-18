package com.mq.mqaiagent.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 限流配置类
 * 用于控制高并发场景下的请求流量
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/1/17
 */
@Configuration
//@Profile({"prod", "prod-optimized"})
public class RateLimiterConfig {

    /**
     * 全局限流器 - 控制整体 API 调用频率
     * 每秒允许 100 个请求
     */
    @Bean
    public RateLimiter globalRateLimiter() {
        return RateLimiter.create(100.0);
    }

    /**
     * AI 接口限流器 - 控制 AI 调用频率
     * 每秒允许 20 个请求（考虑 AI 服务的处理能力）
     */
    @Bean
    public RateLimiter aiRateLimiter() {
        return RateLimiter.create(20.0);
    }

    /**
     * 用户级别限流器管理器
     * 每个用户独立的限流控制
     */
    @Bean
    public UserRateLimiterManager userRateLimiterManager() {
        return new UserRateLimiterManager();
    }

    /**
     * 异步任务执行器
     * 用于处理异步任务，避免阻塞主线程
     */
    @Bean(name = "aiTaskExecutor")
    public ThreadPoolExecutor aiTaskExecutor() {
        return new ThreadPoolExecutor(
                10,                      // 核心线程数
                50,                      // 最大线程数
                60L,                     // 空闲线程存活时间
                TimeUnit.SECONDS,        // 时间单位
                new ArrayBlockingQueue<>(200),  // 任务队列
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }

    /**
     * 定时任务执行器
     * 用于清理过期的限流器等定时任务
     */
    @Bean
    public ScheduledExecutorService scheduledExecutor() {
        return Executors.newScheduledThreadPool(2);
    }

    /**
     * 用户级别限流器管理器
     */
    public static class UserRateLimiterManager {
        // 存储每个用户的限流器
        private final ConcurrentHashMap<Long, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();
        
        // 每个用户每秒允许的请求数
        private static final double USER_RATE_LIMIT = 5.0;
        
        /**
         * 获取或创建用户限流器
         * 
         * @param userId 用户ID
         * @return 用户对应的限流器
         */
        public RateLimiter getRateLimiter(Long userId) {
            return userRateLimiters.computeIfAbsent(userId, 
                k -> RateLimiter.create(USER_RATE_LIMIT));
        }
        
        /**
         * 尝试获取令牌
         * 
         * @param userId 用户ID
         * @param timeout 超时时间
         * @param unit 时间单位
         * @return 是否获取成功
         */
        public boolean tryAcquire(Long userId, long timeout, TimeUnit unit) {
            RateLimiter limiter = getRateLimiter(userId);
            return limiter.tryAcquire(timeout, unit);
        }
        
        /**
         * 清理不活跃用户的限流器
         * 建议定期调用以释放内存
         */
        public void cleanupInactiveUsers() {
            // 这里可以实现清理逻辑
            // 比如清理超过1小时未使用的限流器
        }
    }
}