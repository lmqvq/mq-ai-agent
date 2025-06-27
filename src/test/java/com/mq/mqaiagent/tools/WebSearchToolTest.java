package com.mq.mqaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName：WebSearchToolTest
 * Package:com.mq.mqaiagent.tools
 * Description: 网页搜索工具测试类
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 22:46
 * @Version:v1.0
 */
@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        String query = "小林coding";
        String result = webSearchTool.searchWeb(query);
        assertNotNull(result);
    }
}