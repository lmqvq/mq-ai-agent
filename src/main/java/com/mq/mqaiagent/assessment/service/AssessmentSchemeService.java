package com.mq.mqaiagent.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.assessment.model.dto.scheme.AssessmentSchemeQueryRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentScheme;
import com.mq.mqaiagent.assessment.model.vo.AssessmentSchemeVO;

import java.util.List;

/**
 * Assessment scheme service.
 */
public interface AssessmentSchemeService extends IService<AssessmentScheme> {

    List<AssessmentSchemeVO> listSchemeVO(AssessmentSchemeQueryRequest schemeQueryRequest);

    AssessmentSchemeVO getSchemeDetail(String schemeCode);
}
