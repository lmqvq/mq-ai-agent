package com.mq.mqaiagent.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义模型（OpenAI 兼容）的配置。
 * 支持配置多个模型实例，每个实例有独立的 base-url、api-key、model 等参数。
 */
@Component
@ConfigurationProperties(prefix = "mq.ai.custom")
@Data
public class CustomModelProperties {

    /**
     * 多个自定义模型的配置，key 为模型标识（如 glm-4.7、gemini-2.5-flash）。
     */
    private Map<String, ModelConfig> models = new HashMap<>();

    /**
     * 单个模型的配置。
     */
    @Data
    public static class ModelConfig {
        /**
         * 模型 API Key。
         */
        private String apiKey;

        /**
         * OpenAI 兼容接口的基地址。
         */
        private String baseUrl;

        /**
         * 实际使用的模型名称（传给 API 的 model 参数）。
         */
        private String model;

        /**
         * 温度参数。
         */
        private Double temperature = 0.7D;

        /**
         * 是否启用该模型。
         */
        private Boolean enabled = true;

        public boolean isConfigured() {
            return enabled && apiKey != null && !apiKey.isBlank() && baseUrl != null && !baseUrl.isBlank();
        }
    }

    /**
     * 根据模型标识获取配置。
     */
    public ModelConfig getModelConfig(String modelName) {
        return models.get(modelName);
    }

    /**
     * 检查是否有任何已配置的模型。
     */
    public boolean hasAnyConfiguredModel() {
        return models.values().stream().anyMatch(ModelConfig::isConfigured);
    }
}
