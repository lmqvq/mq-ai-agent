package com.mq.mqaiagent.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek（OpenAI 兼容）模型配置。
 */
@Configuration
@Slf4j
public class DeepSeekChatModelConfig {

    @Bean(name = "deepseekChatModel")
    @ConditionalOnProperty(prefix = "mq.ai.deepseek", name = "api-key")
    public ChatModel deepseekChatModel(DeepSeekProperties properties) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .build();

        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                .model(properties.getModel())
                .temperature(properties.getTemperature())
                // 关闭内置工具调用执行，交由我们自己的 ToolCallingManager 处理
                .proxyToolCalls(true)
                .build();

        log.info("初始化 DeepSeek ChatModel，baseUrl: {}, model: {}, apiKeyPresent: {}",
                properties.getBaseUrl(), properties.getModel(), properties.isConfigured());

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(defaultOptions)
                .build();
    }

    @Bean(name = "deepseekToolCallOptions")
    @ConditionalOnBean(name = "deepseekChatModel")
    public ChatOptions deepseekToolCallOptions(DeepSeekProperties properties,
            @Qualifier("deepseekChatModel") ChatModel deepseekChatModel) {
        // 与 deepseekChatModel 的默认选项保持一致，显式提供给 ToolCallAgent 使用
        return OpenAiChatOptions.builder()
                .model(properties.getModel())
                .temperature(properties.getTemperature())
                .proxyToolCalls(true)
                .build();
    }
}
