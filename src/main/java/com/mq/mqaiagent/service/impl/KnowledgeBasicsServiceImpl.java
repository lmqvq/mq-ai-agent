package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.mapper.KnowledgeBasicsMapper;
import com.mq.mqaiagent.model.entity.KnowledgeBasics;
import com.mq.mqaiagent.model.vo.KnowledgeBasicsVO;
import com.mq.mqaiagent.service.KnowledgeBasicsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author MQ
* @description 针对表【knowledge_basics(健身基础知识表)】的数据库操作Service实现
* @createDate 2025-11-07 00:31:50
*/
@Service
public class KnowledgeBasicsServiceImpl extends ServiceImpl<KnowledgeBasicsMapper, KnowledgeBasics>
    implements KnowledgeBasicsService{

    @Override
    public KnowledgeBasicsVO getKnowledgeBasicsVO(KnowledgeBasics knowledgeBasics) {
        if (knowledgeBasics == null) {
            return null;
        }
        KnowledgeBasicsVO vo = new KnowledgeBasicsVO();
        BeanUtils.copyProperties(knowledgeBasics, vo);
        return vo;
    }
}




