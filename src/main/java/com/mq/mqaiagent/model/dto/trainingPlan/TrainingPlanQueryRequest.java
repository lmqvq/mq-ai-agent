package com.mq.mqaiagent.model.dto.trainingPlan;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 训练计划查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrainingPlanQueryRequest extends PageRequest implements Serializable {

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
     * 是否为默认训练计划（0：否，1：是）
     */
    private Integer isDefault;

    private static final long serialVersionUID = 1L;
}
