-- ============================================================
-- University physical health standard rules
--
-- Purpose:
-- 1. Import the full scoring rules for the built-in university
--    physical health assessment scheme.
-- 2. The script is idempotent for the same schemeCode + ruleVersion.
-- 3. This script requires MySQL 8.0+ because it uses window
--    functions to generate interval rules.
--
-- Notes:
-- 1. The official table publishes the minimum 10-point threshold.
--    This script adds a trailing 0-point interval for worse values.
-- 2. Male pull-up does not publish 78 / 74 / 70 / 66 / 62 rows.
--    The script keeps the official discrete rows instead of fabricating
--    missing score bands.
-- 3. BMI "overweight" is imported as 80 points. This is inferred from
--    the official table layout used by multiple mirror pages, where
--    "low weight" and "overweight" share the same 80-point score cell.
-- ============================================================

use mq_ai_agent;

start transaction;

set @scheme_code := _utf8mb4'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD' collate utf8mb4_unicode_ci;
set @rule_version := _utf8mb4'v1' collate utf8mb4_unicode_ci;

delete from assessment_rule
where schemeCode collate utf8mb4_unicode_ci = @scheme_code
  and ruleVersion collate utf8mb4_unicode_ci = @rule_version;

drop temporary table if exists tmp_assessment_rule_source;

create temporary table tmp_assessment_rule_source
(
    itemCode        varchar(64)   not null,
    gender          varchar(32)   not null,
    gradeGroup      varchar(64)   not null,
    score           decimal(6, 2) not null,
    thresholdValue  decimal(10, 2) not null,
    unitStep        decimal(10, 2) not null,
    direction       varchar(32)   not null
) comment 'Temporary source thresholds for assessment_rule generation';

-- ============================================================
-- BMI
-- gradeGroup is "all" because the official BMI rule only depends on gender
-- ============================================================
insert into assessment_rule
(
    schemeCode,
    itemCode,
    gender,
    gradeGroup,
    score,
    minValue,
    `maxValue`,
    comparisonType,
    ruleVersion,
    sortOrder,
    description
)
values
(@scheme_code, 'BMI', 'male', 'all', 100.00, 17.90, 23.99, 'RANGE', @rule_version, 1, 'BMI normal'),
(@scheme_code, 'BMI', 'male', 'all', 80.00, null, 17.89, 'RANGE', @rule_version, 2, 'BMI low weight'),
(@scheme_code, 'BMI', 'male', 'all', 80.00, 24.00, 27.99, 'RANGE', @rule_version, 3, 'BMI overweight'),
(@scheme_code, 'BMI', 'male', 'all', 60.00, 28.00, null, 'RANGE', @rule_version, 4, 'BMI obesity'),
(@scheme_code, 'BMI', 'female', 'all', 100.00, 17.20, 23.99, 'RANGE', @rule_version, 1, 'BMI normal'),
(@scheme_code, 'BMI', 'female', 'all', 80.00, null, 17.19, 'RANGE', @rule_version, 2, 'BMI low weight'),
(@scheme_code, 'BMI', 'female', 'all', 80.00, 24.00, 27.99, 'RANGE', @rule_version, 3, 'BMI overweight'),
(@scheme_code, 'BMI', 'female', 'all', 60.00, 28.00, null, 'RANGE', @rule_version, 4, 'BMI obesity');

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'LUNG_CAPACITY', 'male', 'freshman_sophomore', score, male_fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 5040.00 as male_fs, 5140.00 as male_js, 3400.00 as female_fs, 3450.00 as female_js union all
    select 95.00, 4920.00, 5020.00, 3350.00, 3400.00 union all
    select 90.00, 4800.00, 4900.00, 3300.00, 3350.00 union all
    select 85.00, 4550.00, 4650.00, 3150.00, 3200.00 union all
    select 80.00, 4300.00, 4400.00, 3000.00, 3050.00 union all
    select 78.00, 4180.00, 4280.00, 2900.00, 2950.00 union all
    select 76.00, 4060.00, 4160.00, 2800.00, 2850.00 union all
    select 74.00, 3940.00, 4040.00, 2700.00, 2750.00 union all
    select 72.00, 3820.00, 3920.00, 2600.00, 2650.00 union all
    select 70.00, 3700.00, 3800.00, 2500.00, 2550.00 union all
    select 68.00, 3580.00, 3680.00, 2400.00, 2450.00 union all
    select 66.00, 3460.00, 3560.00, 2300.00, 2350.00 union all
    select 64.00, 3340.00, 3440.00, 2200.00, 2250.00 union all
    select 62.00, 3220.00, 3320.00, 2100.00, 2150.00 union all
    select 60.00, 3100.00, 3200.00, 2000.00, 2050.00 union all
    select 50.00, 2940.00, 3030.00, 1960.00, 2010.00 union all
    select 40.00, 2780.00, 2860.00, 1920.00, 1970.00 union all
    select 30.00, 2620.00, 2690.00, 1880.00, 1930.00 union all
    select 20.00, 2460.00, 2520.00, 1840.00, 1890.00 union all
    select 10.00, 2300.00, 2350.00, 1800.00, 1850.00
) lung_capacity
union all
select 'LUNG_CAPACITY', 'male', 'junior_senior', score, male_js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 5040.00 as male_fs, 5140.00 as male_js, 3400.00 as female_fs, 3450.00 as female_js union all
    select 95.00, 4920.00, 5020.00, 3350.00, 3400.00 union all
    select 90.00, 4800.00, 4900.00, 3300.00, 3350.00 union all
    select 85.00, 4550.00, 4650.00, 3150.00, 3200.00 union all
    select 80.00, 4300.00, 4400.00, 3000.00, 3050.00 union all
    select 78.00, 4180.00, 4280.00, 2900.00, 2950.00 union all
    select 76.00, 4060.00, 4160.00, 2800.00, 2850.00 union all
    select 74.00, 3940.00, 4040.00, 2700.00, 2750.00 union all
    select 72.00, 3820.00, 3920.00, 2600.00, 2650.00 union all
    select 70.00, 3700.00, 3800.00, 2500.00, 2550.00 union all
    select 68.00, 3580.00, 3680.00, 2400.00, 2450.00 union all
    select 66.00, 3460.00, 3560.00, 2300.00, 2350.00 union all
    select 64.00, 3340.00, 3440.00, 2200.00, 2250.00 union all
    select 62.00, 3220.00, 3320.00, 2100.00, 2150.00 union all
    select 60.00, 3100.00, 3200.00, 2000.00, 2050.00 union all
    select 50.00, 2940.00, 3030.00, 1960.00, 2010.00 union all
    select 40.00, 2780.00, 2860.00, 1920.00, 1970.00 union all
    select 30.00, 2620.00, 2690.00, 1880.00, 1930.00 union all
    select 20.00, 2460.00, 2520.00, 1840.00, 1890.00 union all
    select 10.00, 2300.00, 2350.00, 1800.00, 1850.00
) lung_capacity
union all
select 'LUNG_CAPACITY', 'female', 'freshman_sophomore', score, female_fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 5040.00 as male_fs, 5140.00 as male_js, 3400.00 as female_fs, 3450.00 as female_js union all
    select 95.00, 4920.00, 5020.00, 3350.00, 3400.00 union all
    select 90.00, 4800.00, 4900.00, 3300.00, 3350.00 union all
    select 85.00, 4550.00, 4650.00, 3150.00, 3200.00 union all
    select 80.00, 4300.00, 4400.00, 3000.00, 3050.00 union all
    select 78.00, 4180.00, 4280.00, 2900.00, 2950.00 union all
    select 76.00, 4060.00, 4160.00, 2800.00, 2850.00 union all
    select 74.00, 3940.00, 4040.00, 2700.00, 2750.00 union all
    select 72.00, 3820.00, 3920.00, 2600.00, 2650.00 union all
    select 70.00, 3700.00, 3800.00, 2500.00, 2550.00 union all
    select 68.00, 3580.00, 3680.00, 2400.00, 2450.00 union all
    select 66.00, 3460.00, 3560.00, 2300.00, 2350.00 union all
    select 64.00, 3340.00, 3440.00, 2200.00, 2250.00 union all
    select 62.00, 3220.00, 3320.00, 2100.00, 2150.00 union all
    select 60.00, 3100.00, 3200.00, 2000.00, 2050.00 union all
    select 50.00, 2940.00, 3030.00, 1960.00, 2010.00 union all
    select 40.00, 2780.00, 2860.00, 1920.00, 1970.00 union all
    select 30.00, 2620.00, 2690.00, 1880.00, 1930.00 union all
    select 20.00, 2460.00, 2520.00, 1840.00, 1890.00 union all
    select 10.00, 2300.00, 2350.00, 1800.00, 1850.00
) lung_capacity
union all
select 'LUNG_CAPACITY', 'female', 'junior_senior', score, female_js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 5040.00 as male_fs, 5140.00 as male_js, 3400.00 as female_fs, 3450.00 as female_js union all
    select 95.00, 4920.00, 5020.00, 3350.00, 3400.00 union all
    select 90.00, 4800.00, 4900.00, 3300.00, 3350.00 union all
    select 85.00, 4550.00, 4650.00, 3150.00, 3200.00 union all
    select 80.00, 4300.00, 4400.00, 3000.00, 3050.00 union all
    select 78.00, 4180.00, 4280.00, 2900.00, 2950.00 union all
    select 76.00, 4060.00, 4160.00, 2800.00, 2850.00 union all
    select 74.00, 3940.00, 4040.00, 2700.00, 2750.00 union all
    select 72.00, 3820.00, 3920.00, 2600.00, 2650.00 union all
    select 70.00, 3700.00, 3800.00, 2500.00, 2550.00 union all
    select 68.00, 3580.00, 3680.00, 2400.00, 2450.00 union all
    select 66.00, 3460.00, 3560.00, 2300.00, 2350.00 union all
    select 64.00, 3340.00, 3440.00, 2200.00, 2250.00 union all
    select 62.00, 3220.00, 3320.00, 2100.00, 2150.00 union all
    select 60.00, 3100.00, 3200.00, 2000.00, 2050.00 union all
    select 50.00, 2940.00, 3030.00, 1960.00, 2010.00 union all
    select 40.00, 2780.00, 2860.00, 1920.00, 1970.00 union all
    select 30.00, 2620.00, 2690.00, 1880.00, 1930.00 union all
    select 20.00, 2460.00, 2520.00, 1840.00, 1890.00 union all
    select 10.00, 2300.00, 2350.00, 1800.00, 1850.00
) lung_capacity;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'RUN_50M', 'male', 'freshman_sophomore', score, male_fs, 0.10, 'LOWER_BETTER' from (
    select 100.00 as score, 6.70 as male_fs, 6.60 as male_js, 7.50 as female_fs, 7.40 as female_js union all
    select 95.00, 6.80, 6.70, 7.60, 7.50 union all
    select 90.00, 6.90, 6.80, 7.70, 7.60 union all
    select 85.00, 7.00, 6.90, 8.00, 7.90 union all
    select 80.00, 7.10, 7.00, 8.30, 8.20 union all
    select 78.00, 7.30, 7.20, 8.50, 8.40 union all
    select 76.00, 7.50, 7.40, 8.70, 8.60 union all
    select 74.00, 7.70, 7.60, 8.90, 8.80 union all
    select 72.00, 7.90, 7.80, 9.10, 9.00 union all
    select 70.00, 8.10, 8.00, 9.30, 9.20 union all
    select 68.00, 8.30, 8.20, 9.50, 9.40 union all
    select 66.00, 8.50, 8.40, 9.70, 9.60 union all
    select 64.00, 8.70, 8.60, 9.90, 9.80 union all
    select 62.00, 8.90, 8.80, 10.10, 10.00 union all
    select 60.00, 9.10, 9.00, 10.30, 10.20 union all
    select 50.00, 9.30, 9.20, 10.50, 10.40 union all
    select 40.00, 9.50, 9.40, 10.70, 10.60 union all
    select 30.00, 9.70, 9.60, 10.90, 10.80 union all
    select 20.00, 9.90, 9.80, 11.10, 11.00 union all
    select 10.00, 10.10, 10.00, 11.30, 11.20
) run_50m
union all
select 'RUN_50M', 'male', 'junior_senior', score, male_js, 0.10, 'LOWER_BETTER' from (
    select 100.00 as score, 6.70 as male_fs, 6.60 as male_js, 7.50 as female_fs, 7.40 as female_js union all
    select 95.00, 6.80, 6.70, 7.60, 7.50 union all
    select 90.00, 6.90, 6.80, 7.70, 7.60 union all
    select 85.00, 7.00, 6.90, 8.00, 7.90 union all
    select 80.00, 7.10, 7.00, 8.30, 8.20 union all
    select 78.00, 7.30, 7.20, 8.50, 8.40 union all
    select 76.00, 7.50, 7.40, 8.70, 8.60 union all
    select 74.00, 7.70, 7.60, 8.90, 8.80 union all
    select 72.00, 7.90, 7.80, 9.10, 9.00 union all
    select 70.00, 8.10, 8.00, 9.30, 9.20 union all
    select 68.00, 8.30, 8.20, 9.50, 9.40 union all
    select 66.00, 8.50, 8.40, 9.70, 9.60 union all
    select 64.00, 8.70, 8.60, 9.90, 9.80 union all
    select 62.00, 8.90, 8.80, 10.10, 10.00 union all
    select 60.00, 9.10, 9.00, 10.30, 10.20 union all
    select 50.00, 9.30, 9.20, 10.50, 10.40 union all
    select 40.00, 9.50, 9.40, 10.70, 10.60 union all
    select 30.00, 9.70, 9.60, 10.90, 10.80 union all
    select 20.00, 9.90, 9.80, 11.10, 11.00 union all
    select 10.00, 10.10, 10.00, 11.30, 11.20
) run_50m
union all
select 'RUN_50M', 'female', 'freshman_sophomore', score, female_fs, 0.10, 'LOWER_BETTER' from (
    select 100.00 as score, 6.70 as male_fs, 6.60 as male_js, 7.50 as female_fs, 7.40 as female_js union all
    select 95.00, 6.80, 6.70, 7.60, 7.50 union all
    select 90.00, 6.90, 6.80, 7.70, 7.60 union all
    select 85.00, 7.00, 6.90, 8.00, 7.90 union all
    select 80.00, 7.10, 7.00, 8.30, 8.20 union all
    select 78.00, 7.30, 7.20, 8.50, 8.40 union all
    select 76.00, 7.50, 7.40, 8.70, 8.60 union all
    select 74.00, 7.70, 7.60, 8.90, 8.80 union all
    select 72.00, 7.90, 7.80, 9.10, 9.00 union all
    select 70.00, 8.10, 8.00, 9.30, 9.20 union all
    select 68.00, 8.30, 8.20, 9.50, 9.40 union all
    select 66.00, 8.50, 8.40, 9.70, 9.60 union all
    select 64.00, 8.70, 8.60, 9.90, 9.80 union all
    select 62.00, 8.90, 8.80, 10.10, 10.00 union all
    select 60.00, 9.10, 9.00, 10.30, 10.20 union all
    select 50.00, 9.30, 9.20, 10.50, 10.40 union all
    select 40.00, 9.50, 9.40, 10.70, 10.60 union all
    select 30.00, 9.70, 9.60, 10.90, 10.80 union all
    select 20.00, 9.90, 9.80, 11.10, 11.00 union all
    select 10.00, 10.10, 10.00, 11.30, 11.20
) run_50m
union all
select 'RUN_50M', 'female', 'junior_senior', score, female_js, 0.10, 'LOWER_BETTER' from (
    select 100.00 as score, 6.70 as male_fs, 6.60 as male_js, 7.50 as female_fs, 7.40 as female_js union all
    select 95.00, 6.80, 6.70, 7.60, 7.50 union all
    select 90.00, 6.90, 6.80, 7.70, 7.60 union all
    select 85.00, 7.00, 6.90, 8.00, 7.90 union all
    select 80.00, 7.10, 7.00, 8.30, 8.20 union all
    select 78.00, 7.30, 7.20, 8.50, 8.40 union all
    select 76.00, 7.50, 7.40, 8.70, 8.60 union all
    select 74.00, 7.70, 7.60, 8.90, 8.80 union all
    select 72.00, 7.90, 7.80, 9.10, 9.00 union all
    select 70.00, 8.10, 8.00, 9.30, 9.20 union all
    select 68.00, 8.30, 8.20, 9.50, 9.40 union all
    select 66.00, 8.50, 8.40, 9.70, 9.60 union all
    select 64.00, 8.70, 8.60, 9.90, 9.80 union all
    select 62.00, 8.90, 8.80, 10.10, 10.00 union all
    select 60.00, 9.10, 9.00, 10.30, 10.20 union all
    select 50.00, 9.30, 9.20, 10.50, 10.40 union all
    select 40.00, 9.50, 9.40, 10.70, 10.60 union all
    select 30.00, 9.70, 9.60, 10.90, 10.80 union all
    select 20.00, 9.90, 9.80, 11.10, 11.00 union all
    select 10.00, 10.10, 10.00, 11.30, 11.20
) run_50m;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'SIT_AND_REACH', 'male', 'freshman_sophomore', score, male_fs, 0.10, 'HIGHER_BETTER' from (
    select 100.00 as score, 24.90 as male_fs, 25.10 as male_js, 25.80 as female_fs, 26.30 as female_js union all
    select 95.00, 23.10, 23.30, 24.00, 24.40 union all
    select 90.00, 21.30, 21.50, 22.20, 22.40 union all
    select 85.00, 19.50, 19.90, 20.60, 21.00 union all
    select 80.00, 17.70, 18.20, 19.00, 19.50 union all
    select 78.00, 16.30, 16.80, 17.70, 18.20 union all
    select 76.00, 14.90, 15.40, 16.40, 16.90 union all
    select 74.00, 13.50, 14.00, 15.10, 15.60 union all
    select 72.00, 12.10, 12.60, 13.80, 14.30 union all
    select 70.00, 10.70, 11.20, 12.50, 13.00 union all
    select 68.00, 9.30, 9.80, 11.20, 11.70 union all
    select 66.00, 7.90, 8.40, 9.90, 10.40 union all
    select 64.00, 6.50, 7.00, 8.60, 9.10 union all
    select 62.00, 5.10, 5.60, 7.30, 7.80 union all
    select 60.00, 3.70, 4.20, 6.00, 6.50 union all
    select 50.00, 2.70, 3.20, 5.20, 5.70 union all
    select 40.00, 1.70, 2.20, 4.40, 4.90 union all
    select 30.00, 0.70, 1.20, 3.60, 4.10 union all
    select 20.00, -0.30, 0.20, 2.80, 3.30 union all
    select 10.00, -1.30, -0.80, 2.00, 2.50
) sit_and_reach
union all
select 'SIT_AND_REACH', 'male', 'junior_senior', score, male_js, 0.10, 'HIGHER_BETTER' from (
    select 100.00 as score, 24.90 as male_fs, 25.10 as male_js, 25.80 as female_fs, 26.30 as female_js union all
    select 95.00, 23.10, 23.30, 24.00, 24.40 union all
    select 90.00, 21.30, 21.50, 22.20, 22.40 union all
    select 85.00, 19.50, 19.90, 20.60, 21.00 union all
    select 80.00, 17.70, 18.20, 19.00, 19.50 union all
    select 78.00, 16.30, 16.80, 17.70, 18.20 union all
    select 76.00, 14.90, 15.40, 16.40, 16.90 union all
    select 74.00, 13.50, 14.00, 15.10, 15.60 union all
    select 72.00, 12.10, 12.60, 13.80, 14.30 union all
    select 70.00, 10.70, 11.20, 12.50, 13.00 union all
    select 68.00, 9.30, 9.80, 11.20, 11.70 union all
    select 66.00, 7.90, 8.40, 9.90, 10.40 union all
    select 64.00, 6.50, 7.00, 8.60, 9.10 union all
    select 62.00, 5.10, 5.60, 7.30, 7.80 union all
    select 60.00, 3.70, 4.20, 6.00, 6.50 union all
    select 50.00, 2.70, 3.20, 5.20, 5.70 union all
    select 40.00, 1.70, 2.20, 4.40, 4.90 union all
    select 30.00, 0.70, 1.20, 3.60, 4.10 union all
    select 20.00, -0.30, 0.20, 2.80, 3.30 union all
    select 10.00, -1.30, -0.80, 2.00, 2.50
) sit_and_reach
union all
select 'SIT_AND_REACH', 'female', 'freshman_sophomore', score, female_fs, 0.10, 'HIGHER_BETTER' from (
    select 100.00 as score, 24.90 as male_fs, 25.10 as male_js, 25.80 as female_fs, 26.30 as female_js union all
    select 95.00, 23.10, 23.30, 24.00, 24.40 union all
    select 90.00, 21.30, 21.50, 22.20, 22.40 union all
    select 85.00, 19.50, 19.90, 20.60, 21.00 union all
    select 80.00, 17.70, 18.20, 19.00, 19.50 union all
    select 78.00, 16.30, 16.80, 17.70, 18.20 union all
    select 76.00, 14.90, 15.40, 16.40, 16.90 union all
    select 74.00, 13.50, 14.00, 15.10, 15.60 union all
    select 72.00, 12.10, 12.60, 13.80, 14.30 union all
    select 70.00, 10.70, 11.20, 12.50, 13.00 union all
    select 68.00, 9.30, 9.80, 11.20, 11.70 union all
    select 66.00, 7.90, 8.40, 9.90, 10.40 union all
    select 64.00, 6.50, 7.00, 8.60, 9.10 union all
    select 62.00, 5.10, 5.60, 7.30, 7.80 union all
    select 60.00, 3.70, 4.20, 6.00, 6.50 union all
    select 50.00, 2.70, 3.20, 5.20, 5.70 union all
    select 40.00, 1.70, 2.20, 4.40, 4.90 union all
    select 30.00, 0.70, 1.20, 3.60, 4.10 union all
    select 20.00, -0.30, 0.20, 2.80, 3.30 union all
    select 10.00, -1.30, -0.80, 2.00, 2.50
) sit_and_reach
union all
select 'SIT_AND_REACH', 'female', 'junior_senior', score, female_js, 0.10, 'HIGHER_BETTER' from (
    select 100.00 as score, 24.90 as male_fs, 25.10 as male_js, 25.80 as female_fs, 26.30 as female_js union all
    select 95.00, 23.10, 23.30, 24.00, 24.40 union all
    select 90.00, 21.30, 21.50, 22.20, 22.40 union all
    select 85.00, 19.50, 19.90, 20.60, 21.00 union all
    select 80.00, 17.70, 18.20, 19.00, 19.50 union all
    select 78.00, 16.30, 16.80, 17.70, 18.20 union all
    select 76.00, 14.90, 15.40, 16.40, 16.90 union all
    select 74.00, 13.50, 14.00, 15.10, 15.60 union all
    select 72.00, 12.10, 12.60, 13.80, 14.30 union all
    select 70.00, 10.70, 11.20, 12.50, 13.00 union all
    select 68.00, 9.30, 9.80, 11.20, 11.70 union all
    select 66.00, 7.90, 8.40, 9.90, 10.40 union all
    select 64.00, 6.50, 7.00, 8.60, 9.10 union all
    select 62.00, 5.10, 5.60, 7.30, 7.80 union all
    select 60.00, 3.70, 4.20, 6.00, 6.50 union all
    select 50.00, 2.70, 3.20, 5.20, 5.70 union all
    select 40.00, 1.70, 2.20, 4.40, 4.90 union all
    select 30.00, 0.70, 1.20, 3.60, 4.10 union all
    select 20.00, -0.30, 0.20, 2.80, 3.30 union all
    select 10.00, -1.30, -0.80, 2.00, 2.50
) sit_and_reach;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'STANDING_LONG_JUMP', 'male', 'freshman_sophomore', score, male_fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 273.00 as male_fs, 275.00 as male_js, 207.00 as female_fs, 208.00 as female_js union all
    select 95.00, 268.00, 270.00, 201.00, 202.00 union all
    select 90.00, 263.00, 265.00, 195.00, 196.00 union all
    select 85.00, 256.00, 258.00, 188.00, 189.00 union all
    select 80.00, 248.00, 250.00, 181.00, 182.00 union all
    select 78.00, 244.00, 246.00, 178.00, 179.00 union all
    select 76.00, 240.00, 242.00, 175.00, 176.00 union all
    select 74.00, 236.00, 238.00, 172.00, 173.00 union all
    select 72.00, 232.00, 234.00, 169.00, 170.00 union all
    select 70.00, 228.00, 230.00, 166.00, 167.00 union all
    select 68.00, 224.00, 226.00, 163.00, 164.00 union all
    select 66.00, 220.00, 222.00, 160.00, 161.00 union all
    select 64.00, 216.00, 218.00, 157.00, 158.00 union all
    select 62.00, 212.00, 214.00, 154.00, 155.00 union all
    select 60.00, 208.00, 210.00, 151.00, 152.00 union all
    select 50.00, 203.00, 205.00, 146.00, 147.00 union all
    select 40.00, 198.00, 200.00, 141.00, 142.00 union all
    select 30.00, 193.00, 195.00, 136.00, 137.00 union all
    select 20.00, 188.00, 190.00, 131.00, 132.00 union all
    select 10.00, 183.00, 185.00, 126.00, 127.00
) standing_long_jump
union all
select 'STANDING_LONG_JUMP', 'male', 'junior_senior', score, male_js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 273.00 as male_fs, 275.00 as male_js, 207.00 as female_fs, 208.00 as female_js union all
    select 95.00, 268.00, 270.00, 201.00, 202.00 union all
    select 90.00, 263.00, 265.00, 195.00, 196.00 union all
    select 85.00, 256.00, 258.00, 188.00, 189.00 union all
    select 80.00, 248.00, 250.00, 181.00, 182.00 union all
    select 78.00, 244.00, 246.00, 178.00, 179.00 union all
    select 76.00, 240.00, 242.00, 175.00, 176.00 union all
    select 74.00, 236.00, 238.00, 172.00, 173.00 union all
    select 72.00, 232.00, 234.00, 169.00, 170.00 union all
    select 70.00, 228.00, 230.00, 166.00, 167.00 union all
    select 68.00, 224.00, 226.00, 163.00, 164.00 union all
    select 66.00, 220.00, 222.00, 160.00, 161.00 union all
    select 64.00, 216.00, 218.00, 157.00, 158.00 union all
    select 62.00, 212.00, 214.00, 154.00, 155.00 union all
    select 60.00, 208.00, 210.00, 151.00, 152.00 union all
    select 50.00, 203.00, 205.00, 146.00, 147.00 union all
    select 40.00, 198.00, 200.00, 141.00, 142.00 union all
    select 30.00, 193.00, 195.00, 136.00, 137.00 union all
    select 20.00, 188.00, 190.00, 131.00, 132.00 union all
    select 10.00, 183.00, 185.00, 126.00, 127.00
) standing_long_jump
union all
select 'STANDING_LONG_JUMP', 'female', 'freshman_sophomore', score, female_fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 273.00 as male_fs, 275.00 as male_js, 207.00 as female_fs, 208.00 as female_js union all
    select 95.00, 268.00, 270.00, 201.00, 202.00 union all
    select 90.00, 263.00, 265.00, 195.00, 196.00 union all
    select 85.00, 256.00, 258.00, 188.00, 189.00 union all
    select 80.00, 248.00, 250.00, 181.00, 182.00 union all
    select 78.00, 244.00, 246.00, 178.00, 179.00 union all
    select 76.00, 240.00, 242.00, 175.00, 176.00 union all
    select 74.00, 236.00, 238.00, 172.00, 173.00 union all
    select 72.00, 232.00, 234.00, 169.00, 170.00 union all
    select 70.00, 228.00, 230.00, 166.00, 167.00 union all
    select 68.00, 224.00, 226.00, 163.00, 164.00 union all
    select 66.00, 220.00, 222.00, 160.00, 161.00 union all
    select 64.00, 216.00, 218.00, 157.00, 158.00 union all
    select 62.00, 212.00, 214.00, 154.00, 155.00 union all
    select 60.00, 208.00, 210.00, 151.00, 152.00 union all
    select 50.00, 203.00, 205.00, 146.00, 147.00 union all
    select 40.00, 198.00, 200.00, 141.00, 142.00 union all
    select 30.00, 193.00, 195.00, 136.00, 137.00 union all
    select 20.00, 188.00, 190.00, 131.00, 132.00 union all
    select 10.00, 183.00, 185.00, 126.00, 127.00
) standing_long_jump
union all
select 'STANDING_LONG_JUMP', 'female', 'junior_senior', score, female_js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 273.00 as male_fs, 275.00 as male_js, 207.00 as female_fs, 208.00 as female_js union all
    select 95.00, 268.00, 270.00, 201.00, 202.00 union all
    select 90.00, 263.00, 265.00, 195.00, 196.00 union all
    select 85.00, 256.00, 258.00, 188.00, 189.00 union all
    select 80.00, 248.00, 250.00, 181.00, 182.00 union all
    select 78.00, 244.00, 246.00, 178.00, 179.00 union all
    select 76.00, 240.00, 242.00, 175.00, 176.00 union all
    select 74.00, 236.00, 238.00, 172.00, 173.00 union all
    select 72.00, 232.00, 234.00, 169.00, 170.00 union all
    select 70.00, 228.00, 230.00, 166.00, 167.00 union all
    select 68.00, 224.00, 226.00, 163.00, 164.00 union all
    select 66.00, 220.00, 222.00, 160.00, 161.00 union all
    select 64.00, 216.00, 218.00, 157.00, 158.00 union all
    select 62.00, 212.00, 214.00, 154.00, 155.00 union all
    select 60.00, 208.00, 210.00, 151.00, 152.00 union all
    select 50.00, 203.00, 205.00, 146.00, 147.00 union all
    select 40.00, 198.00, 200.00, 141.00, 142.00 union all
    select 30.00, 193.00, 195.00, 136.00, 137.00 union all
    select 20.00, 188.00, 190.00, 131.00, 132.00 union all
    select 10.00, 183.00, 185.00, 126.00, 127.00
) standing_long_jump;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'UPPER_BODY_OR_CORE', 'male', 'freshman_sophomore', score, fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 19.00 as fs, 20.00 as js union all
    select 95.00, 18.00, 19.00 union all
    select 90.00, 17.00, 18.00 union all
    select 85.00, 16.00, 17.00 union all
    select 80.00, 15.00, 16.00 union all
    select 76.00, 14.00, 15.00 union all
    select 72.00, 13.00, 14.00 union all
    select 68.00, 12.00, 13.00 union all
    select 64.00, 11.00, 12.00 union all
    select 60.00, 10.00, 11.00 union all
    select 50.00, 9.00, 10.00 union all
    select 40.00, 8.00, 9.00 union all
    select 30.00, 7.00, 8.00 union all
    select 20.00, 6.00, 7.00 union all
    select 10.00, 5.00, 6.00
) male_pull_up
union all
select 'UPPER_BODY_OR_CORE', 'male', 'junior_senior', score, js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 19.00 as fs, 20.00 as js union all
    select 95.00, 18.00, 19.00 union all
    select 90.00, 17.00, 18.00 union all
    select 85.00, 16.00, 17.00 union all
    select 80.00, 15.00, 16.00 union all
    select 76.00, 14.00, 15.00 union all
    select 72.00, 13.00, 14.00 union all
    select 68.00, 12.00, 13.00 union all
    select 64.00, 11.00, 12.00 union all
    select 60.00, 10.00, 11.00 union all
    select 50.00, 9.00, 10.00 union all
    select 40.00, 8.00, 9.00 union all
    select 30.00, 7.00, 8.00 union all
    select 20.00, 6.00, 7.00 union all
    select 10.00, 5.00, 6.00
) male_pull_up;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'UPPER_BODY_OR_CORE', 'female', 'freshman_sophomore', score, fs, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 56.00 as fs, 57.00 as js union all
    select 95.00, 54.00, 55.00 union all
    select 90.00, 52.00, 53.00 union all
    select 85.00, 49.00, 50.00 union all
    select 80.00, 46.00, 47.00 union all
    select 78.00, 44.00, 45.00 union all
    select 76.00, 42.00, 43.00 union all
    select 74.00, 40.00, 41.00 union all
    select 72.00, 38.00, 39.00 union all
    select 70.00, 36.00, 37.00 union all
    select 68.00, 34.00, 35.00 union all
    select 66.00, 32.00, 33.00 union all
    select 64.00, 30.00, 31.00 union all
    select 62.00, 28.00, 29.00 union all
    select 60.00, 26.00, 27.00 union all
    select 50.00, 24.00, 25.00 union all
    select 40.00, 22.00, 23.00 union all
    select 30.00, 20.00, 21.00 union all
    select 20.00, 18.00, 19.00 union all
    select 10.00, 16.00, 17.00
) female_sit_up
union all
select 'UPPER_BODY_OR_CORE', 'female', 'junior_senior', score, js, 1.00, 'HIGHER_BETTER' from (
    select 100.00 as score, 56.00 as fs, 57.00 as js union all
    select 95.00, 54.00, 55.00 union all
    select 90.00, 52.00, 53.00 union all
    select 85.00, 49.00, 50.00 union all
    select 80.00, 46.00, 47.00 union all
    select 78.00, 44.00, 45.00 union all
    select 76.00, 42.00, 43.00 union all
    select 74.00, 40.00, 41.00 union all
    select 72.00, 38.00, 39.00 union all
    select 70.00, 36.00, 37.00 union all
    select 68.00, 34.00, 35.00 union all
    select 66.00, 32.00, 33.00 union all
    select 64.00, 30.00, 31.00 union all
    select 62.00, 28.00, 29.00 union all
    select 60.00, 26.00, 27.00 union all
    select 50.00, 24.00, 25.00 union all
    select 40.00, 22.00, 23.00 union all
    select 30.00, 20.00, 21.00 union all
    select 20.00, 18.00, 19.00 union all
    select 10.00, 16.00, 17.00
) female_sit_up;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'UPPER_BODY_OR_CORE', 'male', 'freshman_sophomore', score, fs, 1.00, 'HIGHER_BETTER' from (
    select 101.00 as score, 20.00 as fs, 21.00 as js union all
    select 102.00, 21.00, 22.00 union all
    select 103.00, 22.00, 23.00 union all
    select 104.00, 23.00, 24.00 union all
    select 105.00, 24.00, 25.00 union all
    select 106.00, 25.00, 26.00 union all
    select 107.00, 26.00, 27.00 union all
    select 108.00, 27.00, 28.00 union all
    select 109.00, 28.00, 29.00 union all
    select 110.00, 29.00, 30.00
) male_pull_up_bonus
union all
select 'UPPER_BODY_OR_CORE', 'male', 'junior_senior', score, js, 1.00, 'HIGHER_BETTER' from (
    select 101.00 as score, 20.00 as fs, 21.00 as js union all
    select 102.00, 21.00, 22.00 union all
    select 103.00, 22.00, 23.00 union all
    select 104.00, 23.00, 24.00 union all
    select 105.00, 24.00, 25.00 union all
    select 106.00, 25.00, 26.00 union all
    select 107.00, 26.00, 27.00 union all
    select 108.00, 27.00, 28.00 union all
    select 109.00, 28.00, 29.00 union all
    select 110.00, 29.00, 30.00
) male_pull_up_bonus;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'UPPER_BODY_OR_CORE', 'female', 'freshman_sophomore', score, fs, 1.00, 'HIGHER_BETTER' from (
    select 101.00 as score, 58.00 as fs, 59.00 as js union all
    select 102.00, 60.00, 61.00 union all
    select 103.00, 62.00, 63.00 union all
    select 104.00, 63.00, 64.00 union all
    select 105.00, 64.00, 65.00 union all
    select 106.00, 65.00, 66.00 union all
    select 107.00, 66.00, 67.00 union all
    select 108.00, 67.00, 68.00 union all
    select 109.00, 68.00, 69.00 union all
    select 110.00, 69.00, 70.00
) female_sit_up_bonus
union all
select 'UPPER_BODY_OR_CORE', 'female', 'junior_senior', score, js, 1.00, 'HIGHER_BETTER' from (
    select 101.00 as score, 58.00 as fs, 59.00 as js union all
    select 102.00, 60.00, 61.00 union all
    select 103.00, 62.00, 63.00 union all
    select 104.00, 63.00, 64.00 union all
    select 105.00, 64.00, 65.00 union all
    select 106.00, 65.00, 66.00 union all
    select 107.00, 66.00, 67.00 union all
    select 108.00, 67.00, 68.00 union all
    select 109.00, 68.00, 69.00 union all
    select 110.00, 69.00, 70.00
) female_sit_up_bonus;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'ENDURANCE_RUN', 'male', 'freshman_sophomore', score, male_fs, 1.00, 'LOWER_BETTER' from (
    select 100.00 as score, 197.00 as male_fs, 195.00 as male_js, 198.00 as female_fs, 196.00 as female_js union all
    select 95.00, 202.00, 200.00, 204.00, 202.00 union all
    select 90.00, 207.00, 205.00, 210.00, 208.00 union all
    select 85.00, 214.00, 212.00, 217.00, 215.00 union all
    select 80.00, 222.00, 220.00, 224.00, 222.00 union all
    select 78.00, 227.00, 225.00, 229.00, 227.00 union all
    select 76.00, 232.00, 230.00, 234.00, 232.00 union all
    select 74.00, 237.00, 235.00, 239.00, 237.00 union all
    select 72.00, 242.00, 240.00, 244.00, 242.00 union all
    select 70.00, 247.00, 245.00, 249.00, 247.00 union all
    select 68.00, 252.00, 250.00, 254.00, 252.00 union all
    select 66.00, 257.00, 255.00, 259.00, 257.00 union all
    select 64.00, 262.00, 260.00, 264.00, 262.00 union all
    select 62.00, 267.00, 265.00, 269.00, 267.00 union all
    select 60.00, 272.00, 270.00, 274.00, 272.00 union all
    select 50.00, 292.00, 290.00, 284.00, 282.00 union all
    select 40.00, 312.00, 310.00, 294.00, 292.00 union all
    select 30.00, 332.00, 330.00, 304.00, 302.00 union all
    select 20.00, 352.00, 350.00, 314.00, 312.00 union all
    select 10.00, 372.00, 370.00, 324.00, 322.00
) endurance_run
union all
select 'ENDURANCE_RUN', 'male', 'junior_senior', score, male_js, 1.00, 'LOWER_BETTER' from (
    select 100.00 as score, 197.00 as male_fs, 195.00 as male_js, 198.00 as female_fs, 196.00 as female_js union all
    select 95.00, 202.00, 200.00, 204.00, 202.00 union all
    select 90.00, 207.00, 205.00, 210.00, 208.00 union all
    select 85.00, 214.00, 212.00, 217.00, 215.00 union all
    select 80.00, 222.00, 220.00, 224.00, 222.00 union all
    select 78.00, 227.00, 225.00, 229.00, 227.00 union all
    select 76.00, 232.00, 230.00, 234.00, 232.00 union all
    select 74.00, 237.00, 235.00, 239.00, 237.00 union all
    select 72.00, 242.00, 240.00, 244.00, 242.00 union all
    select 70.00, 247.00, 245.00, 249.00, 247.00 union all
    select 68.00, 252.00, 250.00, 254.00, 252.00 union all
    select 66.00, 257.00, 255.00, 259.00, 257.00 union all
    select 64.00, 262.00, 260.00, 264.00, 262.00 union all
    select 62.00, 267.00, 265.00, 269.00, 267.00 union all
    select 60.00, 272.00, 270.00, 274.00, 272.00 union all
    select 50.00, 292.00, 290.00, 284.00, 282.00 union all
    select 40.00, 312.00, 310.00, 294.00, 292.00 union all
    select 30.00, 332.00, 330.00, 304.00, 302.00 union all
    select 20.00, 352.00, 350.00, 314.00, 312.00 union all
    select 10.00, 372.00, 370.00, 324.00, 322.00
) endurance_run
union all
select 'ENDURANCE_RUN', 'female', 'freshman_sophomore', score, female_fs, 1.00, 'LOWER_BETTER' from (
    select 100.00 as score, 197.00 as male_fs, 195.00 as male_js, 198.00 as female_fs, 196.00 as female_js union all
    select 95.00, 202.00, 200.00, 204.00, 202.00 union all
    select 90.00, 207.00, 205.00, 210.00, 208.00 union all
    select 85.00, 214.00, 212.00, 217.00, 215.00 union all
    select 80.00, 222.00, 220.00, 224.00, 222.00 union all
    select 78.00, 227.00, 225.00, 229.00, 227.00 union all
    select 76.00, 232.00, 230.00, 234.00, 232.00 union all
    select 74.00, 237.00, 235.00, 239.00, 237.00 union all
    select 72.00, 242.00, 240.00, 244.00, 242.00 union all
    select 70.00, 247.00, 245.00, 249.00, 247.00 union all
    select 68.00, 252.00, 250.00, 254.00, 252.00 union all
    select 66.00, 257.00, 255.00, 259.00, 257.00 union all
    select 64.00, 262.00, 260.00, 264.00, 262.00 union all
    select 62.00, 267.00, 265.00, 269.00, 267.00 union all
    select 60.00, 272.00, 270.00, 274.00, 272.00 union all
    select 50.00, 292.00, 290.00, 284.00, 282.00 union all
    select 40.00, 312.00, 310.00, 294.00, 292.00 union all
    select 30.00, 332.00, 330.00, 304.00, 302.00 union all
    select 20.00, 352.00, 350.00, 314.00, 312.00 union all
    select 10.00, 372.00, 370.00, 324.00, 322.00
) endurance_run
union all
select 'ENDURANCE_RUN', 'female', 'junior_senior', score, female_js, 1.00, 'LOWER_BETTER' from (
    select 100.00 as score, 197.00 as male_fs, 195.00 as male_js, 198.00 as female_fs, 196.00 as female_js union all
    select 95.00, 202.00, 200.00, 204.00, 202.00 union all
    select 90.00, 207.00, 205.00, 210.00, 208.00 union all
    select 85.00, 214.00, 212.00, 217.00, 215.00 union all
    select 80.00, 222.00, 220.00, 224.00, 222.00 union all
    select 78.00, 227.00, 225.00, 229.00, 227.00 union all
    select 76.00, 232.00, 230.00, 234.00, 232.00 union all
    select 74.00, 237.00, 235.00, 239.00, 237.00 union all
    select 72.00, 242.00, 240.00, 244.00, 242.00 union all
    select 70.00, 247.00, 245.00, 249.00, 247.00 union all
    select 68.00, 252.00, 250.00, 254.00, 252.00 union all
    select 66.00, 257.00, 255.00, 259.00, 257.00 union all
    select 64.00, 262.00, 260.00, 264.00, 262.00 union all
    select 62.00, 267.00, 265.00, 269.00, 267.00 union all
    select 60.00, 272.00, 270.00, 274.00, 272.00 union all
    select 50.00, 292.00, 290.00, 284.00, 282.00 union all
    select 40.00, 312.00, 310.00, 294.00, 292.00 union all
    select 30.00, 332.00, 330.00, 304.00, 302.00 union all
    select 20.00, 352.00, 350.00, 314.00, 312.00 union all
    select 10.00, 372.00, 370.00, 324.00, 322.00
) endurance_run;

insert into tmp_assessment_rule_source (itemCode, gender, gradeGroup, score, thresholdValue, unitStep, direction)
select 'ENDURANCE_RUN', 'male', 'freshman_sophomore', score, male_fs, 1.00, 'LOWER_BETTER' from (
    select 101.00 as score, 193.00 as male_fs, 191.00 as male_js, 193.00 as female_fs, 191.00 as female_js union all
    select 102.00, 189.00, 187.00, 188.00, 186.00 union all
    select 103.00, 185.00, 183.00, 183.00, 181.00 union all
    select 104.00, 181.00, 179.00, 178.00, 176.00 union all
    select 105.00, 177.00, 175.00, 173.00, 171.00 union all
    select 106.00, 174.00, 172.00, 168.00, 166.00 union all
    select 107.00, 171.00, 169.00, 163.00, 161.00 union all
    select 108.00, 168.00, 166.00, 158.00, 156.00 union all
    select 109.00, 165.00, 163.00, 153.00, 151.00 union all
    select 110.00, 162.00, 160.00, 148.00, 146.00
) endurance_bonus
union all
select 'ENDURANCE_RUN', 'male', 'junior_senior', score, male_js, 1.00, 'LOWER_BETTER' from (
    select 101.00 as score, 193.00 as male_fs, 191.00 as male_js, 193.00 as female_fs, 191.00 as female_js union all
    select 102.00, 189.00, 187.00, 188.00, 186.00 union all
    select 103.00, 185.00, 183.00, 183.00, 181.00 union all
    select 104.00, 181.00, 179.00, 178.00, 176.00 union all
    select 105.00, 177.00, 175.00, 173.00, 171.00 union all
    select 106.00, 174.00, 172.00, 168.00, 166.00 union all
    select 107.00, 171.00, 169.00, 163.00, 161.00 union all
    select 108.00, 168.00, 166.00, 158.00, 156.00 union all
    select 109.00, 165.00, 163.00, 153.00, 151.00 union all
    select 110.00, 162.00, 160.00, 148.00, 146.00
) endurance_bonus
union all
select 'ENDURANCE_RUN', 'female', 'freshman_sophomore', score, female_fs, 1.00, 'LOWER_BETTER' from (
    select 101.00 as score, 193.00 as male_fs, 191.00 as male_js, 193.00 as female_fs, 191.00 as female_js union all
    select 102.00, 189.00, 187.00, 188.00, 186.00 union all
    select 103.00, 185.00, 183.00, 183.00, 181.00 union all
    select 104.00, 181.00, 179.00, 178.00, 176.00 union all
    select 105.00, 177.00, 175.00, 173.00, 171.00 union all
    select 106.00, 174.00, 172.00, 168.00, 166.00 union all
    select 107.00, 171.00, 169.00, 163.00, 161.00 union all
    select 108.00, 168.00, 166.00, 158.00, 156.00 union all
    select 109.00, 165.00, 163.00, 153.00, 151.00 union all
    select 110.00, 162.00, 160.00, 148.00, 146.00
) endurance_bonus
union all
select 'ENDURANCE_RUN', 'female', 'junior_senior', score, female_js, 1.00, 'LOWER_BETTER' from (
    select 101.00 as score, 193.00 as male_fs, 191.00 as male_js, 193.00 as female_fs, 191.00 as female_js union all
    select 102.00, 189.00, 187.00, 188.00, 186.00 union all
    select 103.00, 185.00, 183.00, 183.00, 181.00 union all
    select 104.00, 181.00, 179.00, 178.00, 176.00 union all
    select 105.00, 177.00, 175.00, 173.00, 171.00 union all
    select 106.00, 174.00, 172.00, 168.00, 166.00 union all
    select 107.00, 171.00, 169.00, 163.00, 161.00 union all
    select 108.00, 168.00, 166.00, 158.00, 156.00 union all
    select 109.00, 165.00, 163.00, 153.00, 151.00 union all
    select 110.00, 162.00, 160.00, 148.00, 146.00
) endurance_bonus;

-- ============================================================
-- Generate interval rules for higher-better items
-- ============================================================
insert into assessment_rule
(
    schemeCode,
    itemCode,
    gender,
    gradeGroup,
    score,
    minValue,
    `maxValue`,
    comparisonType,
    ruleVersion,
    sortOrder,
    description
)
select
    @scheme_code,
    itemCode,
    gender,
    gradeGroup,
    score,
    cast(thresholdValue as decimal(10, 2)),
    cast(case when prev_threshold is null then null else prev_threshold - unitStep end as decimal(10, 2)),
    'RANGE',
    @rule_version,
    sort_order,
    null
from (
    select
        itemCode,
        gender,
        gradeGroup,
        score,
        thresholdValue,
        unitStep,
        lag(thresholdValue) over (
            partition by itemCode, gender, gradeGroup
            order by score desc
        ) as prev_threshold,
        row_number() over (
            partition by itemCode, gender, gradeGroup
            order by score desc
        ) as sort_order
    from tmp_assessment_rule_source
    where direction = 'HIGHER_BETTER'
) higher_rules;

insert into assessment_rule
(
    schemeCode,
    itemCode,
    gender,
    gradeGroup,
    score,
    minValue,
    `maxValue`,
    comparisonType,
    ruleVersion,
    sortOrder,
    description
)
select
    @scheme_code,
    itemCode,
    gender,
    gradeGroup,
    0.00,
    null,
    cast(thresholdValue - unitStep as decimal(10, 2)),
    'RANGE',
    @rule_version,
    rule_count + 1,
    'auto generated 0-point interval'
from (
    select
        itemCode,
        gender,
        gradeGroup,
        thresholdValue,
        unitStep,
        row_number() over (
            partition by itemCode, gender, gradeGroup
            order by score asc
        ) as rn,
        count(*) over (
            partition by itemCode, gender, gradeGroup
        ) as rule_count
    from tmp_assessment_rule_source
    where direction = 'HIGHER_BETTER'
) higher_zero_rules
where rn = 1;

-- ============================================================
-- Generate interval rules for lower-better items
-- ============================================================
insert into assessment_rule
(
    schemeCode,
    itemCode,
    gender,
    gradeGroup,
    score,
    minValue,
    `maxValue`,
    comparisonType,
    ruleVersion,
    sortOrder,
    description
)
select
    @scheme_code,
    itemCode,
    gender,
    gradeGroup,
    score,
    cast(case when prev_threshold is null then null else prev_threshold + unitStep end as decimal(10, 2)),
    cast(thresholdValue as decimal(10, 2)),
    'RANGE',
    @rule_version,
    sort_order,
    null
from (
    select
        itemCode,
        gender,
        gradeGroup,
        score,
        thresholdValue,
        unitStep,
        lag(thresholdValue) over (
            partition by itemCode, gender, gradeGroup
            order by score desc
        ) as prev_threshold,
        row_number() over (
            partition by itemCode, gender, gradeGroup
            order by score desc
        ) as sort_order
    from tmp_assessment_rule_source
    where direction = 'LOWER_BETTER'
) lower_rules;

insert into assessment_rule
(
    schemeCode,
    itemCode,
    gender,
    gradeGroup,
    score,
    minValue,
    `maxValue`,
    comparisonType,
    ruleVersion,
    sortOrder,
    description
)
select
    @scheme_code,
    itemCode,
    gender,
    gradeGroup,
    0.00,
    cast(thresholdValue + unitStep as decimal(10, 2)),
    null,
    'RANGE',
    @rule_version,
    rule_count + 1,
    'auto generated 0-point interval'
from (
    select
        itemCode,
        gender,
        gradeGroup,
        thresholdValue,
        unitStep,
        row_number() over (
            partition by itemCode, gender, gradeGroup
            order by score asc
        ) as rn,
        count(*) over (
            partition by itemCode, gender, gradeGroup
        ) as rule_count
    from tmp_assessment_rule_source
    where direction = 'LOWER_BETTER'
) lower_zero_rules
where rn = 1;

drop temporary table if exists tmp_assessment_rule_source;

commit;
