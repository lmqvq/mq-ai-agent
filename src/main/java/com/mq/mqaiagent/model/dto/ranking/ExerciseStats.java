package com.mq.mqaiagent.model.dto.ranking;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 运动统计DTO
 */
@Data
public class ExerciseStats implements Serializable {
    
    /**
     * 总运动时长（分钟）
     */
    private Integer totalMinutes;
    
    /**
     * 总消耗卡路里
     */
    private Float totalCalories;
    
    /**
     * 运动次数
     */
    private Integer exerciseCount;
    
    /**
     * 运动类型列表
     */
    private List<String> exerciseTypes;
    
    private static final long serialVersionUID = 1L;
}
