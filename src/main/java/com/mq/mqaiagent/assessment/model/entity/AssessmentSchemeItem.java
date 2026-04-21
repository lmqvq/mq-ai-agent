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
 * Assessment scheme item entity.
 */
@Data
@TableName(value = "assessment_scheme_item")
public class AssessmentSchemeItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schemeCode;

    private String itemCode;

    private String itemName;

    private String itemCategory;

    private String unit;

    private String inputType;

    private Integer inputPrecision;

    private BigDecimal weight;

    private Integer displayOrder;

    private String applicableGender;

    private String applicableGradeGroup;

    private BigDecimal validationMin;

    private BigDecimal validationMax;

    private Integer isRequired;

    private Integer isBonusItem;

    private String description;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
