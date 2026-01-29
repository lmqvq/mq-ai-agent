package com.mq.mqaiagent.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型路由器：根据配置或参数选择实际使用的模型与选项。
 * 支持内置模型（qwen-plus）和所有 OpenAI 兼容的自定义模型（deepseek、glm、gemini 等）。
 */
@Component
@Slf4j
public class AiModelRouter {

    private final AiModelProperties aiModelProperties;
    private final CustomModelProperties customModelProperties;
    private final ChatModel dashscopeChatModel;
    private final Map<String, CustomModelChatModelConfig.ModelInstance> openAiCompatibleModelPool;

    public AiModelRouter(
            AiModelProperties aiModelProperties,
            CustomModelProperties customModelProperties,
            @Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel,
            @Qualifier("openAiCompatibleModelPool") Map<String, CustomModelChatModelConfig.ModelInstance> openAiCompatibleModelPool) {
        this.aiModelProperties = aiModelProperties;
        this.customModelProperties = customModelProperties;
        this.dashscopeChatModel = dashscopeChatModel;
        this.openAiCompatibleModelPool = openAiCompatibleModelPool;
    }

    public AiModelType getDefaultModelType() {
        return aiModelProperties.getDefaultModelType();
    }

    public String getDefaultModelId() {
        return aiModelProperties.getDefaultModel();
    }

    public AiModelType resolveRequestedType(String rawModel) {
        return AiModelType.from(rawModel, getDefaultModelType());
    }

    /**
     * 根据原始模型名称解析模型。
     */
    public ResolvedModel resolve(String rawModel) {
        return resolve(resolveRequestedType(rawModel), rawModel);
    }

    /**
     * 根据模型类型解析（不支持自定义模型的动态查找）。
     */
    public ResolvedModel resolve(AiModelType requestedType) {
        if (requestedType == AiModelType.CUSTOM) {
            log.warn("调用 resolve(AiModelType.CUSTOM) 但未提供原始模型名称，回退到默认模型");
            return resolve(getDefaultModelType(), null);
        }
        return resolve(requestedType, null);
    }

    /**
     * 核心路由方法：根据模型类型和原始名称解析到具体模型实例。
     */
    public ResolvedModel resolve(AiModelType requestedType, String rawModel) {
        AiModelType effectiveType = requestedType == null ? getDefaultModelType() : requestedType;

        // 对于 CUSTOM 和 DEEPSEEK 类型，都从统一的模型池中查找
        if (effectiveType == AiModelType.CUSTOM || effectiveType == AiModelType.DEEPSEEK) {
            String normalizedModelName = rawModel != null ? rawModel.trim().toLowerCase() : null;
            
            // 如果是 DEEPSEEK 类型但没有提供原始名称，使用 "deepseek" 作为默认键
            if (effectiveType == AiModelType.DEEPSEEK && normalizedModelName == null) {
                normalizedModelName = "deepseek";
            }
            
            if (normalizedModelName != null && openAiCompatibleModelPool.containsKey(normalizedModelName)) {
                CustomModelChatModelConfig.ModelInstance instance = openAiCompatibleModelPool.get(normalizedModelName);
                log.debug("路由到模型: {}", normalizedModelName);
                return new ResolvedModel(effectiveType, instance.chatModel(), instance.chatOptions());
            }
            
            log.warn("请求模型: {}，但未在模型池中找到，回退到 qwen-plus", normalizedModelName);
            effectiveType = AiModelType.QWEN_PLUS;
        }

        // 默认使用 qwen-plus
        return new ResolvedModel(effectiveType, dashscopeChatModel, buildDashscopeToolCallOptions());
    }

    /**
     * 获取所有可用模型列表（用于前端展示）。
     */
    public List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();
        
        // 添加内置模型 qwen-plus
        models.add(new ModelInfo("qwen-plus", "通义千问", "阿里云通义千问大模型", "builtin", true));
        
        // 添加所有配置的 OpenAI 兼容模型
        customModelProperties.getModels().forEach((modelId, config) -> {
            if (config.isConfigured()) {
                String name = config.getName() != null ? config.getName() : modelId;
                String description = config.getDescription() != null ? config.getDescription() : "";
                models.add(new ModelInfo(modelId, name, description, "openai-compatible", true));
            }
        });
        
        return models;
    }

    private ChatOptions buildDashscopeToolCallOptions() {
        return DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 路由后的模型信息。
     */
    public record ResolvedModel(AiModelType modelType, ChatModel chatModel, ChatOptions toolCallOptions) {
    }

    /**
     * 模型信息（用于 API 返回）。
     */
    public record ModelInfo(String id, String name, String description, String type, boolean enabled) {
    }
}

