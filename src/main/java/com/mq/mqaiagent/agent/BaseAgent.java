package com.mq.mqaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mq.mqaiagent.agent.model.AgentState;
import com.mq.mqaiagent.model.dto.AgentSseEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ClassName：BaseAgent
 * Package:com.mq.mqaiagent.agent
 * Description: 抽象基础代理类，用于管理代理状态和执行流程，提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * Author：MQQQ
 * @Create:2025/7/2 - 15:56
 * @Version:v1.0
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();
    
    // SSE 相关
    private SseEmitter currentEmitter;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 3、清理资源
            this.cleanup();
        }
    }

    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
    
    /**
     * 任务完成后的回调方法，用于保存对话到数据库。
     * 子类可以重写此方法实现对话持久化。
     * 
     * @param userMessage 用户消息
     * @param aiResponse AI回答
     */
    protected void onTaskCompleted(String userMessage, String aiResponse) {
        // 默认不做任何操作，子类可以重写此方法
    }
    
    // ============== SSE 事件发送方法 ==============
    
    /**
     * 发送 SSE 事件
     * 
     * @param event 事件对象
     */
    protected void sendSseEvent(AgentSseEvent event) {
        if (currentEmitter == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            currentEmitter.send(SseEmitter.event().data(json));
        } catch (IOException e) {
            log.warn("发送 SSE 事件失败: {}", e.getMessage());
        }
    }
    
    /**
     * 发送思考内容事件
     */
    protected void sendThinkingEvent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            sendSseEvent(AgentSseEvent.thinking(content));
        }
    }
    
    /**
     * 发送步骤开始事件
     */
    protected void sendStepStartEvent(int stepNumber, int maxSteps) {
        sendSseEvent(AgentSseEvent.stepStart(stepNumber, maxSteps));
    }
    
    /**
     * 发送工具开始执行事件
     */
    protected void sendToolStartEvent(String toolName, String arguments, String summary) {
        sendSseEvent(AgentSseEvent.toolStart(toolName, arguments, summary));
    }
    
    /**
     * 发送工具执行完成事件
     */
    protected void sendToolCompleteEvent(String toolName, String result, String summary) {
        sendSseEvent(AgentSseEvent.toolComplete(toolName, result, summary));
    }
    
    /**
     * 发送工具执行失败事件
     */
    protected void sendToolErrorEvent(String toolName, String error) {
        sendSseEvent(AgentSseEvent.toolError(toolName, error));
    }
    
    /**
     * 发送最终结果事件
     */
    protected void sendResultEvent(String content) {
        sendSseEvent(AgentSseEvent.result(content));
    }
    
    /**
     * 发送错误事件
     */
    protected void sendErrorEvent(String errorMessage) {
        sendSseEvent(AgentSseEvent.error(errorMessage));
    }
    
    /**
     * 发送完成事件
     */
    protected void sendCompleteEvent() {
        sendSseEvent(AgentSseEvent.complete());
    }

    /**
     * 运行代理（流式输出）
     * 
     * 支持实时发送 SSE 事件：
     * - 步骤开始/完成
     * - AI 思考内容
     * - 工具调用状态（开始/完成/失败）
     * - 最终结果
     *
     * @param userPrompt 用户提示词
     * @return SseEmitter实例
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建SseEmitter，设置较长的超时时间
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时
        this.currentEmitter = emitter;

        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sendErrorEvent("无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    sendErrorEvent("不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }

                // 更改状态
                state = AgentState.RUNNING;
                // 记录消息上下文
                messageList.add(new UserMessage(userPrompt));

                // 保存最终结果和所有有效的思考结果
                String finalResult = null;
                StringBuilder collectedContent = new StringBuilder();

                try {
                    for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step {}/{}", stepNumber, maxSteps);
                        
                        // 发送步骤开始事件
                        sendStepStartEvent(stepNumber, maxSteps);

                        // 单步执行
                        String stepResult = step();
                        // 保存最后一次结果
                        finalResult = stepResult;
                        
                        // 收集每一步的有效内容
                        if (stepResult != null && !stepResult.isEmpty()) {
                            // 提取 readFile 工具返回的内容（通常是有价值的文档内容）
                            if (stepResult.contains("工具 readFile 返回的结果：")) {
                                String readFileContent = stepResult.replace("工具 readFile 返回的结果：", "").trim();
                                // 去除首尾的引号
                                if (readFileContent.startsWith("\"") && readFileContent.endsWith("\"")) {
                                    readFileContent = readFileContent.substring(1, readFileContent.length() - 1);
                                }
                                if (!readFileContent.isEmpty() && readFileContent.length() > 50) {
                                    // 只收集较长的内容（通常是有价值的文档）
                                    if (collectedContent.length() > 0) {
                                        collectedContent.append("\n\n");
                                    }
                                    collectedContent.append(readFileContent);
                                }
                            }
                            // 收集非工具调用结果的思考内容
                            else if (!stepResult.startsWith("工具 ")  // 过滤其他工具调用结果
                                    && !stepResult.contains("返回的结果：")  // 过滤工具返回信息
                                    && !stepResult.equals("思考完成")
                                    && !stepResult.equals("思考完成 - 无需行动")) {
                                if (collectedContent.length() > 0) {
                                    collectedContent.append("\n\n");
                                }
                                collectedContent.append(stepResult);
                            }
                        }
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                        state = AgentState.FINISHED;
                        // 如果有收集到的内容，优先输出收集的内容
                        if (collectedContent.length() > 0) {
                            finalResult = collectedContent.toString() + "\n\n---\n*（提示：任务已达到最大步骤限制，以上是目前收集到的信息）*";
                        } else if (finalResult != null && !finalResult.isEmpty()) {
                            // 如果没有收集到内容但有最后结果，输出最后结果
                            finalResult = finalResult + "\n\n---\n*（提示：任务已达到最大步骤限制）*";
                        } else {
                            finalResult = "抱歉，任务达到最大步骤限制，暂时无法获取完整信息。请尝试简化您的问题。";
                        }
                    }
                    
                    // 处理最终结果：如果是 doTerminate 工具的结果，使用收集到的内容代替
                    if (finalResult != null && finalResult.contains("doTerminate") && finalResult.contains("返回的结果")) {
                        if (collectedContent.length() > 0) {
                            finalResult = collectedContent.toString();
                        } else {
                            // 如果没有收集到内容，返回一个友好的提示
                            finalResult = "任务已完成，但未能获取到有效的输出内容。";
                        }
                    }
                    
                    // 发送最终结果给用户（使用新的事件格式）
                    if (finalResult != null && !finalResult.isEmpty()) {
                        sendResultEvent(finalResult);
                        // 任务完成后，调用回调方法保存对话（用于子类实现持久化）
                        onTaskCompleted(userPrompt, finalResult);
                    }
                    // 发送完成事件
                    sendCompleteEvent();
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("执行智能体失败", e);
                    try {
                        // 如果有收集到的内容，即使出错也尝试返回
                        String errorResponse;
                        if (collectedContent.length() > 0) {
                            errorResponse = collectedContent.toString() + "\n\n---\n*（提示：处理过程中出现错误，以上是已收集到的信息）*";
                        } else {
                            errorResponse = "抱歉，处理您的请求时出现错误: " + e.getMessage();
                        }
                        sendErrorEvent(errorResponse);
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    // 清理资源
                    this.currentEmitter = null;
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.currentEmitter = null;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.currentEmitter = null;
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }
}
