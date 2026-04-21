package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Assessment scheme item view.
 */
@Data
public class AssessmentSchemeItemVO implements Serializable {

    private Long id;

    private String schemeCode;

    private String itemCode;

    private String itemName;

    private String itemCategory;

    private String unit;

    private String inputType;

    private Integer inputPrecision;

    private BigDecimal weight;

    private Integer displayOrder;

    private String applicableGender;

    private String applicableGradeGroup;

    private BigDecimal validationMin;

    private BigDecimal validationMax;

    private Integer isRequired;

    private Integer isBonusItem;

    private String description;

    private static final long serialVersionUID = 1L;
}
