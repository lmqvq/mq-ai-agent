package com.mq.mqaiagent.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 模型的配置。
 */
@Component
@ConfigurationProperties(prefix = "mq.ai.deepseek")
@Data
public class DeepSeekProperties {

    /**
     * DeepSeek API Key（建议通过环境变量注入）。
     */
    private String apiKey;

    /**
     * OpenAI 兼容接口的基地址。
     */
    private String baseUrl = "https://api.deepseek.com/v1";

    /**
     * 默认使用的模型名称。
     */
    private String model = "deepseek-chat";

    /**
     * 默认温度参数。
     */
    private Double temperature = 0.7D;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}

