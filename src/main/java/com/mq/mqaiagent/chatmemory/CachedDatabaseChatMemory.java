package com.mq.mqaiagent.chatmemory;

import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 带缓存的数据库对话记忆实现
 * 继承DatabaseChatMemory，增加Redis缓存层，实现双层缓存策略
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/8/25
 */
@Slf4j
public class CachedDatabaseChatMemory extends DatabaseChatMemory implements ChatMemory {

    private final CacheService cacheService;

    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 6;

    /**
     * 构造函数
     *
     * @param keepReportMapper 数据库映射器
     * @param cacheService     缓存服务
     */
    public CachedDatabaseChatMemory(KeepReportMapper keepReportMapper, CacheService cacheService) {
        super(keepReportMapper);
        this.cacheService = cacheService;
        log.info("CachedDatabaseChatMemory 初始化完成");
    }

    /**
     * 获取当前用户ID
     *
     * @return 当前用户ID
     */
    protected Long getCurrentUserId() {
        // 通过反射或直接访问父类的currentUserId字段
        // 由于父类的currentUserId是private，我们需要通过其他方式获取
        // 这里我们添加一个字段来跟踪当前用户ID
        return this.currentUserId;
    }

    /**
     * 当前用户ID（用于缓存键生成）
     */
    private Long currentUserId;

    /**
     * 设置当前用户ID（重写父类方法以同步本地字段）
     *
     * @param userId 用户ID
     */
    @Override
    public void setCurrentUserId(Long userId) {
        super.setCurrentUserId(userId);
        this.currentUserId = userId;
    }

    /**
     * 向指定对话 ID 的记忆中添加消息列表（带缓存）
     * 会将新消息追加到现有消息列表末尾，并同时更新缓存和数据库
     *
     * @param conversationId 对话的唯一标识符
     * @param messages       要添加的消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        try {
            // 1. 先调用父类方法更新数据库
            super.add(conversationId, messages);

            // 2. 更新缓存
            updateCache(conversationId, getCurrentUserId());

            log.debug("对话记忆添加成功，conversationId: {}, messageCount: {}", conversationId, messages.size());
        } catch (Exception e) {
            log.error("对话记忆添加失败，conversationId: {}, error: {}", conversationId, e.getMessage(), e);
            // 即使缓存更新失败，数据库操作已成功，不抛出异常
        }
    }

    /**
     * 向指定对话 ID 的记忆中添加消息列表（支持用户ID，带缓存）
     *
     * @param conversationId 对话的唯一标识符
     * @param userId         用户ID
     * @param messages       要添加的消息列表
     */
    public void add(String conversationId, Long userId, List<Message> messages) {
        try {
            // 1. 先调用父类方法更新数据库
            super.add(conversationId, userId, messages);

            // 2. 更新缓存
            updateCache(conversationId, userId);

            log.debug("对话记忆添加成功，conversationId: {}, userId: {}, messageCount: {}",
                    conversationId, userId, messages.size());
        } catch (Exception e) {
            log.error("对话记忆添加失败，conversationId: {}, userId: {}, error: {}",
                    conversationId, userId, e.getMessage(), e);
            // 即使缓存更新失败，数据库操作已成功，不抛出异常
        }
    }

    /**
     * 获取指定对话 ID 的最近 N 条消息（带缓存）
     * 优先从缓存获取，缓存未命中时从数据库获取并回写缓存
     *
     * @param conversationId 对话的唯一标识符
     * @param lastN          要获取的最近消息的数量
     * @return 包含最近 N 条消息的列表，按时间顺序排列（旧 -> 新）
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Message> get(String conversationId, int lastN) {
        String cacheKey = cacheService.generateChatMemoryKey(conversationId, getCurrentUserId());

        try {
            // 1. 优先从缓存获取
            if (cacheService.isRedisAvailable()) {
                List<Message> cachedMessages = cacheService.get(cacheKey, List.class);
                if (cachedMessages != null) {
                    // 缓存命中，返回最近N条消息
                    return getLastNMessages(cachedMessages, lastN);
                }
            }

            // 2. 缓存未命中，从数据库获取
            List<Message> dbMessages = super.get(conversationId, lastN);

            // 3. 回写缓存（存储完整消息列表，而不仅仅是lastN条）
            if (cacheService.isRedisAvailable() && !dbMessages.isEmpty()) {
                // 获取完整消息列表用于缓存
                List<Message> fullMessages = super.get(conversationId, Integer.MAX_VALUE);
                cacheService.set(cacheKey, fullMessages, CACHE_EXPIRE_HOURS);
            }

            return dbMessages;

        } catch (Exception e) {
            log.error("获取对话记忆失败，conversationId: {}, lastN: {}, error: {}",
                    conversationId, lastN, e.getMessage(), e);

            // 缓存异常时降级到数据库直连
            return super.get(conversationId, lastN);
        }
    }

    /**
     * 获取指定对话 ID 的最近 N 条消息（支持用户ID，带缓存）
     *
     * @param conversationId 对话的唯一标识符
     * @param userId         用户ID
     * @param lastN          要获取的最近消息的数量
     * @return 包含最近 N 条消息的列表，按时间顺序排列（旧 -> 新）
     */
    @SuppressWarnings("unchecked")
    public List<Message> get(String conversationId, Long userId, int lastN) {
        String cacheKey = cacheService.generateChatMemoryKey(conversationId, userId);

        try {
            // 1. 优先从缓存获取
            if (cacheService.isRedisAvailable()) {
                List<Message> cachedMessages = cacheService.get(cacheKey, List.class);
                if (cachedMessages != null) {
                    // 缓存命中，返回最近N条消息
                    return getLastNMessages(cachedMessages, lastN);
                }
            }

            // 2. 缓存未命中，从数据库获取
            List<Message> dbMessages = super.get(conversationId, userId, lastN);

            // 3. 回写缓存（存储完整消息列表）
            if (cacheService.isRedisAvailable() && !dbMessages.isEmpty()) {
                // 获取完整消息列表用于缓存
                List<Message> fullMessages = super.get(conversationId, userId, Integer.MAX_VALUE);
                cacheService.set(cacheKey, fullMessages, CACHE_EXPIRE_HOURS);
            }

            return dbMessages;

        } catch (Exception e) {
            log.error("获取对话记忆失败，conversationId: {}, userId: {}, lastN: {}, error: {}",
                    conversationId, userId, lastN, e.getMessage(), e);

            // 缓存异常时降级到数据库直连
            return super.get(conversationId, userId, lastN);
        }
    }

    /**
     * 清除指定对话的缓存
     *
     * @param conversationId 对话ID
     * @param userId         用户ID（可选）
     */
    public void clearCache(String conversationId, Long userId) {
        try {
            String cacheKey = cacheService.generateChatMemoryKey(conversationId, userId);
            cacheService.delete(cacheKey);
            log.debug("对话缓存清除成功，conversationId: {}, userId: {}", conversationId, userId);
        } catch (Exception e) {
            log.error("对话缓存清除失败，conversationId: {}, userId: {}, error: {}",
                    conversationId, userId, e.getMessage(), e);
        }
    }

    /**
     * 更新缓存
     *
     * @param conversationId 对话ID
     * @param userId         用户ID（可选）
     */
    private void updateCache(String conversationId, Long userId) {
        if (!cacheService.isRedisAvailable()) {
            return;
        }

        try {
            // 从数据库获取最新的完整消息列表
            List<Message> fullMessages = super.get(conversationId, userId, Integer.MAX_VALUE);
            if (!fullMessages.isEmpty()) {
                String cacheKey = cacheService.generateChatMemoryKey(conversationId, userId);
                cacheService.set(cacheKey, fullMessages, CACHE_EXPIRE_HOURS);
            }
        } catch (Exception e) {
            log.error("缓存更新失败，conversationId: {}, userId: {}, error: {}",
                    conversationId, userId, e.getMessage(), e);
        }
    }

    /**
     * 从消息列表中获取最近N条消息
     *
     * @param messages 完整消息列表
     * @param lastN    要获取的消息数量
     * @return 最近N条消息
     */
    private List<Message> getLastNMessages(List<Message> messages, int lastN) {
        if (messages.isEmpty() || lastN <= 0) {
            return messages;
        }

        int skipCount = Math.max(0, messages.size() - lastN);
        return messages.stream()
                .skip(skipCount)
                .toList();
    }
}
