package com.mq.mqaiagent.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义模型（OpenAI 兼容）配置。
 * 根据配置动态创建多个模型实例。
 */
@Configuration
@Slf4j
public class CustomModelChatModelConfig {

    /**
     * 创建自定义模型池，根据配置动态初始化所有启用的模型。
     */
    @Bean(name = "customModelPool")
    public Map<String, CustomModelInstance> customModelPool(CustomModelProperties properties) {
        Map<String, CustomModelInstance> modelPool = new HashMap<>();

        properties.getModels().forEach((modelName, config) -> {
            if (config.isConfigured()) {
                try {
                    OpenAiApi openAiApi = OpenAiApi.builder()
                            .apiKey(config.getApiKey())
                            .baseUrl(config.getBaseUrl())
                            .build();

                    OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
                            .model(config.getModel())
                            .temperature(config.getTemperature())
                            // 关闭内置工具调用执行，交由我们自己的 ToolCallingManager 处理
                            .proxyToolCalls(true)
                            .build();

                    ChatModel chatModel = OpenAiChatModel.builder()
                            .openAiApi(openAiApi)
                            .defaultOptions(defaultOptions)
                            .build();

                    log.info("初始化自定义模型: {}, baseUrl: {}, model: {}",
                            modelName, config.getBaseUrl(), config.getModel());

                    modelPool.put(modelName.toLowerCase(), new CustomModelInstance(chatModel, defaultOptions));
                } catch (Exception e) {
                    log.error("初始化自定义模型失败: {}", modelName, e);
                }
            } else {
                log.debug("自定义模型 {} 未配置或未启用，跳过", modelName);
            }
        });

        log.info("自定义模型池初始化完成，共 {} 个模型", modelPool.size());
        return modelPool;
    }

    /**
     * 自定义模型实例封装。
     */
    public record CustomModelInstance(ChatModel chatModel, ChatOptions chatOptions) {
    }
}
