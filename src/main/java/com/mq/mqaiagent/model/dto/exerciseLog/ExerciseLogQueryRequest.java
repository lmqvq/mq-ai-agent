package com.mq.mqaiagent.model.dto.exerciseLog;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 运动记录查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExerciseLogQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 运动类型
     */
    private String exerciseType;

    /**
     * 开始日期
     */
    private Date startDate;

    /**
     * 结束日期
     */
    private Date endDate;

    private static final long serialVersionUID = 1L;
}
