package com.mq.mqaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * ClassName：WebCrawlingTool
 * Package:com.mq.mqaiagent.tools
 * Description: 网页爬取工具
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 22:55
 * @Version:v1.0
 */
public class WebCrawlingTool {

    @Tool(description = "Crawl web pages")
    public String crawl(@ToolParam(description = "URL of the web page to Crawl") String url){
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error Crawl web pages: " + e.getMessage();
        }
    }
}
