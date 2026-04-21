package com.mq.mqaiagent.assessment.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.app.KeepApp;
import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.mapper.AssessmentRecordItemMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentRecordMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentReportMapper;
import com.mq.mqaiagent.assessment.model.dto.report.AssessmentReportGenerateRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecord;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecordItem;
import com.mq.mqaiagent.assessment.model.entity.AssessmentReport;
import com.mq.mqaiagent.assessment.model.vo.AssessmentReportVO;
import com.mq.mqaiagent.assessment.service.AssessmentReportService;
import com.mq.mqaiagent.assessment.util.AssessmentAiSuggestionParser;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Assessment report service implementation.
 */
@Service
@Slf4j
public class AssessmentReportServiceImpl extends ServiceImpl<AssessmentReportMapper, AssessmentReport>
        implements AssessmentReportService {

    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("Asia/Shanghai"));

    private static final String TEXT_UNKNOWN = "未知";
    private static final String TEXT_YES = "是";
    private static final String TEXT_NO = "否";
    private static final String TEXT_NO_WEAKNESS = "当前暂无明显弱项。";
    private static final String TEXT_NO_STRENGTH = "当前暂无明显强项，建议继续保持全面训练。";
    private static final String TEXT_WEAKNESS_PREFIX = "弱项项目：";
    private static final String TEXT_STRENGTH_PREFIX = "强项项目：";
    private static final String TEXT_IMPROVE_PREFIX = "优先提升项目：";
    private static final String TEXT_TRAINING_FOCUS_SUFFIX = "。建议围绕这些项目安排每周 2 到 3 次专项训练。";
    private static final String TEXT_BALANCED_TRAINING =
            "当前各项目表现较均衡，建议围绕优势项目继续巩固，并通过常规有氧和力量训练保持整体水平。";
    private static final String TEXT_RULE_MISSING =
            "当前记录尚未完成有效评分，请先确认 assessment_rule 是否已经正确导入。";
    private static final String TEXT_FAIL_RISK =
            "当前总分处于不及格区间，建议优先补齐弱项，并注意循序渐进，避免突然增加训练量。";
    private static final String TEXT_HAS_WEAKNESS_RISK =
            "虽然总分已经完成计算，但仍存在弱项，建议把训练重点放在短板补齐上。";
    private static final String TEXT_STABLE_RISK =
            "当前成绩整体稳定，训练时仍需注意热身、恢复和训练负荷管理。";
    private static final String TEXT_RULE_BASED_ADVICE_UNSCORED =
            "建议先补充评分规则数据，再结合 KeepApp 生成个性化训练建议。";
    private static final String TEXT_RULE_BASED_ADVICE_BALANCED =
            "可以结合 KeepApp 继续生成维持型训练计划，重点保持当前优势并提升整体稳定性。";
    private static final String TEXT_RULE_BASED_ADVICE_PREFIX = "建议下一步结合 KeepApp，围绕";
    private static final String TEXT_RULE_BASED_ADVICE_SUFFIX =
            "生成专项训练计划，并持续跟踪下一次体测成绩变化。";

    @Resource
    private AssessmentRecordMapper assessmentRecordMapper;

    @Resource
    private AssessmentRecordItemMapper assessmentRecordItemMapper;

    @Resource
    private KeepApp keepApp;

    @Override
    public AssessmentReportVO getUserReport(Long userId, Long recordId) {
        AssessmentRecord assessmentRecord = getOwnedRecord(userId, recordId);
        QueryWrapper<AssessmentReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recordId", assessmentRecord.getId()).last("limit 1");
        return getReportVO(this.getOne(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentReportVO generateUserReport(Long userId, AssessmentReportGenerateRequest generateRequest) {
        ThrowUtils.throwIf(generateRequest == null || generateRequest.getRecordId() == null, ErrorCode.PARAMS_ERROR);
        AssessmentRecord assessmentRecord = getOwnedRecord(userId, generateRequest.getRecordId());
        List<AssessmentRecordItem> recordItemList = listRecordItems(assessmentRecord.getId());

        QueryWrapper<AssessmentReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recordId", assessmentRecord.getId()).last("limit 1");
        AssessmentReport assessmentReport = this.getOne(queryWrapper);
        if (assessmentReport != null && !Boolean.TRUE.equals(generateRequest.getRegenerate())) {
            return getReportVO(assessmentReport);
        }
        if (assessmentReport == null) {
            assessmentReport = new AssessmentReport();
            assessmentReport.setRecordId(assessmentRecord.getId());
        }

        assessmentReport.setVersion(AssessmentConstant.REPORT_VERSION);
        assessmentReport.setOverview(assessmentRecord.getSummary());
        assessmentReport.setAnalysisJson(buildAnalysisJson(assessmentRecord, recordItemList));
        assessmentReport.setWeaknessSummary(buildWeaknessSummary(recordItemList));
        assessmentReport.setStrengthSummary(buildStrengthSummary(recordItemList));
        assessmentReport.setTrainingFocus(buildTrainingFocus(recordItemList));
        assessmentReport.setRiskNotes(buildRiskNotes(assessmentRecord));
        assessmentReport.setAiSuggestion(buildAiSuggestion(userId, assessmentRecord, recordItemList,
                generateRequest.getModel(), Boolean.TRUE.equals(generateRequest.getRegenerate())));

        boolean result = this.saveOrUpdate(assessmentReport);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return getReportVO(assessmentReport);
    }

    @Override
    public AssessmentReportVO getReportVO(AssessmentReport assessmentReport) {
        if (assessmentReport == null) {
            return null;
        }
        AssessmentReportVO reportVO = new AssessmentReportVO();
        BeanUtils.copyProperties(assessmentReport, reportVO);
        reportVO.setAiSuggestionStructured(AssessmentAiSuggestionParser.parse(assessmentReport.getAiSuggestion()));
        return reportVO;
    }

    private AssessmentRecord getOwnedRecord(Long userId, Long recordId) {
        ThrowUtils.throwIf(userId == null || recordId == null || recordId <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentRecord assessmentRecord = assessmentRecordMapper.selectById(recordId);
        ThrowUtils.throwIf(assessmentRecord == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!userId.equals(assessmentRecord.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return assessmentRecord;
    }

    private List<AssessmentRecordItem> listRecordItems(Long recordId) {
        QueryWrapper<AssessmentRecordItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recordId", recordId).orderByAsc("itemOrder");
        return assessmentRecordItemMapper.selectList(queryWrapper);
    }

    private String buildAnalysisJson(AssessmentRecord assessmentRecord, List<AssessmentRecordItem> recordItemList) {
        JSONObject analysisJson = new JSONObject();
        analysisJson.put("recordId", assessmentRecord.getId());
        analysisJson.put("schemeCode", assessmentRecord.getSchemeCode());
        analysisJson.put("totalScore", assessmentRecord.getTotalScore());
        analysisJson.put("level", assessmentRecord.getLevel());
        analysisJson.put("weaknessCount", assessmentRecord.getWeaknessCount());
        analysisJson.put("strengthCount", assessmentRecord.getStrengthCount());

        JSONArray itemArray = new JSONArray();
        for (AssessmentRecordItem recordItem : recordItemList) {
            JSONObject itemJson = new JSONObject();
            itemJson.put("itemCode", recordItem.getItemCode());
            itemJson.put("itemName", recordItem.getItemName());
            itemJson.put("rawValue", recordItem.getRawValue());
            itemJson.put("itemScore", recordItem.getItemScore());
            itemJson.put("extraScore", recordItem.getExtraScore());
            itemJson.put("itemLevel", recordItem.getItemLevel());
            itemJson.put("isWeakness", recordItem.getIsWeakness());
            itemJson.put("isStrength", recordItem.getIsStrength());
            itemJson.put("remark", recordItem.getRemark());
            itemArray.add(itemJson);
        }
        analysisJson.put("items", itemArray);
        return analysisJson.toJSONString();
    }

    private String buildWeaknessSummary(List<AssessmentRecordItem> recordItemList) {
        List<AssessmentRecordItem> weaknessItemList = recordItemList.stream()
                .filter(item -> item.getIsWeakness() != null && item.getIsWeakness() == 1)
                .collect(Collectors.toList());
        if (weaknessItemList.isEmpty()) {
            return TEXT_NO_WEAKNESS;
        }
        return TEXT_WEAKNESS_PREFIX + weaknessItemList.stream()
                .map(item -> item.getItemName() + "（" + decimalText(item.getItemScore()) + "分）")
                .collect(Collectors.joining("、"));
    }

    private String buildStrengthSummary(List<AssessmentRecordItem> recordItemList) {
        List<AssessmentRecordItem> strengthItemList = recordItemList.stream()
                .filter(item -> item.getIsStrength() != null && item.getIsStrength() == 1)
                .collect(Collectors.toList());
        if (strengthItemList.isEmpty()) {
            return TEXT_NO_STRENGTH;
        }
        return TEXT_STRENGTH_PREFIX + strengthItemList.stream()
                .map(item -> item.getItemName() + "（" + decimalText(item.getItemScore()) + "分）")
                .collect(Collectors.joining("、"));
    }

    private String buildTrainingFocus(List<AssessmentRecordItem> recordItemList) {
        List<String> weaknessNameList = recordItemList.stream()
                .filter(item -> item.getIsWeakness() != null && item.getIsWeakness() == 1)
                .map(AssessmentRecordItem::getItemName)
                .collect(Collectors.toList());
        if (weaknessNameList.isEmpty()) {
            return TEXT_BALANCED_TRAINING;
        }
        return TEXT_IMPROVE_PREFIX + String.join("、", weaknessNameList) + TEXT_TRAINING_FOCUS_SUFFIX;
    }

    private String buildRiskNotes(AssessmentRecord assessmentRecord) {
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_UNSCORED)) {
            return TEXT_RULE_MISSING;
        }
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_FAIL)) {
            return TEXT_FAIL_RISK;
        }
        if (assessmentRecord.getWeaknessCount() != null && assessmentRecord.getWeaknessCount() > 0) {
            return TEXT_HAS_WEAKNESS_RISK;
        }
        return TEXT_STABLE_RISK;
    }

    private String buildAiSuggestion(Long userId, AssessmentRecord assessmentRecord,
            List<AssessmentRecordItem> recordItemList, String model, boolean forceRefresh) {
        String fallbackSuggestion = buildRuleBasedAiSuggestion(assessmentRecord, recordItemList);
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_UNSCORED)) {
            return fallbackSuggestion;
        }
        try {
            List<AssessmentRecord> recentRecordList = listRecentRecords(userId, assessmentRecord.getSchemeCode(),
                    assessmentRecord.getId(), 3);
            String prompt = buildAssessmentAdvicePrompt(assessmentRecord, recordItemList, recentRecordList);
            String aiSuggestion = keepApp.generateAssessmentAdvice(prompt, userId, assessmentRecord.getId(), model,
                    forceRefresh);
            if (StringUtils.isBlank(aiSuggestion)) {
                return fallbackSuggestion;
            }
            return aiSuggestion.trim();
        } catch (Exception e) {
            log.error("生成 assessment AI 建议失败, recordId={}, userId={}, error={}",
                    assessmentRecord.getId(), userId, e.getMessage(), e);
            return fallbackSuggestion;
        }
    }

    private String buildRuleBasedAiSuggestion(AssessmentRecord assessmentRecord,
            List<AssessmentRecordItem> recordItemList) {
        List<String> weaknessNameList = recordItemList.stream()
                .filter(item -> item.getIsWeakness() != null && item.getIsWeakness() == 1)
                .map(AssessmentRecordItem::getItemName)
                .collect(Collectors.toList());
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_UNSCORED)) {
            return TEXT_RULE_BASED_ADVICE_UNSCORED;
        }
        if (weaknessNameList.isEmpty()) {
            return TEXT_RULE_BASED_ADVICE_BALANCED;
        }
        return TEXT_RULE_BASED_ADVICE_PREFIX + String.join("、", weaknessNameList) + TEXT_RULE_BASED_ADVICE_SUFFIX;
    }

    private List<AssessmentRecord> listRecentRecords(Long userId, String schemeCode, Long currentRecordId, int limit) {
        if (userId == null || StringUtils.isBlank(schemeCode) || limit <= 0) {
            return List.of();
        }
        QueryWrapper<AssessmentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("schemeCode", schemeCode)
                .ne(currentRecordId != null, "id", currentRecordId)
                .orderByDesc("assessmentDate")
                .orderByDesc("id")
                .last("limit " + limit);
        return assessmentRecordMapper.selectList(queryWrapper);
    }

    private String buildAssessmentAdvicePrompt(AssessmentRecord assessmentRecord,
            List<AssessmentRecordItem> recordItemList, List<AssessmentRecord> recentRecordList) {
        StringBuilder prompt = new StringBuilder(1600);
        prompt.append("请基于以下大学生体测数据，生成个性化健身建议。\n");
        prompt.append("【用户画像】\n");
        appendPromptLine(prompt, "性别", defaultText(assessmentRecord.getGenderSnapshot()));
        appendPromptLine(prompt, "年级", defaultText(assessmentRecord.getGradeSnapshot()));
        appendPromptLine(prompt, "年级组", defaultText(assessmentRecord.getGradeGroupSnapshot()));
        appendPromptLine(prompt, "身高(cm)", decimalText(assessmentRecord.getHeightSnapshot()));
        appendPromptLine(prompt, "体重(kg)", decimalText(assessmentRecord.getWeightSnapshot()));
        appendPromptLine(prompt, "BMI", decimalText(assessmentRecord.getBmiSnapshot()));
        appendPromptLine(prompt, "测试时间", formatDate(assessmentRecord.getAssessmentDate()));

        prompt.append("\n【本次评测】\n");
        appendPromptLine(prompt, "总分", decimalText(assessmentRecord.getTotalScore()));
        appendPromptLine(prompt, "等级", defaultText(assessmentRecord.getLevel()));
        appendPromptLine(prompt, "强项数量", String.valueOf(defaultInteger(assessmentRecord.getStrengthCount())));
        appendPromptLine(prompt, "弱项数量", String.valueOf(defaultInteger(assessmentRecord.getWeaknessCount())));
        appendPromptLine(prompt, "结果摘要", defaultText(assessmentRecord.getSummary()));
        appendPromptLine(prompt, "弱项总结", buildWeaknessSummary(recordItemList));
        appendPromptLine(prompt, "强项总结", buildStrengthSummary(recordItemList));
        appendPromptLine(prompt, "训练重点", buildTrainingFocus(recordItemList));
        appendPromptLine(prompt, "风险提示", buildRiskNotes(assessmentRecord));

        prompt.append("\n【单项明细】\n");
        for (AssessmentRecordItem recordItem : recordItemList) {
            prompt.append("- ")
                    .append(defaultText(recordItem.getItemName()))
                    .append("：成绩=").append(decimalText(recordItem.getRawValue()))
                    .append(defaultText(recordItem.getUnit()))
                    .append("，分数=").append(decimalText(recordItem.getItemScore()))
                    .append("，附加分=").append(decimalText(recordItem.getExtraScore()))
                    .append("，等级=").append(defaultText(recordItem.getItemLevel()))
                    .append("，是否弱项=").append(isFlagged(recordItem.getIsWeakness()))
                    .append("，是否强项=").append(isFlagged(recordItem.getIsStrength()));
            if (StringUtils.isNotBlank(recordItem.getRemark())) {
                prompt.append("，备注=").append(recordItem.getRemark());
            }
            prompt.append("\n");
        }

        prompt.append("\n【历史趋势】\n");
        prompt.append(buildRecentTrendText(assessmentRecord, recentRecordList)).append("\n");

        prompt.append("\n【输出要求】\n");
        prompt.append("1. 使用简体中文输出，不要使用 JSON。\n");
        prompt.append("2. 必须严格按以下六个标题输出：整体判断、训练建议、每周安排、恢复与营养、注意事项、下一次体测前目标。\n");
        prompt.append("3. 内容控制在 400 到 800 字之间，建议要具体到训练频次、训练方向和注意事项。\n");
        prompt.append("4. 如果 BMI 偏高或偏低，要给出合理的体重管理建议；如果没有明显弱项，要给出从当前分数提升到下一档的训练目标。\n");
        prompt.append("5. 不要编造疾病史或受伤史，也不要给出医疗诊断。");
        return prompt.toString();
    }

    private String buildRecentTrendText(AssessmentRecord currentRecord, List<AssessmentRecord> recentRecordList) {
        if (recentRecordList == null || recentRecordList.isEmpty()) {
            return "暂无更早的历史记录，可先基于本次评测制定后续 4 到 8 周训练安排。";
        }
        StringBuilder trendText = new StringBuilder();
        AssessmentRecord latestRecord = recentRecordList.get(0);
        if (currentRecord.getTotalScore() != null && latestRecord.getTotalScore() != null) {
            BigDecimal diff = currentRecord.getTotalScore().subtract(latestRecord.getTotalScore())
                    .setScale(2, RoundingMode.HALF_UP);
            trendText.append("与上一条记录相比，总分变化 ")
                    .append(formatSignedDecimal(diff))
                    .append(" 分。");
            if (StringUtils.isNotBlank(latestRecord.getLevel())) {
                trendText.append(" 上一次等级为 ").append(latestRecord.getLevel()).append("。");
            }
            trendText.append("\n");
        }
        trendText.append("最近历史记录：\n");
        for (AssessmentRecord record : recentRecordList) {
            trendText.append("- 时间=").append(formatDate(record.getAssessmentDate()))
                    .append("，总分=").append(decimalText(record.getTotalScore()))
                    .append("，等级=").append(defaultText(record.getLevel()))
                    .append("，摘要=").append(defaultText(record.getSummary()))
                    .append("\n");
        }
        return trendText.toString().trim();
    }

    private void appendPromptLine(StringBuilder prompt, String label, String value) {
        prompt.append(label).append("：").append(value).append("\n");
    }

    private String decimalText(BigDecimal value) {
        if (value == null) {
            return TEXT_UNKNOWN;
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return TEXT_UNKNOWN;
        }
        return REPORT_DATE_FORMATTER.format(date.toInstant());
    }

    private String formatSignedDecimal(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.signum() > 0 ? "+" + decimalText(value) : decimalText(value);
    }

    private String defaultText(String value) {
        return StringUtils.defaultIfBlank(value, TEXT_UNKNOWN);
    }

    private String isFlagged(Integer flag) {
        return flag != null && flag == 1 ? TEXT_YES : TEXT_NO;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
