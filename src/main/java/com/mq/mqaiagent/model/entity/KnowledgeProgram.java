package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 训练计划表
 * @TableName knowledge_program
 */
@TableName(value ="knowledge_program")
@Data
public class KnowledgeProgram {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
}