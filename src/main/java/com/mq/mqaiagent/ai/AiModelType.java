package com.mq.mqaiagent.ai;

import java.util.Locale;
import java.util.Objects;

/**
 * 支持的模型类型枚举。
 */
public enum AiModelType {

    QWEN_PLUS("qwen-plus"),
    DEEPSEEK("deepseek"),
    CUSTOM("custom");

    private final String code;

    AiModelType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 将外部传入的模型标识解析为枚举。
     * 支持常见别名并对空值返回默认值。
     */
    public static AiModelType from(String raw, AiModelType defaultType) {
        if (raw == null || raw.isBlank()) {
            return Objects.requireNonNullElse(defaultType, QWEN_PLUS);
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "qwen", "qwen-plus", "dashscope", "tongyi", "通义千问" -> QWEN_PLUS;
            case "deepseek", "deepseek-chat", "deep-seek" -> DEEPSEEK;
            // 其他不识别的模型名称都当作 CUSTOM 处理，交由 AiModelRouter 进行动态匹配
            default -> CUSTOM;
        };
    }
}

