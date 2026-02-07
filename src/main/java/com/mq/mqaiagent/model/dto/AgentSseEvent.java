package com.mq.mqaiagent.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mq.mqaiagent.model.enums.SseEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 超级智能体 SSE 事件 DTO
 * 
 * 统一的 SSE 消息格式，支持：
 * - 思考内容流式输出
 * - 工具调用状态展示（折叠卡片）
 * - 最终结果输出
 * 
 * @author MQQQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentSseEvent {
    
    /**
     * 事件类型
     */
    private String type;
    
    /**
     * 内容（思考内容/最终结果）
     */
    private String content;
    
    /**
     * 工具名称（工具调用事件时使用）
     */
    private String toolName;
    
    /**
     * 工具调用状态：executing/completed/failed
     */
    private String status;
    
    /**
     * 摘要信息（工具调用的简短描述）
     */
    private String summary;
    
    /**
     * 工具调用的详细参数（可选，用于折叠展示）
     */
    private String arguments;
    
    /**
     * 工具调用的结果（可选，用于折叠展示）
     */
    private String result;
    
    /**
     * 当前步骤编号
     */
    private Integer stepNumber;
    
    /**
     * 最大步骤数
     */
    private Integer maxSteps;
    
    /**
     * 是否可折叠（前端控制是否显示折叠按钮）
     */
    private Boolean collapsible;
    
    // ============== 静态工厂方法 ==============
    
    /**
     * 创建思考事件
     */
    public static AgentSseEvent thinking(String content) {
        return AgentSseEvent.builder()
                .type(SseEventType.THINKING.getCode())
                .content(content)
                .build();
    }
    
    /**
     * 创建步骤开始事件
     */
    public static AgentSseEvent stepStart(int stepNumber, int maxSteps) {
        return AgentSseEvent.builder()
                .type(SseEventType.STEP_START.getCode())
                .stepNumber(stepNumber)
                .maxSteps(maxSteps)
                .build();
    }
    
    /**
     * 创建工具开始执行事件
     */
    public static AgentSseEvent toolStart(String toolName, String arguments, String summary) {
        return AgentSseEvent.builder()
                .type(SseEventType.TOOL_START.getCode())
                .toolName(toolName)
                .status("executing")
                .arguments(arguments)
                .summary(summary)
                .collapsible(true)
                .build();
    }
    
    /**
     * 创建工具执行完成事件
     */
    public static AgentSseEvent toolComplete(String toolName, String result, String summary) {
        return AgentSseEvent.builder()
                .type(SseEventType.TOOL_COMPLETE.getCode())
                .toolName(toolName)
                .status("completed")
                .result(result)
                .summary(summary)
                .collapsible(true)
                .build();
    }
    
    /**
     * 创建工具执行失败事件
     */
    public static AgentSseEvent toolError(String toolName, String error) {
        return AgentSseEvent.builder()
                .type(SseEventType.TOOL_ERROR.getCode())
                .toolName(toolName)
                .status("failed")
                .result(error)
                .summary("执行失败: " + error)
                .collapsible(true)
                .build();
    }
    
    /**
     * 创建最终结果事件
     */
    public static AgentSseEvent result(String content) {
        return AgentSseEvent.builder()
                .type(SseEventType.RESULT.getCode())
                .content(content)
                .build();
    }
    
    /**
     * 创建错误事件
     */
    public static AgentSseEvent error(String errorMessage) {
        return AgentSseEvent.builder()
                .type(SseEventType.ERROR.getCode())
                .content(errorMessage)
                .build();
    }
    
    /**
     * 创建完成事件
     */
    public static AgentSseEvent complete() {
        return AgentSseEvent.builder()
                .type(SseEventType.COMPLETE.getCode())
                .build();
    }
}
