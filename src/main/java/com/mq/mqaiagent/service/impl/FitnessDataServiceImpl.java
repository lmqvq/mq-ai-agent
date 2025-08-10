package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.mapper.FitnessDataMapper;
import com.mq.mqaiagent.model.dto.FitnessDataQueryRequest;
import com.mq.mqaiagent.model.entity.FitnessData;
import com.mq.mqaiagent.model.vo.FitnessDataVO;
import com.mq.mqaiagent.service.FitnessDataService;
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
 * 健身数据服务实现
 */
@Service
@Slf4j
public class FitnessDataServiceImpl extends ServiceImpl<FitnessDataMapper, FitnessData> implements FitnessDataService {

    @Override
    public void validFitnessData(FitnessData fitnessData, boolean add) {
        if (fitnessData == null) {
            throw new RuntimeException("健身数据不能为空");
        }
        Float weight = fitnessData.getWeight();
        Float height = fitnessData.getHeight();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(ObjectUtils.anyNull(weight, height), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (weight != null && (weight <= 0 || weight > 500)) {
            throw new RuntimeException("体重数据不合理");
        }
        if (height != null && (height <= 0 || height > 300)) {
            throw new RuntimeException("身高数据不合理");
        }
        Float bodyFat = fitnessData.getBodyFat();
        if (bodyFat != null && (bodyFat < 0 || bodyFat > 100)) {
            throw new RuntimeException("体脂率数据不合理");
        }
    }

    @Override
    public QueryWrapper<FitnessData> getQueryWrapper(FitnessDataQueryRequest fitnessDataQueryRequest) {
        QueryWrapper<FitnessData> queryWrapper = new QueryWrapper<>();
        if (fitnessDataQueryRequest == null) {
            return queryWrapper;
        }
        Long userId = fitnessDataQueryRequest.getUserId();
        Date startDate = fitnessDataQueryRequest.getStartDate();
        Date endDate = fitnessDataQueryRequest.getEndDate();
        String sortField = fitnessDataQueryRequest.getSortField();
        String sortOrder = fitnessDataQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.ge(ObjectUtils.isNotEmpty(startDate), "dateRecorded", startDate);
        queryWrapper.le(ObjectUtils.isNotEmpty(endDate), "dateRecorded", endDate);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public FitnessDataVO getFitnessDataVO(FitnessData fitnessData) {
        if (fitnessData == null) {
            return null;
        }
        FitnessDataVO fitnessDataVO = new FitnessDataVO();
        BeanUtils.copyProperties(fitnessData, fitnessDataVO);
        return fitnessDataVO;
    }

    @Override
    public Page<FitnessDataVO> getFitnessDataVOPage(Page<FitnessData> fitnessDataPage) {
        List<FitnessData> fitnessDataList = fitnessDataPage.getRecords();
        Page<FitnessDataVO> fitnessDataVOPage = new Page<>(fitnessDataPage.getCurrent(), fitnessDataPage.getSize(), fitnessDataPage.getTotal());
        if (fitnessDataList.isEmpty()) {
            return fitnessDataVOPage;
        }
        List<FitnessDataVO> fitnessDataVOList = fitnessDataList.stream()
                .map(this::getFitnessDataVO)
                .collect(Collectors.toList());
        fitnessDataVOPage.setRecords(fitnessDataVOList);
        return fitnessDataVOPage;
    }

    @Override
    public Float calculateBMI(Float weight, Float height) {
        if (weight == null || height == null || weight <= 0 || height <= 0) {
            return null;
        }
        // 身高转换为米
        float heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    @Override
    public List<FitnessDataVO> getFitnessTrends(Long userId, Integer days) {
        if (userId == null || days == null || days <= 0) {
            return List.of();
        }
        
        // 计算开始日期
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days);
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        
        QueryWrapper<FitnessData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId)
                .ge("dateRecorded", startDate)
                .orderByAsc("dateRecorded");
        
        List<FitnessData> fitnessDataList = this.list(queryWrapper);
        return fitnessDataList.stream()
                .map(this::getFitnessDataVO)
                .collect(Collectors.toList());
    }
}
