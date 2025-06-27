package com.mq.mqaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName：ResourceDownloadToolTest
 * Package:com.mq.mqaiagent.tools
 * Description: 资源下载工具测试类
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 23:11
 * @Version:v1.0
 */
class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://mq-picture-1324656182.cos.ap-guangzhou.myqcloud.com/public/1904924220320710658/2025-03-28_iUvVuGlBbzd4RDOB.webp";
        String name = "test.png";
        String result = tool.downloadResource(url, name);
        assertNotNull(result);
    }
}