package com.mq.mqaiagent.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.mapper.AssessmentRuleMapper;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRule;
import com.mq.mqaiagent.assessment.service.AssessmentRuleService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Assessment rule service implementation.
 */
@Service
public class AssessmentRuleServiceImpl extends ServiceImpl<AssessmentRuleMapper, AssessmentRule>
        implements AssessmentRuleService {

    @Override
    public List<AssessmentRule> listApplicableRules(String schemeCode, String itemCode, String gender, String gradeGroup,
            String ruleVersion) {
        QueryWrapper<AssessmentRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("schemeCode", schemeCode)
                .eq("itemCode", itemCode)
                .eq("ruleVersion", StringUtils.defaultIfBlank(ruleVersion, AssessmentConstant.DEFAULT_RULE_VERSION))
                .in("gender", buildDimensionValues(gender))
                .in("gradeGroup", buildDimensionValues(gradeGroup));

        List<AssessmentRule> ruleList = this.list(queryWrapper);
        ruleList.sort(Comparator.comparingInt(this::getRuleSpecificity).reversed()
                .thenComparing(rule -> ObjectUtils.defaultIfNull(rule.getSortOrder(), 0))
                .thenComparing(rule -> ObjectUtils.defaultIfNull(rule.getId(), 0L)));
        return ruleList;
    }

    private List<String> buildDimensionValues(String actualValue) {
        List<String> values = new ArrayList<>();
        if (StringUtils.isNotBlank(actualValue)) {
            values.add(actualValue);
        }
        values.add("all");
        return values;
    }

    private int getRuleSpecificity(AssessmentRule rule) {
        int score = 0;
        if (!StringUtils.equalsIgnoreCase("all", rule.getGender())) {
            score += 2;
        }
        if (!StringUtils.equalsIgnoreCase("all", rule.getGradeGroup())) {
            score += 1;
        }
        return score;
    }
}
