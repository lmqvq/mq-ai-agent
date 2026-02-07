package com.mq.mqaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.mq.mqaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName：ToolCallAgent
 * Package:com.mq.mqaiagent.agent
 * Description: 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 16:06
 * @Version:v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存了工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    // 保存 LLM 的回答内容（用于不需要工具调用时返回给用户）
    private String lastThinkResult;
    
    // 保存最后一次 writeFile 工具写入的内容（用于 doTerminate 时返回给用户）
    private String lastWriteFileContent;

    public ToolCallAgent(ToolCallback[] availableTools) {
        this(availableTools, null);
    }

    public ToolCallAgent(ToolCallback[] availableTools, ChatOptions chatOptions) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 关闭内置工具调用执行，交由我们自己的 ToolCallingManager 处理
        this.chatOptions = chatOptions != null
                ? chatOptions
                : DashScopeChatOptions.builder()
                        .withProxyToolCalls(true)
                        .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 构建发送给 LLM 的消息列表
        // 注意：nextStepPrompt 只是临时的指导提示，不应保存到对话历史中
        List<Message> messagesForLLM = new ArrayList<>(getMessageList());
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            messagesForLLM.add(userMessage);
        }
        Prompt prompt = new Prompt(messagesForLLM, chatOptions);
        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 保存 LLM 的回答内容
            this.lastThinkResult = result;
            
            // 发送思考内容事件（实时流式输出）
            sendThinkingEvent(result);
            
            if (toolCallList.isEmpty()) {
                // 不调用工具时，记录助手消息并标记任务完成
                getMessageList().add(assistantMessage);
                // LLM 认为不需要工具，说明可以直接回答，任务完成
                setState(AgentState.FINISHED);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            this.lastThinkResult = "处理时遇到错误: " + e.getMessage();
            getMessageList().add(
                    new AssistantMessage(this.lastThinkResult));
            setState(AgentState.FINISHED);
            return false;
        }
    }

    /**
     * 获取 LLM 的思考/回答内容
     *
     * @return LLM 生成的回答
     */
    @Override
    protected String getThinkResult() {
        return this.lastThinkResult != null ? this.lastThinkResult : "思考完成";
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        
        // 在执行工具之前，检查是否是 writeFile 工具，如果是则保存其内容
        AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
        
        // 发送工具开始执行事件（实时流式输出）
        for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
            String toolName = toolCall.name();
            String arguments = toolCall.arguments();
            String summary = generateToolSummary(toolName, arguments);
            sendToolStartEvent(toolName, arguments, summary);
            
            // 如果是 writeFile 工具，保存内容
            if ("writeFile".equals(toolName)) {
                try {
                    // 从参数中提取 content
                    // 简单解析 JSON 获取 content 字段
                    int contentStart = arguments.indexOf("\"content\":");
                    if (contentStart != -1) {
                        contentStart = arguments.indexOf("\"", contentStart + 10) + 1;
                        int contentEnd = arguments.lastIndexOf("\"");
                        if (contentStart > 0 && contentEnd > contentStart) {
                            String content = arguments.substring(contentStart, contentEnd);
                            // 处理转义字符
                            content = content.replace("\\n", "\n")
                                           .replace("\\\"", "\"")
                                           .replace("\\\\", "\\");
                            this.lastWriteFileContent = content;
                            log.info("保存 writeFile 内容，长度：" + content.length());
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析 writeFile 参数失败: " + e.getMessage());
                }
            }
        }
        
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        
        // 发送工具执行完成事件（实时流式输出）
        for (ToolResponseMessage.ToolResponse response : toolResponseMessage.getResponses()) {
            String toolName = response.name();
            String result = response.responseData() != null ? response.responseData().toString() : "";
            String summary = generateToolResultSummary(toolName, result);
            
            // 判断是成功还是失败
            if (result.contains("失败") || result.contains("error") || result.contains("Error")) {
                sendToolErrorEvent(toolName, result);
            } else {
                sendToolCompleteEvent(toolName, result, summary);
            }
        }
        
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        
        // 如果是终止工具调用，优先返回有价值的内容
        if (terminateToolCalled) {
            // 1. 优先返回 writeFile 的内容
            if (this.lastWriteFileContent != null && !this.lastWriteFileContent.isEmpty()) {
                return this.lastWriteFileContent;
            }
            // 2. 其次返回 AI 的思考结果（非空且有意义的内容）
            if (this.lastThinkResult != null && !this.lastThinkResult.trim().isEmpty()) {
                return this.lastThinkResult;
            }
            // 3. 如果都没有，让 AI 重新生成一个回答
            try {
                log.info("工具调用均失败，让 AI 基于自身知识生成回答");
                String fallbackResponse = generateFallbackResponse();
                if (fallbackResponse != null && !fallbackResponse.isEmpty()) {
                    return fallbackResponse;
                }
            } catch (Exception e) {
                log.warn("生成备用回答失败: " + e.getMessage());
            }
        }
        return results;
    }
    
    /**
     * 生成工具调用的摘要信息
     * 
     * @param toolName 工具名称
     * @param arguments 工具参数
     * @return 摘要信息
     */
    private String generateToolSummary(String toolName, String arguments) {
        return switch (toolName) {
            case "searchWeb" -> extractSearchQuery(arguments, "正在搜索: ");
            case "googleSearch" -> extractSearchQuery(arguments, "正在搜索: ");
            case "crawl" -> extractUrl(arguments, "正在爬取网页: ");
            case "writeFile" -> extractFilePath(arguments, "正在写入文件: ");
            case "readFile" -> extractFilePath(arguments, "正在读取文件: ");
            case "doTerminate" -> "正在结束任务";
            default -> "正在执行 " + toolName;
        };
    }
    
    /**
     * 生成工具执行结果的摘要信息
     * 
     * @param toolName 工具名称
     * @param result 执行结果
     * @return 摘要信息
     */
    private String generateToolResultSummary(String toolName, String result) {
        if (result == null || result.isEmpty()) {
            return "执行完成";
        }
        
        return switch (toolName) {
            case "searchWeb", "googleSearch" -> {
                int count = countSearchResults(result);
                yield count > 0 ? "找到 " + count + " 条结果" : "搜索完成";
            }
            case "crawl" -> result.length() > 100 ? "获取了 " + result.length() + " 字符的内容" : "爬取完成";
            case "writeFile" -> "文件写入成功";
            case "readFile" -> result.length() > 50 ? "读取了 " + result.length() + " 字符" : "读取完成";
            case "doTerminate" -> "任务结束";
            default -> "执行完成";
        };
    }
    
    /**
     * 从参数中提取搜索查询
     */
    private String extractSearchQuery(String arguments, String prefix) {
        try {
            int start = arguments.indexOf("\"query\":");
            if (start == -1) start = arguments.indexOf("\"keyword\":");
            if (start != -1) {
                int valueStart = arguments.indexOf("\"", start + 9) + 1;
                int valueEnd = arguments.indexOf("\"", valueStart);
                if (valueStart > 0 && valueEnd > valueStart) {
                    String query = arguments.substring(valueStart, valueEnd);
                    return prefix + (query.length() > 30 ? query.substring(0, 30) + "..." : query);
                }
            }
        } catch (Exception ignored) {}
        return prefix + "...";
    }
    
    /**
     * 从参数中提取 URL
     */
    private String extractUrl(String arguments, String prefix) {
        try {
            int start = arguments.indexOf("\"url\":");
            if (start != -1) {
                int valueStart = arguments.indexOf("\"", start + 7) + 1;
                int valueEnd = arguments.indexOf("\"", valueStart);
                if (valueStart > 0 && valueEnd > valueStart) {
                    String url = arguments.substring(valueStart, valueEnd);
                    return prefix + (url.length() > 40 ? url.substring(0, 40) + "..." : url);
                }
            }
        } catch (Exception ignored) {}
        return prefix + "...";
    }
    
    /**
     * 从参数中提取文件路径
     */
    private String extractFilePath(String arguments, String prefix) {
        try {
            int start = arguments.indexOf("\"path\":");
            if (start == -1) start = arguments.indexOf("\"filePath\":");
            if (start != -1) {
                int valueStart = arguments.indexOf("\"", start + 8) + 1;
                int valueEnd = arguments.indexOf("\"", valueStart);
                if (valueStart > 0 && valueEnd > valueStart) {
                    String path = arguments.substring(valueStart, valueEnd);
                    // 只显示文件名
                    int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
                    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
                    return prefix + fileName;
                }
            }
        } catch (Exception ignored) {}
        return prefix + "...";
    }
    
    /**
     * 统计搜索结果数量
     */
    private int countSearchResults(String result) {
        try {
            // 简单统计，计算结果中 URL 的数量
            int count = 0;
            int index = 0;
            while ((index = result.indexOf("http", index)) != -1) {
                count++;
                index++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 当工具调用失败时，让 AI 基于自身知识生成一个回答。
     * 
     * @return AI 生成的备用回答
     */
    private String generateFallbackResponse() {
        // 从消息列表中获取用户的原始问题
        String userQuestion = getMessageList().stream()
                .filter(msg -> msg instanceof org.springframework.ai.chat.messages.UserMessage)
                .map(org.springframework.ai.chat.messages.Message::getText)
                .findFirst()
                .orElse("");
        
        if (userQuestion.isEmpty()) {
            return null;
        }
        
        // 让 AI 直接回答问题，不使用工具
        String fallbackPrompt = """
            工具调用均失败，请基于你的内置知识直接回答用户的问题。
            不要提及工具失败的事情，直接给出有价值的回答。
            请用中文回答，内容要详细、实用。
            
            用户问题：%s
            """.formatted(userQuestion);
        
        ChatResponse response = getChatClient().prompt()
                .system(getSystemPrompt())
                .user(fallbackPrompt)
                .call()
                .chatResponse();
        
        if (response != null && response.getResult() != null) {
            return response.getResult().getOutput().getText();
        }
        return null;
    }
}
