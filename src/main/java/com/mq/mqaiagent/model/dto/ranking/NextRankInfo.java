package com.mq.mqaiagent.model.dto.ranking;

import lombok.Data;

import java.io.Serializable;

/**
 * 下一名信息DTO
 */
@Data
public class NextRankInfo implements Serializable {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 分数
     */
    private Integer score;
    
    /**
     * 分数差距
     */
    private Integer scoreDiff;
    
    private static final long serialVersionUID = 1L;
}
