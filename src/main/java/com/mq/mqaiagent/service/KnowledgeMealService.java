package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.entity.KnowledgeMeal;
import com.mq.mqaiagent.model.vo.KnowledgeMealVO;

/**
 * @author MQ
 * @description 针对表【knowledge_meal(饮食计划表)】的数据库操作Service
 */
public interface KnowledgeMealService extends IService<KnowledgeMeal> {

    /**
     * 获取饮食计划VO对象
     *
     * @param knowledgeMeal 饮食计划实体
     * @return VO对象
     */
    KnowledgeMealVO getKnowledgeMealVO(KnowledgeMeal knowledgeMeal);
}
