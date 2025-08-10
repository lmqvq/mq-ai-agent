package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.dto.TrainingPlanQueryRequest;
import com.mq.mqaiagent.model.entity.TrainingPlan;
import com.mq.mqaiagent.model.vo.TrainingPlanVO;

import java.util.List;

/**
 * 训练计划服务
 */
public interface TrainingPlanService extends IService<TrainingPlan> {

    /**
     * 校验数据
     *
     * @param trainingPlan
     * @param add      对创建的数据进行校验
     */
    void validTrainingPlan(TrainingPlan trainingPlan, boolean add);

    /**
     * 获取查询条件
     *
     * @param trainingPlanQueryRequest
     * @return
     */
    QueryWrapper<TrainingPlan> getQueryWrapper(TrainingPlanQueryRequest trainingPlanQueryRequest);
    
    /**
     * 获取训练计划封装
     *
     * @param trainingPlan
     * @return
     */
    TrainingPlanVO getTrainingPlanVO(TrainingPlan trainingPlan);

    /**
     * 分页获取训练计划封装
     *
     * @param trainingPlanPage
     * @return
     */
    Page<TrainingPlanVO> getTrainingPlanVOPage(Page<TrainingPlan> trainingPlanPage);

    /**
     * 获取用户的训练计划
     *
     * @param userId 用户ID
     * @return 训练计划列表
     */
    List<TrainingPlanVO> getUserTrainingPlans(Long userId);

    /**
     * 获取默认训练计划
     *
     * @return 默认训练计划列表
     */
    List<TrainingPlanVO> getDefaultTrainingPlans();
}
