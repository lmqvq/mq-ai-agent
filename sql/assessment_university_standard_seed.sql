-- ============================================================
-- 大学生体测方案基础种子数据
--
-- 说明：
-- 1. 本脚本只初始化方案元数据与项目定义
-- 2. assessment_rule 的详细评分阈值建议在确认标准表后单独导入
-- 3. 这里使用统一编码，便于后端和前端稳定对接
-- ============================================================

use mq_ai_agent;

-- 1. 初始化大学生体测方案
insert into assessment_scheme
(
    schemeCode,
    schemeName,
    sceneType,
    description,
    version,
    source,
    configJson,
    status
)
values
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    '大学生体测',
    'student_physical_health',
    '国家学生体质健康标准（大学生组）内置方案',
    'v1',
    '国家学生体质健康标准（大学生组）',
    '{"gradeGroups":["freshman_sophomore","junior_senior"],"genders":["male","female"]}',
    1
)
on duplicate key update
schemeName = values(schemeName),
sceneType = values(sceneType),
description = values(description),
version = values(version),
source = values(source),
configJson = values(configJson),
status = values(status);

-- 2. 初始化方案项目定义
insert into assessment_scheme_item
(
    schemeCode,
    itemCode,
    itemName,
    itemCategory,
    unit,
    inputType,
    inputPrecision,
    weight,
    displayOrder,
    applicableGender,
    applicableGradeGroup,
    validationMin,
    validationMax,
    isRequired,
    isBonusItem,
    description
)
values
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'BMI',
    'BMI',
    'body_composition',
    'index',
    'number',
    2,
    15.00,
    1,
    'all',
    'all',
    10.00,
    50.00,
    1,
    0,
    '体质指数，支持由身高体重自动换算'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'LUNG_CAPACITY',
    '肺活量',
    'cardiopulmonary',
    'ml',
    'number',
    0,
    15.00,
    2,
    'all',
    'all',
    500.00,
    10000.00,
    1,
    0,
    '肺活量测试结果'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'RUN_50M',
    '50米跑',
    'speed',
    'second',
    'number',
    2,
    20.00,
    3,
    'all',
    'all',
    5.00,
    20.00,
    1,
    0,
    '50米跑成绩，统一换算为秒'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'SIT_AND_REACH',
    '坐位体前屈',
    'flexibility',
    'cm',
    'number',
    1,
    10.00,
    4,
    'all',
    'all',
    -30.00,
    50.00,
    1,
    0,
    '坐位体前屈成绩'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'STANDING_LONG_JUMP',
    '立定跳远',
    'explosive_power',
    'cm',
    'number',
    0,
    10.00,
    5,
    'all',
    'all',
    50.00,
    400.00,
    1,
    0,
    '立定跳远成绩'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'UPPER_BODY_OR_CORE',
    '引体向上/仰卧起坐',
    'strength_endurance',
    'count',
    'number',
    0,
    10.00,
    6,
    'all',
    'all',
    0.00,
    100.00,
    1,
    0,
    '男性对应引体向上，女性对应1分钟仰卧起坐'
),
(
    'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD',
    'ENDURANCE_RUN',
    '1000米跑/800米跑',
    'endurance',
    'second',
    'number',
    0,
    20.00,
    7,
    'all',
    'all',
    60.00,
    1200.00,
    1,
    0,
    '男性对应1000米跑，女性对应800米跑，统一换算为秒'
)
on duplicate key update
itemName = values(itemName),
itemCategory = values(itemCategory),
unit = values(unit),
inputType = values(inputType),
inputPrecision = values(inputPrecision),
weight = values(weight),
displayOrder = values(displayOrder),
applicableGender = values(applicableGender),
applicableGradeGroup = values(applicableGradeGroup),
validationMin = values(validationMin),
validationMax = values(validationMax),
isRequired = values(isRequired),
isBonusItem = values(isBonusItem),
description = values(description);

-- 3. 评分规则导入说明
-- assessment_rule 建议在你确认最终标准表后再单独导入。
-- 推荐后续新增脚本：
--   sql/assessment_university_standard_rules.sql
--
-- 推荐编码取值：
--   gender: male / female / all
--   gradeGroup: freshman_sophomore / junior_senior / all
--   comparisonType: RANGE / EXACT
