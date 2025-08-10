package com.mq.mqaiagent.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 训练计划更新请求
 */
@Data
public class TrainingPlanUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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

    private static final long serialVersionUID = 1L;
}
