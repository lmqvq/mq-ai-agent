package com.mq.mqaiagent.service;

import com.mq.mqaiagent.model.dto.ranking.MyRankingResponse;
import com.mq.mqaiagent.model.dto.ranking.RankingListResponse;

import java.time.LocalDate;
import java.util.Date;

/**
 * 排行榜服务接口
 */
public interface RankingService {

    /**
     * 添加运动记录后更新排行榜
     *
     * @param userId 用户ID
     * @param dateRecorded 记录时间
     * @param weekStartDate 周起始日期
     * @param monthStartDate 月起始日期
     */
    void updateRankingAfterAdd(Long userId, Date dateRecorded, 
                              LocalDate weekStartDate, LocalDate monthStartDate);

    /**
     * 删除运动记录后更新排行榜
     *
     * @param userId 用户ID
     * @param weekStartDate 周起始日期
     * @param monthStartDate 月起始日期
     */
    void updateRankingAfterDelete(Long userId, Date weekStartDate, Date monthStartDate);

    /**
     * 获取排行榜列表
     *
     * @param rankingType 排行类型（week/month）
     * @param current 当前页码
     * @param pageSize 每页条数
     * @return 排行榜列表
     */
    RankingListResponse getRankingList(String rankingType, Integer current, Integer pageSize);

    /**
     * 获取我的排名
     *
     * @param userId 用户ID
     * @param rankingType 排行类型（week/month）
     * @return 我的排名信息
     */
    MyRankingResponse getMyRanking(Long userId, String rankingType);

    /**
     * 刷新排行榜缓存（管理员接口）
     *
     * @param rankingType 排行类型（week/month）
     * @return 是否刷新成功
     */
    boolean refreshRanking(String rankingType);
}
