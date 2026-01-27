package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 营养知识视图对象
 */
@Data
public class KnowledgeNutrientVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 营养素名称
     */
    private String name;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 显示颜色（用于前端展示）
     */
    private String color;

    /**
     * 图标标识
     */
    private String icon;

    /**
     * 益处数组（JSON格式）
     */
    private Object benefits;

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

