package com.mq.mqaiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身数据实体类
 * @TableName fitness_data
 */
@TableName(value = "fitness_data")
@Data
public class FitnessData implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 体重(kg)
     */
    private Float weight;

    /**
     * 体脂率(%)
     */
    private Float bodyFat;

    /**
     * 身高(cm)
     */
    private Float height;

    /**
     * BMI指数
     */
    private Float bmi;

    /**
     * 数据记录时间
     */
    private Date dateRecorded;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
