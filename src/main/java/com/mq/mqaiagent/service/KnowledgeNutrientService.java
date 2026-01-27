package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.entity.KnowledgeNutrient;
import com.mq.mqaiagent.model.vo.KnowledgeNutrientVO;

/**
* @author MQ
* @description 针对表【knowledge_nutrient(营养知识表)】的数据库操作Service
* @createDate 2025-11-07 00:31:50
*/
public interface KnowledgeNutrientService extends IService<KnowledgeNutrient> {

    /**
     * 获取营养知识VO对象
     *
     * @param knowledgeNutrient 营养知识实体
     * @return VO对象
     */
    KnowledgeNutrientVO getKnowledgeNutrientVO(KnowledgeNutrient knowledgeNutrient);
}
