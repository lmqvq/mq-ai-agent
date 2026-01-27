package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.entity.KnowledgeBasics;
import com.mq.mqaiagent.model.vo.KnowledgeBasicsVO;

/**
* @author MQ
* @description 针对表【knowledge_basics(健身基础知识表)】的数据库操作Service
* @createDate 2025-11-07 00:31:50
*/
public interface KnowledgeBasicsService extends IService<KnowledgeBasics> {

    /**
     * 获取基础知识VO对象
     *
     * @param knowledgeBasics 基础知识实体
     * @return VO对象
     */
    KnowledgeBasicsVO getKnowledgeBasicsVO(KnowledgeBasics knowledgeBasics);
}
