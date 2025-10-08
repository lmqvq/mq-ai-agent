package com.mq.mqaiagent.model.dto.ranking;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 我的排名响应DTO
 */
@Data
@Builder
public class MyRankingResponse implements Serializable {
    
    /**
     * 是否上榜
     */
    private Boolean isOnBoard;
    
    /**
     * 我的排名
     */
    private Integer myRank;
    
    /**
     * 我的分数（运动次数）
     */
    private Integer myScore;
    
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
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 超越百分比
     */
    private Double beatPercent;
    
    /**
     * 排名变化
     */
    private Integer rankChange;
    
    /**
     * 排名变化类型：up-上升, down-下降, same-不变
     */
    private String rankChangeType;
    
    /**
     * 下一名信息
     */
    private NextRankInfo nextRankInfo;
    
    /**
     * 里程碑列表
     */
    private List<Milestone> milestones;
    
    /**
     * 鼓励消息（未上榜时显示）
     */
    private String encourageMessage;
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 创建未上榜的响应
     */
    public static MyRankingResponse notOnBoard(Integer totalUsers) {
        return MyRankingResponse.builder()
                .isOnBoard(false)
                .myRank(0)
                .myScore(0)
                .totalUsers(totalUsers)
                .beatPercent(0.0)
                .encourageMessage("开始运动，加入排行榜吧！💪")
                .build();
    }
}
