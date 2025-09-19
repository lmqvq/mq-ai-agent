package com.mq.mqaiagent.pool;

import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * ChatClient 对象池测试类
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/1/17
 */
@SpringBootTest
class ChatClientPoolTest {

    @Mock
    private ChatModel dashscopeChatModel;
    
    @Mock
    private KeepReportMapper keepReportMapper;
    
    @Mock
    private CacheService cacheService;

    private ChatClientPool chatClientPool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 模拟 CacheService 的行为
        when(cacheService.isRedisAvailable()).thenReturn(true);
        
        // 创建 ChatClientPool 实例
        chatClientPool = new ChatClientPool();
        // 通过反射设置私有字段（在实际应用中，这些会通过 @Autowired 注入）
        setPrivateField(chatClientPool, "dashscopeChatModel", dashscopeChatModel);
        setPrivateField(chatClientPool, "keepReportMapper", keepReportMapper);
        setPrivateField(chatClientPool, "cacheService", cacheService);
    }

    @Test
    void testGetKeepAppClient_ShouldReturnSameInstanceForSamePrompt() {
        // Given
        String systemPrompt = "You are a fitness assistant";
        
        // When
        ChatClient client1 = chatClientPool.getKeepAppClient(systemPrompt);
        ChatClient client2 = chatClientPool.getKeepAppClient(systemPrompt);
        
        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        assertSame(client1, client2, "相同系统提示词应该返回相同的ChatClient实例");
        
        // 验证缓存统计
        ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
        assertEquals(1, stats.cacheMisses(), "应该有1次缓存未命中");
        assertEquals(1, stats.cacheHits(), "应该有1次缓存命中");
        assertEquals(1, stats.totalCreated(), "应该总共创建1个实例");
    }

    @Test
    void testGetKeepAppClientWithMemory_ShouldReturnDifferentInstancesForDifferentUsers() {
        // Given
        String systemPrompt = "You are a fitness assistant";
        Long userId1 = 1L;
        Long userId2 = 2L;
        
        // When
        ChatClient client1 = chatClientPool.getKeepAppClientWithMemory(userId1, systemPrompt);
        ChatClient client2 = chatClientPool.getKeepAppClientWithMemory(userId2, systemPrompt);
        ChatClient client3 = chatClientPool.getKeepAppClientWithMemory(userId1, systemPrompt); // 相同用户
        
        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        assertNotNull(client3);
        assertNotSame(client1, client2, "不同用户应该返回不同的ChatClient实例");
        assertSame(client1, client3, "相同用户应该返回相同的ChatClient实例");
        
        // 验证缓存统计
        ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
        assertEquals(2, stats.cacheMisses(), "应该有2次缓存未命中");
        assertEquals(1, stats.cacheHits(), "应该有1次缓存命中");
        assertEquals(2, stats.totalCreated(), "应该总共创建2个实例");
    }

    @Test
    void testGetMqManusClient_ShouldCacheCorrectly() {
        // Given
        String systemPrompt = "You are MqManus AI assistant";
        
        // When
        ChatClient client1 = chatClientPool.getMqManusClient(systemPrompt);
        ChatClient client2 = chatClientPool.getMqManusClient(systemPrompt);
        
        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        assertSame(client1, client2, "相同系统提示词应该返回相同的MqManus ChatClient实例");
    }

    @Test
    void testCacheStats_ShouldCalculateHitRateCorrectly() {
        // Given
        String systemPrompt = "Test prompt";
        
        // When
        chatClientPool.getKeepAppClient(systemPrompt); // miss
        chatClientPool.getKeepAppClient(systemPrompt); // hit
        chatClientPool.getKeepAppClient(systemPrompt); // hit
        
        // Then
        ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
        assertEquals(1, stats.cacheMisses());
        assertEquals(2, stats.cacheHits());
        assertEquals(1, stats.totalCreated());
        assertEquals(2.0/3.0, stats.hitRate(), 0.001, "命中率应该是 2/3");
    }

    @Test
    void testClearAll_ShouldClearAllCaches() {
        // Given
        chatClientPool.getKeepAppClient("prompt1");
        chatClientPool.getKeepAppClient("prompt2");
        
        ChatClientPool.CacheStats beforeStats = chatClientPool.getCacheStats();
        assertTrue(beforeStats.currentCacheSize() > 0, "清理前应该有缓存");
        
        // When
        chatClientPool.clearAll();
        
        // Then
        ChatClientPool.CacheStats afterStats = chatClientPool.getCacheStats();
        assertEquals(0, afterStats.currentCacheSize(), "清理后缓存大小应该为0");
    }

    @Test
    void testDifferentPrompts_ShouldCreateDifferentInstances() {
        // Given
        String prompt1 = "You are a fitness assistant";
        String prompt2 = "You are a nutrition expert";
        
        // When
        ChatClient client1 = chatClientPool.getKeepAppClient(prompt1);
        ChatClient client2 = chatClientPool.getKeepAppClient(prompt2);
        
        // Then
        assertNotNull(client1);
        assertNotNull(client2);
        assertNotSame(client1, client2, "不同的系统提示词应该创建不同的实例");
        
        ChatClientPool.CacheStats stats = chatClientPool.getCacheStats();
        assertEquals(2, stats.cacheMisses(), "应该有2次缓存未命中");
        assertEquals(0, stats.cacheHits(), "应该没有缓存命中");
        assertEquals(2, stats.totalCreated(), "应该总共创建2个实例");
    }

    /**
     * 通过反射设置私有字段
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set private field: " + fieldName, e);
        }
    }
}
