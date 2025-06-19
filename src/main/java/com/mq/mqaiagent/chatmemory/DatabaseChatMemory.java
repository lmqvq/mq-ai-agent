package com.mq.mqaiagent.chatmemory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mq.mqaiagent.model.KeepReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.*;

/**
 * ClassName：DatabaseChatMemory
 * Package:com.mq.mqaiagent.chatmemory
 * Description: 持久化对话到 MySQL
 * Author：MQQQ
 *
 * @Create:2025/6/19 - 18:51
 * @Version:v1.0
 */
@Slf4j
public class DatabaseChatMemory implements ChatMemory {

    private final com.mq.mqaiagent.mapper.KeepReportMapper KeepReportMapper;

    /**
     * 构造函数，初始化 KeepReportMapper。
     *
     * @param KeepReportMapper 恋爱报告数据库操作 Mapper
     */
    public DatabaseChatMemory(com.mq.mqaiagent.mapper.KeepReportMapper KeepReportMapper) {
        this.KeepReportMapper = KeepReportMapper;
    }

    /**
     * 向指定对话 ID 的记忆中添加消息列表。
     * 会将新消息追加到现有消息列表末尾，并持久化到数据库。
     *
     * @param conversationId 对话的唯一标识符。
     * @param messages       要添加的消息列表。
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取当前对话的完整历史记录
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        // 将新消息添加到历史记录末尾
        conversationMessages.addAll(messages);
        // 保存更新后的完整对话历史
        saveConversation(conversationId, conversationMessages);
    }

    /**
     * 获取指定对话 ID 的最近 N 条消息。
     * 如果请求的消息数量 N 大于等于实际消息总数，则返回所有消息。
     * 如果对话不存在或为空，则返回空列表。
     *
     * @param conversationId 对话的唯一标识符。
     * @param lastN          要获取的最近消息的数量。
     * @return 包含最近 N 条消息的列表，按时间顺序排列（旧 -> 新）。
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 获取完整的对话历史
        List<Message> allMessages = getOrCreateConversation(conversationId);
        if (allMessages.isEmpty()) {
            // 对话为空，返回空列表
            return allMessages;
        }
        // 计算需要跳过的消息数量
        int skipCount = Math.max(0, allMessages.size() - lastN);
        return allMessages.stream()
                // 跳过前面的消息
                .skip(skipCount)
                // 返回最后 N 条消息
                .toList();
    }

    /**
     * 清除指定对话 ID 的所有记忆（删除对应的数据库记录）。
     *
     * @param conversationId 对话的唯一标识符。
     */
    @Override
    public void clear(String conversationId) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<KeepReport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeepReport::getChat_id, getSafeConversationId(conversationId));

            // 删除对应的记录
            int deletedRows = KeepReportMapper.delete(queryWrapper);
            if (deletedRows > 0) {
                log.info("成功清除对话记录: {}", conversationId);
            } else {
                log.warn("尝试清除不存在的对话记录: {}", conversationId);
            }
        } catch (Exception e) {
            log.error("清除对话记录失败: {}", conversationId, e);
        }
    }

    /**
     * 获取或创建指定对话 ID 的消息列表。
     * 如果对应的记录存在，则从中反序列化消息列表；否则，返回一个新的空列表。
     *
     * @param conversationId 对话的唯一标识符。
     * @return 对话的消息列表。如果反序列化失败或记录不存在，则返回空列表。
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        String safeConversationId = getSafeConversationId(conversationId);
        try {
            // 构建查询条件
            LambdaQueryWrapper<KeepReport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeepReport::getChat_id, safeConversationId);

            // 查询数据库
            KeepReport loveReport = KeepReportMapper.selectOne(queryWrapper);

            if (loveReport != null && loveReport.getMessages() != null && !loveReport.getMessages().isBlank()) {
                // 从数据库记录中反序列化完整的消息列表
                try {
                    // 解析存储的简化消息格式
                    return deserializeMessages(loveReport.getMessages());
                } catch (Exception e) {
                    log.error("反序列化对话消息失败，对话ID: {}, 错误: {}", safeConversationId, e.getMessage(), e);
                    // 反序列化失败，可能数据已损坏，返回空列表
                    return new ArrayList<>();
                }
            } else {
                log.debug("未找到对话记录或消息为空，对话ID: {}", safeConversationId);
            }
        } catch (Exception e) {
            log.error("获取对话记录失败，对话ID: {}, 错误: {}", safeConversationId, e.getMessage(), e);
        }

        // 如果记录不存在或发生异常，返回新的空列表
        return new ArrayList<>();
    }

    /**
     * 将指定对话的完整消息列表序列化并保存到数据库。
     * 会覆盖之前的记录。
     *
     * @param conversationId 对话的唯一标识符。
     * @param messages       要保存的完整消息列表。
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        String safeConversationId = getSafeConversationId(conversationId);
        try {
            // 将消息列表转换为简化的JSON格式
            String messagesJson = serializeMessages(messages);

            // 构建查询条件
            LambdaQueryWrapper<KeepReport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeepReport::getChat_id, safeConversationId);

            // 查询数据库中是否已存在该对话的记录
            KeepReport existingReport = KeepReportMapper.selectOne(queryWrapper);

            if (existingReport != null) {
                // 更新现有记录的 messages 字段
                existingReport.setMessages(messagesJson);
                int updatedRows = KeepReportMapper.updateById(existingReport);
                if (updatedRows > 0) {
                    log.debug("成功更新对话记录: {}", safeConversationId);
                } else {
                    log.warn("更新对话记录失败（可能已被删除）: {}", safeConversationId);
                }
            } else {
                // 创建新记录
                KeepReport newReport = KeepReport.builder()
                        .chat_id(safeConversationId)
                        .createTime(new Date())
                        .updateTime(new Date())
                        // 存储序列化后的完整消息列表
                        .messages(messagesJson)
                        .build();
                int insertedRows = KeepReportMapper.insert(newReport);
                if (insertedRows > 0) {
                    log.debug("成功插入新对话记录: {}", safeConversationId);
                } else {
                    log.error("插入新对话记录失败: {}", safeConversationId);
                }
            }
        } catch (Exception e) {
            log.error("保存对话记录失败，对话ID: {}, 错误: {}", safeConversationId, e.getMessage(), e);
        }
    }

    /**
     * 处理对话 ID，确保其可以安全地用作数据库记录的聊天ID。
     * 将对话 ID 加上 "chat_" 前缀，并替换掉不安全的字符。
     *
     * @param conversationId 原始对话的唯一标识符。
     * @return 处理后的安全对话 ID，例如 "chat_your_conversation_id"。
     */
    private String getSafeConversationId(String conversationId) {
        // 确保 conversationId 不为 null 或空
        if (conversationId == null || conversationId.isBlank()) {
            log.warn("接收到空的 conversationId，将使用默认值 'default_chat'");
            conversationId = "default_chat";
        }
        // 替换掉不安全的字符，保留字母、数字、下划线和连字符
        String sanitizedId = conversationId.replaceAll("[^a-zA-Z0-9\\-_]", "_");
        // 添加前缀以避免潜在的冲突或保留字问题
        return "chat_" + sanitizedId;
    }

    /**
     * 将消息列表序列化为简化的JSON格式。
     * 格式为：[
     * {
     * "messageType": "USER",
     * "message": "用户消息内容"
     * },
     * {
     * "messageType": "ASSISTANT",
     * "message": { ... JSON对象 ... }
     * }
     * ]
     *
     * @param messages 要序列化的消息列表
     * @return 序列化后的JSON字符串
     */
    private String serializeMessages(List<Message> messages) {
        JSONArray jsonArray = new JSONArray();

        for (Message message : messages) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("messageType", message.getMessageType().toString());

            if (message instanceof UserMessage) {
                // 用户消息直接存储文本内容
                jsonObject.put("message", message.getText());
            } else if (message instanceof AssistantMessage) {
                // 助手消息可能包含JSON格式的内容，尝试解析
                String content = message.getText();
                try {
                    // 尝试将内容解析为JSON对象
                    JSONObject contentJson = JSON.parseObject(content);
                    jsonObject.put("message", contentJson);
                } catch (Exception e) {
                    // 如果解析失败，则直接存储文本内容
                    jsonObject.put("message", content);
                }
            } else {
                // 其他类型的消息直接存储文本内容
                jsonObject.put("message", message.getText());
            }

            jsonArray.add(jsonObject);
        }

        return jsonArray.toString();
    }

    /**
     * 将简化的JSON格式反序列化为消息列表。
     *
     * @param json 要反序列化的JSON字符串
     * @return 反序列化后的消息列表
     */
    private List<Message> deserializeMessages(String json) {
        List<Message> messages = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(json);

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String messageTypeStr = jsonObject.getString("messageType");
            MessageType messageType = MessageType.valueOf(messageTypeStr);

            if (messageType == MessageType.USER) {
                // 用户消息
                String content = jsonObject.getString("message");
                messages.add(new UserMessage(content));
            } else if (messageType == MessageType.ASSISTANT) {
                // 助手消息
                Object messageObj = jsonObject.get("message");
                String content;

                if (messageObj instanceof JSONObject) {
                    // 如果是JSON对象，转换为字符串
                    content = messageObj.toString();
                } else {
                    // 否则直接使用字符串值
                    content = jsonObject.getString("message");
                }

                // 创建助手消息，使用空的元数据
                Map<String, Object> metadata = new HashMap<>();
                messages.add(new AssistantMessage(content, metadata));
            } else {
                // 其他类型的消息（暂不支持）
                log.warn("不支持的消息类型: {}", messageType);
            }
        }
        return messages;
    }
}