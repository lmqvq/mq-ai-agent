package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Assessment trend view.
 */
@Data
public class AssessmentTrendVO implements Serializable {

    private Long recordId;

    private Date assessmentDate;

    private BigDecimal totalScore;

    private String level;

    private String summary;

    private static final long serialVersionUID = 1L;
}
