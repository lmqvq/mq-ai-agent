package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 运动记录实体类
 * @TableName exercise_log
 */
@TableName(value = "exercise_log")
@Data
public class ExerciseLog implements Serializable {
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
     * 运动类型（如：力量训练、有氧、瑜伽等）
     */
    private String exerciseType;

    /**
     * 运动时长（分钟）
     */
    private Integer duration;

    /**
     * 消耗卡路里
     */
    private Float caloriesBurned;

    /**
     * 运动记录时间
     */
    private Date dateRecorded;

    /**
     * 运动备注（如：训练动作、强度感受等）
     */
    private String notes;

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
