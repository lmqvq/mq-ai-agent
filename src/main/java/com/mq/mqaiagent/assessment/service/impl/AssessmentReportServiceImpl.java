package com.mq.mqaiagent.assessment.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Assessment report service implementation.
 */
@Service
public class AssessmentReportServiceImpl extends ServiceImpl<AssessmentReportMapper, AssessmentReport>
        implements AssessmentReportService {

    @Resource
    private AssessmentRecordMapper assessmentRecordMapper;

    @Resource
    private AssessmentRecordItemMapper assessmentRecordItemMapper;

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
        assessmentReport.setAiSuggestion(buildAiSuggestion(assessmentRecord, recordItemList));

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
            return "当前暂无明显弱项。";
        }
        return "弱项项目：" + weaknessItemList.stream()
                .map(item -> item.getItemName() + "（" + item.getItemScore() + "分）")
                .collect(Collectors.joining("、"));
    }

    private String buildStrengthSummary(List<AssessmentRecordItem> recordItemList) {
        List<AssessmentRecordItem> strengthItemList = recordItemList.stream()
                .filter(item -> item.getIsStrength() != null && item.getIsStrength() == 1)
                .collect(Collectors.toList());
        if (strengthItemList.isEmpty()) {
            return "当前暂无明显强项，建议继续保持全面训练。";
        }
        return "强项项目：" + strengthItemList.stream()
                .map(item -> item.getItemName() + "（" + item.getItemScore() + "分）")
                .collect(Collectors.joining("、"));
    }

    private String buildTrainingFocus(List<AssessmentRecordItem> recordItemList) {
        List<String> weaknessNameList = recordItemList.stream()
                .filter(item -> item.getIsWeakness() != null && item.getIsWeakness() == 1)
                .map(AssessmentRecordItem::getItemName)
                .collect(Collectors.toList());
        if (weaknessNameList.isEmpty()) {
            return "当前各项目表现较均衡，建议围绕优势项目继续巩固，并通过常规有氧和力量训练保持整体水平。";
        }
        return "优先提升项目：" + String.join("、", weaknessNameList) + "。建议围绕这些项目安排每周 2 到 3 次专项训练。";
    }

    private String buildRiskNotes(AssessmentRecord assessmentRecord) {
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_UNSCORED)) {
            return "当前记录尚未完成有效评分，请先确认 assessment_rule 是否已导入。";
        }
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_FAIL)) {
            return "当前总分处于不及格区间，建议优先补齐弱项，并注意循序渐进，避免突然增加训练量。";
        }
        if (assessmentRecord.getWeaknessCount() != null && assessmentRecord.getWeaknessCount() > 0) {
            return "虽然总分已完成计算，但仍存在弱项，建议把训练重点放在短板补齐上。";
        }
        return "当前成绩整体稳定，训练时仍需注意热身、恢复和训练负荷管理。";
    }

    private String buildAiSuggestion(AssessmentRecord assessmentRecord, List<AssessmentRecordItem> recordItemList) {
        List<String> weaknessNameList = recordItemList.stream()
                .filter(item -> item.getIsWeakness() != null && item.getIsWeakness() == 1)
                .map(AssessmentRecordItem::getItemName)
                .collect(Collectors.toList());
        if (StringUtils.equals(assessmentRecord.getLevel(), AssessmentConstant.LEVEL_UNSCORED)) {
            return "建议先补充评分规则数据，再结合 KeepApp 生成个性化训练建议。";
        }
        if (weaknessNameList.isEmpty()) {
            return "可以结合 KeepApp 继续生成维持型训练计划，重点保持当前优势并提升整体稳定性。";
        }
        return "建议下一步结合 KeepApp，围绕 " + String.join("、", weaknessNameList)
                + " 生成专项训练计划，并持续跟踪下一次体测成绩变化。";
    }
}
