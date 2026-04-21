package com.mq.mqaiagent.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.mapper.AssessmentProfileMapper;
import com.mq.mqaiagent.assessment.model.dto.profile.AssessmentProfileSaveRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentProfile;
import com.mq.mqaiagent.assessment.model.vo.AssessmentProfileVO;
import com.mq.mqaiagent.assessment.service.AssessmentProfileService;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Assessment profile service implementation.
 */
@Service
public class AssessmentProfileServiceImpl extends ServiceImpl<AssessmentProfileMapper, AssessmentProfile>
        implements AssessmentProfileService {

    @Override
    public void validProfile(AssessmentProfile assessmentProfile, boolean add) {
        if (assessmentProfile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(assessmentProfile.getSchemeCode()), ErrorCode.PARAMS_ERROR,
                    "schemeCode is required");
            ThrowUtils.throwIf(StringUtils.isBlank(assessmentProfile.getGender()), ErrorCode.PARAMS_ERROR,
                    "gender is required");
        }
        if (assessmentProfile.getHeight() != null) {
            ThrowUtils.throwIf(assessmentProfile.getHeight().compareTo(BigDecimal.ZERO) <= 0, ErrorCode.PARAMS_ERROR,
                    "height must be greater than 0");
        }
        if (assessmentProfile.getWeight() != null) {
            ThrowUtils.throwIf(assessmentProfile.getWeight().compareTo(BigDecimal.ZERO) <= 0, ErrorCode.PARAMS_ERROR,
                    "weight must be greater than 0");
        }
    }

    @Override
    public AssessmentProfileVO saveOrUpdateProfile(Long userId, AssessmentProfileSaveRequest profileSaveRequest) {
        ThrowUtils.throwIf(userId == null || profileSaveRequest == null, ErrorCode.PARAMS_ERROR);
        String schemeCode = StringUtils.defaultIfBlank(profileSaveRequest.getSchemeCode(),
                AssessmentConstant.DEFAULT_SCHEME_CODE);
        AssessmentProfile oldProfile = getByUserIdAndSchemeCode(userId, schemeCode);
        AssessmentProfile assessmentProfile = oldProfile == null ? new AssessmentProfile() : oldProfile;
        mergeProfile(assessmentProfile, profileSaveRequest);
        assessmentProfile.setUserId(userId);
        assessmentProfile.setSchemeCode(schemeCode);
        assessmentProfile.setBmi(calculateBmi(assessmentProfile.getWeight(), assessmentProfile.getHeight()));
        validProfile(assessmentProfile, true);
        boolean result = this.saveOrUpdate(assessmentProfile);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return getProfileVO(assessmentProfile);
    }

    @Override
    public AssessmentProfileVO getUserProfile(Long userId, String schemeCode) {
        return getProfileVO(getByUserIdAndSchemeCode(userId, schemeCode));
    }

    @Override
    public AssessmentProfile getByUserIdAndSchemeCode(Long userId, String schemeCode) {
        if (userId == null) {
            return null;
        }
        QueryWrapper<AssessmentProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("schemeCode", StringUtils.defaultIfBlank(schemeCode, AssessmentConstant.DEFAULT_SCHEME_CODE))
                .last("limit 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public AssessmentProfileVO getProfileVO(AssessmentProfile assessmentProfile) {
        if (assessmentProfile == null) {
            return null;
        }
        AssessmentProfileVO profileVO = new AssessmentProfileVO();
        BeanUtils.copyProperties(assessmentProfile, profileVO);
        return profileVO;
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

    private void mergeProfile(AssessmentProfile target, AssessmentProfileSaveRequest source) {
        if (StringUtils.isNotBlank(source.getGender())) {
            target.setGender(normalizeGender(source.getGender()));
        }
        if (StringUtils.isNotBlank(source.getGrade())) {
            target.setGrade(source.getGrade());
        }
        if (StringUtils.isNotBlank(source.getGradeGroup())) {
            target.setGradeGroup(normalizeGradeGroup(source.getGradeGroup()));
        }
        if (source.getHeight() != null) {
            target.setHeight(source.getHeight());
        }
        if (source.getWeight() != null) {
            target.setWeight(source.getWeight());
        }
        if (source.getExtraProfileJson() != null) {
            target.setExtraProfileJson(source.getExtraProfileJson());
        }
        if (StringUtils.isBlank(target.getGradeGroup()) && StringUtils.isNotBlank(target.getGrade())) {
            target.setGradeGroup(inferGradeGroup(target.getGrade()));
        }
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

    private String normalizeGradeGroup(String gradeGroup) {
        if (StringUtils.isBlank(gradeGroup)) {
            return null;
        }
        String trimmedGradeGroup = gradeGroup.trim().toLowerCase();
        if (StringUtils.equalsAny(trimmedGradeGroup, "freshman_sophomore", "大一大二")) {
            return "freshman_sophomore";
        }
        if (StringUtils.equalsAny(trimmedGradeGroup, "junior_senior", "大三大四")) {
            return "junior_senior";
        }
        return trimmedGradeGroup;
    }

    private String inferGradeGroup(String grade) {
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
