package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练计划视图对象
 */
@Data
public class KnowledgeProgramVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 计划名称
     */
    private String name;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 类型（beginner/intermediate/advanced/fat-loss等）
     */
    private String type;

    /**
     * 图标标识
     */
    private String icon;

    /**
     * 持续时间
     */
    private String duration;

    /**
     * 强度级别
     */
    private String intensity;

    /**
     * 适合级别（初级/中级/高级）
     */
    private String level;

    /**
     * 训练安排数组（JSON格式）
     */
    private Object schedule;

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

