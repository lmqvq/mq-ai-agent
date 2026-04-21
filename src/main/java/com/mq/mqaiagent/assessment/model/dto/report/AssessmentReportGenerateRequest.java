package com.mq.mqaiagent.assessment.model.dto.report;

import lombok.Data;

import java.io.Serializable;

/**
 * Assessment report generation request.
 */
@Data
public class AssessmentReportGenerateRequest implements Serializable {

    private Long recordId;

    private Boolean regenerate;

    private static final long serialVersionUID = 1L;
}
