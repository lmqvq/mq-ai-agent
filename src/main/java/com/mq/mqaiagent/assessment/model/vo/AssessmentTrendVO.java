package com.mq.mqaiagent.assessment.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private Date assessmentDate;

    private BigDecimal totalScore;

    private String level;

    private String summary;

    private static final long serialVersionUID = 1L;
}
