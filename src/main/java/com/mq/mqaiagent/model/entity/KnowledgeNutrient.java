package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 营养知识表
 * @TableName knowledge_nutrient
 */
@TableName(value ="knowledge_nutrient", autoResultMap = true)
@Data
public class KnowledgeNutrient {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> benefits;

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