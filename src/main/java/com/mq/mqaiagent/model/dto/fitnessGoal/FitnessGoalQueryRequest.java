package com.mq.mqaiagent.model.dto.fitnessGoal;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 健身目标查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FitnessGoalQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 目标类型（增肌、减脂、体态改善等）
     */
    private String goalType;

    /**
     * 是否达成（0-未达成，1-已达成）
     */
    private Integer isAchieved;

    private static final long serialVersionUID = 1L;
}
