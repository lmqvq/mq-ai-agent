package com.mq.mqaiagent.assessment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Structured AI suggestion view.
 */
@Data
public class AssessmentAiSuggestionStructuredVO implements Serializable {

    private Boolean structured;

    private Integer sectionCount;

    private String overallJudgment;

    private String trainingAdvice;

    private String weeklyPlan;

    private String recoveryAndNutrition;

    private String precautions;

    private String nextAssessmentGoal;

    private List<AssessmentAiSuggestionSectionVO> sectionList;

    private static final long serialVersionUID = 1L;
}
