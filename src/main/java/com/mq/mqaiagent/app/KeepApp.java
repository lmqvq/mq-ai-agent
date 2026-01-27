package com.mq.mqaiagent.app;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.ai.AiModelRouter;
import com.mq.mqaiagent.ai.AiModelType;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.pool.ChatClientPool;
import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.mq.mqaiagent.constant.AiConstant.SYSTEM_PROMPT;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 健身助手应用。
 */
@Component
@Slf4j
public class KeepApp {

    private final KeepReportMapper keepReportMapper;
    private final CacheService cacheService;
    private final AiResponseCacheService aiResponseCacheService;
    private final ChatClientPool chatClientPool;

    public KeepApp(KeepReportMapper keepReportMapper,
            CacheService cacheService,
            AiResponseCacheService aiResponseCacheService,
            ChatClientPool chatClientPool) {
        this.keepReportMapper = keepReportMapper;
        this.cacheService = cacheService;
        this.aiResponseCacheService = aiResponseCacheService;
        this.chatClientPool = chatClientPool;
    }

    /**
     * KeepAPP 基础对话（默认模型）。
     */
    public String doChat(String message, String chatId) {
        return doChatWithCache(message, chatId, null, null);
    }

    /**
     * KeepAPP 基础对话（默认模型，支持用户 ID）。
     */
    public String doChat(String message, String chatId, Long userId) {
        return doChatWithCache(message, chatId, userId, null);
    }

    /**
     * KeepAPP 基础对话（支持模型选择）。
     */
    public String doChat(String message, String chatId, String model) {
        return doChatWithCache(message, chatId, null, model);
    }

    /**
     * KeepAPP 基础对话（支持模型选择 + 用户 ID）。
     */
    public String doChat(String message, String chatId, Long userId, String model) {
        return doChatWithCache(message, chatId, userId, model);
    }

    /**
     * 带缓存的对话方法（内部实现）。
     */
    private String doChatWithCache(String message, String chatId, Long userId, String rawModel) {
        AiModelRouter.ResolvedModel resolvedModel = chatClientPool.resolveModel(rawModel);
        AiModelType modelType = resolvedModel.modelType();

        // 1. 尝试从缓存获取响应（按模型隔离）
        String cachedResponse = aiResponseCacheService.getCachedResponse(message, userId, modelType);
        if (cachedResponse != null) {
            log.info("使用缓存的 AI 响应，model: {}, message: {}", modelType.getCode(), abbreviate(message, 50));
            return cachedResponse;
        }

        // 2. 缓存未命中，调用 AI 模型
        ChatClient chatClient = resolveKeepAppClient(modelType, userId);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String response = chatResponse.getResult().getOutput().getText();

        // 3. 缓存 AI 响应（按模型隔离）
        aiResponseCacheService.cacheResponse(message, response, userId, modelType);

        log.info("AI 模型响应，model: {}, response: {}", modelType.getCode(), abbreviate(response, 80));
        return response;
    }

    // 定义 KeepReport 的 record，包含 title 和 suggestions 列表
    record KeepReport(String title, List<String> suggestions) {
    }

    /**
     * KeepAPP 生成健身报告（默认模型）。
     */
    public KeepReport doChatWithReport(String message, String chatId) {
        return doChatWithReport(message, chatId, null);
    }

    /**
     * KeepAPP 生成健身报告（支持模型选择）。
     */
    public KeepReport doChatWithReport(String message, String chatId, String model) {
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        ChatClient chatClient = resolveKeepAppClient(modelType, null);
        KeepReport keepReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成健身结果，标题为‘用户的健身报告’，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(KeepReport.class);
        log.info("keepReport(model={}): {}", modelType.getCode(), keepReport);
        return keepReport;
    }

    /**
     * KeepApp 使用 RAG 知识库问答。
     */
    @Resource
    private VectorStore keepAppVectorStore;

    @Resource
    private Advisor KeepAppRagCloudAdvisor;

    public String doChatWithRag(String message, String chatId) {
        return doChatWithRag(message, chatId, null);
    }

    public String doChatWithRag(String message, String chatId, String model) {
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        ChatClient chatClient = resolveKeepAppClient(modelType, null);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用增强检索服务（云知识库服务）
                .advisors(KeepAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("rag content(model={}): {}", modelType.getCode(), abbreviate(content, 120));
        return content;
    }

    /**
     * KeepApp 使用工具。
     */
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        return doChatWithTools(message, chatId, null);
    }

    public String doChatWithTools(String message, String chatId, String model) {
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        ChatClient chatClient = resolveKeepAppClient(modelType, null);
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("tools content(model={}): {}", modelType.getCode(), abbreviate(content, 120));
        return content;
    }

    /**
     * KeepApp 流式对话（默认模型）。
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return doChatByStream(message, chatId, null, null);
    }

    /**
     * KeepApp 流式对话（支持模型选择）。
     */
    public Flux<String> doChatByStream(String message, String chatId, String model) {
        return doChatByStream(message, chatId, null, model);
    }

    /**
     * KeepApp 流式对话（默认模型，支持用户 ID）。
     */
    public Flux<String> doChatByStream(String message, String chatId, Long userId) {
        return doChatByStream(message, chatId, userId, null);
    }

    /**
     * KeepApp 流式对话（支持模型选择 + 用户 ID）。
     */
    public Flux<String> doChatByStream(String message, String chatId, Long userId, String model) {
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        ChatClient chatClient = resolveKeepAppClient(modelType, userId);
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    private ChatClient resolveKeepAppClient(AiModelType modelType, Long userId) {
        if (userId != null) {
            return chatClientPool.getKeepAppClientWithMemory(modelType, userId, SYSTEM_PROMPT);
        }
        return chatClientPool.getKeepAppClient(modelType, SYSTEM_PROMPT);
    }

    private String abbreviate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        return text.substring(0, Math.min(maxLen, text.length()));
    }
}
