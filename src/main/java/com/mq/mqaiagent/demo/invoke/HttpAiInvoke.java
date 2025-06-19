package com.mq.mqaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName：HttpAiInvoke
 * Package:com.mq.mqaiagent.demo.invoke
 * Description: Http 调用示例
 * Author：MQQQ
 *
 * @Create:2025/6/18 - 14:28
 * @Version:v1.0
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        // 设置请求URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + TestApiKey.API_KEY);
        headers.put("Content-Type", "application/json");

        // 设置请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-plus");

        JSONObject input = new JSONObject();
        JSONObject[] messages = new JSONObject[2];

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messages[0] = systemMessage;

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "Hello");
        messages[1] = userMessage;

        input.put("messages", messages);
        requestBody.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);

        // 发送请求
        HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .body(requestBody.toString())
                .execute();

        // 处理响应
        if (response.isOk()) {
            System.out.println("请求成功，响应内容：");
            System.out.println(response.body());
        } else {
            System.out.println("请求失败，状态码：" + response.getStatus());
            System.out.println("响应内容：" + response.body());
        }
    }
}
