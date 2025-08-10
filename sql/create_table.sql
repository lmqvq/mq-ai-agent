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

-- 训练计划表
create table if not exists training_plan
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                             not null comment '用户id',
    planName     varchar(256)                       not null comment '计划名称',
    planType     varchar(255)                       null comment '计划类型（增肌、减脂、塑形等）',
    planDetails  text                               not null comment '训练计划详细信息（JSON格式存储）',
    isDefault    tinyint  default 0                 not null comment '是否为默认训练计划（0：否，1：是）',
    startDate    datetime default CURRENT_TIMESTAMP not null comment '计划开始时间',
    endDate      datetime                           null comment '计划结束时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_user_plan (userId, startDate)         -- 优化查询效率
) comment '用户训练计划表' collate = utf8mb4_unicode_ci;

-- 健身目标表
create table if not exists fitness_goal
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint                             not null comment '用户id',
    goalType     varchar(255)                       not null comment '目标类型（增肌、减脂、体态改善等）',
    targetValue  varchar(255)                       not null comment '目标值（如：体脂率降到15%、体重增到70kg）',
    startDate    datetime default CURRENT_TIMESTAMP not null comment '目标开始时间',
    endDate      datetime                           null comment '目标结束时间',
    progress     text                               null comment '进度记录（JSON格式）',
    isAchieved   tinyint  default 0                 not null comment '是否达成（0-未达成，1-已达成）',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_user_goal (userId, startDate)         -- 优化查询效率
) comment '健身目标表' collate = utf8mb4_unicode_ci;