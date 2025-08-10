package com.mq.mqaiagent.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * BMI计算结果视图对象
 */
@Data
public class BMICalculateVO implements Serializable {

    /**
     * BMI值
     */
    private Float bmi;

    /**
     * BMI分类描述
     */
    private String category;

    /**
     * 理想体重范围最小值(kg)
     */
    private Float idealWeightMin;

    /**
     * 理想体重范围最大值(kg)
     */
    private Float idealWeightMax;

    /**
     * 健康建议
     */
    private String healthAdvice;

    /**
     * 输入的体重(kg)
     */
    private Float inputWeight;

    /**
     * 输入的身高(cm)
     */
    private Float inputHeight;

    private static final long serialVersionUID = 1L;
}
