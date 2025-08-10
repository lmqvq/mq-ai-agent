package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.dto.FitnessGoalQueryRequest;
import com.mq.mqaiagent.model.entity.FitnessGoal;
import com.mq.mqaiagent.model.vo.FitnessGoalVO;

import java.util.List;

/**
 * 健身目标服务
 */
public interface FitnessGoalService extends IService<FitnessGoal> {

    /**
     * 校验数据
     *
     * @param fitnessGoal
     * @param add      对创建的数据进行校验
     */
    void validFitnessGoal(FitnessGoal fitnessGoal, boolean add);

    /**
     * 获取查询条件
     *
     * @param fitnessGoalQueryRequest
     * @return
     */
    QueryWrapper<FitnessGoal> getQueryWrapper(FitnessGoalQueryRequest fitnessGoalQueryRequest);
    
    /**
     * 获取健身目标封装
     *
     * @param fitnessGoal
     * @return
     */
    FitnessGoalVO getFitnessGoalVO(FitnessGoal fitnessGoal);

    /**
     * 分页获取健身目标封装
     *
     * @param fitnessGoalPage
     * @return
     */
    Page<FitnessGoalVO> getFitnessGoalVOPage(Page<FitnessGoal> fitnessGoalPage);

    /**
     * 获取用户的健身目标
     *
     * @param userId 用户ID
     * @return 健身目标列表
     */
    List<FitnessGoalVO> getUserFitnessGoals(Long userId);

    /**
     * 获取用户进行中的健身目标
     *
     * @param userId 用户ID
     * @return 进行中的健身目标列表
     */
    List<FitnessGoalVO> getActiveFitnessGoals(Long userId);
}
