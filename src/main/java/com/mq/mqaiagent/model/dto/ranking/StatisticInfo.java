package com.mq.mqaiagent.model.dto.ranking;

import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜统计信息DTO
 */
@Data
public class StatisticInfo implements Serializable {
    
    /**
     * 上榜总人数
     */
    private Integer totalUsers;
    
    /**
     * 平均分数
     */
    private Double averageScore;
    
    /**
     * 最高分数
     */
    private Integer topScore;
    
    /**
     * 数据更新时间
     */
    private String updateTime;
    
    private static final long serialVersionUID = 1L;
}
