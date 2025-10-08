package com.mq.mqaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mq.mqaiagent.model.dto.ranking.ExerciseStats;
import com.mq.mqaiagent.model.entity.ExerciseLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 运动记录数据库操作
 */
public interface ExerciseLogMapper extends BaseMapper<ExerciseLog> {

    /**
     * 统计用户在指定周期内的运动数据（周榜）
     */
    ExerciseStats sumStatsByUserIdAndWeek(@Param("userId") Long userId, 
                                          @Param("weekStartDate") LocalDate weekStartDate);

    /**
     * 统计用户在指定周期内的运动数据（月榜）
     */
    ExerciseStats sumStatsByUserIdAndMonth(@Param("userId") Long userId, 
                                           @Param("monthStartDate") LocalDate monthStartDate);

    /**
     * 获取用户的运动类型列表（去重）
     */
    List<String> getDistinctExerciseTypes(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("limit") Integer limit);

    /**
     * 统计用户在指定周期内的运动记录数（周榜）
     */
    int countByUserIdAndWeek(@Param("userId") Long userId, 
                            @Param("weekStartDate") LocalDate weekStartDate);

    /**
     * 统计用户在指定周期内的运动记录数（月榜）
     */
    int countByUserIdAndMonth(@Param("userId") Long userId, 
                             @Param("monthStartDate") LocalDate monthStartDate);
}
