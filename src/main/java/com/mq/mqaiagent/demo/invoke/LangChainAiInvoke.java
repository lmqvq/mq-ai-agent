package com.mq.mqaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * ClassName：LangChainAiInvoke
 * Package:com.mq.mqaiagent.demo.invoke
 * Description: LangChain4j 调用示例
 * Author：MQQQ
 *
 * @Create:2025/6/18 - 14:53
 * @Version:v1.0
 */
public class LangChainAiInvoke {

    public static void main(String[] args) {
        ChatLanguageModel qwenModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String answer = qwenModel.chat("Hello");
        System.out.println(answer);
    }
}
