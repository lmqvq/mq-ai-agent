package com.mq.mqaiagent.agent;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.ai.AiModelType;
import com.mq.mqaiagent.chatmemory.DatabaseChatMemory;
import com.mq.mqaiagent.pool.ChatClientPool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;

/**
 * MqManus AI超级智能体
 * 
 * 支持对话持久化：
 * - 通过 conversationId 隔离不同对话，解决"对话污染"问题
 * - 用户消息和 AI 回答持久化到数据库
 * - 刷新页面后可查看历史对话
 */
@Slf4j
public class MqManus extends ToolCallAgent {

    /**
     * 对话记忆，用于持久化用户消息和AI回答
     */
    @Getter
    private DatabaseChatMemory chatMemory;
    
    /**
     * 对话ID，用于隔离不同对话
     */
    @Getter
    private String conversationId;
    
    /**
     * 用户ID
     */
    @Getter
    private Long userId;

    public MqManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        initPrompts();
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }

    /**
     * 支持对话记忆和用户 ID 的构造函数。
     */
    public MqManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, DatabaseChatMemory chatMemory) {
        super(allTools);
        initPrompts();
        this.chatMemory = chatMemory;
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }

    /**
     * 使用 ChatClientPool 的构造函数（不支持记忆）。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool) {
        this(allTools, chatClientPool, chatClientPool.getDefaultModelType());
    }

    /**
     * 使用 ChatClientPool 的构造函数（不支持记忆，支持模型选择）。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, AiModelType modelType) {
        super(allTools, chatClientPool.getToolCallChatOptions(modelType));
        initPrompts();
        ChatClient chatClient = chatClientPool.getMqManusClient(modelType, getSystemPrompt());
        this.setChatClient(chatClient);
    }

    /**
     * 使用 ChatClientPool 的构造函数（支持用户 ID，但不使用持久化对话记忆）。
     * 注意：此构造函数不保存对话历史，仅用于不需要持久化的场景。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId) {
        this(allTools, chatClientPool, userId, chatClientPool.getDefaultModelType());
    }

    /**
     * 使用 ChatClientPool 的构造函数（支持用户 ID 和模型选择，但不使用持久化对话记忆）。
     * 注意：此构造函数不保存对话历史，仅用于不需要持久化的场景。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId, AiModelType modelType) {
        super(allTools, chatClientPool.getToolCallChatOptions(modelType));
        initPrompts();
        this.userId = userId;
        ChatClient chatClient = chatClientPool.getMqManusClient(modelType, getSystemPrompt());
        this.setChatClient(chatClient);
    }
    
    /**
     * 支持对话持久化的构造函数（推荐使用）。
     * 
     * 对话持久化逻辑：
     * - 通过 conversationId 隔离不同对话，解决"对话污染"问题
     * - 用户消息和 AI 回答持久化到数据库
     * - 刷新页面后可查看历史对话
     * 
     * @param allTools 可用工具
     * @param chatClientPool ChatClient对象池
     * @param userId 用户ID
     * @param conversationId 对话ID（前端生成，"新对话"时生成新ID）
     * @param modelType AI模型类型
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId, String conversationId, AiModelType modelType) {
        super(allTools, chatClientPool.getToolCallChatOptions(modelType));
        initPrompts();
        this.userId = userId;
        this.conversationId = conversationId;
        // 创建 DatabaseChatMemory 用于持久化对话
        this.chatMemory = chatClientPool.createMqManusChatMemory(userId);
        ChatClient chatClient = chatClientPool.getMqManusClient(modelType, getSystemPrompt());
        this.setChatClient(chatClient);
    }
    
    /**
     * 任务完成后的回调方法，自动保存对话到数据库。
     * 重写父类方法实现对话持久化。
     * 
     * @param userMessage 用户消息
     * @param aiResponse AI回答
     */
    @Override
    protected void onTaskCompleted(String userMessage, String aiResponse) {
        if (chatMemory != null && conversationId != null && userId != null) {
            try {
                // 创建消息对象
                UserMessage userMsg = new UserMessage(userMessage);
                AssistantMessage assistantMsg = new AssistantMessage(aiResponse);
                // 保存到数据库
                chatMemory.add(conversationId, userId, Arrays.asList(userMsg, assistantMsg));
                log.info("对话已保存，conversationId: {}, userId: {}", conversationId, userId);
            } catch (Exception e) {
                log.error("保存对话失败，conversationId: {}, userId: {}, error: {}", conversationId, userId, e.getMessage());
            }
        }
    }

    private void initPrompts() {
        this.setName("mqmanus");
        String systemPrompt = """
                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                
                IMPORTANT GUIDELINES:
                1. Always respond in Chinese (Simplified) to Chinese users.
                2. If tools fail or return errors, you MUST still provide a helpful response based on your own knowledge.
                3. Before calling `terminate`, you MUST provide a complete, detailed answer in your response text.
                4. Never end a conversation with just tool results - always include your own analysis and summary.
                5. If search tools fail, use your built-in knowledge to answer the user's question.
                """;
        this.setSystemPrompt(systemPrompt);
        String nextStepPrompt = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                
                CRITICAL: Before calling `terminate`, you MUST output a complete, helpful response in your thinking/response text.
                If all tools fail, provide an answer based on your own knowledge - do NOT just terminate with no content.
                Always ensure the user receives valuable information, even if tools encounter errors.
                """;
        this.setNextStepPrompt(nextStepPrompt);
        this.setMaxSteps(20);
    }
}
