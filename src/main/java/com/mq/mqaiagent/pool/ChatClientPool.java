package com.mq.mqaiagent.pool;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.chatmemory.CachedDatabaseChatMemory;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ChatClient 对象池
 * 用于缓存和复用 ChatClient 实例，避免频繁创建对象造成的性能开销
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/1/17
 */
@Component
@Slf4j
public class ChatClientPool {

    @Resource
    private ChatModel dashscopeChatModel;
    
    @Resource
    private KeepReportMapper keepReportMapper;
    
    @Resource
    private CacheService cacheService;

    /**
     * ChatClient 缓存池
     * Key: 缓存键（由用户ID、系统提示词、是否支持记忆等组成）
     * Value: 缓存的 ChatClient 实例信息
     */
    private final ConcurrentHashMap<String, CachedChatClient> clientCache = new ConcurrentHashMap<>();

    /**
     * 性能统计
     */
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalCreated = new AtomicLong(0);

    /**
     * 缓存配置
     */
    private static final int MAX_CACHE_SIZE = 1000;           // 最大缓存数量
    private static final long CACHE_EXPIRE_MINUTES = 60;     // 缓存过期时间（分钟）
    private static final long INACTIVE_THRESHOLD_MINUTES = 30; // 不活跃阈值（分钟）

    /**
     * 获取或创建 KeepApp 专用的 ChatClient（不支持用户记忆）
     * 
     * @param systemPrompt 系统提示词
     * @return ChatClient 实例
     */
    public ChatClient getKeepAppClient(String systemPrompt) {
        String cacheKey = generateCacheKey("keepapp", null, systemPrompt, false);
        return getOrCreateClient(cacheKey, () -> createKeepAppClient(systemPrompt));
    }

    /**
     * 获取或创建支持用户记忆的 KeepApp ChatClient
     * 
     * @param userId 用户ID
     * @param systemPrompt 系统提示词
     * @return ChatClient 实例
     */
    public ChatClient getKeepAppClientWithMemory(Long userId, String systemPrompt) {
        String cacheKey = generateCacheKey("keepapp", userId, systemPrompt, true);
        return getOrCreateClient(cacheKey, () -> createKeepAppClientWithMemory(userId, systemPrompt));
    }

    /**
     * 获取或创建 MqManus 智能体专用的 ChatClient（不支持记忆）
     * 
     * @param systemPrompt 系统提示词
     * @return ChatClient 实例
     */
    public ChatClient getMqManusClient(String systemPrompt) {
        String cacheKey = generateCacheKey("mqmanus", null, systemPrompt, false);
        return getOrCreateClient(cacheKey, () -> createMqManusClient(systemPrompt));
    }

    /**
     * 获取或创建支持记忆的 MqManus ChatClient
     * 
     * @param userId 用户ID
     * @param systemPrompt 系统提示词
     * @return ChatClient 实例
     */
    public ChatClient getMqManusClientWithMemory(Long userId, String systemPrompt) {
        String cacheKey = generateCacheKey("mqmanus", userId, systemPrompt, true);
        return getOrCreateClient(cacheKey, () -> createMqManusClientWithMemory(userId, systemPrompt));
    }

    /**
     * 通用的获取或创建 ChatClient 方法
     * 
     * @param cacheKey 缓存键
     * @param clientFactory ChatClient 工厂方法
     * @return ChatClient 实例
     */
    private ChatClient getOrCreateClient(String cacheKey, ClientFactory clientFactory) {
        CachedChatClient cachedClient = clientCache.get(cacheKey);
        // 检查缓存是否命中且未过期
        if (cachedClient != null && !cachedClient.isExpired()) {
            cachedClient.updateLastAccessed(); // 更新最后访问时间
            cacheHits.incrementAndGet();
            log.debug("ChatClient 缓存命中，key: {}", cacheKey);
            return cachedClient.getChatClient();
        }
        // 缓存未命中或已过期，创建新实例
        cacheMisses.incrementAndGet();
        totalCreated.incrementAndGet();
        // 检查缓存大小，如果超过限制则清理
        if (clientCache.size() >= MAX_CACHE_SIZE) {
            cleanupExpiredClients();
        }
        ChatClient newClient = clientFactory.create();
        CachedChatClient newCachedClient = new CachedChatClient(newClient);
        clientCache.put(cacheKey, newCachedClient);
        log.debug("创建新的 ChatClient 实例，key: {}, 当前缓存大小: {}", cacheKey, clientCache.size());
        return newClient;
    }

    /**
     * 创建 KeepApp 专用的 ChatClient（不支持记忆）
     */
    private ChatClient createKeepAppClient(String systemPrompt) {
        CachedDatabaseChatMemory chatMemory = new CachedDatabaseChatMemory(keepReportMapper, cacheService);
        return ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 创建支持用户记忆的 KeepApp ChatClient
     */
    private ChatClient createKeepAppClientWithMemory(Long userId, String systemPrompt) {
        CachedDatabaseChatMemory chatMemory = new CachedDatabaseChatMemory(keepReportMapper, cacheService);
        chatMemory.setCurrentUserId(userId);
        return ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 创建 MqManus 专用的 ChatClient（不支持记忆）
     */
    private ChatClient createMqManusClient(String systemPrompt) {
        return ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }

    /**
     * 创建支持记忆的 MqManus ChatClient
     */
    private ChatClient createMqManusClientWithMemory(Long userId, String systemPrompt) {
        CachedDatabaseChatMemory chatMemory = new CachedDatabaseChatMemory(keepReportMapper, cacheService);
        chatMemory.setCurrentUserId(userId);
        return ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 生成缓存键
     * 
     * @param clientType 客户端类型（keepapp/mqmanus）
     * @param userId 用户ID（可为null）
     * @param systemPrompt 系统提示词
     * @param withMemory 是否支持记忆
     * @return 缓存键
     */
    private String generateCacheKey(String clientType, Long userId, String systemPrompt, boolean withMemory) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(clientType);
        if (userId != null) {
            keyBuilder.append(":user:").append(userId);
        }
        
        // 使用系统提示词的哈希值避免键过长
        keyBuilder.append(":prompt:").append(systemPrompt.hashCode());
        
        if (withMemory) {
            keyBuilder.append(":memory");
        }
        return keyBuilder.toString();
    }

    /**
     * 定时清理过期的 ChatClient 实例
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    public void cleanupExpiredClients() {
        int beforeSize = clientCache.size();
        clientCache.entrySet().removeIf(entry -> {
            CachedChatClient cachedClient = entry.getValue();
            boolean shouldRemove = cachedClient.isExpired() || cachedClient.isInactive();
            if (shouldRemove) {
                log.debug("清理过期的 ChatClient，key: {}", entry.getKey());
            }
            return shouldRemove;
        });
        int afterSize = clientCache.size();
        if (beforeSize != afterSize) {
            log.info("ChatClient 缓存清理完成，清理前: {}, 清理后: {}, 清理数量: {}", 
                    beforeSize, afterSize, beforeSize - afterSize);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
                cacheHits.get(),
                cacheMisses.get(),
                totalCreated.get(),
                clientCache.size(),
                calculateHitRate()
        );
    }

    /**
     * 计算缓存命中率
     */
    private double calculateHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    /**
     * 清空所有缓存（用于测试或紧急情况）
     */
    public void clearAll() {
        int size = clientCache.size();
        clientCache.clear();
        log.warn("清空所有 ChatClient 缓存，清理数量: {}", size);
    }

    /**
     * ChatClient 工厂接口
     */
    @FunctionalInterface
    private interface ClientFactory {
        ChatClient create();
    }

    /**
     * 缓存的 ChatClient 包装类
     */
    private static class CachedChatClient {
        private final ChatClient chatClient;
        private final LocalDateTime createdTime;
        private volatile LocalDateTime lastAccessedTime;
        public CachedChatClient(ChatClient chatClient) {
            this.chatClient = chatClient;
            this.createdTime = LocalDateTime.now();
            this.lastAccessedTime = LocalDateTime.now();
        }
        public ChatClient getChatClient() {
            return chatClient;
        }
        public void updateLastAccessed() {
            this.lastAccessedTime = LocalDateTime.now();
        }
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(createdTime.plusMinutes(CACHE_EXPIRE_MINUTES));
        }
        public boolean isInactive() {
            return LocalDateTime.now().isAfter(lastAccessedTime.plusMinutes(INACTIVE_THRESHOLD_MINUTES));
        }
    }

    /**
     * 缓存统计信息
     */
    public record CacheStats(
            long cacheHits,
            long cacheMisses,
            long totalCreated,
            int currentCacheSize,
            double hitRate
    ) {}
}
