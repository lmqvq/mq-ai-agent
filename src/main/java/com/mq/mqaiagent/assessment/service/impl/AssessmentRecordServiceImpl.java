package com.mq.mqaiagent.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.engine.AssessmentScoringEngine;
import com.mq.mqaiagent.assessment.engine.model.AssessmentItemScoreResult;
import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreContext;
import com.mq.mqaiagent.assessment.engine.model.AssessmentScoreResult;
import com.mq.mqaiagent.assessment.mapper.AssessmentReportMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentRecordItemMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentRecordMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentSchemeItemMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentSchemeMapper;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordAddRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordItemInput;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordQueryRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordUpdateRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentProfile;
import com.mq.mqaiagent.assessment.model.entity.AssessmentReport;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecord;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecordItem;
import com.mq.mqaiagent.assessment.model.entity.AssessmentScheme;
import com.mq.mqaiagent.assessment.model.entity.AssessmentSchemeItem;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordDetailVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordItemVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentTrendVO;
import com.mq.mqaiagent.assessment.service.AssessmentProfileService;
import com.mq.mqaiagent.assessment.service.AssessmentRecordService;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.utils.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Assessment record service implementation.
 */
@Service
public class AssessmentRecordServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord>
        implements AssessmentRecordService {

    @Resource
    private AssessmentSchemeMapper assessmentSchemeMapper;

    @Resource
    private AssessmentSchemeItemMapper assessmentSchemeItemMapper;

    @Resource
    private AssessmentRecordItemMapper assessmentRecordItemMapper;

    @Resource
    private AssessmentReportMapper assessmentReportMapper;

    @Resource
    private AssessmentProfileService assessmentProfileService;

    @Resource
    private AssessmentScoringEngine assessmentScoringEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRecord(Long userId, AssessmentRecordAddRequest recordAddRequest) {
        return saveRecord(userId, recordAddRequest, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateRecord(Long userId, AssessmentRecordUpdateRequest recordUpdateRequest) {
        ThrowUtils.throwIf(userId == null || recordUpdateRequest == null || recordUpdateRequest.getId() == null
                || recordUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentRecord existingRecord = this.getById(recordUpdateRequest.getId());
        ThrowUtils.throwIf(existingRecord == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!userId.equals(existingRecord.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return saveRecord(userId, recordUpdateRequest, existingRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRecord(Long userId, Long recordId) {
        ThrowUtils.throwIf(userId == null || recordId == null || recordId <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentRecord existingRecord = this.getById(recordId);
        ThrowUtils.throwIf(existingRecord == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!userId.equals(existingRecord.getUserId()), ErrorCode.NO_AUTH_ERROR);

        deleteRelatedItemsAndReports(recordId);
        boolean removeResult = this.removeById(recordId);
        ThrowUtils.throwIf(!removeResult, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public Page<AssessmentRecordVO> listUserRecordByPage(Long userId, AssessmentRecordQueryRequest recordQueryRequest) {
        ThrowUtils.throwIf(userId == null || recordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<AssessmentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(recordQueryRequest.getSchemeCode()), "schemeCode",
                recordQueryRequest.getSchemeCode());
        queryWrapper.eq(StringUtils.isNotBlank(recordQueryRequest.getSourceType()), "sourceType",
                recordQueryRequest.getSourceType());
        queryWrapper.eq(StringUtils.isNotBlank(recordQueryRequest.getLevel()), "level",
                recordQueryRequest.getLevel());
        queryWrapper.ge(recordQueryRequest.getStartDate() != null, "assessmentDate", recordQueryRequest.getStartDate());
        queryWrapper.le(recordQueryRequest.getEndDate() != null, "assessmentDate", recordQueryRequest.getEndDate());
        if (SqlUtils.validSortField(recordQueryRequest.getSortField())) {
            queryWrapper.orderBy(true, "ascend".equals(recordQueryRequest.getSortOrder()),
                    recordQueryRequest.getSortField());
        } else {
            queryWrapper.orderByDesc("assessmentDate");
        }
        Page<AssessmentRecord> recordPage = this.page(
                new Page<>(recordQueryRequest.getCurrent(), recordQueryRequest.getPageSize()), queryWrapper);
        Page<AssessmentRecordVO> recordVOPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(),
                recordPage.getTotal());
        recordVOPage.setRecords(recordPage.getRecords().stream().map(this::getRecordVO).collect(Collectors.toList()));
        return recordVOPage;
    }

    @Override
    public AssessmentRecordDetailVO getUserRecordDetail(Long userId, Long recordId) {
        ThrowUtils.throwIf(userId == null || recordId == null || recordId <= 0, ErrorCode.PARAMS_ERROR);
        AssessmentRecord assessmentRecord = this.getById(recordId);
        ThrowUtils.throwIf(assessmentRecord == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!userId.equals(assessmentRecord.getUserId()), ErrorCode.NO_AUTH_ERROR);

        AssessmentRecordDetailVO detailVO = new AssessmentRecordDetailVO();
        BeanUtils.copyProperties(assessmentRecord, detailVO);
        QueryWrapper<AssessmentRecordItem> itemQueryWrapper = new QueryWrapper<>();
        itemQueryWrapper.eq("recordId", recordId).orderByAsc("itemOrder");
        List<AssessmentRecordItemVO> itemVOList = assessmentRecordItemMapper.selectList(itemQueryWrapper).stream()
                .map(this::getRecordItemVO)
                .collect(Collectors.toList());
        detailVO.setItemList(itemVOList);
        return detailVO;
    }

    @Override
    public List<AssessmentTrendVO> getUserTrends(Long userId, String schemeCode, Integer limit) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        int safeLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 50);
        QueryWrapper<AssessmentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("schemeCode", resolveSchemeCode(schemeCode))
                .orderByDesc("assessmentDate");
        Page<AssessmentRecord> recordPage = this.page(new Page<>(1, safeLimit), queryWrapper);
        if (recordPage.getRecords().isEmpty()) {
            return Collections.emptyList();
        }
        return recordPage.getRecords().stream().map(record -> {
            AssessmentTrendVO trendVO = new AssessmentTrendVO();
            trendVO.setRecordId(record.getId());
            trendVO.setAssessmentDate(record.getAssessmentDate());
            trendVO.setTotalScore(record.getTotalScore());
            trendVO.setLevel(record.getLevel());
            trendVO.setSummary(record.getSummary());
            return trendVO;
        }).collect(Collectors.toList());
    }

    @Override
    public AssessmentRecordVO getRecordVO(AssessmentRecord assessmentRecord) {
        if (assessmentRecord == null) {
            return null;
        }
        AssessmentRecordVO recordVO = new AssessmentRecordVO();
        BeanUtils.copyProperties(assessmentRecord, recordVO);
        return recordVO;
    }

    private AssessmentRecordItemVO getRecordItemVO(AssessmentRecordItem assessmentRecordItem) {
        if (assessmentRecordItem == null) {
            return null;
        }
        AssessmentRecordItemVO itemVO = new AssessmentRecordItemVO();
        BeanUtils.copyProperties(assessmentRecordItem, itemVO);
        return itemVO;
    }

    private Long saveRecord(Long userId, AssessmentRecordAddRequest recordRequest, AssessmentRecord existingRecord) {
        ThrowUtils.throwIf(userId == null || recordRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(recordRequest.getItemList() == null || recordRequest.getItemList().isEmpty(),
                ErrorCode.PARAMS_ERROR, "itemList is required");

        String schemeCode = resolveSchemeCode(firstNonBlank(recordRequest.getSchemeCode(),
                existingRecord == null ? null : existingRecord.getSchemeCode()));
        AssessmentScheme assessmentScheme = getAssessmentScheme(schemeCode);
        AssessmentProfile assessmentProfile = assessmentProfileService.getByUserIdAndSchemeCode(userId, schemeCode);
        Map<String, AssessmentSchemeItem> schemeItemMap = listSchemeItemMap(schemeCode);

        AssessmentRecord assessmentRecord = existingRecord == null ? new AssessmentRecord() : existingRecord;
        assessmentRecord.setUserId(userId);
        assessmentRecord.setSchemeCode(schemeCode);
        assessmentRecord.setSchemeVersion(StringUtils.defaultIfBlank(assessmentScheme.getVersion(),
                AssessmentConstant.DEFAULT_RULE_VERSION));
        assessmentRecord.setAssessmentDate(ObjectUtils.defaultIfNull(recordRequest.getAssessmentDate(), new Date()));
        assessmentRecord.setSourceType(StringUtils.defaultIfBlank(recordRequest.getSourceType(),
                AssessmentConstant.SOURCE_MANUAL));

        String genderSnapshot = normalizeGender(firstNonBlank(recordRequest.getGender(),
                assessmentProfile == null ? null : assessmentProfile.getGender()));
        String gradeSnapshot = firstNonBlank(recordRequest.getGrade(),
                assessmentProfile == null ? null : assessmentProfile.getGrade());
        String gradeGroupSnapshot = resolveGradeGroup(firstNonBlank(recordRequest.getGradeGroup(),
                assessmentProfile == null ? null : assessmentProfile.getGradeGroup()), gradeSnapshot);
        assessmentRecord.setGenderSnapshot(genderSnapshot);
        assessmentRecord.setGradeSnapshot(gradeSnapshot);
        assessmentRecord.setGradeGroupSnapshot(gradeGroupSnapshot);
        assessmentRecord.setHeightSnapshot(firstNonNull(recordRequest.getHeight(),
                assessmentProfile == null ? null : assessmentProfile.getHeight()));
        assessmentRecord.setWeightSnapshot(firstNonNull(recordRequest.getWeight(),
                assessmentProfile == null ? null : assessmentProfile.getWeight()));
        assessmentRecord.setBmiSnapshot(
                calculateBmi(assessmentRecord.getWeightSnapshot(), assessmentRecord.getHeightSnapshot()));
        assessmentRecord.setExtraDataJson(recordRequest.getExtraDataJson());

        List<AssessmentRecordItemInput> workingItemList = buildWorkingItemList(recordRequest.getItemList(),
                assessmentRecord, schemeItemMap);
        validateDuplicateItemCode(workingItemList);
        validateRequiredItems(workingItemList, schemeItemMap, assessmentRecord.getGenderSnapshot(),
                assessmentRecord.getGradeGroupSnapshot());

        AssessmentScoreResult scoreResult = assessmentScoringEngine.score(buildScoreContext(assessmentRecord,
                workingItemList, schemeItemMap));
        assessmentRecord.setTotalScore(scoreResult.getTotalScore());
        assessmentRecord.setLevel(scoreResult.getLevel());
        assessmentRecord.setWeaknessCount(scoreResult.getWeaknessCount());
        assessmentRecord.setStrengthCount(scoreResult.getStrengthCount());
        assessmentRecord.setSummary(StringUtils.defaultIfBlank(recordRequest.getSummary(), scoreResult.getSummary()));

        boolean persisted = existingRecord == null ? this.save(assessmentRecord) : this.updateById(assessmentRecord);
        ThrowUtils.throwIf(!persisted, ErrorCode.OPERATION_ERROR);

        if (existingRecord != null) {
            deleteRelatedItemsAndReports(assessmentRecord.getId());
        }
        saveRecordItems(assessmentRecord, scoreResult, workingItemList, schemeItemMap);
        return assessmentRecord.getId();
    }

    private AssessmentScheme getAssessmentScheme(String schemeCode) {
        QueryWrapper<AssessmentScheme> schemeQueryWrapper = new QueryWrapper<>();
        schemeQueryWrapper.eq("schemeCode", schemeCode).last("limit 1");
        AssessmentScheme assessmentScheme = assessmentSchemeMapper.selectOne(schemeQueryWrapper);
        ThrowUtils.throwIf(assessmentScheme == null, ErrorCode.NOT_FOUND_ERROR, "assessment scheme not found");
        return assessmentScheme;
    }

    private void saveRecordItems(AssessmentRecord assessmentRecord, AssessmentScoreResult scoreResult,
            List<AssessmentRecordItemInput> workingItemList, Map<String, AssessmentSchemeItem> schemeItemMap) {
        Map<String, AssessmentRecordItemInput> itemInputMap = workingItemList.stream().collect(Collectors.toMap(
                AssessmentRecordItemInput::getItemCode, Function.identity(), (left, right) -> left));
        for (AssessmentItemScoreResult itemScoreResult : scoreResult.getItemScoreResults()) {
            AssessmentSchemeItem schemeItem = schemeItemMap.get(itemScoreResult.getItemCode());
            AssessmentRecordItemInput itemInput = itemInputMap.get(itemScoreResult.getItemCode());
            AssessmentRecordItem recordItem = new AssessmentRecordItem();
            recordItem.setRecordId(assessmentRecord.getId());
            recordItem.setItemCode(schemeItem.getItemCode());
            recordItem.setItemName(resolveItemDisplayName(schemeItem, assessmentRecord.getGenderSnapshot()));
            recordItem.setItemOrder(ObjectUtils.defaultIfNull(schemeItem.getDisplayOrder(), 0));
            recordItem.setUnit(schemeItem.getUnit());
            recordItem.setRawValue(itemScoreResult.getRawValue());
            recordItem.setItemWeight(itemScoreResult.getItemWeight());
            recordItem.setItemScore(itemScoreResult.getItemScore());
            recordItem.setExtraScore(itemScoreResult.getExtraScore());
            recordItem.setItemLevel(itemScoreResult.getItemLevel());
            recordItem.setIsWeakness(itemScoreResult.isWeakness() ? 1 : 0);
            recordItem.setIsStrength(itemScoreResult.isStrength() ? 1 : 0);
            recordItem.setRemark(buildRecordItemRemark(itemInput, itemScoreResult));
            assessmentRecordItemMapper.insert(recordItem);
        }
    }

    private void deleteRelatedItemsAndReports(Long recordId) {
        assessmentRecordItemMapper.deleteByRecordIdForce(recordId);
        assessmentReportMapper.deleteByRecordIdForce(recordId);
    }

    private void validateRawValue(BigDecimal rawValue, AssessmentSchemeItem schemeItem) {
        ThrowUtils.throwIf(rawValue.compareTo(BigDecimal.ZERO) < 0, ErrorCode.PARAMS_ERROR,
                "rawValue must not be negative");
        if (schemeItem.getValidationMin() != null) {
            ThrowUtils.throwIf(rawValue.compareTo(schemeItem.getValidationMin()) < 0, ErrorCode.PARAMS_ERROR,
                    "rawValue is lower than validationMin");
        }
        if (schemeItem.getValidationMax() != null) {
            ThrowUtils.throwIf(rawValue.compareTo(schemeItem.getValidationMax()) > 0, ErrorCode.PARAMS_ERROR,
                    "rawValue is greater than validationMax");
        }
    }

    private AssessmentScoreContext buildScoreContext(AssessmentRecord assessmentRecord,
            List<AssessmentRecordItemInput> itemInputList, Map<String, AssessmentSchemeItem> schemeItemMap) {
        List<AssessmentScoreContext.ItemContext> itemContextList = new ArrayList<>();
        for (AssessmentRecordItemInput itemInput : itemInputList) {
            ThrowUtils.throwIf(itemInput == null || StringUtils.isBlank(itemInput.getItemCode())
                    || itemInput.getRawValue() == null, ErrorCode.PARAMS_ERROR, "invalid assessment item input");
            AssessmentSchemeItem schemeItem = schemeItemMap.get(itemInput.getItemCode());
            ThrowUtils.throwIf(schemeItem == null, ErrorCode.PARAMS_ERROR,
                    "itemCode does not belong to the selected scheme");
            ThrowUtils.throwIf(!isItemApplicable(schemeItem, assessmentRecord.getGenderSnapshot(),
                    assessmentRecord.getGradeGroupSnapshot()), ErrorCode.PARAMS_ERROR,
                    "itemCode is not applicable to current gender or grade group");
            validateRawValue(itemInput.getRawValue(), schemeItem);

            itemContextList.add(AssessmentScoreContext.ItemContext.builder()
                    .itemCode(schemeItem.getItemCode())
                    .itemName(resolveItemDisplayName(schemeItem, assessmentRecord.getGenderSnapshot()))
                    .rawValue(itemInput.getRawValue())
                    .itemWeight(defaultDecimal(schemeItem.getWeight()))
                    .build());
        }
        return AssessmentScoreContext.builder()
                .schemeCode(assessmentRecord.getSchemeCode())
                .ruleVersion(assessmentRecord.getSchemeVersion())
                .gender(assessmentRecord.getGenderSnapshot())
                .gradeGroup(assessmentRecord.getGradeGroupSnapshot())
                .itemList(itemContextList)
                .build();
    }

    private Map<String, AssessmentSchemeItem> listSchemeItemMap(String schemeCode) {
        QueryWrapper<AssessmentSchemeItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("schemeCode", schemeCode);
        return assessmentSchemeItemMapper.selectList(queryWrapper).stream().collect(Collectors.toMap(
                AssessmentSchemeItem::getItemCode, Function.identity(), (left, right) -> left));
    }

    private String resolveSchemeCode(String schemeCode) {
        return StringUtils.defaultIfBlank(schemeCode, AssessmentConstant.DEFAULT_SCHEME_CODE);
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.isNotBlank(first) ? first : second;
    }

    private BigDecimal firstNonNull(BigDecimal first, BigDecimal second) {
        return first != null ? first : second;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value;
    }

    private BigDecimal calculateBmi(BigDecimal weight, BigDecimal height) {
        if (weight == null || height == null || height.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightInMeter = height.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal divisor = heightInMeter.multiply(heightInMeter);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return weight.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private List<AssessmentRecordItemInput> buildWorkingItemList(List<AssessmentRecordItemInput> sourceItemList,
            AssessmentRecord assessmentRecord, Map<String, AssessmentSchemeItem> schemeItemMap) {
        List<AssessmentRecordItemInput> workingItemList = new ArrayList<>(sourceItemList);
        boolean containsBmi = workingItemList.stream()
                .anyMatch(item -> item != null && StringUtils.equalsIgnoreCase("BMI", item.getItemCode()));
        if (!containsBmi && schemeItemMap.containsKey("BMI") && assessmentRecord.getBmiSnapshot() != null) {
            AssessmentRecordItemInput bmiItem = new AssessmentRecordItemInput();
            bmiItem.setItemCode("BMI");
            bmiItem.setRawValue(assessmentRecord.getBmiSnapshot());
            bmiItem.setRemark("系统根据身高和体重自动换算 BMI");
            workingItemList.add(bmiItem);
        }
        return workingItemList;
    }

    private void validateDuplicateItemCode(List<AssessmentRecordItemInput> itemList) {
        Set<String> itemCodeSet = new HashSet<>();
        for (AssessmentRecordItemInput itemInput : itemList) {
            if (itemInput == null || StringUtils.isBlank(itemInput.getItemCode())) {
                continue;
            }
            ThrowUtils.throwIf(!itemCodeSet.add(itemInput.getItemCode()), ErrorCode.PARAMS_ERROR,
                    "duplicate itemCode is not allowed");
        }
    }

    private void validateRequiredItems(List<AssessmentRecordItemInput> itemList, Map<String, AssessmentSchemeItem> schemeItemMap,
            String gender, String gradeGroup) {
        Set<String> itemCodeSet = itemList.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getItemCode()))
                .map(AssessmentRecordItemInput::getItemCode)
                .collect(Collectors.toSet());
        for (AssessmentSchemeItem schemeItem : schemeItemMap.values()) {
            if (!ObjectUtils.isNotEmpty(schemeItem.getIsRequired()) || schemeItem.getIsRequired() != 1) {
                continue;
            }
            if (!isItemApplicable(schemeItem, gender, gradeGroup)) {
                continue;
            }
            ThrowUtils.throwIf(!itemCodeSet.contains(schemeItem.getItemCode()), ErrorCode.PARAMS_ERROR,
                    schemeItem.getItemName() + " is required");
        }
    }

    private boolean isItemApplicable(AssessmentSchemeItem schemeItem, String gender, String gradeGroup) {
        return matchDimension(schemeItem.getApplicableGender(), gender)
                && matchDimension(schemeItem.getApplicableGradeGroup(), gradeGroup);
    }

    private boolean matchDimension(String configuredValue, String actualValue) {
        if (StringUtils.isBlank(configuredValue) || StringUtils.equalsIgnoreCase("all", configuredValue)) {
            return true;
        }
        return StringUtils.equalsIgnoreCase(configuredValue, actualValue);
    }

    private String resolveItemDisplayName(AssessmentSchemeItem schemeItem, String gender) {
        if (schemeItem == null) {
            return null;
        }
        if (StringUtils.equals("UPPER_BODY_OR_CORE", schemeItem.getItemCode())) {
            return StringUtils.equalsIgnoreCase("female", gender) ? "仰卧起坐" : "引体向上";
        }
        if (StringUtils.equals("ENDURANCE_RUN", schemeItem.getItemCode())) {
            return StringUtils.equalsIgnoreCase("female", gender) ? "800米跑" : "1000米跑";
        }
        return schemeItem.getItemName();
    }

    private String buildRecordItemRemark(AssessmentRecordItemInput itemInput, AssessmentItemScoreResult itemScoreResult) {
        String inputRemark = itemInput == null ? null : itemInput.getRemark();
        String scoreRemark = itemScoreResult == null ? null : itemScoreResult.getRemark();
        if (StringUtils.isBlank(inputRemark)) {
            return scoreRemark;
        }
        if (StringUtils.isBlank(scoreRemark)) {
            return inputRemark;
        }
        return inputRemark + "；" + scoreRemark;
    }

    private String normalizeGender(String gender) {
        if (StringUtils.isBlank(gender)) {
            return null;
        }
        String trimmedGender = gender.trim().toLowerCase();
        if (StringUtils.equalsAny(trimmedGender, "male", "man", "boy", "男", "男生")) {
            return "male";
        }
        if (StringUtils.equalsAny(trimmedGender, "female", "woman", "girl", "女", "女生")) {
            return "female";
        }
        return trimmedGender;
    }

    private String resolveGradeGroup(String gradeGroup, String grade) {
        if (StringUtils.isNotBlank(gradeGroup)) {
            return gradeGroup;
        }
        if (StringUtils.isBlank(grade)) {
            return null;
        }
        String trimmedGrade = grade.trim().toLowerCase();
        if (StringUtils.containsAny(trimmedGrade, "大一", "大二", "一年级", "二年级", "freshman", "sophomore")) {
            return "freshman_sophomore";
        }
        if (StringUtils.containsAny(trimmedGrade, "大三", "大四", "三年级", "四年级", "junior", "senior")) {
            return "junior_senior";
        }
        return trimmedGrade;
    }
}
