package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 排行榜快照实体类
 * @TableName ranking_snapshot
 */
@TableName(value = "ranking_snapshot")
@Data
public class RankingSnapshot implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 排行类型：week-周榜, month-月榜
     */
    private String rankingType;

    /**
     * 统计开始日期
     */
    private Date startDate;

    /**
     * 运动记录次数
     */
    private Integer exerciseCount;

    /**
     * 首次记录时间戳（秒）
     */
    private Long firstRecordTime;

    /**
     * 排名
     */
    private Integer rankPosition;

    /**
     * 总运动时长（分钟）
     */
    private Integer totalMinutes;

    /**
     * 总消耗卡路里
     */
    private Float totalCalories;

    /**
     * 快照时间
     */
    private Date snapshotTime;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
