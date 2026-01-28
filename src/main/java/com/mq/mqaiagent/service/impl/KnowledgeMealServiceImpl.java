package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.mapper.KnowledgeMealMapper;
import com.mq.mqaiagent.model.entity.KnowledgeMeal;
import com.mq.mqaiagent.model.vo.KnowledgeMealVO;
import com.mq.mqaiagent.service.KnowledgeMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author MQ
 * @description 针对表【knowledge_meal(饮食计划表)】的数据库操作Service实现
 */
@Service
public class KnowledgeMealServiceImpl extends ServiceImpl<KnowledgeMealMapper, KnowledgeMeal>
        implements KnowledgeMealService {

    @Override
    public KnowledgeMealVO getKnowledgeMealVO(KnowledgeMeal knowledgeMeal) {
        if (knowledgeMeal == null) {
            return null;
        }
        KnowledgeMealVO vo = new KnowledgeMealVO();
        BeanUtils.copyProperties(knowledgeMeal, vo);
        return vo;
    }
}
