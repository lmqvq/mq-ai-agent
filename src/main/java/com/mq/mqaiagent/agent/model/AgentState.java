package com.mq.mqaiagent.agent.model;

/**
 * ClassName：AgentState
 * Package:com.mq.mqaiagent.agent.model
 * Description: agent 代理状态枚举类
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 15:56
 * @Version:v1.0
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}