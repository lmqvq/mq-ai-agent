package com.mq.mqaiagent.model.dto.knowledge;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 饮食计划查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeMealQueryRequest extends PageRequest implements Serializable {

    /**
     * 餐食类型（早餐/午餐/晚餐/加餐/训练前/训练后）
     */
    private String mealType;

    /**
     * 搜索关键词（名称或描述）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}
