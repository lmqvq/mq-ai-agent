package com.mq.mqaiagent.model.dto.knowledge;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 健身动作指导查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KnowledgeExerciseQueryRequest extends PageRequest implements Serializable {

    /**
     * 分类（胸部训练/背部训练/腿部训练等）
     */
    private String category;

    /**
     * 难度级别（初级/中级/高级）
     */
    private String difficulty;

    /**
     * 目标肌群
     */
    private String muscleGroup;

    /**
     * 搜索关键词（动作名称或描述）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}

