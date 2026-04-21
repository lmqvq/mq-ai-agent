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
 * Assessment report entity.
 */
@Data
@TableName(value = "assessment_report")
public class AssessmentReport implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String version;

    private String overview;

    private String analysisJson;

    private String weaknessSummary;

    private String strengthSummary;

    private String trainingFocus;

    private String riskNotes;

    private String aiSuggestion;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
