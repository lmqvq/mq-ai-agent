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
 * Assessment record entity.
 */
@Data
@TableName(value = "assessment_record")
public class AssessmentRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String schemeCode;

    private String schemeVersion;

    private Date assessmentDate;

    private String sourceType;

    private String genderSnapshot;

    private String gradeSnapshot;

    private String gradeGroupSnapshot;

    private BigDecimal heightSnapshot;

    private BigDecimal weightSnapshot;

    private BigDecimal bmiSnapshot;

    private BigDecimal totalScore;

    private String level;

    private Integer weaknessCount;

    private Integer strengthCount;

    private String summary;

    private String extraDataJson;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
