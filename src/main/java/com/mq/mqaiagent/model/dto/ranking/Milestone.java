package com.mq.mqaiagent.model.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 里程碑DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Milestone implements Serializable {
    
    /**
     * 类型：top100, top50, top10等
     */
    private String type;
    
    /**
     * 是否已达成
     */
    private Boolean achieved;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 图标
     */
    private String icon;
    
    private static final long serialVersionUID = 1L;
}
