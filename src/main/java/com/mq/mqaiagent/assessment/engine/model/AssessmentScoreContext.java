package com.mq.mqaiagent.assessment.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assessment score context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentScoreContext {

    private String schemeCode;

    private String ruleVersion;

    private String gender;

    private String gradeGroup;

    private List<ItemContext> itemList;

    /**
     * Item context.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemContext {

        private String itemCode;

        private String itemName;

        private BigDecimal rawValue;

        private BigDecimal itemWeight;
    }
}
