package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.agent.MqManus;
import com.mq.mqaiagent.ai.AiModelRouter;
import com.mq.mqaiagent.ai.AiModelType;
import com.mq.mqaiagent.app.KeepApp;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.pool.ChatClientPool;
import com.mq.mqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName：AiController
 * Package:com.mq.mqaiagent.controller
 * Description: AI 接口
 * Author：MQQQ
 *
 * @Create:2025/7/4 - 0:45
 * @Version:v1.0
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private KeepApp keepApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private UserService userService;

    @Resource
    private ChatClientPool chatClientPool;

    @Resource
    private AiModelRouter aiModelRouter;

    /**
     * KeepApp 使用流式对话（支持用户认证）前端调用的接口
     *
     * @param message
     * @param chatId
     * @param request
     * @return
     */
    @GetMapping(value = "/keep_app/chat/sse/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithKeepAppSSEUser(String message, String chatId, String model, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        return keepApp.doChatByStream(message, chatId, currentUser.getId(), model);
    }

    /**
     * KeepApp 使用流式对话。
     */
    @GetMapping("/keep_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithKeepAppServerSentEvent(String message, String chatId, String model) {
        return keepApp.doChatByStream(message, chatId, model)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * KeepApp 使用流式对话
     * 
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/keep_app/chat/sse/emitter")
    public SseEmitter doChatWithKeepAppSseEmitter(String message, String chatId, String model) {
        SseEmitter emitter = new SseEmitter(180000L);
        keepApp.doChatByStream(message, chatId, model)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete);
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体。
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message, String model) {
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        MqManus mqManus = new MqManus(allTools, chatClientPool, modelType);
        return mqManus.runStream(message);
    }

    /**
     * 流式调用 Manus 超级智能体（支持用户认证和对话记忆）前端调用的接口
     *
     * @param message
     * @param chatId
     * @param request
     * @return
     */
    @GetMapping("/manus/chat/user")
    public SseEmitter doChatWithManusUser(String message, String chatId, String model, HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        AiModelType modelType = chatClientPool.resolveModel(model).modelType();
        MqManus mqManus = new MqManus(allTools, chatClientPool, currentUser.getId(), modelType);
        return mqManus.runStream(message);
    }

    /**
     * 获取可用的 AI 模型列表
     * 
     * @return 模型列表，包含 ID、名称、描述等信息
     */
    @GetMapping("/models")
    public BaseResponse<Map<String, Object>> getAvailableModels() {
        List<AiModelRouter.ModelInfo> models = aiModelRouter.getAvailableModels();
        String defaultModel = aiModelRouter.getDefaultModelId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("models", models);
        result.put("defaultModel", defaultModel);
        
        return ResultUtils.success(result);
    }
}
