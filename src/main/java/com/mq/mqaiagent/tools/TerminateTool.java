package com.mq.mqaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * ClassName：TerminateTool
 * Package:com.mq.mqaiagent.tools
 * Description: 终止工具类
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 16:11
 * @Version:v1.0
 */
public class TerminateTool {

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
            "When you have finished all the tasks, call this tool to end the work.  
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
