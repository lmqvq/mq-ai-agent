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
 * Assessment rule entity.
 */
@Data
@TableName(value = "assessment_rule")
public class AssessmentRule implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schemeCode;

    private String itemCode;

    private String gender;

    private String gradeGroup;

    private BigDecimal score;

    private BigDecimal minValue;

    @TableField("maxValue")
    private BigDecimal maxValue;

    private String comparisonType;

    private String ruleVersion;

    private Integer sortOrder;

    private String description;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
