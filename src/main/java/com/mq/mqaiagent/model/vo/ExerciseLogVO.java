package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 运动记录视图对象
 */
@Data
public class ExerciseLogVO implements Serializable {

    /**
     * id
     */
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

    private static final long serialVersionUID = 1L;
}
