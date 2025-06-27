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

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "如何避免肩部撞击？";
        String answer = String.valueOf(keepApp.doChatWithReport(message, chatId));
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("如何避免肩部撞击？");

        // 测试网页抓取：健身案例分析
        testMessage("最近我想通过健身来增肌，请你帮我找找如何科学的增肌？https://xiaolincoding.com/");

        // 测试资源下载：图片下载
        testMessage("帮我下载一张布布一二图片为文件");

        // 测试文件操作：保存用户档案
        testMessage("保存我的健身计划为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘增肌健身计划’PDF，包含健身项目、健身动作和健身方式");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = keepApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
}