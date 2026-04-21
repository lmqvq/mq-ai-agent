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
 * Assessment profile entity.
 */
@Data
@TableName(value = "assessment_profile")
public class AssessmentProfile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String schemeCode;

    private String gender;

    private String grade;

    private String gradeGroup;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal bmi;

    private String extraProfileJson;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
