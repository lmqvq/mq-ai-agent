package com.mq.mqaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mq.mqaiagent.model.entity.KnowledgeProgram;
import com.mq.mqaiagent.model.vo.KnowledgeProgramVO;

/**
* @author MQ
* @description 针对表【knowledge_program(训练计划表)】的数据库操作Service
* @createDate 2025-11-07 00:31:50
*/
public interface KnowledgeProgramService extends IService<KnowledgeProgram> {

    /**
     * 获取训练计划VO对象
     *
     * @param knowledgeProgram 训练计划实体
     * @return VO对象
     */
    KnowledgeProgramVO getKnowledgeProgramVO(KnowledgeProgram knowledgeProgram);
}
