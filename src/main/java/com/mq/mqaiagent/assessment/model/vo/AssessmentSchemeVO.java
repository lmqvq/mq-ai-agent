package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Assessment scheme view.
 */
@Data
public class AssessmentSchemeVO implements Serializable {

    private Long id;

    private String schemeCode;

    private String schemeName;

    private String sceneType;

    private String description;

    private String version;

    private String source;

    private String configJson;

    private Integer status;

    private Date createTime;

    private List<AssessmentSchemeItemVO> itemList;

    private static final long serialVersionUID = 1L;
}
