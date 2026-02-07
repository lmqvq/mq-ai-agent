package com.mq.mqaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName：ToolRegistration
 * Package:com.mq.mqaiagent.tools
 * Description: 工具注册类（统一管理和绑定所有工具）
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 23:22
 * @Version:v1.0
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebCrawlingTool webCrawlingTool = new WebCrawlingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        GoogleWebSearchTool googleWebSearchTool = new GoogleWebSearchTool(searchApiKey);
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webCrawlingTool,
                resourceDownloadTool,
                googleWebSearchTool,
                terminateTool
        );
    }
}
