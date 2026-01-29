package com.mq.mqaiagent.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一的 AI 模型配置（OpenAI 兼容格式）。
 * 支持配置多个模型实例，包括 DeepSeek、GLM、Gemini 等所有 OpenAI 兼容的模型。
 * 
 * 配置示例：
 * <pre>
 * mq:
 *   ai:
 *     models:
 *       deepseek:
 *         name: "DeepSeek"
 *         description: "DeepSeek 官方模型"
 *         api-key: ${DEEPSEEK_API_KEY}
 *         base-url: https://api.deepseek.com
 *         model: deepseek-chat
 *         enabled: true
 *       "glm-4.7":
 *         name: "GLM-4.7"
 *         description: "智谱 GLM-4.7 模型"
 *         api-key: ${CUSTOM_API_KEY}
 *         base-url: https://your-api.com/v1
 *         model: glm-4.7
 *         enabled: true
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "mq.ai")
@Data
public class CustomModelProperties {

    /**
     * 多个模型的配置，key 为模型标识（如 deepseek、glm-4.7、gemini-2.5-flash）。
     */
    private Map<String, ModelConfig> models = new HashMap<>();

    /**
     * 单个模型的配置。
     */
    @Data
    public static class ModelConfig {
        /**
         * 模型显示名称（前端展示用）。
         */
        private String name;

        /**
         * 模型描述（前端展示用）。
         */
        private String description;

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
    public ModelConfig getModelConfig(String modelId) {
        return models.get(modelId);
    }

    /**
     * 获取所有已启用的模型配置。
     */
    public List<ModelInfo> getEnabledModels() {
        return models.entrySet().stream()
                .filter(entry -> entry.getValue().isConfigured())
                .map(entry -> new ModelInfo(
                        entry.getKey(),
                        entry.getValue().getName() != null ? entry.getValue().getName() : entry.getKey(),
                        entry.getValue().getDescription(),
                        "openai-compatible"
                ))
                .collect(Collectors.toList());
    }

    /**
     * 检查是否有任何已配置的模型。
     */
    public boolean hasAnyConfiguredModel() {
        return models.values().stream().anyMatch(ModelConfig::isConfigured);
    }

    /**
     * 模型信息（用于 API 返回）。
     */
    public record ModelInfo(String id, String name, String description, String type) {
    }
}
