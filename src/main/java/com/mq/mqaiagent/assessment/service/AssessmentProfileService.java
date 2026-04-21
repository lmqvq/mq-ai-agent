package com.mq.mqaiagent.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.assessment.model.dto.profile.AssessmentProfileSaveRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentProfile;
import com.mq.mqaiagent.assessment.model.vo.AssessmentProfileVO;

/**
 * Assessment profile service.
 */
public interface AssessmentProfileService extends IService<AssessmentProfile> {

    void validProfile(AssessmentProfile assessmentProfile, boolean add);

    AssessmentProfileVO saveOrUpdateProfile(Long userId, AssessmentProfileSaveRequest profileSaveRequest);

    AssessmentProfileVO getUserProfile(Long userId, String schemeCode);

    AssessmentProfile getByUserIdAndSchemeCode(Long userId, String schemeCode);

    AssessmentProfileVO getProfileVO(AssessmentProfile assessmentProfile);
}
