package com.mq.mqaiagent.model.dto.knowledge;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 营养知识查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeNutrientQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键词（营养素名称或描述）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}

