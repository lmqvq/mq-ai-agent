package com.mq.mqaiagent.app;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.pool.ChatClientPool;
import com.mq.mqaiagent.service.AiResponseCacheService;
import com.mq.mqaiagent.service.CacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
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
 * ClassName：KeepApp
 * Package:com.mq.mqaiagent.app
 * Description: 健身助手
 * Author：MQQQ
 *
 * @Create:2025/6/18 - 18:07
 * @Version:v1.0
 */
@Component
@Slf4j
public class KeepApp {

        private final ChatClient chatClient;
        private final ChatModel dashscopeChatModel;
        private final KeepReportMapper keepReportMapper;
        private final CacheService cacheService;
        private final AiResponseCacheService aiResponseCacheService;
        private final ChatClientPool chatClientPool;

        // /**
        // * 初始化 ChatClient
        // *
        // * @param dashscopeChatModel
        // */
        // public KeepApp(ChatModel dashscopeChatModel) {
        // // 初始化基于文件的对话记忆
        // // String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        // // ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // // 初始化基于内存的对话记忆
        // ChatMemory chatMemory = new InMemoryChatMemory();
        // chatClient = ChatClient.builder(dashscopeChatModel)
        // .defaultSystem(SYSTEM_PROMPT)
        // .defaultAdvisors(
        // new MessageChatMemoryAdvisor(chatMemory),
        // // 自定义日志 Advisor，可按需开启
        // new MyLoggerAdvisor()
        // // 自定义违禁词 Advisor
        // ,new ForbiddenWordAdvisor()
        // // 自定义推理增强 Advisor，可按需开启
        // // new ReReadingAdvisor()
        // )
        // .build();
        // }

        /**
         * 初始化 ChatClient
         *
         * @param dashscopeChatModel     聊天模型
         * @param keepReportMapper       数据库映射器
         * @param cacheService           缓存服务
         * @param aiResponseCacheService AI响应缓存服务
         * @param chatClientPool         ChatClient对象池
         */
        public KeepApp(ChatModel dashscopeChatModel, KeepReportMapper keepReportMapper,
                        CacheService cacheService, AiResponseCacheService aiResponseCacheService,
                        ChatClientPool chatClientPool) {
                this.dashscopeChatModel = dashscopeChatModel;
                this.keepReportMapper = keepReportMapper;
                this.cacheService = cacheService;
                this.aiResponseCacheService = aiResponseCacheService;
                this.chatClientPool = chatClientPool;
                // 使用对象池获取默认的 ChatClient（不支持用户记忆）
                this.chatClient = chatClientPool.getKeepAppClient(SYSTEM_PROMPT);
        }

        /**
         * KeepAPP 基础对话（支持多轮对话记忆和AI响应缓存）
         *
         * @param message 用户消息
         * @param chatId  对话ID
         * @return AI响应内容
         */
        public String doChat(String message, String chatId) {
                return doChatWithCache(message, chatId, null);
        }

        /**
         * KeepAPP 基础对话（支持多轮对话记忆和用户ID）
         *
         * @param message 用户消息
         * @param chatId  对话ID
         * @param userId  用户ID
         * @return AI响应内容
         */
        public String doChat(String message, String chatId, Long userId) {
                return doChatWithCache(message, chatId, userId);
        }

        /**
         * 带缓存的对话方法（内部实现）
         *
         * @param message 用户消息
         * @param chatId  对话ID
         * @param userId  用户ID（可选）
         * @return AI响应内容
         */
        private String doChatWithCache(String message, String chatId, Long userId) {
                // 1. 尝试从缓存获取响应
                String cachedResponse = aiResponseCacheService.getCachedResponse(message, userId);
                if (cachedResponse != null) {
                        log.info("使用缓存的AI响应，message: {}", message.substring(0, Math.min(50, message.length())));
                        return cachedResponse;
                }
                // 2. 缓存未命中，调用AI模型
                String response;
                if (userId != null) {
                        // 使用对象池获取支持用户记忆的ChatClient
                        ChatClient userChatClient = chatClientPool.getKeepAppClientWithMemory(userId, SYSTEM_PROMPT);
                        ChatResponse chatResponse = userChatClient
                                        .prompt()
                                        .user(message)
                                        .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                        .call()
                                        .chatResponse();
                        response = chatResponse.getResult().getOutput().getText();
                } else {
                        // 使用默认的chatClient（从对象池获取）
                        ChatResponse chatResponse = chatClient
                                        .prompt()
                                        .user(message)
                                        .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                        .call()
                                        .chatResponse();
                        response = chatResponse.getResult().getOutput().getText();
                }

                // 3. 缓存AI响应
                aiResponseCacheService.cacheResponse(message, response, userId);

                log.info("AI模型响应: {}", response);
                return response;
        }

        // 定义 KeepReport 的 record，包含 title 和 suggestions 列表
        record KeepReport(String title, List<String> suggestions) {
        }

        /**
         * KeepAPP 基础对话（支持多轮对话记忆），并生成健身报告
         *
         * @param message
         * @param chatId
         * @return
         */
        public KeepReport doChatWithReport(String message, String chatId) {
                KeepReport loveReport = chatClient
                                .prompt()
                                .system(SYSTEM_PROMPT + "每次对话后都要生成健身结果，标题为{用户名}的健身报告，内容为建议列表")
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .call()
                                .entity(KeepReport.class);
                log.info("loveReport: {}", loveReport);
                return loveReport;
        }

        /**
         * KeepApp 使用 RAG 知识库问答
         */
        @Resource
        private VectorStore keepAppVectorStore;

        @Resource
        private Advisor KeepAppRagCloudAdvisor;

        public String doChatWithRag(String message, String chatId) {
                ChatResponse chatResponse = chatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                // 开启日志，便于观察效果
                                .advisors(new MyLoggerAdvisor())
                                // 应用知识库问答
                                // .advisors(new QuestionAnswerAdvisor(keepAppVectorStore))
                                // 应用增强检索服务（云知识库服务）
                                .advisors(KeepAppRagCloudAdvisor)
                                .call()
                                .chatResponse();
                String content = chatResponse.getResult().getOutput().getText();
                log.info("content: {}", content);
                return content;
        }

        /**
         * KeepApp 使用工具
         */
        @Resource
        private ToolCallback[] allTools;

        public String doChatWithTools(String message, String chatId) {
                ChatResponse response = chatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                // 开启日志，便于观察效果
                                .advisors(new MyLoggerAdvisor())
                                .tools(allTools)
                                .call()
                                .chatResponse();
                String content = response.getResult().getOutput().getText();
                log.info("content: {}", content);
                return content;
        }

        /**
         * KeepApp 使用流式对话
         */
        public Flux<String> doChatByStream(String message, String chatId) {
                return chatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .stream()
                                .content();
        }

        /**
         * KeepApp 使用流式对话（支持用户ID）
         *
         * @param message 用户消息
         * @param chatId  对话ID
         * @param userId  用户ID
         * @return 流式响应
         */
        public Flux<String> doChatByStream(String message, String chatId, Long userId) {
                // 使用对象池获取支持用户记忆的ChatClient
                ChatClient userChatClient = chatClientPool.getKeepAppClientWithMemory(userId, SYSTEM_PROMPT);
                return userChatClient
                                .prompt()
                                .user(message)
                                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                                .stream()
                                .content();
        }
}
