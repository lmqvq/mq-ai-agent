package com.mq.mqaiagent.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 模型路由器：根据配置或参数选择实际使用的模型与选项。
 */
@Component
@Slf4j
public class AiModelRouter {

    private final AiModelProperties aiModelProperties;
    private final DeepSeekProperties deepSeekProperties;
    private final ChatModel dashscopeChatModel;
    private final ObjectProvider<ChatModel> deepseekChatModelProvider;
    private final ObjectProvider<ChatOptions> deepseekToolCallOptionsProvider;

    public AiModelRouter(
            AiModelProperties aiModelProperties,
            DeepSeekProperties deepSeekProperties,
            @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
            @Qualifier("deepseekChatModel") ObjectProvider<ChatModel> deepseekChatModelProvider,
            @Qualifier("deepseekToolCallOptions") ObjectProvider<ChatOptions> deepseekToolCallOptionsProvider) {
        this.aiModelProperties = aiModelProperties;
        this.deepSeekProperties = deepSeekProperties;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepseekChatModelProvider = deepseekChatModelProvider;
        this.deepseekToolCallOptionsProvider = deepseekToolCallOptionsProvider;
    }

    public AiModelType getDefaultModelType() {
        return aiModelProperties.getDefaultModelType();
    }

    public AiModelType resolveRequestedType(String rawModel) {
        return AiModelType.from(rawModel, getDefaultModelType());
    }

    public ResolvedModel resolve(String rawModel) {
        return resolve(resolveRequestedType(rawModel));
    }

    public ResolvedModel resolve(AiModelType requestedType) {
        AiModelType effectiveType = requestedType == null ? getDefaultModelType() : requestedType;

        if (effectiveType == AiModelType.DEEPSEEK) {
            ChatModel deepseekChatModel = deepseekChatModelProvider.getIfAvailable();
            ChatOptions deepseekToolCallOptions = deepseekToolCallOptionsProvider.getIfAvailable();
            if (deepseekChatModel != null && deepseekToolCallOptions != null) {
                return new ResolvedModel(AiModelType.DEEPSEEK, deepseekChatModel, deepseekToolCallOptions);
            }
            log.warn("请求 DeepSeek，但 deepseekChatModel 未就绪（apiKeyPresent: {}），回退到 qwen-plus",
                    deepSeekProperties.isConfigured());
            effectiveType = AiModelType.QWEN_PLUS;
        }

        return new ResolvedModel(effectiveType, dashscopeChatModel, buildDashscopeToolCallOptions());
    }

    private ChatOptions buildDashscopeToolCallOptions() {
        // 关闭内置工具调用执行，交由我们自己的 ToolCallingManager 处理
        return DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 路由后的模型信息（包含实际生效的模型类型，便于缓存键隔离）。
     */
    public record ResolvedModel(AiModelType modelType, ChatModel chatModel, ChatOptions toolCallOptions) {
    }
}

