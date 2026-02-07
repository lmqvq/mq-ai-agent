package com.mq.mqaiagent.model.enums;

/**
 * SSE 事件类型枚举
 * 
 * 用于区分 AI 超级智能体执行过程中的不同事件类型，
 * 支持前端实现流式输出和工具调用状态展示。
 * 
 * @author MQQQ
 */
public enum SseEventType {
    
    /**
     * 思考内容 - AI 正在分析和思考
     */
    THINKING("thinking"),
    
    /**
     * 工具开始执行
     */
    TOOL_START("tool_start"),
    
    /**
     * 工具执行完成
     */
    TOOL_COMPLETE("tool_complete"),
    
    /**
     * 工具执行失败
     */
    TOOL_ERROR("tool_error"),
    
    /**
     * 步骤开始
     */
    STEP_START("step_start"),
    
    /**
     * 步骤完成
     */
    STEP_COMPLETE("step_complete"),
    
    /**
     * 最终结果
     */
    RESULT("result"),
    
    /**
     * 错误信息
     */
    ERROR("error"),
    
    /**
     * 任务完成
     */
    COMPLETE("complete");
    
    private final String code;
    
    SseEventType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
