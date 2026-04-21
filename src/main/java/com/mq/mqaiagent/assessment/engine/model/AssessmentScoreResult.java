package com.mq.mqaiagent.assessment.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assessment score result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentScoreResult {

    private BigDecimal totalScore;

    private String level;

    private Integer weaknessCount;

    private Integer strengthCount;

    private Integer scoredItemCount;

    private Integer unmatchedItemCount;

    private String summary;

    private List<AssessmentItemScoreResult> itemScoreResults;
}
