package com.mq.mqaiagent.assessment.util;

import com.mq.mqaiagent.assessment.model.vo.AssessmentAiSuggestionStructuredVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AssessmentAiSuggestionParserTest {

    private static final String OVERALL = "整体判断";
    private static final String TRAINING = "训练建议";
    private static final String WEEKLY = "每周安排";
    private static final String RECOVERY = "恢复与营养";
    private static final String PRECAUTIONS = "注意事项";
    private static final String NEXT_GOAL = "下一次体测前目标";

    @Test
    void parseShouldExtractSixSections() {
        String text = """
                整体判断
                当前总分 80 分，整体状态良好，但耐力还有提升空间。

                训练建议
                优先提升耐力跑和核心力量，每周安排 2 次专项训练。

                每周安排
                周一力量、周三间歇跑、周五核心、周末恢复跑。

                恢复与营养
                保证 7 小时以上睡眠，训练后补充蛋白质和碳水。

                注意事项
                避免连续高强度训练，跑前充分热身。

                下一次体测前目标
                将 800 米成绩提升 10 秒，核心项目保持稳定。
                """;

        AssessmentAiSuggestionStructuredVO structuredVO = AssessmentAiSuggestionParser.parse(text);

        Assertions.assertTrue(structuredVO.getStructured());
        Assertions.assertEquals(6, structuredVO.getSectionCount());
        Assertions.assertEquals("当前总分 80 分，整体状态良好，但耐力还有提升空间。", structuredVO.getOverallJudgment());
        Assertions.assertTrue(structuredVO.getWeeklyPlan().contains("周一力量"));
        Assertions.assertTrue(structuredVO.getNextAssessmentGoal().contains("800"));
    }

    @Test
    void parseShouldSupportMarkdownHeadingStyle() {
        String text = """
                **整体判断**
                目前体重控制良好。
                ### 训练建议
                继续保持有氧与力量结合。
                - 每周安排
                每周 4 天训练。
                > 恢复与营养
                睡眠不少于 7 小时。
                注意事项：注意膝踝负荷。
                下次体测前目标：50 米稳定在 7.2 秒内。
                """;

        AssessmentAiSuggestionStructuredVO structuredVO = AssessmentAiSuggestionParser.parse(text);

        Assertions.assertTrue(structuredVO.getStructured());
        Assertions.assertEquals(6, structuredVO.getSectionCount());
        Assertions.assertEquals("目前体重控制良好。", structuredVO.getOverallJudgment());
        Assertions.assertEquals("注意膝踝负荷。", structuredVO.getPrecautions());
        Assertions.assertEquals("50 米稳定在 7.2 秒内。", structuredVO.getNextAssessmentGoal());
    }
}
