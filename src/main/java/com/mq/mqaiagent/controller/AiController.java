package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.agent.MqManus;
import com.mq.mqaiagent.app.KeepApp;
import com.mq.mqaiagent.chatmemory.DatabaseChatMemory;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

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
    private ChatModel dashscopeChatModel;

    @Resource
    private UserService userService;

    @Resource
    private KeepReportMapper keepReportMapper;

    /**
     * KeepAPP 基础对话（同步调用）
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/keep_app/chat/sync")
    public String doChatWithKeepAppSync(String message, String chatId) {
        return keepApp.doChat(message, chatId);
    }

    /**
     * KeepAPP 基础对话（同步调用，支持用户认证）
     * 
     * @param message
     * @param chatId
     * @param request
     * @return
     */
    @GetMapping("/keep_app/chat/sync/user")
    public String doChatWithKeepAppSyncUser(String message, String chatId, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        return keepApp.doChat(message, chatId, currentUser.getId());
    }

    /**
     * KeepApp 使用流式对话
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/keep_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithKeepAppSSE(String message, String chatId, HttpServletRequest request) {
        // 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        return keepApp.doChatByStream(message, chatId, currentUser.getId());
    }

    /**
     * KeepApp 使用流式对话（支持用户认证）
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
        MqManus mqManus = new MqManus(allTools, dashscopeChatModel);
        return mqManus.runStream(message);
    }

    /**
     * 流式调用 Manus 超级智能体（支持用户认证和对话记忆）
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

        // 创建支持用户ID的DatabaseChatMemory
        DatabaseChatMemory chatMemory = new DatabaseChatMemory(keepReportMapper);
        chatMemory.setCurrentUserId(currentUser.getId());

        // 创建带对话记忆的MqManus实例
        MqManus mqManus = new MqManus(allTools, dashscopeChatModel, chatMemory);

        return mqManus.runStream(message);
    }
}
