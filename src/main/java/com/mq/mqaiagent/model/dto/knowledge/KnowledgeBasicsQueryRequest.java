package com.mq.mqaiagent.model.dto.knowledge;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 健身基础知识查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeBasicsQueryRequest extends PageRequest implements Serializable {

    /**
     * 难度级别（初级/中级/高级）
     */
    private String difficulty;

    /**
     * 搜索关键词（标题或描述）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}

