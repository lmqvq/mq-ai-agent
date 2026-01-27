package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.mapper.KnowledgeExerciseMapper;
import com.mq.mqaiagent.model.entity.KnowledgeExercise;
import com.mq.mqaiagent.model.vo.KnowledgeExerciseVO;
import com.mq.mqaiagent.service.KnowledgeExerciseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author MQ
* @description 针对表【knowledge_exercise(健身动作指导表)】的数据库操作Service实现
* @createDate 2025-11-07 00:31:50
*/
@Service
public class KnowledgeExerciseServiceImpl extends ServiceImpl<KnowledgeExerciseMapper, KnowledgeExercise>
    implements KnowledgeExerciseService{

    @Override
    public KnowledgeExerciseVO getKnowledgeExerciseVO(KnowledgeExercise knowledgeExercise) {
        if (knowledgeExercise == null) {
            return null;
        }
        KnowledgeExerciseVO vo = new KnowledgeExerciseVO();
        BeanUtils.copyProperties(knowledgeExercise, vo);
        return vo;
    }
}




