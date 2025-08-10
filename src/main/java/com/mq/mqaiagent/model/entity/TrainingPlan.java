package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练计划实体类
 * @TableName training_plan
 */
@TableName(value = "training_plan")
@Data
public class TrainingPlan implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 计划类型（增肌、减脂、塑形等）
     */
    private String planType;

    /**
     * 训练计划详细信息（JSON格式存储）
     */
    private String planDetails;

    /**
     * 是否为默认训练计划（0：否，1：是）
     */
    private Integer isDefault;

    /**
     * 计划开始时间
     */
    private Date startDate;

    /**
     * 计划结束时间
     */
    private Date endDate;

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
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
