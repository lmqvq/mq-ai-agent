package com.mq.mqaiagent.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.mq.mqaiagent.agent.MqManus;
import com.mq.mqaiagent.app.KeepApp;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.config.RateLimiterConfig;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.model.entity.User;
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
import java.util.concurrent.TimeUnit;

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
    private RateLimiter aiRateLimiter;

    @Resource
    private RateLimiterConfig.UserRateLimiterManager userRateLimiterManager;

    @Resource
    private com.mq.mqaiagent.pool.ChatClientPool chatClientPool;

    /**
     * KeepApp 使用流式对话（支持用户认证）前端调用的接口
     *
     * @param message
     * @param chatId
     * @param request
     * @return
     */
    @GetMapping(value = "/keep_app/chat/sse/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithKeepAppSSEUser(String message, String chatId, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        // 用户级别限流
        if (!userRateLimiterManager.tryAcquire(currentUser.getId(), 1, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
        // AI 接口限流
        if (!aiRateLimiter.tryAcquire()) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "系统繁忙，请稍后再试");
        }
        return keepApp.doChatByStream(message, chatId, currentUser.getId());
    }

    /**
     * KeepApp 使用流式对话
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/keep_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithKeepAppServerSentEvent(String message, String chatId) {
        return keepApp.doChatByStream(message, chatId)
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
    public SseEmitter doChatWithKeepAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter 3分钟超时
        SseEmitter emitter = new SseEmitter(180000L);
        // 获取 Flux 数据流并直接订阅
        keepApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete);
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        // 使用对象池创建MqManus实例（不支持记忆）
        MqManus mqManus = new MqManus(allTools, chatClientPool);
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
    public SseEmitter doChatWithManusUser(String message, String chatId, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        // 使用对象池创建支持用户记忆的MqManus实例
        MqManus mqManus = new MqManus(allTools, chatClientPool, currentUser.getId());
        return mqManus.runStream(message);
    }
}
