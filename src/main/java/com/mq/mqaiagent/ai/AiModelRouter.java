package com.mq.mqaiagent.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    private final Map<String, CustomModelChatModelConfig.CustomModelInstance> customModelPool;

    public AiModelRouter(
            AiModelProperties aiModelProperties,
            DeepSeekProperties deepSeekProperties,
            @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
            @Qualifier("deepseekChatModel") ObjectProvider<ChatModel> deepseekChatModelProvider,
            @Qualifier("deepseekToolCallOptions") ObjectProvider<ChatOptions> deepseekToolCallOptionsProvider,
            @Qualifier("customModelPool") Map<String, CustomModelChatModelConfig.CustomModelInstance> customModelPool) {
        this.aiModelProperties = aiModelProperties;
        this.deepSeekProperties = deepSeekProperties;
        this.dashscopeChatModel = dashscopeChatModel;
        this.deepseekChatModelProvider = deepseekChatModelProvider;
        this.deepseekToolCallOptionsProvider = deepseekToolCallOptionsProvider;
        this.customModelPool = customModelPool;
    }

    public AiModelType getDefaultModelType() {
        return aiModelProperties.getDefaultModelType();
    }

    public AiModelType resolveRequestedType(String rawModel) {
        return AiModelType.from(rawModel, getDefaultModelType());
    }

    public ResolvedModel resolve(String rawModel) {
        return resolve(resolveRequestedType(rawModel), rawModel);
    }

    /**
     * 根据模型类型解析（不支持自定义模型的动态查找）。
     * 注意：如果需要使用自定义模型，请使用 resolve(String rawModel) 或 resolve(AiModelType, String)。
     */
    public ResolvedModel resolve(AiModelType requestedType) {
        // 对于 CUSTOM 类型，由于没有原始模型名称，无法从池中查找，回退到默认模型
        if (requestedType == AiModelType.CUSTOM) {
            log.warn("调用 resolve(AiModelType.CUSTOM) 但未提供原始模型名称，无法匹配自定义模型，回退到默认模型");
            return resolve(getDefaultModelType(), null);
        }
        return resolve(requestedType, null);
    }

    public ResolvedModel resolve(AiModelType requestedType, String rawModel) {
        AiModelType effectiveType = requestedType == null ? getDefaultModelType() : requestedType;

        if (effectiveType == AiModelType.CUSTOM) {
            // 尝试从自定义模型池中查找
            String normalizedModelName = rawModel != null ? rawModel.trim().toLowerCase() : null;
            if (normalizedModelName != null && customModelPool.containsKey(normalizedModelName)) {
                CustomModelChatModelConfig.CustomModelInstance instance = customModelPool.get(normalizedModelName);
                log.debug("路由到自定义模型: {}", normalizedModelName);
                return new ResolvedModel(AiModelType.CUSTOM, instance.chatModel(), instance.chatOptions());
            }
            log.warn("请求自定义模型: {}，但未在模型池中找到，回退到 qwen-plus", normalizedModelName);
            effectiveType = AiModelType.QWEN_PLUS;
        }

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

