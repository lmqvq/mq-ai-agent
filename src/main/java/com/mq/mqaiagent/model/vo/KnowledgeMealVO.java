package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 饮食计划视图对象
 */
@Data
public class KnowledgeMealVO implements Serializable {

    private Long id;

    /**
     * 餐食名称
     */
    private String name;

    /**
     * 餐食类型
     */
    private String mealType;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 卡路里(kcal)
     */
    private Integer calories;

    /**
     * 蛋白质(g)
     */
    private Integer protein;

    /**
     * 碳水化合物(g)
     */
    private Integer carbs;

    /**
     * 脂肪(g)
     */
    private Integer fat;

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
