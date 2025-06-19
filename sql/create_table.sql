-- 创建库
create database if not exists mq_ai_agent;

-- 切换库
use mq_ai_agent;

create table if not exists keep_report
(
    id       bigint auto_increment comment 'id' primary key,
    chat_id  varchar(255) not null comment '对话id',
    messages text         not null comment '对话记录（JSON格式存储）',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '健身报告记录表' collate = utf8mb4_unicode_ci;