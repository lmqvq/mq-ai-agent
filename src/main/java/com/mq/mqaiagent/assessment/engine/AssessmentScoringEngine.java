package com.mq.mqaiagent.assessment.engine;

import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreContext;
import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreResult;

/**
 * Assessment scoring engine.
 */
public interface AssessmentScoringEngine {

    AssessmentScoreResult score(AssessmentScoreContext context);
}
