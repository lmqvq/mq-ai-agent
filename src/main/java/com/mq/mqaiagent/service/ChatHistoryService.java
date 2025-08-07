package com.mq.mqaiagent.service;

import com.mq.mqaiagent.model.dto.ChatHistoryDetailDTO;
import com.mq.mqaiagent.model.dto.ChatHistoryListDTO;

import java.util.List;

/**
 * 聊天历史服务接口
 * 
 * @author MQQQ
 * @create 2025-08-07
 */
public interface ChatHistoryService {

    /**
     * 获取用户的历史对话列表
     *
     * @param userId 用户ID
     * @return 历史对话列表
     */
    List<ChatHistoryListDTO> getChatHistoryList(Long userId);

    /**
     * 获取单个对话的完整消息
     *
     * @param userId 用户ID
     * @param chatId 对话ID
     * @return 对话详情
     */
    ChatHistoryDetailDTO getChatHistoryDetail(Long userId, String chatId);

    /**
     * 删除用户的某个对话
     *
     * @param userId 用户ID
     * @param chatId 对话ID
     * @return 是否删除成功
     */
    boolean deleteChatHistory(Long userId, String chatId);
}
