package com.mq.mqaiagent.assessment.engine;

import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.engine.model.AssessmentItemScoreResult;
import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreContext;
import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreResult;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRule;
import com.mq.mqaiagent.assessment.service.AssessmentRuleService;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rule-based assessment scoring engine.
 */
@Component
public class RuleBasedAssessmentScoringEngine implements AssessmentScoringEngine {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final BigDecimal WEAKNESS_THRESHOLD = BigDecimal.valueOf(60);

    private static final BigDecimal STRENGTH_THRESHOLD = BigDecimal.valueOf(80);

    @Resource
    private AssessmentRuleService assessmentRuleService;

    @Override
    public AssessmentScoreResult score(AssessmentScoreContext context) {
        ThrowUtils.throwIf(context == null || StringUtils.isBlank(context.getSchemeCode()), ErrorCode.PARAMS_ERROR);
        List<AssessmentScoreContext.ItemContext> itemContextList = context.getItemList();
        if (itemContextList == null || itemContextList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "assessment item list is empty");
        }

        List<AssessmentItemScoreResult> itemScoreResultList = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        int weaknessCount = 0;
        int strengthCount = 0;
        int scoredItemCount = 0;
        int unmatchedItemCount = 0;

        for (AssessmentScoreContext.ItemContext itemContext : itemContextList) {
            AssessmentItemScoreResult itemScoreResult = scoreItem(context, itemContext);
            itemScoreResultList.add(itemScoreResult);
            totalScore = totalScore.add(itemScoreResult.getWeightedScore());
            if (itemScoreResult.isMatched()) {
                scoredItemCount++;
                if (itemScoreResult.isWeakness()) {
                    weaknessCount++;
                }
                if (itemScoreResult.isStrength()) {
                    strengthCount++;
                }
            } else {
                unmatchedItemCount++;
            }
        }

        totalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
        String level = scoredItemCount > 0 ? resolveLevel(totalScore) : AssessmentConstant.LEVEL_UNSCORED;
        String summary = buildSummary(totalScore, level, weaknessCount, strengthCount, scoredItemCount,
                unmatchedItemCount, itemContextList.size());

        return AssessmentScoreResult.builder()
                .totalScore(totalScore)
                .level(level)
                .weaknessCount(weaknessCount)
                .strengthCount(strengthCount)
                .scoredItemCount(scoredItemCount)
                .unmatchedItemCount(unmatchedItemCount)
                .summary(summary)
                .itemScoreResults(itemScoreResultList)
                .build();
    }

    private AssessmentItemScoreResult scoreItem(AssessmentScoreContext context,
            AssessmentScoreContext.ItemContext itemContext) {
        List<AssessmentRule> ruleList = assessmentRuleService.listApplicableRules(context.getSchemeCode(),
                itemContext.getItemCode(), context.getGender(), context.getGradeGroup(), context.getRuleVersion());
        AssessmentRule matchedRule = findMatchedRule(ruleList, itemContext.getRawValue());
        if (matchedRule == null) {
            return AssessmentItemScoreResult.builder()
                    .itemCode(itemContext.getItemCode())
                    .itemName(itemContext.getItemName())
                    .rawValue(itemContext.getRawValue())
                    .itemWeight(defaultWeight(itemContext.getItemWeight()))
                    .itemScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .extraScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .weightedScore(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .itemLevel(AssessmentConstant.LEVEL_UNSCORED)
                    .matched(false)
                    .weakness(false)
                    .strength(false)
                    .remark("未匹配到评分规则")
                    .build();
        }

        BigDecimal rawScore = defaultScore(matchedRule.getScore());
        BigDecimal baseScore = rawScore.min(HUNDRED).setScale(2, RoundingMode.HALF_UP);
        BigDecimal extraScore = rawScore.compareTo(HUNDRED) > 0
                ? rawScore.subtract(HUNDRED).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal weightedScore = baseScore.multiply(defaultWeight(itemContext.getItemWeight()))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP)
                .add(extraScore)
                .setScale(2, RoundingMode.HALF_UP);
        boolean weakness = baseScore.compareTo(WEAKNESS_THRESHOLD) < 0;
        boolean strength = baseScore.compareTo(STRENGTH_THRESHOLD) >= 0;

        return AssessmentItemScoreResult.builder()
                .itemCode(itemContext.getItemCode())
                .itemName(itemContext.getItemName())
                .rawValue(itemContext.getRawValue())
                .itemWeight(defaultWeight(itemContext.getItemWeight()))
                .itemScore(baseScore)
                .extraScore(extraScore)
                .weightedScore(weightedScore)
                .itemLevel(resolveLevel(baseScore))
                .matched(true)
                .weakness(weakness)
                .strength(strength)
                .matchedRuleId(matchedRule.getId())
                .matchedRuleDescription(matchedRule.getDescription())
                .remark(StringUtils.defaultIfBlank(matchedRule.getDescription(), "命中评分规则"))
                .build();
    }

    private AssessmentRule findMatchedRule(List<AssessmentRule> ruleList, BigDecimal rawValue) {
        if (ruleList == null || ruleList.isEmpty() || rawValue == null) {
            return null;
        }
        for (AssessmentRule rule : ruleList) {
            if (isRuleMatched(rule, rawValue)) {
                return rule;
            }
        }
        return null;
    }

    private boolean isRuleMatched(AssessmentRule rule, BigDecimal rawValue) {
        if (rule == null || rawValue == null) {
            return false;
        }
        String comparisonType = StringUtils.defaultIfBlank(rule.getComparisonType(), AssessmentConstant.COMPARISON_RANGE)
                .toUpperCase();
        BigDecimal minValue = rule.getMinValue();
        BigDecimal maxValue = rule.getMaxValue();
        return switch (comparisonType) {
            case AssessmentConstant.COMPARISON_EXACT -> isExactMatched(rawValue, minValue, maxValue);
            case AssessmentConstant.COMPARISON_ASC, AssessmentConstant.COMPARISON_RANGE -> isRangeMatched(rawValue,
                    minValue, maxValue);
            case AssessmentConstant.COMPARISON_DESC -> isDescMatched(rawValue, minValue, maxValue);
            case AssessmentConstant.COMPARISON_GREATER_THAN -> minValue != null && rawValue.compareTo(minValue) > 0;
            case AssessmentConstant.COMPARISON_GREATER_THAN_OR_EQUAL ->
                    minValue != null && rawValue.compareTo(minValue) >= 0;
            case AssessmentConstant.COMPARISON_LESS_THAN -> maxValue != null && rawValue.compareTo(maxValue) < 0;
            case AssessmentConstant.COMPARISON_LESS_THAN_OR_EQUAL ->
                    maxValue != null && rawValue.compareTo(maxValue) <= 0;
            default -> isRangeMatched(rawValue, minValue, maxValue);
        };
    }

    private boolean isExactMatched(BigDecimal rawValue, BigDecimal minValue, BigDecimal maxValue) {
        if (minValue != null) {
            return rawValue.compareTo(minValue) == 0;
        }
        return maxValue != null && rawValue.compareTo(maxValue) == 0;
    }

    private boolean isRangeMatched(BigDecimal rawValue, BigDecimal minValue, BigDecimal maxValue) {
        boolean geMin = minValue == null || rawValue.compareTo(minValue) >= 0;
        boolean leMax = maxValue == null || rawValue.compareTo(maxValue) <= 0;
        return geMin && leMax;
    }

    private boolean isDescMatched(BigDecimal rawValue, BigDecimal minValue, BigDecimal maxValue) {
        if (minValue != null && maxValue != null) {
            return isRangeMatched(rawValue, minValue, maxValue);
        }
        if (maxValue != null) {
            return rawValue.compareTo(maxValue) <= 0;
        }
        return minValue != null && rawValue.compareTo(minValue) >= 0;
    }

    private BigDecimal defaultWeight(BigDecimal itemWeight) {
        return itemWeight == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : itemWeight;
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : score;
    }

    private String resolveLevel(BigDecimal score) {
        if (score == null) {
            return AssessmentConstant.LEVEL_UNSCORED;
        }
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return AssessmentConstant.LEVEL_EXCELLENT;
        }
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return AssessmentConstant.LEVEL_GOOD;
        }
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return AssessmentConstant.LEVEL_PASS;
        }
        return AssessmentConstant.LEVEL_FAIL;
    }

    private String buildSummary(BigDecimal totalScore, String level, int weaknessCount, int strengthCount,
            int scoredItemCount, int unmatchedItemCount, int totalItemCount) {
        if (scoredItemCount <= 0) {
            return "当前记录已保存，但尚未匹配到评分规则，请先导入 assessment_rule 数据。";
        }
        List<String> messageList = new ArrayList<>();
        messageList.add(String.format("共 %d 项，已评分 %d 项", totalItemCount, scoredItemCount));
        messageList.add(String.format("总分 %s", totalScore.setScale(2, RoundingMode.HALF_UP)));
        messageList.add(String.format("等级 %s", level));
        messageList.add(String.format("强项 %d 项", strengthCount));
        messageList.add(String.format("弱项 %d 项", weaknessCount));
        if (unmatchedItemCount > 0) {
            messageList.add(String.format("另有 %d 项未匹配评分规则", unmatchedItemCount));
        }
        return String.join("，", messageList);
    }
}
