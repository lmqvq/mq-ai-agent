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
 * 健身动作指导表
 * @TableName knowledge_exercise
 */
@TableName(value ="knowledge_exercise", autoResultMap = true)
@Data
public class KnowledgeExercise {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> instructions;

    /**
     * 提示数组（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tips;

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