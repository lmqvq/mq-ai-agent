package com.mq.mqaiagent.service;

/**
 * @author MQ
 * @description 针对缓存操作的数据库操作Service
 * @createDate 2025-01-25 00:00:00
 */
public interface CacheService {

    /**
     * 设置缓存
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param expireHours 过期时间（小时）
     * @return 是否设置成功
     */
    boolean set(String key, Object value, long expireHours);

    /**
     * 设置缓存（使用默认过期时间）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @return 是否设置成功
     */
    boolean set(String key, Object value);

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在或发生异常则返回null
     */
    Object get(String key);

    /**
     * 获取缓存并转换为指定类型
     *
     * @param key   缓存键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的缓存值，如果不存在、类型不匹配或发生异常则返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 检查缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 设置缓存过期时间
     *
     * @param key         缓存键
     * @param expireHours 过期时间（小时）
     * @return 是否设置成功
     */
    boolean expire(String key, long expireHours);

    /**
     * 生成对话记忆缓存键
     *
     * @param conversationId 对话ID
     * @param userId         用户ID（可选）
     * @return 缓存键
     */
    String generateChatMemoryKey(String conversationId, Long userId);

    /**
     * 生成AI响应缓存键
     *
     * @param questionHash 问题哈希值
     * @return 缓存键
     */
    String generateAiResponseKey(String questionHash);

    /**
     * 检查Redis连接状态
     *
     * @return 是否连接正常
     */
    boolean isRedisAvailable();
}
