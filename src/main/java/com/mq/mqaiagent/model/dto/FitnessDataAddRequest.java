package com.mq.mqaiagent.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 健身数据添加请求
 */
@Data
public class FitnessDataAddRequest implements Serializable {

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
     * 数据记录时间
     */
    private Date dateRecorded;

    private static final long serialVersionUID = 1L;
}
