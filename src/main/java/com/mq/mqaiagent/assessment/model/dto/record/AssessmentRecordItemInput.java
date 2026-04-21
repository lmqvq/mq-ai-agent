package com.mq.mqaiagent.assessment.model.dto.record;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Assessment record item input.
 */
@Data
public class AssessmentRecordItemInput implements Serializable {

    private String itemCode;

    private BigDecimal rawValue;

    private String remark;

    private static final long serialVersionUID = 1L;
}
