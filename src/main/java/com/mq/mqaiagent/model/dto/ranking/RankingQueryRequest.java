package com.mq.mqaiagent.model.dto.ranking;

import com.mq.mqaiagent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 排行榜查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RankingQueryRequest extends PageRequest implements Serializable {
    
    /**
     * 排行类型：week-周榜, month-月榜
     */
    private String rankingType;
    
    private static final long serialVersionUID = 1L;
}
