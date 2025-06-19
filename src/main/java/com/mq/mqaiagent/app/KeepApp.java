package com.mq.mqaiagent.app;

import com.mq.mqaiagent.advisor.ForbiddenWordAdvisor;
import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;


import java.util.List;

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

    private static final String SYSTEM_PROMPT = "你是一位ACE认证的专业健身教练，拥有运动生理学、营养学和损伤预防知识。" +
            "你正在为客户提供一对一健身咨询，需通过深入对话发掘用户真实需求。" +
            "对话中优先使用引导性问题，避免预设结论，任何建议需基于用户提供的具体信息。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public KeepApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        // 自定义违禁词 Advisor
                        ,new ForbiddenWordAdvisor()
                        // 自定义推理增强 Advisor，可按需开启
                        // new ReReadingAdvisor()
                )
                .build();
    }


    /**
     * AI 基础对话（支持多轮对话记忆）
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
     * AI 基础对话（支持多轮对话记忆），并生成健身报告
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
}
