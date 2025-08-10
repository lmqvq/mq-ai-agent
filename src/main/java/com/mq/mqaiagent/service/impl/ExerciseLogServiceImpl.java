package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.mapper.ExerciseLogMapper;
import com.mq.mqaiagent.model.dto.ExerciseLogQueryRequest;
import com.mq.mqaiagent.model.entity.ExerciseLog;
import com.mq.mqaiagent.model.vo.ExerciseLogVO;
import com.mq.mqaiagent.service.ExerciseLogService;
import com.mq.mqaiagent.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运动记录服务实现
 */
@Service
@Slf4j
public class ExerciseLogServiceImpl extends ServiceImpl<ExerciseLogMapper, ExerciseLog> implements ExerciseLogService {

    @Override
    public void validExerciseLog(ExerciseLog exerciseLog, boolean add) {
        if (exerciseLog == null) {
            throw new RuntimeException("运动记录不能为空");
        }
        String exerciseType = exerciseLog.getExerciseType();
        Integer duration = exerciseLog.getDuration();
        Float caloriesBurned = exerciseLog.getCaloriesBurned();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(exerciseType) || ObjectUtils.anyNull(duration, caloriesBurned), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(exerciseType) && exerciseType.length() > 256) {
            throw new RuntimeException("运动类型过长");
        }
        if (duration != null && (duration <= 0 || duration > 1440)) { // 最多24小时
            throw new RuntimeException("运动时长不合理");
        }
        if (caloriesBurned != null && (caloriesBurned < 0 || caloriesBurned > 10000)) {
            throw new RuntimeException("卡路里消耗数据不合理");
        }
    }

    @Override
    public QueryWrapper<ExerciseLog> getQueryWrapper(ExerciseLogQueryRequest exerciseLogQueryRequest) {
        QueryWrapper<ExerciseLog> queryWrapper = new QueryWrapper<>();
        if (exerciseLogQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = exerciseLogQueryRequest.getUserId();
        String exerciseType = exerciseLogQueryRequest.getExerciseType();
        Date startDate = exerciseLogQueryRequest.getStartDate();
        Date endDate = exerciseLogQueryRequest.getEndDate();
        String sortField = exerciseLogQueryRequest.getSortField();
        String sortOrder = exerciseLogQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(exerciseType), "exerciseType", exerciseType);
        queryWrapper.ge(ObjectUtils.isNotEmpty(startDate), "dateRecorded", startDate);
        queryWrapper.le(ObjectUtils.isNotEmpty(endDate), "dateRecorded", endDate);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public ExerciseLogVO getExerciseLogVO(ExerciseLog exerciseLog) {
        if (exerciseLog == null) {
            return null;
        }
        ExerciseLogVO exerciseLogVO = new ExerciseLogVO();
        BeanUtils.copyProperties(exerciseLog, exerciseLogVO);
        return exerciseLogVO;
    }

    @Override
    public Page<ExerciseLogVO> getExerciseLogVOPage(Page<ExerciseLog> exerciseLogPage) {
        List<ExerciseLog> exerciseLogList = exerciseLogPage.getRecords();
        Page<ExerciseLogVO> exerciseLogVOPage = new Page<>(exerciseLogPage.getCurrent(), exerciseLogPage.getSize(), exerciseLogPage.getTotal());
        if (exerciseLogList.isEmpty()) {
            return exerciseLogVOPage;
        }
        List<ExerciseLogVO> exerciseLogVOList = exerciseLogList.stream()
                .map(this::getExerciseLogVO)
                .collect(Collectors.toList());
        exerciseLogVOPage.setRecords(exerciseLogVOList);
        return exerciseLogVOPage;
    }

    @Override
    public List<ExerciseLogVO> getExerciseStats(Long userId, Integer days) {
        if (userId == null || days == null || days <= 0) {
            return List.of();
        }
        
        // 计算开始日期
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days);
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        
        QueryWrapper<ExerciseLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .ge("dateRecorded", startDate)
                .orderByDesc("dateRecorded");
        
        List<ExerciseLog> exerciseLogList = this.list(queryWrapper);
        return exerciseLogList.stream()
                .map(this::getExerciseLogVO)
                .collect(Collectors.toList());
    }

    @Override
    public Float getTotalCaloriesBurned(Long userId, Integer days) {
        if (userId == null || days == null || days <= 0) {
            return 0.0f;
        }
        
        // 计算开始日期
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days);
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        
        QueryWrapper<ExerciseLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .ge("dateRecorded", startDate)
                .select("SUM(caloriesBurned) as totalCalories");
        
        List<ExerciseLog> result = this.list(queryWrapper);
        if (result.isEmpty()) {
            return 0.0f;
        }
        
        return result.stream()
                .map(ExerciseLog::getCaloriesBurned)
                .reduce(0.0f, Float::sum);
    }
}
