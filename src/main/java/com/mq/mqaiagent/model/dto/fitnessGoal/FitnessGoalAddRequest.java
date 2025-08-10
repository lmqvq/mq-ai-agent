package com.mq.mqaiagent.model.dto.fitnessGoal;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身目标添加请求
 */
@Data
public class FitnessGoalAddRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}
