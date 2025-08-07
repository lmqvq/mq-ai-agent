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