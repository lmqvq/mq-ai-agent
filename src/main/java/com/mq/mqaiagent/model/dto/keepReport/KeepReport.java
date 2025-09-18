package com.mq.mqaiagent.model.dto.keepReport;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身报告记录表
 * 
 * @TableName keep_report
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "keep_report")
public class KeepReport implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对话id
     */
    @TableField(value = "chatId")
    private String chatId;

    /**
     * 创建用户id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 对话记录（JSON格式存储）
     */
    @TableField(value = "messages")
    private String messages;

    /**
     * 最后一条消息内容（用于列表展示）
     */
    @TableField(value = "lastMessage")
    private String lastMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}