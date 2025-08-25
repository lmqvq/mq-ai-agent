package com.mq.mqaiagent.service.impl;

import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
* @author MQ
* @description 针对缓存操作的数据库操作Service实现
* @createDate 2025-01-25 00:00:00
*/
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存键前缀
     */
    private static final String CACHE_PREFIX = "mq:ai:agent:";

    /**
     * 对话记忆缓存键前缀
     */
    public static final String CHAT_MEMORY_PREFIX = CACHE_PREFIX + "chat:memory:";

    /**
     * AI响应缓存键前缀
     */
    public static final String AI_RESPONSE_PREFIX = CACHE_PREFIX + "ai:response:";

    /**
     * 默认缓存过期时间（小时）
     */
    private static final long DEFAULT_EXPIRE_HOURS = 24;

    @Override
    public boolean set(String key, Object value, long expireHours) {
        try {
            redisTemplate.opsForValue().set(key, value, expireHours, TimeUnit.HOURS);
            log.debug("缓存设置成功，key: {}, expireHours: {}", key, expireHours);
            return true;
        } catch (Exception e) {
            log.error("缓存设置失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean set(String key, Object value) {
        return set(key, value, DEFAULT_EXPIRE_HOURS);
    }

    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("缓存命中，key: {}", key);
            } else {
                log.debug("缓存未命中，key: {}", key);
            }
            return value;
        } catch (Exception e) {
            log.error("缓存获取失败，key: {}, error: {}", key, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("缓存类型转换失败，key: {}, targetType: {}, error: {}", key, clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("缓存删除，key: {}, result: {}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("缓存删除失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("缓存存在性检查失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, long expireHours) {
        try {
            Boolean result = redisTemplate.expire(key, expireHours, TimeUnit.HOURS);
            log.debug("缓存过期时间设置，key: {}, expireHours: {}, result: {}", key, expireHours, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("缓存过期时间设置失败，key: {}, error: {}", key, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateChatMemoryKey(String conversationId, Long userId) {
        if (userId != null) {
            return CHAT_MEMORY_PREFIX + userId + ":" + conversationId;
        }
        return CHAT_MEMORY_PREFIX + conversationId;
    }

    @Override
    public String generateAiResponseKey(String questionHash) {
        return AI_RESPONSE_PREFIX + questionHash;
    }

    @Override
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis连接检查失败: {}", e.getMessage());
            return false;
        }
    }
}
