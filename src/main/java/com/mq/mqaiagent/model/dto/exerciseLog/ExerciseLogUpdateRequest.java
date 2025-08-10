package com.mq.mqaiagent.model.dto.exerciseLog;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 运动记录更新请求
 */
@Data
public class ExerciseLogUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}
