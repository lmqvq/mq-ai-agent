package com.mq.mqaiagent.assessment.model.dto.scheme;

import lombok.Data;

import java.io.Serializable;

/**
 * Assessment scheme query request.
 */
@Data
public class AssessmentSchemeQueryRequest implements Serializable {

    private String schemeCode;

    private String sceneType;

    private Integer status;

    private static final long serialVersionUID = 1L;
}
