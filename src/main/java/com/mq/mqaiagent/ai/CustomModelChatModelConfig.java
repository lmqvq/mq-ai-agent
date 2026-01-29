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
 * 统一的 AI 模型配置（OpenAI 兼容格式）。
 * 根据配置动态创建模型池，支持 DeepSeek、GLM、Gemini 等所有 OpenAI 兼容的模型。
 */
@Configuration
@Slf4j
public class CustomModelChatModelConfig {

    /**
     * 创建统一模型池，根据配置动态初始化所有启用的 OpenAI 兼容模型。
     * 包括 DeepSeek、GLM、Gemini 等所有配置在 mq.ai.models 下的模型。
     */
    @Bean(name = "openAiCompatibleModelPool")
    public Map<String, ModelInstance> openAiCompatibleModelPool(CustomModelProperties properties) {
        Map<String, ModelInstance> modelPool = new HashMap<>();

        log.info("配置的模型数量: {}", properties.getModels().size());
        properties.getModels().forEach((modelId, config) -> {
            log.info("检查模型 {}: apiKey={}, baseUrl={}, enabled={}, isConfigured={}",
                    modelId, 
                    config.getApiKey() != null ? "***" : "null",
                    config.getBaseUrl(),
                    config.getEnabled(),
                    config.isConfigured());
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

                    String displayName = config.getName() != null ? config.getName() : modelId;
                    log.info("初始化模型: {} ({}), baseUrl: {}, model: {}",
                            displayName, modelId, config.getBaseUrl(), config.getModel());

                    modelPool.put(modelId.toLowerCase(), new ModelInstance(chatModel, defaultOptions));
                } catch (Exception e) {
                    log.error("初始化模型失败: {}", modelId, e);
                }
            } else {
                log.debug("模型 {} 未配置或未启用，跳过", modelId);
            }
        });

        log.info("模型池初始化完成，共 {} 个 OpenAI 兼容模型可用", modelPool.size());
        return modelPool;
    }

    /**
     * 模型实例封装。
     */
    public record ModelInstance(ChatModel chatModel, ChatOptions chatOptions) {
    }
}
