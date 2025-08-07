package com.mq.mqaiagent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天历史详情DTO
 * 
 * @author MQQQ
 * @create 2025-08-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryDetailDTO {

    /**
     * 对话ID
     */
    private String chatId;

    /**
     * 消息列表
     */
    private List<ChatMessageDTO> messages;

    /**
     * 单条消息DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMessageDTO {
        
        /**
         * 消息类型：USER/ASSISTANT
         */
        private String messageType;
        
        /**
         * 消息内容
         */
        private String message;
    }
}
