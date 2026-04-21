package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Assessment record item view.
 */
@Data
public class AssessmentRecordItemVO implements Serializable {

    private Long id;

    private Long recordId;

    private String itemCode;

    private String itemName;

    private Integer itemOrder;

    private String unit;

    private BigDecimal rawValue;

    private BigDecimal itemWeight;

    private BigDecimal itemScore;

    private BigDecimal extraScore;

    private String itemLevel;

    private Integer isWeakness;

    private Integer isStrength;

    private String remark;

    private static final long serialVersionUID = 1L;
}
