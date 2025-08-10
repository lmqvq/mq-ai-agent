package com.mq.mqaiagent.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * BMI计算请求
 */
@Data
public class BMICalculateRequest implements Serializable {

    /**
     * 体重(kg)
     */
    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "20.0", message = "体重不能小于20kg")
    @DecimalMax(value = "500.0", message = "体重不能大于500kg")
    private Float weight;

    /**
     * 身高(cm)
     */
    @NotNull(message = "身高不能为空")
    @DecimalMin(value = "50.0", message = "身高不能小于50cm")
    @DecimalMax(value = "300.0", message = "身高不能大于300cm")
    private Float height;

    private static final long serialVersionUID = 1L;
}
