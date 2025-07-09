package com.mq.mqaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * ClassName：MqManusTest
 * Package:com.mq.mqaiagent.agent
 * Description: MqManus 测试类
 * Author：MQQQ
 *
 * @Create:2025/7/2 - 16:21
 * @Version:v1.0
 */
@SpringBootTest
class MqManusTest {

    @Resource
    private MqManus mqManus;

    @Test
    void run() {
        String userPrompt = """  
                我的想通过健身来增肌，请你帮我找找我应该如何科学健身，
                并结合一些健身知识，制定一份详细的健身计划，
                并以 PDF 格式输出""";
        String answer = mqManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}