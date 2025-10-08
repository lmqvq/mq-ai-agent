package com.mq.mqaiagent.model.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 排行榜列表响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingListResponse implements Serializable {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页条数
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 排行榜记录列表
     */
    private List<RankingRecord> records;
    
    /**
     * 统计信息
     */
    private StatisticInfo statisticInfo;
    
    private static final long serialVersionUID = 1L;
}
