package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.dto.FitnessDataQueryRequest;
import com.mq.mqaiagent.model.entity.FitnessData;
import com.mq.mqaiagent.model.vo.FitnessDataVO;

import java.util.List;

/**
 * 健身数据服务
 */
public interface FitnessDataService extends IService<FitnessData> {

    /**
     * 校验数据
     *
     * @param fitnessData
     * @param add      对创建的数据进行校验
     */
    void validFitnessData(FitnessData fitnessData, boolean add);

    /**
     * 获取查询条件
     *
     * @param fitnessDataQueryRequest
     * @return
     */
    QueryWrapper<FitnessData> getQueryWrapper(FitnessDataQueryRequest fitnessDataQueryRequest);
    
    /**
     * 获取健身数据封装
     *
     * @param fitnessData
     * @return
     */
    FitnessDataVO getFitnessDataVO(FitnessData fitnessData);

    /**
     * 分页获取健身数据封装
     *
     * @param fitnessDataPage
     * @return
     */
    Page<FitnessDataVO> getFitnessDataVOPage(Page<FitnessData> fitnessDataPage);

    /**
     * 计算BMI
     *
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @return BMI值
     */
    Float calculateBMI(Float weight, Float height);

    /**
     * 获取用户健身数据趋势
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 健身数据列表
     */
    List<FitnessDataVO> getFitnessTrends(Long userId, Integer days);
}
