package com.mq.mqaiagent.app;

import cn.hutool.core.lang.UUID;
import com.mq.mqaiagent.advisor.ForbiddenWordAdvisor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * ClassName：KeepAppTest
 * Package:com.mq.mqaiagent.app
 * Description: 测试健身助手
 * Author：MQQQ
 *
 * @Create:2025/6/18 - 18:33
 * @Version:v1.0
 */
@SpringBootTest
class KeepAppTest {

    @Resource
    private KeepApp keepApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是LMQ，我最近在健身方面遇到了一些问题，你能帮我吗？";
        String answer = keepApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我最近在增肌方面遇到了一些问题，你能帮我吗？";
        answer = keepApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我在哪方面遇到了一些问题，你知道吗？";
        answer = keepApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 包含违禁词输入测试，验证是否抛出异常
        String prohibitedMessage = "自拍";
        Assertions.assertThrows(
                ForbiddenWordAdvisor.ProhibitedWordException.class, () -> keepApp.doChat(prohibitedMessage, chatId),
                "Expected ProhibitedWordException for prohibited message"
        );
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是LMQ，我想通过健身来增肌，请你给我一些健身的建议";
        String answer = String.valueOf(keepApp.doChatWithReport(message, chatId));
        Assertions.assertNotNull(answer);
    }
}