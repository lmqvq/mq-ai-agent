package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.mapper.FitnessGoalMapper;
import com.mq.mqaiagent.model.dto.FitnessGoalQueryRequest;
import com.mq.mqaiagent.model.entity.FitnessGoal;
import com.mq.mqaiagent.model.vo.FitnessGoalVO;
import com.mq.mqaiagent.service.FitnessGoalService;
import com.mq.mqaiagent.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健身目标服务实现
 */
@Service
@Slf4j
public class FitnessGoalServiceImpl extends ServiceImpl<FitnessGoalMapper, FitnessGoal> implements FitnessGoalService {

    @Override
    public void validFitnessGoal(FitnessGoal fitnessGoal, boolean add) {
        if (fitnessGoal == null) {
            throw new RuntimeException("健身目标不能为空");
        }
        String goalType = fitnessGoal.getGoalType();
        String targetValue = fitnessGoal.getTargetValue();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goalType, targetValue), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(goalType) && goalType.length() > 255) {
            throw new RuntimeException("目标类型过长");
        }
        if (StringUtils.isNotBlank(targetValue) && targetValue.length() > 255) {
            throw new RuntimeException("目标值过长");
        }
        String progress = fitnessGoal.getProgress();
        if (StringUtils.isNotBlank(progress) && progress.length() > 8192) {
            throw new RuntimeException("进度记录过长");
        }
    }

    @Override
    public QueryWrapper<FitnessGoal> getQueryWrapper(FitnessGoalQueryRequest fitnessGoalQueryRequest) {
        QueryWrapper<FitnessGoal> queryWrapper = new QueryWrapper<>();
        if (fitnessGoalQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = fitnessGoalQueryRequest.getUserId();
        String goalType = fitnessGoalQueryRequest.getGoalType();
        Integer isAchieved = fitnessGoalQueryRequest.getIsAchieved();
        String sortField = fitnessGoalQueryRequest.getSortField();
        String sortOrder = fitnessGoalQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(goalType), "goalType", goalType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isAchieved), "isAchieved", isAchieved);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public FitnessGoalVO getFitnessGoalVO(FitnessGoal fitnessGoal) {
        if (fitnessGoal == null) {
            return null;
        }
        FitnessGoalVO fitnessGoalVO = new FitnessGoalVO();
        BeanUtils.copyProperties(fitnessGoal, fitnessGoalVO);
        return fitnessGoalVO;
    }

    @Override
    public Page<FitnessGoalVO> getFitnessGoalVOPage(Page<FitnessGoal> fitnessGoalPage) {
        List<FitnessGoal> fitnessGoalList = fitnessGoalPage.getRecords();
        Page<FitnessGoalVO> fitnessGoalVOPage = new Page<>(fitnessGoalPage.getCurrent(), fitnessGoalPage.getSize(), fitnessGoalPage.getTotal());
        if (fitnessGoalList.isEmpty()) {
            return fitnessGoalVOPage;
        }
        List<FitnessGoalVO> fitnessGoalVOList = fitnessGoalList.stream()
                .map(this::getFitnessGoalVO)
                .collect(Collectors.toList());
        fitnessGoalVOPage.setRecords(fitnessGoalVOList);
        return fitnessGoalVOPage;
    }

    @Override
    public List<FitnessGoalVO> getUserFitnessGoals(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        QueryWrapper<FitnessGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .orderByDesc("createTime");
        
        List<FitnessGoal> fitnessGoalList = this.list(queryWrapper);
        return fitnessGoalList.stream()
                .map(this::getFitnessGoalVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FitnessGoalVO> getActiveFitnessGoals(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        QueryWrapper<FitnessGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .eq("isAchieved", 0)
                .and(wrapper -> wrapper.isNull("endDate").or().gt("endDate", new Date()))
                .orderByDesc("createTime");
        
        List<FitnessGoal> fitnessGoalList = this.list(queryWrapper);
        return fitnessGoalList.stream()
                .map(this::getFitnessGoalVO)
                .collect(Collectors.toList());
    }
}
