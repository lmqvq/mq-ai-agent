package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.dto.ExerciseLogQueryRequest;
import com.mq.mqaiagent.model.entity.ExerciseLog;
import com.mq.mqaiagent.model.vo.ExerciseLogVO;

import java.util.List;

/**
 * 运动记录服务
 */
public interface ExerciseLogService extends IService<ExerciseLog> {

    /**
     * 校验数据
     *
     * @param exerciseLog
     * @param add      对创建的数据进行校验
     */
    void validExerciseLog(ExerciseLog exerciseLog, boolean add);

    /**
     * 获取查询条件
     *
     * @param exerciseLogQueryRequest
     * @return
     */
    QueryWrapper<ExerciseLog> getQueryWrapper(ExerciseLogQueryRequest exerciseLogQueryRequest);
    
    /**
     * 获取运动记录封装
     *
     * @param exerciseLog
     * @return
     */
    ExerciseLogVO getExerciseLogVO(ExerciseLog exerciseLog);

    /**
     * 分页获取运动记录封装
     *
     * @param exerciseLogPage
     * @return
     */
    Page<ExerciseLogVO> getExerciseLogVOPage(Page<ExerciseLog> exerciseLogPage);

    /**
     * 获取用户运动统计
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 运动记录列表
     */
    List<ExerciseLogVO> getExerciseStats(Long userId, Integer days);

    /**
     * 计算总卡路里消耗
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 总卡路里消耗
     */
    Float getTotalCaloriesBurned(Long userId, Integer days);
}
