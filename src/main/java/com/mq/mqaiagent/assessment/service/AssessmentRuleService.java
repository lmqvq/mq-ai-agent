package com.mq.mqaiagent.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRule;

import java.util.List;

/**
 * Assessment rule service.
 */
public interface AssessmentRuleService extends IService<AssessmentRule> {

    List<AssessmentRule> listApplicableRules(String schemeCode, String itemCode, String gender, String gradeGroup,
            String ruleVersion);
}
