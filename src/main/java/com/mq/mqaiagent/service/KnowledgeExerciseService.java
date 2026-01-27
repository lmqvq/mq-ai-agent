package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.entity.KnowledgeExercise;
import com.mq.mqaiagent.model.vo.KnowledgeExerciseVO;

/**
* @author MQ
* @description 针对表【knowledge_exercise(健身动作指导表)】的数据库操作Service
* @createDate 2025-11-07 00:31:50
*/
public interface KnowledgeExerciseService extends IService<KnowledgeExercise> {

    /**
     * 获取动作指导VO对象
     *
     * @param knowledgeExercise 动作指导实体
     * @return VO对象
     */
    KnowledgeExerciseVO getKnowledgeExerciseVO(KnowledgeExercise knowledgeExercise);
}
