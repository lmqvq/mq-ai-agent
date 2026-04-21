package com.mq.mqaiagent.assessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.assessment.model.dto.report.AssessmentReportGenerateRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentReport;
import com.mq.mqaiagent.assessment.model.vo.AssessmentReportVO;

/**
 * Assessment report service.
 */
public interface AssessmentReportService extends IService<AssessmentReport> {

    AssessmentReportVO getUserReport(Long userId, Long recordId);

    AssessmentReportVO generateUserReport(Long userId, AssessmentReportGenerateRequest generateRequest);

    AssessmentReportVO getReportVO(AssessmentReport assessmentReport);
}
