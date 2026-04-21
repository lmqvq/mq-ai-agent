package com.mq.mqaiagent.assessment.model.dto.profile;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Assessment profile save request.
 */
@Data
public class AssessmentProfileSaveRequest implements Serializable {

    private String schemeCode;

    private String gender;

    private String grade;

    private String gradeGroup;

    private BigDecimal height;

    private BigDecimal weight;

    private String extraProfileJson;

    private static final long serialVersionUID = 1L;
}
