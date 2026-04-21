package com.mq.mqaiagent.assessment.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Assessment scheme entity.
 */
@Data
@TableName(value = "assessment_scheme")
public class AssessmentScheme implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String schemeCode;

    private String schemeName;

    private String sceneType;

    private String description;

    private String version;

    private String source;

    private String configJson;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
