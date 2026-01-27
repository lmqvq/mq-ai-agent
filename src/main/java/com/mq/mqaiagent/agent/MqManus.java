package com.mq.mqaiagent.agent;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.ai.AiModelType;
import com.mq.mqaiagent.chatmemory.DatabaseChatMemory;
import com.mq.mqaiagent.pool.ChatClientPool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * MqManus 实例。
 */
public class MqManus extends ToolCallAgent {

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
     * 使用 ChatClientPool 的构造函数（支持用户记忆）。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId) {
        this(allTools, chatClientPool, userId, chatClientPool.getDefaultModelType());
    }

    /**
     * 使用 ChatClientPool 的构造函数（支持用户记忆，支持模型选择）。
     */
    public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId, AiModelType modelType) {
        super(allTools, chatClientPool.getToolCallChatOptions(modelType));
        initPrompts();
        ChatClient chatClient = chatClientPool.getMqManusClientWithMemory(modelType, userId, getSystemPrompt());
        this.setChatClient(chatClient);
    }

    private void initPrompts() {
        this.setName("mqmanus");
        String systemPrompt = """
                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(systemPrompt);
        String nextStepPrompt = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(nextStepPrompt);
        this.setMaxSteps(20);
    }
}
