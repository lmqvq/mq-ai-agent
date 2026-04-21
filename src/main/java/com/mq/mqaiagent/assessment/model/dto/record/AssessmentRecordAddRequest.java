package com.mq.mqaiagent.assessment.model.dto.record;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Assessment record add request.
 */
@Data
public class AssessmentRecordAddRequest implements Serializable {

    private String schemeCode;

    private Date assessmentDate;

    private String sourceType;

    private String gender;

    private String grade;

    private String gradeGroup;

    private BigDecimal height;

    private BigDecimal weight;

    private String summary;

    private String extraDataJson;

    @JsonAlias("items")
    private List<AssessmentRecordItemInput> itemList;

    private static final long serialVersionUID = 1L;
}
