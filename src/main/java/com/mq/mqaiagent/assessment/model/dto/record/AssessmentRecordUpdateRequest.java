package com.mq.mqaiagent.assessment.model.dto.record;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Assessment record update request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentRecordUpdateRequest extends AssessmentRecordAddRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;
}
