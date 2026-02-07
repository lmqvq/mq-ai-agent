package com.mq.mqaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClassName：ReActAgent
 * Package:com.mq.mqaiagent.agent
 * Description: ReAct (Reasoning and Acting) 模式的代理抽象类，实现了思考-行动的循环模式
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 16:03
 * @Version:v1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                // 不需要调用工具，返回 LLM 的思考结果
                return getThinkResult();
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }

    /**
     * 获取思考结果（子类可重写）
     *
     * @return LLM 的思考/回答内容
     */
    protected String getThinkResult() {
        return "思考完成 - 无需行动";
    }
}
