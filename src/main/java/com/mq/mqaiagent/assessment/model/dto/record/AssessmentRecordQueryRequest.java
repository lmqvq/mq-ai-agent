package com.mq.mqaiagent.assessment.model.dto.record;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Assessment record query request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentRecordQueryRequest extends PageRequest implements Serializable {

    private String schemeCode;

    private String sourceType;

    private String level;

    private Date startDate;

    private Date endDate;

    private static final long serialVersionUID = 1L;
}
