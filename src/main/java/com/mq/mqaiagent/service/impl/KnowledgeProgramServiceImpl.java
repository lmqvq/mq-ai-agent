package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.mapper.KnowledgeProgramMapper;
import com.mq.mqaiagent.model.entity.KnowledgeProgram;
import com.mq.mqaiagent.model.vo.KnowledgeProgramVO;
import com.mq.mqaiagent.service.KnowledgeProgramService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author MQ
* @description 针对表【knowledge_program(训练计划表)】的数据库操作Service实现
* @createDate 2025-11-07 00:31:50
*/
@Service
public class KnowledgeProgramServiceImpl extends ServiceImpl<KnowledgeProgramMapper, KnowledgeProgram>
    implements KnowledgeProgramService{

    @Override
    public KnowledgeProgramVO getKnowledgeProgramVO(KnowledgeProgram knowledgeProgram) {
        if (knowledgeProgram == null) {
            return null;
        }
        KnowledgeProgramVO vo = new KnowledgeProgramVO();
        BeanUtils.copyProperties(knowledgeProgram, vo);
        return vo;
    }
}




