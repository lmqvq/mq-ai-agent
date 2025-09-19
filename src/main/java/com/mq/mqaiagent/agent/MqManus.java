package com.mq.mqaiagent.agent;

import com.mq.mqaiagent.advisor.MyLoggerAdvisor;
import com.mq.mqaiagent.chatmemory.DatabaseChatMemory;
import com.mq.mqaiagent.pool.ChatClientPool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * ClassName：MqManus
 * Package:com.mq.mqaiagent.agent
 * Description: MqManus 实例
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 16:15
 * @Version:v1.0
 */
public class MqManus extends ToolCallAgent {

        public MqManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
                super(allTools);
                this.setName("mqmanus");
                String SYSTEM_PROMPT = """
                                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                                """;
                this.setSystemPrompt(SYSTEM_PROMPT);
                String NEXT_STEP_PROMPT = """
                                Based on user needs, proactively select the most appropriate tool or combination of tools.
                                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                                After using each tool, clearly explain the execution results and suggest the next steps.
                                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                                """;
                this.setNextStepPrompt(NEXT_STEP_PROMPT);
                this.setMaxSteps(20);
                // 初始化客户端
                ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                                .defaultAdvisors(new MyLoggerAdvisor())
                                .build();
                this.setChatClient(chatClient);
        }

        /**
         * 支持对话记忆和用户ID的构造函数
         *
         * @param allTools           工具数组
         * @param dashscopeChatModel 聊天模型
         * @param chatMemory         对话记忆
         */
        public MqManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, DatabaseChatMemory chatMemory) {
                super(allTools);
                this.setName("mqmanus");
                String SYSTEM_PROMPT = """
                                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                                """;
                this.setSystemPrompt(SYSTEM_PROMPT);
                String NEXT_STEP_PROMPT = """
                                Based on user needs, proactively select the most appropriate tool or combination of tools.
                                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                                After using each tool, clearly explain the execution results and suggest the next steps.
                                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                                """;
                this.setNextStepPrompt(NEXT_STEP_PROMPT);
                this.setMaxSteps(20);
                // 初始化带对话记忆的客户端
                ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                                .defaultAdvisors(
                                                new MessageChatMemoryAdvisor(chatMemory),
                                                new MyLoggerAdvisor())
                                .build();
                this.setChatClient(chatClient);
        }

        /**
         * 使用 ChatClientPool 的构造函数（不支持记忆）
         *
         * @param allTools       工具数组
         * @param chatClientPool ChatClient对象池
         */
        public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool) {
                super(allTools);
                this.setName("mqmanus");
                String SYSTEM_PROMPT = """
                                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                                """;
                this.setSystemPrompt(SYSTEM_PROMPT);
                String NEXT_STEP_PROMPT = """
                                Based on user needs, proactively select the most appropriate tool or combination of tools.
                                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                                After using each tool, clearly explain the execution results and suggest the next steps.
                                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                                """;
                this.setNextStepPrompt(NEXT_STEP_PROMPT);
                this.setMaxSteps(20);
                // 使用对象池获取ChatClient
                ChatClient chatClient = chatClientPool.getMqManusClient(SYSTEM_PROMPT);
                this.setChatClient(chatClient);
        }

        /**
         * 使用 ChatClientPool 的构造函数（支持用户记忆）
         *
         * @param allTools       工具数组
         * @param chatClientPool ChatClient对象池
         * @param userId         用户ID
         */
        public MqManus(ToolCallback[] allTools, ChatClientPool chatClientPool, Long userId) {
                super(allTools);
                this.setName("mqmanus");
                String SYSTEM_PROMPT = """
                                You are MqManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                                """;
                this.setSystemPrompt(SYSTEM_PROMPT);
                String NEXT_STEP_PROMPT = """
                                Based on user needs, proactively select the most appropriate tool or combination of tools.
                                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                                After using each tool, clearly explain the execution results and suggest the next steps.
                                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                                """;
                this.setNextStepPrompt(NEXT_STEP_PROMPT);
                this.setMaxSteps(20);
                // 使用对象池获取支持用户记忆的ChatClient
                ChatClient chatClient = chatClientPool.getMqManusClientWithMemory(userId, SYSTEM_PROMPT);
                this.setChatClient(chatClient);
        }
}
