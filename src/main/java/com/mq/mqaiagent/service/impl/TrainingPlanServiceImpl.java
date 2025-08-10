package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.mapper.TrainingPlanMapper;
import com.mq.mqaiagent.model.dto.trainingPlan.TrainingPlanQueryRequest;
import com.mq.mqaiagent.model.entity.TrainingPlan;
import com.mq.mqaiagent.model.vo.TrainingPlanVO;
import com.mq.mqaiagent.service.TrainingPlanService;
import com.mq.mqaiagent.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练计划服务实现
 */
@Service
@Slf4j
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {

    @Override
    public void validTrainingPlan(TrainingPlan trainingPlan, boolean add) {
        if (trainingPlan == null) {
            throw new RuntimeException("训练计划不能为空");
        }
        String planName = trainingPlan.getPlanName();
        String planDetails = trainingPlan.getPlanDetails();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(planName, planDetails), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(planName) && planName.length() > 256) {
            throw new RuntimeException("计划名称过长");
        }
        if (StringUtils.isNotBlank(planDetails) && planDetails.length() > 8192) {
            throw new RuntimeException("计划详情过长");
        }
        String planType = trainingPlan.getPlanType();
        if (StringUtils.isNotBlank(planType) && planType.length() > 255) {
            throw new RuntimeException("计划类型过长");
        }
    }

    @Override
    public QueryWrapper<TrainingPlan> getQueryWrapper(TrainingPlanQueryRequest trainingPlanQueryRequest) {
        QueryWrapper<TrainingPlan> queryWrapper = new QueryWrapper<>();
        if (trainingPlanQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = trainingPlanQueryRequest.getUserId();
        String planName = trainingPlanQueryRequest.getPlanName();
        String planType = trainingPlanQueryRequest.getPlanType();
        Integer isDefault = trainingPlanQueryRequest.getIsDefault();
        String sortField = trainingPlanQueryRequest.getSortField();
        String sortOrder = trainingPlanQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(planName), "planName", planName);
        queryWrapper.eq(StringUtils.isNotBlank(planType), "planType", planType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isDefault), "isDefault", isDefault);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public TrainingPlanVO getTrainingPlanVO(TrainingPlan trainingPlan) {
        if (trainingPlan == null) {
            return null;
        }
        TrainingPlanVO trainingPlanVO = new TrainingPlanVO();
        BeanUtils.copyProperties(trainingPlan, trainingPlanVO);
        return trainingPlanVO;
    }

    @Override
    public Page<TrainingPlanVO> getTrainingPlanVOPage(Page<TrainingPlan> trainingPlanPage) {
        List<TrainingPlan> trainingPlanList = trainingPlanPage.getRecords();
        Page<TrainingPlanVO> trainingPlanVOPage = new Page<>(trainingPlanPage.getCurrent(), trainingPlanPage.getSize(), trainingPlanPage.getTotal());
        if (trainingPlanList.isEmpty()) {
            return trainingPlanVOPage;
        }
        List<TrainingPlanVO> trainingPlanVOList = trainingPlanList.stream()
                .map(this::getTrainingPlanVO)
                .collect(Collectors.toList());
        trainingPlanVOPage.setRecords(trainingPlanVOList);
        return trainingPlanVOPage;
    }

    @Override
    public List<TrainingPlanVO> getUserTrainingPlans(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        QueryWrapper<TrainingPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .orderByDesc("createTime");
        
        List<TrainingPlan> trainingPlanList = this.list(queryWrapper);
        return trainingPlanList.stream()
                .map(this::getTrainingPlanVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrainingPlanVO> getDefaultTrainingPlans() {
        QueryWrapper<TrainingPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDefault", 1)
                .orderByDesc("createTime");
        
        List<TrainingPlan> trainingPlanList = this.list(queryWrapper);
        return trainingPlanList.stream()
                .map(this::getTrainingPlanVO)
                .collect(Collectors.toList());
    }
}
