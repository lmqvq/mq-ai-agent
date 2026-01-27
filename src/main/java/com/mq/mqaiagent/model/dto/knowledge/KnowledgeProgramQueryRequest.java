package com.mq.mqaiagent.model.dto.knowledge;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 训练计划查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeProgramQueryRequest extends PageRequest implements Serializable {

    /**
     * 类型（beginner/intermediate/advanced/fat-loss等）
     */
    private String type;

    /**
     * 适合级别（初级/中级/高级）
     */
    private String level;

    /**
     * 搜索关键词（计划名称或描述）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}

