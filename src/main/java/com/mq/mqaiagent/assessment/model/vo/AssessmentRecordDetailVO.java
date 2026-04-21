package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * Assessment record detail view.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssessmentRecordDetailVO extends AssessmentRecordVO implements Serializable {

    private List<AssessmentRecordItemVO> itemList;

    private static final long serialVersionUID = 1L;
}
