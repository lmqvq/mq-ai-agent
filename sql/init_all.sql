-- ============================================================
-- MQ-AI-Agent 数据库完整初始化脚本
--
-- 说明：本脚本包含项目所需的全部数据库表结构、初始化数据和 assessment 评分规则
-- 使用方法：直接在 MySQL 中运行此脚本即可完成初始化
--
-- 包含表：
--   1. user - 用户表
--   2. keep_report - 上下文对话表
--   3. fitness_data - 用户健身数据表
--   4. exercise_log - 运动记录表
--   5. ranking_snapshot - 排行榜快照表
--   6. knowledge_basics - 健身基础知识表
--   7. knowledge_exercise - 健身动作指导表
--   8. knowledge_nutrient - 营养知识表
--   9. knowledge_program - 训练计划表
--  10. knowledge_meal - 饮食计划表
--  11. assessment_scheme - 评测方案表
--  12. assessment_scheme_item - 评测方案项目表
--  13. assessment_rule - 评测规则表
--  14. assessment_profile - 评测画像表
--  15. assessment_record - 评测记录主表
--  16. assessment_record_item - 评测记录明细表
--  17. assessment_report - 评测报告表
-- ============================================================

-- ============================================================
-- 第一部分：基础业务表
-- ============================================================
-- 创建库
create database if not exists mq_ai_agent;

-- 切换库
use mq_ai_agent;

-- 1. 用户表
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

-- 2. 上下文对话表
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

-- 3. 用户健身数据表
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

-- 4. 运动记录表
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

-- 为 exercise_log 表添加字段（用于排行榜周期统计）
ALTER TABLE exercise_log
    ADD COLUMN weekStartDate DATE COMMENT '所属周的开始日期（周一）' AFTER dateRecorded,
    ADD COLUMN monthStartDate DATE COMMENT '所属月的开始日期（30天前）' AFTER weekStartDate;

-- 添加索引以优化查询性能
ALTER TABLE exercise_log
    ADD INDEX idx_week_start (userId, weekStartDate, isDelete),
    ADD INDEX idx_month_start (userId, monthStartDate, isDelete);

-- 5. 创建排行榜快照表（用于数据恢复和历史记录）
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

-- ============================================================
-- 第二部分：健身知识模块表
-- ============================================================
-- 健身知识模块数据库表创建脚本
-- 用于存储健身知识、动作指导、营养知识、训练计划等内容

-- 切换到数据库
use mq_ai_agent;

-- 6. 健身基础知识表
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

-- 7. 健身动作指导表
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

-- 8. 营养知识表
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

-- 9. 训练计划表
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

-- 10. 饮食计划表
-- 饮食计划表创建脚本
-- 用于存储健身营养模块中的推荐饮食计划
create table if not exists knowledge_meal
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)                       not null comment '餐食名称',
    mealType    varchar(100)                       not null comment '餐食类型（早餐/午餐/晚餐/加餐/训练前/训练后）',
    description varchar(512)                       null comment '简短描述',
    image       varchar(1024)                      null comment '图片URL',
    calories    int                                not null comment '卡路里(kcal)',
    protein     int                                not null comment '蛋白质(g)',
    carbs       int                                not null comment '碳水化合物(g)',
    fat         int                                not null comment '脂肪(g)',
    sortOrder   int      default 0                 not null comment '排序字段',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_type (mealType, isDelete),
    INDEX idx_sort (sortOrder, isDelete)
) comment '饮食计划表' collate = utf8mb4_unicode_ci;

-- ============================================================
-- 第三部分：测试数据初始化
-- ============================================================
-- 健身知识库数据初始化SQL
-- 使用前请确保已创建相关表结构

-- 切换到数据库
USE mq_ai_agent;

-- 1. 基础知识表数据 (knowledge_basics)
INSERT INTO knowledge_basics (id, title, description, image, difficulty, readTime, views, content, tips, sortOrder, createTime, updateTime, isDelete) VALUES
(1, '健身入门指南', '新手必读的健身基础知识，从零开始学习健身', '', '初级', 5, 1234, 
'健身是一个循序渐进的过程。作为初学者，首先要明确自己的健身目标：是为了减脂、增肌还是提升体能？不同的目标需要不同的训练方式。建议新手从每周3-4次的全身训练开始，每次训练30-45分钟，逐步适应运动强度。记住，坚持比强度更重要，循序渐进才能避免受伤并获得长期效果。',
'["制定合理的训练计划", "注意休息和恢复", "保持饮食均衡", "学习正确的动作姿势", "记录训练进度"]', 1, NOW(), NOW(), 0),

(2, '肌肉增长原理', '了解肌肉是如何增长的，掌握增肌的科学方法', '', '中级', 8, 856, 
'肌肉增长（肌肥大）需要三个关键因素：机械张力、肌肉损伤和代谢压力。通过阻力训练造成肌纤维微损伤，身体在恢复期通过蛋白质合成修复并增强肌纤维。渐进式超负荷是增肌的核心原则——持续增加训练强度、重量或次数，迫使肌肉适应更大的负荷。同时，充足的蛋白质摄入（每公斤体重1.6-2.2克）和高质量睡眠（7-9小时）是肌肉恢复和生长的必要条件。',
'["保证充足的蛋白质摄入（体重kg×1.8-2.2g）", "渐进式超负荷训练", "充足的睡眠（7-9小时）", "训练后48-72小时恢复期", "适度热量盈余（300-500kcal/天）"]', 2, NOW(), NOW(), 0),

(3, '科学减脂攻略', '掌握正确的减脂方法，健康高效地降低体脂率', '', '初级', 7, 2341, 
'减脂的核心原理是热量缺口，即消耗的热量大于摄入的热量。建议每日热量赤字控制在300-500卡，这样每周可以减重0.5-1kg，既能保持肌肉量，又能持续减脂。结合力量训练和有氧运动效果最佳：力量训练维持肌肉量提高基础代谢，有氧运动增加热量消耗。避免过度节食，保证每日蛋白质摄入（1.8-2.2g/kg体重），多吃蔬菜水果和全谷物，控制精制碳水和高脂肪食物。',
'["每日热量赤字300-500卡", "保持高蛋白饮食", "结合力量训练和有氧", "每周减重0.5-1kg为宜", "多喝水保持代谢", "避免极端节食"]', 3, NOW(), NOW(), 0),

(4, '核心力量训练的重要性', '强大的核心是一切运动的基础，提升运动表现和预防损伤', '', '初级', 6, 1567, 
'核心肌群包括腹肌、背部肌群、骨盆底肌和髋部肌群，它们像身体的"动力中枢"，负责稳定脊柱、传递力量和保持平衡。强大的核心不仅能让你拥有好看的腹肌，更能提升所有运动的表现，减少下背痛和运动损伤风险。核心训练应该包括抗伸展（平板支撑）、抗旋转（侧平板）、抗屈曲（死虫式）等多个维度，而不仅仅是传统的仰卧起坐。',
'["每周2-3次核心专项训练", "注重动作质量而非数量", "包含多角度核心动作", "呼吸与动作配合", "从静态到动态逐步进阶"]', 4, NOW(), NOW(), 0),

(5, '运动后恢复的科学', '正确的恢复方法能让你训练效果翻倍，避免过度训练', '', '中级', 9, 1123, 
'恢复和训练同样重要。肌肉在休息时生长，而非训练时。充足睡眠是最佳恢复方式，深度睡眠时生长激素分泌达到峰值。运动后30分钟内是"黄金窗口期"，及时补充蛋白质和碳水化合物能加速肌肉修复。主动恢复如轻度有氧、拉伸、泡沫轴按摩能促进血液循环，加速代谢废物排出。过度训练的信号包括：持续疲劳、表现下降、睡眠质量差、情绪低落，出现这些症状应立即休息调整。',
'["保证每晚7-9小时高质量睡眠", "训练后及时补充营养", "主动恢复：拉伸、泡沫轴", "每周至少1-2天完全休息", "关注过度训练信号", "保持充足水分摄入"]', 5, NOW(), NOW(), 0),

(6, '柔韧性与活动度训练', '改善柔韧性和关节活动度，提升运动表现并减少受伤风险', '', '初级', 6, 892, 
'柔韧性是指肌肉的延展能力，而活动度是指关节的运动范围。良好的柔韧性和活动度能让你完成更标准的动作，激活更多肌肉纤维，同时降低拉伤和关节损伤风险。训练前应进行动态拉伸（如腿部摆动、臂部环绕）来激活肌肉和提高关节活动度；训练后则进行静态拉伸，每个部位保持15-30秒，促进肌肉放松和恢复。瑜伽和普拉提也是提升柔韧性的绝佳选择。',
'["训练前动态拉伸激活", "训练后静态拉伸放松", "每个拉伸保持15-30秒", "呼吸自然不要憋气", "循序渐进避免过度拉伸", "将柔韧训练纳入常规计划"]', 6, NOW(), NOW(), 0),

(7, '健身常见误区解析', '避开这些健身误区，让你的训练事半功倍', '', '初级', 7, 1876, 
'误区1：局部减脂——脂肪消耗是全身性的，无法通过某个部位的训练来减掉该部位的脂肪。误区2：女性力量训练会变壮——女性睾酮水平远低于男性，适度力量训练只会让身材更紧致。误区3：出汗越多效果越好——出汗只是调节体温，与燃脂效果无直接关系。误区4：蛋白质越多越好——过量蛋白质无法被利用，还会增加肾脏负担。误区5：每天训练效果更好——肌肉需要休息恢复，过度训练反而适得其反。',
'["减脂需要全身性方案", "女性应该进行力量训练", "关注训练质量而非出汗量", "蛋白质适量即可", "合理安排休息日", "避免盲目跟风"]', 7, NOW(), NOW(), 0),

(8, '如何突破健身平台期', '当进步停滞时，这些策略能帮你重新突破', '', '高级', 10, 745, 
'平台期是每个健身者都会遇到的挑战。身体适应了当前训练强度后，进步会变慢甚至停滞。突破方法：1.改变训练变量（重量、次数、组数、休息时间）；2.尝试新的训练方法（超级组、递减组、金字塔训练）；3.调整训练频率和分化方式；4.增加或减少训练量；5.检查饮食和睡眠是否充足；6.deload周（降低训练强度让身体完全恢复）。记住，有时候退一步是为了进两步。',
'["定期改变训练计划（每4-6周）", "尝试不同的训练方法", "每6-8周安排deload周", "重新评估饮食和恢复", "保持训练日志追踪进度", "保持耐心和信心"]', 8, NOW(), NOW(), 0);

-- 2. 动作指导表数据 (knowledge_exercise)
INSERT INTO knowledge_exercise (id, name, category, description, image, muscleGroup, difficulty, instructions, tips, sortOrder, createTime, updateTime, isDelete) VALUES
-- 胸部训练
(1, '俯卧撑', '胸部训练', '经典的胸部训练动作，适合各个水平的健身者', '', '胸肌', '初级',
'["双手撑地与肩同宽，手指朝前", "身体从头到脚保持一条直线，核心收紧", "弯曲手肘下降身体，直到胸部接近地面", "保持肘部与身体约45度角", "用力推起至起始位置，胸肌主动发力"]',
'["保持核心收紧避免塌腰", "动作要缓慢控制避免下落过快", "下降时吸气，推起时呼气", "初学者可从跪姿俯卧撑开始", "保持颈部中立不要低头或仰头"]', 1, NOW(), NOW(), 0),

(2, '哑铃卧推', '胸部训练', '最有效的胸肌增长动作，可独立训练两侧胸肌', '', '胸肌', '中级',
'["仰卧在平板凳上，双脚踏实地面", "双手各握一个哑铃，举至胸部上方", "缓慢下放哑铃至胸部两侧，感受胸肌拉伸", "大臂与身体约45度角", "用力推起哑铃至起始位置，胸肌主动收缩"]',
'["选择合适重量，保证动作质量", "下放时吸气，推起时呼气", "保持肩胛骨后收下沉", "避免手腕过度弯曲", "哑铃轨迹呈弧线而非直线"]', 2, NOW(), NOW(), 0),

(3, '双杠臂屈伸', '胸部训练', '上胸和下胸的极佳训练动作，被称为"上肢深蹲"', '', '胸肌下部', '中级',
'["双手撑在双杠上，身体悬空", "身体略前倾，激活胸肌", "弯曲手肘缓慢下降，直到肩部低于肘部", "胸肌发力推起身体回到起始位置", "全程保持身体稳定控制"]',
'["身体前倾练胸，直立练肱三头肌", "初学者可使用弹力带辅助", "避免肩部过度前伸", "下降时吸气，推起时呼气", "循序渐进增加下降深度"]', 3, NOW(), NOW(), 0),

-- 背部训练
(4, '引体向上', '背部训练', '背部训练的王者动作，全面强化背部肌群', '', '背阔肌', '中级',
'["正手抓住单杠，手距略宽于肩", "身体悬空，肩胛骨下沉后收", "背部肌群发力，将身体向上拉", "直到下巴超过单杠或胸部接近单杠", "控制速度缓慢下降至起始位置"]',
'["避免耸肩，保持肩部下沉", "用背部发力而非手臂", "下降时不要完全放松", "初学者可使用弹力带辅助", "每次都要做到全程运动"]', 4, NOW(), NOW(), 0),

(5, '哑铃划船', '背部训练', '增加背部厚度的经典动作，可单侧训练', '', '背阔肌中部', '初级',
'["单手和同侧膝盖支撑在平板凳上", "另一只手抓住哑铃，手臂自然下垂", "保持背部平直，核心稳定", "将哑铃向上拉至髋部侧面，手肘贴近身体", "挤压肩胛骨，然后控制下放"]',
'["手肘贴近身体，不要外展", "上拉时呼气，下放时吸气", "背部全程保持平直", "不要用惯性甩动哑铃", "感受背部肌群发力"]', 5, NOW(), NOW(), 0),

(6, '硬拉', '背部训练', '全身力量的综合动作，强化后侧链肌群', '', '下背/腿后', '高级',
'["双脚与髋同宽站立，杠铃置于脚前", "弯曲髋部下蹲，背部保持自然中立", "双手正反握或正手掌抓住杠铃", "腿部和背部同时发力，上拉杠铃", "站直后肩胛骨后收，然后控制下放"]',
'["保持背部中立，不要弓背", "动作开始时杠铃贴近胫骨", "上拉时呼气，下放时吸气", "初学者一定要请教练指导", "选择合适重量，不要勉强"]', 6, NOW(), NOW(), 0),

-- 腿部训练
(7, '深蹲', '腿部训练', '力量训练之王，全面发展下肢力量', '', '股四头肌/臀大肌', '中级',
'["双脚与肩同宽或略宽，脚尖微向外", "杠铃放在斜方肌上，不要压在颈椎上", "胸部挺起，核心稳定，眼睛直视前方", "屈髋屈膝下蹲，直至大腿与地面平行或以下", "腿部发力站起，髋部前顶回到起始位置"]',
'["膝盖与脚尖方向一致", "下蹲时吸气，站起时呼气", "保持胸部挺起不要前倾", "重心放在脚跟中部", "初学者先掌握徒手深蹲"]', 7, NOW(), NOW(), 0),

(8, '弓步蹲', '腿部训练', '单腿训练动作，改善左右力量不平衡', '', '股四头肌/臀部', '初级',
'["站直，双脚并拢，双手叉腰或持哑铃", "一腿向前跨一大步", "下蹲直到后腿膝盖接近地面", "前腿大腿与地面平行，膝盖不超过脚尖", "前腿发力站起，回到起始位置后换腿"]',
'["保持身体直立不要前倾", "前腿膝盖与脚尖方向一致", "下蹲时吸气，站起时呼气", "可以行走弓步或原地弓步", "注意保持平衡和控制"]', 8, NOW(), NOW(), 0),

(9, '罗马尼亚硬拉', '腿部训练', '专注腿后侧链和臀部的高效动作', '', '腿后侧链/臀大肌', '中级',
'["双脚与髋同宽站立，正手抓住杠铃", "肩胛骨后收，背部保持自然中立", "髋部向后推，身体以髋为轴前屈", "感受腿后侧拉伸，直到杠铃到小腿中部", "髋部和腿后侧发力，站直身体"]',
'["背部全程保持中立，不要弓背", "髋部主动向后推", "膝盖微屈但不大幅弯曲", "感受腿后侧和臀部发力", "返回时髋部前顶"]', 9, NOW(), NOW(), 0),

-- 肩部训练
(10, '哑铃推举', '肩部训练', '打造饱满三角肌的首选动作', '', '三角肌', '中级',
'["坐姿或站姿，双手各持一个哑铃", "哑铃举至肩部两侧，掌心朝前", "垂直向上推起哑铃至手臂伸直", "顶峰略停，感受三角肌收缩", "控制速度下放至起始位置"]',
'["保持核心稳定，避免塌腰", "不要完全锁死手肘", "推起时呼气，下放时吸气", "手肘略在手腕前方", "避免肩部过度耸动"]', 10, NOW(), NOW(), 0),

(11, '侧平举', '肩部训练', '针对三角肌中束的孤立动作', '', '三角肌中束', '初级',
'["站立，双手各持一个哑铃置于身体两侧", "保持手肘微屈，身体直立", "以三角肌发力，将哑铃向两侧举起", "直到手臂与地面平行或略高", "控制下放至起始位置"]',
'["不要用惯性甩动哑铃", "手肘略高于手腕", "上举时呼气，下放时吸气", "选择较轻重量保证动作质量", "感受三角肌中束发力"]', 11, NOW(), NOW(), 0),

(12, '俯身飞鸟', '肩部训练', '强化三角肌后束和上背部', '', '三角肌后束', '初级',
'["俯身前屈或坐在平板凳边缘", "双手各持哑铃，手臂自然下垂", "保持背部平直，手肘微屈", "以三角肌后束发力，将哑铃向两侧后上方举起", "顶峰收缩肩胛骨，然后控制下放"]',
'["避免用手臂代偿发力", "挤压肩胛骨增强刺激", "上举时呼气，下放时吸气", "保持背部中立不要弓背", "选择较轻重量保证动作质量"]', 12, NOW(), NOW(), 0),

-- 手臂训练
(13, '哑铃弯举', '手臂训练', '打造肱二头肌峰值的经典动作', '', '肱二头肌', '初级',
'["站立，双手各持哑铃，手臂自然下垂", "保持大臂贴近身体，不要摆动", "以肱二头肌发力，弯曲手肘向上卷起哑铃", "直到哑铃接近肩部，顶峰收缩", "控制下放至起始位置"]',
'["大臂保持固定不要摆动", "不要用身体借力摇摆", "卷起时呼气，下放时吸气", "控制下放速度增强离心收缩", "可尝试交替弯举或锤式弯举"]', 13, NOW(), NOW(), 0),

(14, '绳索下压', '手臂训练', '塑造肱三头肌线条的首选动作', '', '肱三头肌', '初级',
'["站在缆绳器械前，双手抓住绳索或把手", "大臂贴近身体两侧，手肘弯曲约90度", "以肱三头肌发力，向下推伸直手臂", "顶峰收缩肱三头肌，留在下方略停", "控制速度返回起始位置"]',
'["大臂紧贴身体不要离开", "只移动前臂，大臂固定", "下压时呼气，返回时吸气", "不要用身体重量压下去", "感受肱三头肌的收缩和拉伸"]', 14, NOW(), NOW(), 0),

-- 核心训练
(15, '平板支撑', '核心训练', '最佳核心稳定性训练动作', '', '核心肌群', '初级',
'["俯卧，前臂支撑地面，手肘位于肩部下方", "双脚后伸，脚尖支撑", "身体从头到脚保持一条直线", "核心紧缩，臀部不要下沉或抬高", "保持该姿势30-60秒或更长"]',
'["呼吸保持均匀不要憋气", "颈部保持中立，眼睛看地面", "不要塌腰或拱背", "初学者从短时间开始逐步增加", "感受核心肌群的持续紧张"]', 15, NOW(), NOW(), 0),

(16, '俄罗斯转体', '核心训练', '强化腹斜肌和旋转力量的动作', '', '腹斜肌', '中级',
'["坐在地上，双腿微屈上抬，上身后倾45度", "双手合十或持哑铃置于胸前", "保持身体稳定，上身向左右两侧旋转", "手部或哑铃轻触地面，感受腹斜肌收缩", "左右交替进行"]',
'["保持腿部稳定不要摇摆", "用核心力量旋转而非手臂", "呼吸保持规律", "初学者可以脚跟着地", "动作要控制不要过快"]', 16, NOW(), NOW(), 0),

(17, '死虫式', '核心训练', '下背痛克星，安全高效的核心训练', '', '腹直肌/核心', '初级',
'["仰卧，双手垂直上举，双腿屈膝90度抬起", "下背贴地，核心紧缩", "同时伸展对侧手臂和腿部", "保持下背始终贴地", "返回起始位置后换另一侧"]',
'["下背全程贴地不要拱起", "动作缓慢控制", "呼吸保持均匀", "腹部持续紧缩", "如不能全伸直可先小范围"]', 17, NOW(), NOW(), 0);

-- 3. 营养知识表数据 (knowledge_nutrient)
INSERT INTO knowledge_nutrient (id, name, description, color, icon, benefits, sortOrder, createTime, updateTime, isDelete) VALUES
(1, '蛋白质', '肌肉生长和修复的基础，建议每公斤体重摄入1.8-2.2克', '#ff7875', 'icon-fire', 
'["肌肉合成与修复", "增强饱腹感", "维持免疫系统", "促进代谢", "保持肌肉量"]', 1, NOW(), NOW(), 0),

(2, '碳水化合物', '身体的主要能量来源，为训练提供燃料和促进肌糖原恢复', '#ffa940', 'icon-fire', 
'["提供训练能量", "促进肌糖原恢复", "节省蛋白质", "提高训练表现", "支持大脑功能"]', 2, NOW(), NOW(), 0),

(3, '健康脂肪', '激素合成、维生素吸收的必需营养素，占总热量20-30%', '#ffc53d', 'icon-heart', 
'["激素分泌平衡", "脂溶性维生素吸收", "抗炎作用", "保护关节健康", "增强饱腹感"]', 3, NOW(), NOW(), 0),

(4, '维生素与矿物质', '微量营养素支持各项生理功能，对健身者尤其重要', '#95de64', 'icon-star', 
'["支持能量代谢", "增强免疫力", "骨骼健康", "肌肉收缩功能", "抗氧化保护"]', 4, NOW(), NOW(), 0),

(5, '水分', '生命之源，建议每日3-4升，训练时需增加摄入', '#69c0ff', 'icon-heart', 
'["维持体温调节", "营养物质转运", "关节润滑", "代谢废物排出", "预防运动疲劳"]', 5, NOW(), NOW(), 0),

(6, '补剂与运动营养', '合理使用补剂可以辅助训练，但不能替代真实食物', '#b37feb', 'icon-star', 
'["方便补充蛋白质", "提高训练表现", "加速恢复过程", "弥补饮食不足", "优化身体成分"]', 6, NOW(), NOW(), 0);

-- 4. 饮食计划表数据 (knowledge_meal)
INSERT INTO knowledge_meal (id, name, mealType, description, image, calories, protein, carbs, fat, sortOrder, createTime, updateTime, isDelete) VALUES
(1, '增肌能量早餐', '早餐', '高蛋白、适量碳水的增肌早餐搭配：燕麦粥+鸡蛋+香蕉+坚果', '', 520, 30, 55, 15, 1, NOW(), NOW(), 0),
(2, '训练前能量餐', '训练前', '提供持久能量的训练前餐：全麦面包+花生酱+香蕉+咖啡', '', 380, 15, 65, 8, 2, NOW(), NOW(), 0),
(3, '训练后恢复餐', '训练后', '黄金窗口期营养：蛋白粉+葡萄汁+香蕉+麦片', '', 420, 35, 50, 5, 3, NOW(), NOW(), 0),
(4, '增肌主食午餐', '午餐', '均衡营养的增肌午餐：鸡胸肉+糙米+西兰花+甘薯', '', 650, 45, 70, 15, 4, NOW(), NOW(), 0),
(5, '减脂清淡午餐', '午餐', '高蛋白低脂的减脂餐：三文鱼+藜麦+混合蔬菜沙拉', '', 480, 40, 45, 12, 5, NOW(), NOW(), 0),
(6, '高蛋白晚餐', '晚餐', '促进夜间恢复的晚餐：牛肉+红薯+芦笋+蔬菜', '', 580, 42, 50, 18, 6, NOW(), NOW(), 0),
(7, '轻食主义沙拉餐', '晚餐', '低热量高营养：烤鸡胸沙拉+牛油果+藜麦+橄榄油', '', 420, 35, 30, 20, 7, NOW(), NOW(), 0),
(8, '健康加餐小食', '加餐', '补充能量的健康小食：希腊酸奶+水果+坚果+蜂蜜', '', 280, 15, 28, 12, 8, NOW(), NOW(), 0),
(9, '素食蛋白餐', '午餐', '素食者高蛋白选择：豆腐+鹰嘴豆+糙米+混合蔬菜', '', 500, 28, 60, 14, 9, NOW(), NOW(), 0),
(10, '快速便携餐', '加餐', '忙碌时的快速补充：全麦三明治+牛奶+鸡蛋+香蕉', '', 450, 25, 50, 16, 10, NOW(), NOW(), 0),
(11, '低碳高蛋白餐', '晚餐', '减脂期低碳餐：三文鱼+水煮鸡蛋+牛油果+芦笋', '', 380, 45, 15, 18, 11, NOW(), NOW(), 0),
(12, '高热量增重餐', '午餐', '增重期高热量餐：意大利面+肉丸+芝士+橄榄油', '', 850, 40, 95, 28, 12, NOW(), NOW(), 0);

-- 5. 训练计划表数据 (knowledge_program)
INSERT INTO knowledge_program (id, name, description, type, icon, duration, intensity, level, schedule, sortOrder, createTime, updateTime, isDelete) VALUES
(1, '新手全身训练', '适合健身初学者的全身训练计划，每周三练，每次45分钟', 'beginner', 'icon-user', '8周', '低-中等', '初级',
'[{"day":"周一","content":"全身力量：深蹲、俯卧撑、哑铃划船、平板支撑"},{"day":"周三","content":"轻度有氧+柔韧拉伸，30分钟慢跑或快走"},{"day":"周五","content":"全身力量：弓步蹲、哑铃卧推、引体向上、俄罗斯转体"},{"day":"周六","content":"活动性恢复：瑜伽或散步"},{"day":"其他时间","content":"完全休息或轻度活动"}]', 1, NOW(), NOW(), 0),

(2, '上下肢分化训练', '经典的上下肢分化计划，适合有一定基础的训练者', 'intermediate', 'icon-fire', '12周', '中-高等', '中级',
'[{"day":"周一","content":"上肢：胸+肱三头肌 - 卧推、双杠臂屈伸、飞鸟、下压"},{"day":"周二","content":"下肢：腿+核心 - 深蹲、硬拉、弓步蹲、提踵、平板"},{"day":"周四","content":"上肢：背+肱二头肌 - 引体向上、划船、硬拉、弯举"},{"day":"周五","content":"下肢：腿+核心 - 前蹲、腿弯举、腿屈伸、RDL、核心组合"},{"day":"周六","content":"上肢：肩+核心 - 推举、侧平举、俯身飞鸟、核心训练"},{"day":"周日","content":"完全休息或轻度有氧"}]', 2, NOW(), NOW(), 0),

(3, 'PPL推拉腿分化', '高效的推拉腿分化计划，适合中高级训练者', 'advanced', 'icon-trophy', '16周+', '高等', '中高级',
'[{"day":"周一","content":"推：胸+肩+肱三 - 卧推、上斜卧推、推举、侧平举"},{"day":"周二","content":"拉：背+肱二 - 引体向上、划船、硬拉、弯举、锤式弯举"},{"day":"周三","content":"腿：股四+腿后+臀 - 深蹲、RDL、腿举、腿弯举、提踵"},{"day":"周四","content":"推：胸+肩+肱三 - 不同角度和动作变化"},{"day":"周五","content":"拉：背+肱二 - 不同角度和动作变化"},{"day":"周六","content":"腿：股四+腿后+臀 - 不同角度和动作变化"},{"day":"周日","content":"完全休息或主动恢复"}]', 3, NOW(), NOW(), 0),

(4, '减脂塑形计划', '结合力量和有氧的减脂计划，保持肌肉同时减脂', 'fat-loss', 'icon-fire', '12周', '中-高等', '初中级',
'[{"day":"周一","content":"全身力量 + 20分钟HIIT"},{"day":"周二","content":"中等强度有氧 40分钟（快走/轻跑）"},{"day":"周三","content":"上肢力量 + 15分钟核心"},{"day":"周四","content":"中等强度有氧 40分钟（单车/游泳）"},{"day":"周五","content":"下肢力量 + 20分钟HIIT"},{"day":"周六","content":"活动性恢复：轻度有氧 30分钟"},{"day":"周日","content":"完全休息"}]', 4, NOW(), NOW(), 0),

(5, '力量举训练计划', '专注于提升三大项（深蹲、卧推、硬拉）的力量', 'strength', 'icon-trophy', '12-16周', '高等', '中高级',
'[{"day":"周一","content":"深蹲日：主项深蹲 + 辅助动作（前蹲、腿举）"},{"day":"周二","content":"卧推日：主项卧推 + 辅助动作（上斜、双杠）"},{"day":"周四","content":"硬拉日：主项硬拉 + 辅助动作（RDL、划船）"},{"day":"周五","content":"辅助训练：肩、手臂、核心"},{"day":"其他时间","content":"休息恢复和柔韧训练"}]', 5, NOW(), NOW(), 0),

(6, '居家徒手训练', '不需要器械的徒手训练计划，随时随地训练', 'bodyweight', 'icon-user', '8周', '中等', '初中级',
'[{"day":"周一","content":"上肢推：俯卧撑变式、三头撑臂屈伸、冲刺波比"},{"day":"周二","content":"下肢：徒手深蹲、弓步蹲、单腿深蹲、提踵"},{"day":"周三","content":"核心：平板变式、山式跑、俄罗斯转体、卧卷腹"},{"day":"周四","content":"上肢拉：反手俯卧撑、引体向上（有条件）、超人式"},{"day":"周五","content":"下肢+核心：跳跃深蹲、波比跳、登山者、平板支撑"},{"day":"周六","content":"全身循环：组合全身动作进行循环训练"},{"day":"周日","content":"休息或轻度拉伸/瑜伽"}]', 6, NOW(), NOW(), 0),

(7, '女性美体塑形', '针对女性的全面塑形计划，侧重臀腿和核心', 'female', 'icon-heart', '12周', '中等', '初中级',
'[{"day":"周一","content":"下肢+臀：深蹲、臀桥、弓步蹲、蚌壳式"},{"day":"周二","content":"上肢+核心：哑铃卧推、划船、推举、核心训练"},{"day":"周三","content":"有氧+柔韧：瑜伽或普拉提 60分钟"},{"day":"周四","content":"下肢+臀：单腿深蹲、RDL、臀冲、腿外展"},{"day":"周五","content":"全身循环+有氧：循环训练 + 20分钟HIIT"},{"day":"周六","content":"轻度有氧：慢跑、单车或游泳 40分钟"},{"day":"周日","content":"休息或活动性恢复"}]', 7, NOW(), NOW(), 0),

(8, '职场人士快速训练', '适合时间紧张的上班族，每次30-40分钟高效训练', 'busy', 'icon-clock-circle', '8周', '中-高等', '初中级',
'[{"day":"周一","content":"全身复合：深蹲+卧推+划船 3轮循环 30分钟"},{"day":"周三","content":"HIIT训练：波比、登山者、跳跃深蹲 25分钟"},{"day":"周五","content":"全身力量：硬拉+推举+引体向上 3轮 35分钟"},{"day":"周六/日","content":"轻度活动：散步、瑜伽或休息"}]', 8, NOW(), NOW(), 0);

-- ============================================================
-- Source: sql/assessment_tables.sql
-- ============================================================
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

-- 11. 评测方案表
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

-- 12. 评测方案项目定义表
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

-- 13. 评测规则表
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

-- 14. 用户评测画像表
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

-- 15. 评测记录主表
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

-- 16. 评测记录明细表
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

-- 17. 评测报告表
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

-- ============================================================
-- Source: sql/assessment_university_standard_seed.sql
-- ============================================================
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

-- ============================================================
-- Source: sql/assessment_university_standard_rules.sql
-- ============================================================
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

-- ============================================================
-- 初始化成功校验 SQL
-- ============================================================
use mq_ai_agent;

select count(*) as assessmentSchemeCount
from assessment_scheme
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and isDelete = 0;

select count(*) as assessmentSchemeItemCount
from assessment_scheme_item
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and isDelete = 0;

select count(*) as assessmentRuleCount
from assessment_rule
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and ruleVersion = 'v1'
  and isDelete = 0;

-- 预期结果：
-- assessmentSchemeCount = 1
-- assessmentSchemeItemCount = 7
-- assessmentRuleCount = 582

-- ============================================================
-- 初始化完成提示
-- ============================================================
select '数据库初始化完成（已包含 assessment 模块）' as message;
select concat('共创建 ', count(distinct table_name), ' 张表') as table_count
from information_schema.tables
where table_schema = 'mq_ai_agent';
