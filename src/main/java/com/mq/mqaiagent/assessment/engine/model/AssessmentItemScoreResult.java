package com.mq.mqaiagent.assessment.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Assessment item score result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentItemScoreResult {

    private String itemCode;

    private String itemName;

    private BigDecimal rawValue;

    private BigDecimal itemWeight;

    private BigDecimal itemScore;

    private BigDecimal extraScore;

    private BigDecimal weightedScore;

    private String itemLevel;

    private boolean matched;

    private boolean weakness;

    private boolean strength;

    private Long matchedRuleId;

    private String matchedRuleDescription;

    private String remark;
}
