package com.mq.mqaiagent.app;

import com.mq.mqaiagent.advisor.ForbiddenWordAdvisor;
import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.chatmemory.DatabaseChatMemory;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
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

//    /**
//     * 初始化 ChatClient
//     *
//     * @param dashscopeChatModel
//     */
//    public KeepApp(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        // String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        // ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(
//                        new MessageChatMemoryAdvisor(chatMemory),
//                        // 自定义日志 Advisor，可按需开启
//                        new MyLoggerAdvisor()
//                        // 自定义违禁词 Advisor
//                        ,new ForbiddenWordAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                        // new ReReadingAdvisor()
//                )
//                .build();
//    }

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public KeepApp(ChatModel dashscopeChatModel, KeepReportMapper keepReportMapper) {
        // 初始化基于数据库的对话记忆
        DatabaseChatMemory chatMemory = new DatabaseChatMemory(keepReportMapper);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义违禁词 Advisor
//                        ,new ForbiddenWordAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
                        // new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * KeepAPP 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
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
}
