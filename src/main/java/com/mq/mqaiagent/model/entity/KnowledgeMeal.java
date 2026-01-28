package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 饮食计划表
 * @TableName knowledge_meal
 */
@TableName(value = "knowledge_meal")
@Data
public class KnowledgeMeal {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 餐食名称
     */
    private String name;

    /**
     * 餐食类型（早餐/午餐/晚餐/加餐/训练前/训练后）
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

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}
