package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身动作指导视图对象
 */
@Data
public class KnowledgeExerciseVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 动作名称
     */
    private String name;

    /**
     * 分类（胸部训练/背部训练/腿部训练等）
     */
    private String category;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 目标肌群
     */
    private String muscleGroup;

    /**
     * 难度级别（初级/中级/高级）
     */
    private String difficulty;

    /**
     * 动作步骤数组（JSON格式）
     */
    private Object instructions;

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

