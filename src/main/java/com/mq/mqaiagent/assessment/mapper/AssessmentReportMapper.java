package com.mq.mqaiagent.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mq.mqaiagent.assessment.model.entity.AssessmentReport;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * Assessment report mapper.
 */
public interface AssessmentReportMapper extends BaseMapper<AssessmentReport> {

    @Delete("DELETE FROM assessment_report WHERE recordId = #{recordId}")
    int deleteByRecordIdForce(@Param("recordId") Long recordId);
}
