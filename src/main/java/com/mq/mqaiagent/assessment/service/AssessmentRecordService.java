package com.mq.mqaiagent.assessment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordAddRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordQueryRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordUpdateRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecord;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordDetailVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentTrendVO;

import java.util.List;

/**
 * Assessment record service.
 */
public interface AssessmentRecordService extends IService<AssessmentRecord> {

    Long createRecord(Long userId, AssessmentRecordAddRequest recordAddRequest);

    Long updateRecord(Long userId, AssessmentRecordUpdateRequest recordUpdateRequest);

    boolean deleteRecord(Long userId, Long recordId);

    Page<AssessmentRecordVO> listUserRecordByPage(Long userId, AssessmentRecordQueryRequest recordQueryRequest);

    AssessmentRecordDetailVO getUserRecordDetail(Long userId, Long recordId);

    List<AssessmentTrendVO> getUserTrends(Long userId, String schemeCode, Integer limit);

    AssessmentRecordVO getRecordVO(AssessmentRecord assessmentRecord);
}
