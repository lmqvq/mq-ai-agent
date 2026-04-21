package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Assessment report view.
 */
@Data
public class AssessmentReportVO implements Serializable {

    private Long id;

    private Long recordId;

    private String version;

    private String overview;

    private String analysisJson;

    private String weaknessSummary;

    private String strengthSummary;

    private String trainingFocus;

    private String riskNotes;

    private String aiSuggestion;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
