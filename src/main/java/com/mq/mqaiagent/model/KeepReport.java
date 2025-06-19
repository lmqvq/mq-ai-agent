package com.mq.mqaiagent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身报告记录表
 * @TableName keep_report
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value ="keep_report")
public class KeepReport implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对话id
     */
    @TableField(value = "chat_id")
    private String chat_id;

    /**
     * 对话记录（JSON格式存储）
     */
    @TableField(value = "messages")
    private String messages;

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
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}