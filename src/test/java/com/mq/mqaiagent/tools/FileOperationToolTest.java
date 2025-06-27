package com.mq.mqaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName：FileOperationToolTest
 * Package:com.mq.mqaiagent.tools
 * Description: 文件操作工具测试类
 * Author：MQQQ
 *
 * @Create:2025/6/26 - 22:12
 * @Version:v1.0
 */
class FileOperationToolTest {

    @Test
    public void testReadFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "LMQICU.txt";
        String result = tool.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    public void testWriteFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "LMQICU.txt";
        String content = "https://xxx.online/ 云图库";
        String result = tool.writeFile(fileName, content);
        assertNotNull(result);
    }
}