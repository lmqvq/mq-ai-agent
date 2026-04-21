-- ============================================================
-- 标准化评测模块建表脚本
--
-- 说明：
-- 1. 本脚本用于 mq-ai-agent 项目的 assessment 模块
-- 2. 当前定位是“通用评测能力”，大学生体测只是第一套内置方案
-- 3. 为了保持与现有项目风格一致，本脚本使用逻辑删除，不额外增加外键约束
-- ============================================================

create database if not exists mq_ai_agent;

use mq_ai_agent;

-- 1. 评测方案表
create table if not exists assessment_scheme
(
    id          bigint auto_increment comment 'id' primary key,
    schemeCode  varchar(64)                         not null comment '方案编码',
    schemeName  varchar(128)                        not null comment '方案名称',
    sceneType   varchar(64)                         not null comment '场景类型，如 student_physical_health',
    description varchar(512)                        null comment '方案说明',
    version     varchar(32) default 'v1'            not null comment '方案版本',
    source      varchar(255)                        null comment '标准来源',
    configJson  text                                null comment '扩展配置JSON',
    status      tinyint     default 1               not null comment '状态：0-禁用 1-启用',
    createTime  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint     default 0               not null comment '是否删除',
    unique key uk_scheme_code (schemeCode),
    index idx_scene_type (sceneType, status, isDelete)
) comment '评测方案表' collate = utf8mb4_unicode_ci;

-- 2. 评测方案项目定义表
create table if not exists assessment_scheme_item
(
    id                   bigint auto_increment comment 'id' primary key,
    schemeCode           varchar(64)                         not null comment '方案编码',
    itemCode             varchar(64)                         not null comment '项目编码',
    itemName             varchar(128)                        not null comment '项目名称',
    itemCategory         varchar(64)                         null comment '项目分类',
    unit                 varchar(32)                         not null comment '单位',
    inputType            varchar(32) default 'number'       not null comment '输入类型',
    inputPrecision       int         default 2               not null comment '输入精度',
    weight               decimal(5, 2) default 0.00         not null comment '权重',
    displayOrder         int         default 0               not null comment '展示顺序',
    applicableGender     varchar(32) default 'all'          not null comment '适用性别：all/male/female',
    applicableGradeGroup varchar(64) default 'all'          not null comment '适用年级组',
    validationMin        decimal(10, 2)                     null comment '允许最小输入值',
    validationMax        decimal(10, 2)                     null comment '允许最大输入值',
    isRequired           tinyint     default 1               not null comment '是否必填',
    isBonusItem          tinyint     default 0               not null comment '是否为附加分项目',
    description          varchar(512)                        null comment '项目说明',
    createTime           datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime           datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete             tinyint     default 0               not null comment '是否删除',
    unique key uk_scheme_item (schemeCode, itemCode),
    index idx_scheme_display (schemeCode, displayOrder, isDelete),
    index idx_gender_grade (schemeCode, applicableGender, applicableGradeGroup, isDelete)
) comment '评测方案项目定义表' collate = utf8mb4_unicode_ci;

-- 3. 评测规则表
create table if not exists assessment_rule
(
    id             bigint auto_increment comment 'id' primary key,
    schemeCode     varchar(64)                         not null comment '方案编码',
    itemCode       varchar(64)                         not null comment '项目编码',
    gender         varchar(32) default 'all'          not null comment '性别：all/male/female',
    gradeGroup     varchar(64) default 'all'          not null comment '年级组',
    score          decimal(6, 2)                       not null comment '对应得分',
    minValue       decimal(10, 2)                      null comment '匹配最小值',
    `maxValue`     decimal(10, 2)                      null comment '匹配最大值',
    comparisonType varchar(32) default 'RANGE'        not null comment '比较方式：RANGE/EXACT',
    ruleVersion    varchar(32) default 'v1'           not null comment '规则版本',
    sortOrder      int         default 0               not null comment '规则顺序',
    description    varchar(255)                        null comment '规则说明',
    createTime     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint     default 0               not null comment '是否删除',
    unique key uk_rule_key (schemeCode, itemCode, gender, gradeGroup, ruleVersion, sortOrder),
    index idx_scheme_item (schemeCode, itemCode, isDelete),
    index idx_match_key (schemeCode, itemCode, gender, gradeGroup, ruleVersion, isDelete)
) comment '评测规则表' collate = utf8mb4_unicode_ci;

-- 4. 用户评测画像表
create table if not exists assessment_profile
(
    id               bigint auto_increment comment 'id' primary key,
    userId           bigint                              not null comment '用户ID',
    schemeCode       varchar(64)                         not null comment '方案编码',
    gender           varchar(32)                         not null comment '性别：male/female',
    grade            varchar(32)                         null comment '年级，如 大一/大二',
    gradeGroup       varchar(64)                         null comment '年级组，如 freshman_sophomore',
    height           decimal(6, 2)                       null comment '身高(cm)',
    weight           decimal(6, 2)                       null comment '体重(kg)',
    bmi              decimal(6, 2)                       null comment 'BMI',
    extraProfileJson text                                null comment '扩展画像JSON',
    createTime       datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint     default 0               not null comment '是否删除',
    unique key uk_user_scheme (userId, schemeCode),
    index idx_scheme_gender_grade (schemeCode, gender, gradeGroup, isDelete)
) comment '用户评测画像表' collate = utf8mb4_unicode_ci;

-- 5. 评测记录主表
create table if not exists assessment_record
(
    id                 bigint auto_increment comment 'id' primary key,
    userId             bigint                              not null comment '用户ID',
    schemeCode         varchar(64)                         not null comment '方案编码',
    schemeVersion      varchar(32) default 'v1'           not null comment '方案版本快照',
    assessmentDate     datetime                            not null comment '评测日期',
    sourceType         varchar(32) default 'manual'       not null comment '数据来源：manual/import/upload',
    genderSnapshot     varchar(32)                         null comment '性别快照',
    gradeSnapshot      varchar(32)                         null comment '年级快照',
    gradeGroupSnapshot varchar(64)                         null comment '年级组快照',
    heightSnapshot     decimal(6, 2)                       null comment '身高快照(cm)',
    weightSnapshot     decimal(6, 2)                       null comment '体重快照(kg)',
    bmiSnapshot        decimal(6, 2)                       null comment 'BMI快照',
    totalScore         decimal(6, 2) default 0.00         not null comment '总分',
    level              varchar(32)                         null comment '等级',
    weaknessCount      int         default 0               not null comment '弱项数量',
    strengthCount      int         default 0               not null comment '强项数量',
    summary            varchar(1024)                       null comment '简要摘要',
    extraDataJson      text                                null comment '扩展数据JSON',
    createTime         datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime         datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete           tinyint     default 0               not null comment '是否删除',
    index idx_user_scheme_date (userId, schemeCode, assessmentDate, isDelete),
    index idx_scheme_level (schemeCode, level, assessmentDate, isDelete)
) comment '评测记录主表' collate = utf8mb4_unicode_ci;

-- 6. 评测记录明细表
create table if not exists assessment_record_item
(
    id          bigint auto_increment comment 'id' primary key,
    recordId    bigint                              not null comment '评测记录ID',
    itemCode    varchar(64)                         not null comment '项目编码',
    itemName    varchar(128)                        not null comment '项目名称快照',
    itemOrder   int         default 0               not null comment '项目顺序快照',
    unit        varchar(32)                         null comment '单位快照',
    rawValue    decimal(10, 2)                      not null comment '原始成绩',
    itemWeight  decimal(5, 2) default 0.00         not null comment '项目权重快照',
    itemScore   decimal(6, 2) default 0.00         not null comment '单项分',
    extraScore  decimal(6, 2) default 0.00         not null comment '附加分',
    itemLevel   varchar(32)                         null comment '单项等级',
    isWeakness  tinyint     default 0               not null comment '是否弱项',
    isStrength  tinyint     default 0               not null comment '是否强项',
    remark      varchar(255)                        null comment '备注',
    createTime  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint     default 0               not null comment '是否删除',
    unique key uk_record_item (recordId, itemCode),
    index idx_record_order (recordId, itemOrder, isDelete),
    index idx_item_code (itemCode, isDelete)
) comment '评测记录明细表' collate = utf8mb4_unicode_ci;

-- 7. 评测报告表
create table if not exists assessment_report
(
    id              bigint auto_increment comment 'id' primary key,
    recordId        bigint                              not null comment '评测记录ID',
    version         varchar(32) default 'v1'           not null comment '报告版本',
    overview        varchar(1024)                       null comment '总览摘要',
    analysisJson    text                                null comment '结构化分析JSON',
    weaknessSummary varchar(1024)                       null comment '弱项总结',
    strengthSummary varchar(1024)                       null comment '强项总结',
    trainingFocus   varchar(1024)                       null comment '训练重点',
    riskNotes       varchar(1024)                       null comment '风险提示',
    aiSuggestion    text                                null comment 'AI建议',
    createTime      datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint     default 0               not null comment '是否删除',
    unique key uk_record_id (recordId),
    index idx_create_time (createTime, isDelete)
) comment '评测报告表' collate = utf8mb4_unicode_ci;
