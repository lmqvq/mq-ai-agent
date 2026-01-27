package com.mq.mqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.mapper.KnowledgeNutrientMapper;
import com.mq.mqaiagent.model.entity.KnowledgeNutrient;
import com.mq.mqaiagent.model.vo.KnowledgeNutrientVO;
import com.mq.mqaiagent.service.KnowledgeNutrientService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author MQ
* @description 针对表【knowledge_nutrient(营养知识表)】的数据库操作Service实现
* @createDate 2025-11-07 00:31:50
*/
@Service
public class KnowledgeNutrientServiceImpl extends ServiceImpl<KnowledgeNutrientMapper, KnowledgeNutrient>
    implements KnowledgeNutrientService{

    @Override
    public KnowledgeNutrientVO getKnowledgeNutrientVO(KnowledgeNutrient knowledgeNutrient) {
        if (knowledgeNutrient == null) {
            return null;
        }
        KnowledgeNutrientVO vo = new KnowledgeNutrientVO();
        BeanUtils.copyProperties(knowledgeNutrient, vo);
        return vo;
    }
}




