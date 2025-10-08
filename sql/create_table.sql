-- 创建库
create database if not exists mq_ai_agent;

-- 切换库
use mq_ai_agent;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/vip/ban',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

create table if not exists keep_report
(
    id         bigint auto_increment comment 'id' primary key,
    chatId     varchar(255)                       not null comment '对话id',
    userId     bigint                             not null comment '创建用户id',
    messages   text                               not null comment '对话记录（JSON格式存储）',
    lastMessage text                              null comment '最后一条消息内容（用于列表展示）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    INDEX idx_user_chat (chatId, userId),  -- 新增索引，优化查询效率
    INDEX idx_user_time (userId, updateTime)  -- 按用户和时间查询的索引
) comment '上下文对话表' collate = utf8mb4_unicode_ci;

-- 健身数据表
create table if not exists fitness_data
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                             not null comment '用户id',
    weight       float                              not null comment '体重(kg)',
    bodyFat      float                              null comment '体脂率(%)',
    height       float                              not null comment '身高(cm)',
    bmi          float                              null comment 'BMI指数',
    dateRecorded datetime default CURRENT_TIMESTAMP not null comment '数据记录时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_user_data (userId, dateRecorded)      -- 优化查询效率
) comment '用户健身数据表' collate = utf8mb4_unicode_ci;

-- 运动记录表
create table if not exists exercise_log
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                             not null comment '用户id',
    exerciseType varchar(256)                       not null comment '运动类型（如：力量训练、有氧、瑜伽等）',
    duration     int                                not null comment '运动时长（分钟）',
    caloriesBurned float                            not null comment '消耗卡路里',
    dateRecorded datetime default CURRENT_TIMESTAMP not null comment '运动记录时间',
    notes        text                               null comment '运动备注（如：训练动作、强度感受等）',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_user_exercise (userId, dateRecorded)   -- 优化查询效率
) comment '运动记录表' collate = utf8mb4_unicode_ci;

-- 健身排行榜功能数据库更新脚本
use mq_ai_agent;

-- 1. 为 exercise_log 表添加字段（用于排行榜周期统计）
ALTER TABLE exercise_log
    ADD COLUMN weekStartDate DATE COMMENT '所属周的开始日期（周一）' AFTER dateRecorded,
    ADD COLUMN monthStartDate DATE COMMENT '所属月的开始日期（30天前）' AFTER weekStartDate;

-- 2. 添加索引以优化查询性能
ALTER TABLE exercise_log
    ADD INDEX idx_week_start (userId, weekStartDate, isDelete),
    ADD INDEX idx_month_start (userId, monthStartDate, isDelete);

-- 3. 创建排行榜快照表（用于数据恢复和历史记录）
CREATE TABLE IF NOT EXISTS ranking_snapshot
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userId          BIGINT                             NOT NULL COMMENT '用户ID',
    rankingType     VARCHAR(20)                        NOT NULL COMMENT '排行类型：week-周榜, month-月榜',
    startDate       DATE                               NOT NULL COMMENT '统计开始日期',
    exerciseCount   INT                                NOT NULL COMMENT '运动记录次数',
    firstRecordTime BIGINT                             NOT NULL COMMENT '首次记录时间戳（秒）',
    rankPosition    INT                                NOT NULL COMMENT '排名',
    totalMinutes    INT      DEFAULT 0                 NOT NULL COMMENT '总运动时长（分钟）',
    totalCalories   FLOAT    DEFAULT 0                 NOT NULL COMMENT '总消耗卡路里',
    snapshotTime    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '快照时间',
    createTime      DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    INDEX idx_type_date (rankingType, startDate),
    INDEX idx_user_type (userId, rankingType, startDate),
    UNIQUE KEY uk_user_type_date (userId, rankingType, startDate)
) COMMENT '排行榜快照表（用于数据恢复和历史查询）' COLLATE = utf8mb4_unicode_ci;