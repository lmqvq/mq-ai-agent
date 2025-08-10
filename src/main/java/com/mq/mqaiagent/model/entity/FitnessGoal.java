package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身目标实体类
 * @TableName fitness_goal
 */
@TableName(value = "fitness_goal")
@Data
public class FitnessGoal implements Serializable {
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
     * 目标类型（增肌、减脂、体态改善等）
     */
    private String goalType;

    /**
     * 目标值（如：体脂率降到15%、体重增到70kg）
     */
    private String targetValue;

    /**
     * 目标开始时间
     */
    private Date startDate;

    /**
     * 目标结束时间
     */
    private Date endDate;

    /**
     * 进度记录（JSON格式）
     */
    private String progress;

    /**
     * 是否达成（0-未达成，1-已达成）
     */
    private Integer isAchieved;

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
