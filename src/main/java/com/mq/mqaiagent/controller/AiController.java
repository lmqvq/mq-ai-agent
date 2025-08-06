package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.agent.MqManus;
import com.mq.mqaiagent.app.KeepApp;
import jakarta.annotation.Resource;
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

    /**
     * KeepAPP 基础对话（同步调用）
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/keep_app/chat/sync")
    public String doChatWithKeepAppSync(String message, String chatId) {
        return keepApp.doChat(message,chatId);
    }

    /**
     * KeepApp 使用流式对话
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/keep_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithKeepAppSSE(String message, String chatId) {
        return keepApp.doChatByStream(message, chatId);
    }

    /**
     * KeepApp 使用流式对话
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
                        emitter::complete
                );
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
}
