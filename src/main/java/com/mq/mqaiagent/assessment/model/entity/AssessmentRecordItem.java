package com.mq.mqaiagent.assessment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Assessment record item entity.
 */
@Data
@TableName(value = "assessment_record_item")
public class AssessmentRecordItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String itemCode;

    private String itemName;

    private Integer itemOrder;

    private String unit;

    private BigDecimal rawValue;

    private BigDecimal itemWeight;

    private BigDecimal itemScore;

    private BigDecimal extraScore;

    private String itemLevel;

    private Integer isWeakness;

    private Integer isStrength;

    private String remark;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
