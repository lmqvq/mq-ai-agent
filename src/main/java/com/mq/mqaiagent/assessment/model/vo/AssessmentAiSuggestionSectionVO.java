package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Structured AI suggestion section view.
 */
@Data
public class AssessmentAiSuggestionSectionVO implements Serializable {

    private String key;

    private String title;

    private String content;

    private Integer order;

    private static final long serialVersionUID = 1L;
}
