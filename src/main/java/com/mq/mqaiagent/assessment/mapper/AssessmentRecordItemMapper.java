package com.mq.mqaiagent.assessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mq.mqaiagent.assessment.model.entity.AssessmentRecordItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * Assessment record item mapper.
 */
public interface AssessmentRecordItemMapper extends BaseMapper<AssessmentRecordItem> {

    @Delete("DELETE FROM assessment_record_item WHERE recordId = #{recordId}")
    int deleteByRecordIdForce(@Param("recordId") Long recordId);
}
