package com.mq.mqaiagent.assessment.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AssessmentRuleMappingTest {

    @Test
    void shouldQuoteMaxValueColumnName() throws NoSuchFieldException {
        Field field = AssessmentRule.class.getDeclaredField("maxValue");
        TableField tableField = field.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals("`maxValue`", tableField.value());
    }
}
