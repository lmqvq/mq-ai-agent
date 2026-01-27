package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身基础知识视图对象
 */
@Data
public class KnowledgeBasicsVO implements Serializable {

    /**
     * id
     */
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

    private static final long serialVersionUID = 1L;
}

