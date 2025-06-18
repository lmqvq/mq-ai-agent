package com.mq.mqaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ClassName：SpringAiAiInvoke
 * Package:com.mq.mqaiagent.demo.invoke
 * Description: Spring AI 框架调用 AI 大模型（阿里）
 * Author：MQQQ
 *
 * @Create:2025/6/18 - 14:44
 * @Version:v1.0
 */
// 取消注释即可在 SpringBoot 项目启动时执行
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("Hello"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}

