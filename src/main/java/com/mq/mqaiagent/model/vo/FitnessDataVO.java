package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身数据视图对象
 */
@Data
public class FitnessDataVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 体重(kg)
     */
    private Float weight;

    /**
     * 体脂率(%)
     */
    private Float bodyFat;

    /**
     * 身高(cm)
     */
    private Float height;

    /**
     * BMI指数
     */
    private Float bmi;

    /**
     * 数据记录时间
     */
    private Date dateRecorded;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
