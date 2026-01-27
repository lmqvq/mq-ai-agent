package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 健身基础知识表
 * @TableName knowledge_basics
 */
@TableName(value ="knowledge_basics")
@Data
public class KnowledgeBasics {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 难度级别（初级/中级/高级）
     */
    private String difficulty;

    /**
     * 阅读时长（分钟）
     */
    private Integer readTime;

    /**
     * 浏览次数
     */
    private Integer views;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 提示数组（JSON格式）
     */
    private Object tips;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}