package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Assessment profile view.
 */
@Data
public class AssessmentProfileVO implements Serializable {

    private Long id;

    private Long userId;

    private String schemeCode;

    private String gender;

    private String grade;

    private String gradeGroup;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bmi;

    private String extraProfileJson;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
