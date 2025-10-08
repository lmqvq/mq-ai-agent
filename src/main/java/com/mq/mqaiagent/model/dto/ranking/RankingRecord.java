package com.mq.mqaiagent.model.dto.ranking;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 排行榜记录DTO
 */
@Data
public class RankingRecord implements Serializable {
    
    /**
     * 排名（从1开始）
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userName;
    
    /**
     * 用户头像URL
     */
    private String userAvatar;
    
    /**
     * 运动记录次数（分数）
     */
    private Integer score;
    
    /**
     * 首次记录时间
     */
    private String firstRecordTime;
    
    /**
     * 总运动时长（分钟）
     */
    private Integer totalMinutes;
    
    /**
     * 总消耗卡路里
     */
    private Float totalCalories;
    
    /**
     * 运动类型列表
     */
    private List<String> exerciseTypes;
    
    private static final long serialVersionUID = 1L;
}
