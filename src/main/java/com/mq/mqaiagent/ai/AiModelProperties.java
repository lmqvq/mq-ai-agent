package com.mq.mqaiagent.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模型相关的全局配置。
 */
@Component
@ConfigurationProperties(prefix = "mq.ai")
@Data
public class AiModelProperties {

    /**
     * 默认模型（保持现有行为：qwen-plus）。
     */
    private String defaultModel = "qwen-plus";

    public AiModelType getDefaultModelType() {
        return AiModelType.from(defaultModel, AiModelType.QWEN_PLUS);
    }
}

