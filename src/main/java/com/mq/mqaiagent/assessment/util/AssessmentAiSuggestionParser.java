package com.mq.mqaiagent.assessment.util;

import com.mq.mqaiagent.assessment.model.vo.AssessmentAiSuggestionSectionVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentAiSuggestionStructuredVO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for converting free-text AI suggestions into structured sections.
 */
public final class AssessmentAiSuggestionParser {

    private static final String HEADING_OVERALL = "整体判断";
    private static final String HEADING_TRAINING = "训练建议";
    private static final String HEADING_WEEKLY = "每周安排";
    private static final String HEADING_RECOVERY = "恢复与营养";
    private static final String HEADING_RECOVERY_ALIAS = "恢复营养";
    private static final String HEADING_PRECAUTIONS = "注意事项";
    private static final String HEADING_NEXT_GOAL = "下一次体测前目标";
    private static final String HEADING_NEXT_GOAL_ALIAS_1 = "下次体测前目标";
    private static final String HEADING_NEXT_GOAL_ALIAS_2 = "下一次体测目标";
    private static final String HEADING_NEXT_GOAL_ALIAS_3 = "下次体测目标";

    private static final Pattern LEADING_DECORATOR_PATTERN = Pattern.compile("^[#>*\\-\\d.()\\s]+");

    private static final Pattern HEADING_PATTERN = Pattern.compile(
            "^(" + Pattern.quote(HEADING_OVERALL)
                    + "|" + Pattern.quote(HEADING_TRAINING)
                    + "|" + Pattern.quote(HEADING_WEEKLY)
                    + "|" + Pattern.quote(HEADING_RECOVERY)
                    + "|" + Pattern.quote(HEADING_RECOVERY_ALIAS)
                    + "|" + Pattern.quote(HEADING_PRECAUTIONS)
                    + "|" + Pattern.quote(HEADING_NEXT_GOAL)
                    + "|" + Pattern.quote(HEADING_NEXT_GOAL_ALIAS_1)
                    + "|" + Pattern.quote(HEADING_NEXT_GOAL_ALIAS_2)
                    + "|" + Pattern.quote(HEADING_NEXT_GOAL_ALIAS_3)
                    + ")\\s*(?:[:：]\\s*(.*))?$");

    private static final List<SectionDefinition> SECTION_DEFINITION_LIST = List.of(
            new SectionDefinition("overallJudgment", HEADING_OVERALL, 1, List.of(HEADING_OVERALL)),
            new SectionDefinition("trainingAdvice", HEADING_TRAINING, 2, List.of(HEADING_TRAINING)),
            new SectionDefinition("weeklyPlan", HEADING_WEEKLY, 3, List.of(HEADING_WEEKLY)),
            new SectionDefinition("recoveryAndNutrition", HEADING_RECOVERY, 4,
                    List.of(HEADING_RECOVERY, HEADING_RECOVERY_ALIAS)),
            new SectionDefinition("precautions", HEADING_PRECAUTIONS, 5, List.of(HEADING_PRECAUTIONS)),
            new SectionDefinition("nextAssessmentGoal", HEADING_NEXT_GOAL, 6,
                    List.of(HEADING_NEXT_GOAL, HEADING_NEXT_GOAL_ALIAS_1,
                            HEADING_NEXT_GOAL_ALIAS_2, HEADING_NEXT_GOAL_ALIAS_3))
    );

    private static final Map<String, SectionDefinition> SECTION_DEFINITION_MAP = buildSectionDefinitionMap();

    private AssessmentAiSuggestionParser() {
    }

    public static AssessmentAiSuggestionStructuredVO parse(String aiSuggestion) {
        AssessmentAiSuggestionStructuredVO structuredVO = new AssessmentAiSuggestionStructuredVO();
        structuredVO.setStructured(false);
        structuredVO.setSectionCount(0);
        structuredVO.setSectionList(List.of());
        if (StringUtils.isBlank(aiSuggestion)) {
            return structuredVO;
        }

        List<AssessmentAiSuggestionSectionVO> sectionList = parseSections(aiSuggestion);
        if (sectionList.isEmpty()) {
            return structuredVO;
        }

        structuredVO.setStructured(true);
        structuredVO.setSectionCount(sectionList.size());
        structuredVO.setSectionList(sectionList);
        for (AssessmentAiSuggestionSectionVO sectionVO : sectionList) {
            switch (sectionVO.getKey()) {
                case "overallJudgment" -> structuredVO.setOverallJudgment(sectionVO.getContent());
                case "trainingAdvice" -> structuredVO.setTrainingAdvice(sectionVO.getContent());
                case "weeklyPlan" -> structuredVO.setWeeklyPlan(sectionVO.getContent());
                case "recoveryAndNutrition" -> structuredVO.setRecoveryAndNutrition(sectionVO.getContent());
                case "precautions" -> structuredVO.setPrecautions(sectionVO.getContent());
                case "nextAssessmentGoal" -> structuredVO.setNextAssessmentGoal(sectionVO.getContent());
                default -> {
                }
            }
        }
        return structuredVO;
    }

    private static List<AssessmentAiSuggestionSectionVO> parseSections(String aiSuggestion) {
        String normalizedText = normalize(aiSuggestion);
        if (StringUtils.isBlank(normalizedText)) {
            return List.of();
        }

        List<AssessmentAiSuggestionSectionVO> sectionList = new ArrayList<>();
        SectionDefinition currentDefinition = null;
        StringBuilder currentContent = null;

        for (String rawLine : normalizedText.split("\n")) {
            String line = StringUtils.stripEnd(rawLine, null);
            String headingCandidate = toHeadingCandidate(line);
            Matcher matcher = HEADING_PATTERN.matcher(headingCandidate);
            if (matcher.matches()) {
                if (currentDefinition != null) {
                    addSection(sectionList, currentDefinition, currentContent);
                }
                currentDefinition = SECTION_DEFINITION_MAP.get(matcher.group(1));
                currentContent = new StringBuilder();
                String inlineContent = StringUtils.trimToEmpty(matcher.group(2));
                if (StringUtils.isNotBlank(inlineContent)) {
                    currentContent.append(inlineContent);
                }
                continue;
            }

            if (currentDefinition == null) {
                continue;
            }
            if (currentContent.length() > 0) {
                currentContent.append("\n");
            }
            currentContent.append(line);
        }

        if (currentDefinition != null) {
            addSection(sectionList, currentDefinition, currentContent);
        }
        return sectionList;
    }

    private static void addSection(List<AssessmentAiSuggestionSectionVO> sectionList,
            SectionDefinition definition, StringBuilder contentBuilder) {
        String content = StringUtils.trimToNull(contentBuilder == null ? null : contentBuilder.toString());
        if (content == null) {
            return;
        }
        AssessmentAiSuggestionSectionVO sectionVO = new AssessmentAiSuggestionSectionVO();
        sectionVO.setKey(definition.key());
        sectionVO.setTitle(definition.title());
        sectionVO.setOrder(definition.order());
        sectionVO.setContent(content);
        sectionList.add(sectionVO);
    }

    private static String normalize(String text) {
        return StringUtils.trimToEmpty(text)
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private static String toHeadingCandidate(String line) {
        String candidate = StringUtils.trimToEmpty(line);
        candidate = LEADING_DECORATOR_PATTERN.matcher(candidate).replaceFirst("");
        candidate = StringUtils.removeStart(candidate, "**");
        candidate = StringUtils.removeEnd(candidate, "**");
        candidate = StringUtils.removeStart(candidate, "__");
        candidate = StringUtils.removeEnd(candidate, "__");
        return StringUtils.trimToEmpty(candidate);
    }

    private static Map<String, SectionDefinition> buildSectionDefinitionMap() {
        Map<String, SectionDefinition> definitionMap = new LinkedHashMap<>();
        for (SectionDefinition definition : SECTION_DEFINITION_LIST) {
            for (String alias : definition.aliases()) {
                definitionMap.put(alias, definition);
            }
        }
        return definitionMap;
    }

    private record SectionDefinition(String key, String title, int order, List<String> aliases) {
    }
}
