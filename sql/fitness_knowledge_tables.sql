-- 健身知识模块数据库表创建脚本
-- 用于存储健身知识、动作指导、营养知识、训练计划等内容

-- 切换到数据库
use mq_ai_agent;

-- 1. 基础知识表
create table if not exists knowledge_basics
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(256)                       not null comment '标题',
    description varchar(512)                       null comment '简短描述',
    image       varchar(1024)                      null comment '图片URL',
    difficulty  varchar(50)                        null comment '难度级别（初级/中级/高级）',
    readTime    int                                null comment '阅读时长（分钟）',
    views       int      default 0                 not null comment '浏览次数',
    content     text                               null comment '详细内容',
    tips        json                               null comment '提示数组（JSON格式）',
    sortOrder   int      default 0                 not null comment '排序字段',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_difficulty (difficulty, isDelete),
    INDEX idx_sort (sortOrder, isDelete)
) comment '健身基础知识表' collate = utf8mb4_unicode_ci;

-- 2. 动作指导表
create table if not exists knowledge_exercise
(
    id           bigint auto_increment comment 'id' primary key,
    name         varchar(256)                       not null comment '动作名称',
    category     varchar(100)                       not null comment '分类（胸部训练/背部训练/腿部训练等）',
    description  varchar(512)                       null comment '简短描述',
    image        varchar(1024)                      null comment '图片URL',
    muscleGroup  varchar(100)                       null comment '目标肌群',
    difficulty   varchar(50)                        null comment '难度级别（初级/中级/高级）',
    instructions json                               null comment '动作步骤数组（JSON格式）',
    tips         json                               null comment '提示数组（JSON格式）',
    sortOrder    int      default 0                 not null comment '排序字段',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_category (category, isDelete),
    INDEX idx_difficulty (difficulty, isDelete),
    INDEX idx_muscle (muscleGroup, isDelete),
    INDEX idx_sort (sortOrder, isDelete)
) comment '健身动作指导表' collate = utf8mb4_unicode_ci;

-- 3. 营养知识表
create table if not exists knowledge_nutrient
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '营养素名称',
    description varchar(512)                       null comment '简短描述',
    color       varchar(50)                        null comment '显示颜色（用于前端展示）',
    icon        varchar(100)                       null comment '图标标识',
    benefits    json                               null comment '益处数组（JSON格式）',
    sortOrder   int      default 0                 not null comment '排序字段',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_sort (sortOrder, isDelete)
) comment '营养知识表' collate = utf8mb4_unicode_ci;

-- 4. 训练计划表
create table if not exists knowledge_program
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '计划名称',
    description varchar(512)                       null comment '简短描述',
    type        varchar(100)                       null comment '类型（beginner/intermediate/advanced/fat-loss等）',
    icon        varchar(100)                       null comment '图标标识',
    duration    varchar(100)                       null comment '持续时间',
    intensity   varchar(100)                       null comment '强度级别',
    level       varchar(50)                        null comment '适合级别（初级/中级/高级）',
    schedule    json                               null comment '训练安排数组（JSON格式）',
    sortOrder   int      default 0                 not null comment '排序字段',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_type (type, isDelete),
    INDEX idx_level (level, isDelete),
    INDEX idx_sort (sortOrder, isDelete)
) comment '训练计划表' collate = utf8mb4_unicode_ci;

