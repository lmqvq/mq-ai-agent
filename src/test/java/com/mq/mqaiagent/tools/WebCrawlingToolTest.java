package com.mq.mqaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName：WebCrawlingToolTest
 * Package:com.mq.mqaiagent.tools
 * Description: 网页爬取工具测试类
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 23:01
 * @Version:v1.0
 */
@SpringBootTest
class WebCrawlingToolTest {

    @Test
    void crawl() {
        WebCrawlingTool webCrawlingTool = new WebCrawlingTool();
        String url = "https://xiaolincoding.com/";
        String result = webCrawlingTool.crawl(url);
        assertNotNull(result);
    }
}