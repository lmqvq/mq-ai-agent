package com.mq.mqaiagent.assessment.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Assessment record view.
 */
@Data
public class AssessmentRecordVO implements Serializable {

    private Long id;

    private Long userId;

    private String schemeCode;

    private String schemeVersion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private Date assessmentDate;

    private String sourceType;

    private String genderSnapshot;

    private String gradeSnapshot;

    private String gradeGroupSnapshot;

    private BigDecimal heightSnapshot;

    private BigDecimal weightSnapshot;

    private BigDecimal bmiSnapshot;

    private BigDecimal totalScore;

    private String level;

    private Integer weaknessCount;

    private Integer strengthCount;

    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
