package com.mq.mqaiagent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 聊天历史列表DTO
 * 
 * @author MQQQ
 * @create 2025-08-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryListDTO {

    /**
     * 对话ID
     */
    private String chatId;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 对话创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;
}
