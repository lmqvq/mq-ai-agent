-- 健身数据管理系统初始化数据脚本

USE mq_ai_agent;

-- 插入示例用户（如果不存在）
INSERT IGNORE INTO user (id, userAccount, userPassword, userName, userRole) VALUES
(1, 'testuser', '$2a$10$example', '测试用户', 'user'),
(2, 'admin', '$2a$10$example', '管理员', 'admin');

-- 插入示例健身数据
INSERT INTO fitness_data (userId, weight, bodyFat, height, bmi, dateRecorded) VALUES
(1, 70.5, 15.2, 175.0, 23.02, '2024-01-01 08:00:00'),
(1, 70.2, 15.0, 175.0, 22.92, '2024-01-08 08:00:00'),
(1, 69.8, 14.8, 175.0, 22.79, '2024-01-15 08:00:00'),
(1, 69.5, 14.5, 175.0, 22.69, '2024-01-22 08:00:00'),
(1, 69.2, 14.2, 175.0, 22.59, '2024-01-29 08:00:00');

-- 插入示例运动记录
INSERT INTO exercise_log (userId, exerciseType, duration, caloriesBurned, dateRecorded, notes) VALUES
(1, '跑步', 30, 300.0, '2024-01-01 18:00:00', '今天跑步感觉很好，配速稳定'),
(1, '力量训练', 45, 250.0, '2024-01-02 19:00:00', '胸部训练，卧推进步明显'),
(1, '瑜伽', 60, 150.0, '2024-01-03 07:00:00', '晨练瑜伽，身体柔韧性有提升'),
(1, '游泳', 40, 400.0, '2024-01-04 20:00:00', '游泳1000米，技术有改善'),
(1, '骑行', 90, 450.0, '2024-01-05 16:00:00', '户外骑行，风景很好'),
(1, '跑步', 35, 350.0, '2024-01-06 18:30:00', '增加了5分钟，体能在提升'),
(1, '力量训练', 50, 280.0, '2024-01-07 19:30:00', '背部训练，引体向上有进步');

-- 插入示例训练计划
INSERT INTO training_plan (userId, planName, planType, planDetails, isDefault, startDate, endDate) VALUES
(1, '增肌训练计划', '增肌', '{"day1":{"exercise":"深蹲（4组×10次）、卧推（3组×12次）","note":"注意动作规范，避免受伤"},"day2":{"exercise":"硬拉（3组×8次）、引体向上（辅助2组×8次）","note":"背部发力感优先"},"day3":{"exercise":"肩部推举（3组×10次）、臂弯举（3组×12次）","note":"控制重量，感受肌肉发力"},"day4":{"exercise":"有氧运动30分钟","note":"心率保持在130-150之间"},"day5":{"exercise":"腿部训练（深蹲、腿举）","note":"大重量训练，注意安全"},"day6":{"exercise":"核心训练","note":"平板支撑、卷腹等"},"day7":{"exercise":"休息","note":"充分休息，肌肉恢复"}}', 0, '2024-01-01 00:00:00', '2024-04-01 00:00:00'),

(1, '减脂训练计划', '减脂', '{"day1":{"exercise":"有氧运动45分钟（跑步或椭圆机）","note":"心率保持在120-140之间"},"day2":{"exercise":"全身力量训练（轻重量多次数）","note":"每个动作15-20次，3组"},"day3":{"exercise":"HIIT训练30分钟","note":"高强度间歇训练，燃脂效果好"},"day4":{"exercise":"瑜伽或普拉提60分钟","note":"拉伸放松，提高柔韧性"},"day5":{"exercise":"有氧运动50分钟","note":"可选择游泳、骑行等"},"day6":{"exercise":"核心+有氧组合训练","note":"先核心训练20分钟，再有氧30分钟"},"day7":{"exercise":"轻松散步或休息","note":"主动恢复或完全休息"}}', 0, '2024-01-01 00:00:00', '2024-06-01 00:00:00');

-- 插入默认训练计划模板
INSERT INTO training_plan (userId, planName, planType, planDetails, isDefault, startDate) VALUES
(2, '新手入门计划', '塑形', '{"day1":{"exercise":"热身10分钟 + 基础力量训练30分钟","note":"从轻重量开始，学习正确动作"},"day2":{"exercise":"有氧运动30分钟","note":"慢跑或快走，适应运动强度"},"day3":{"exercise":"休息或轻度拉伸","note":"让身体适应运动节奏"},"day4":{"exercise":"全身力量训练40分钟","note":"增加训练强度"},"day5":{"exercise":"有氧运动35分钟","note":"可尝试不同有氧运动"},"day6":{"exercise":"瑜伽或拉伸60分钟","note":"提高柔韧性，放松身心"},"day7":{"exercise":"休息","note":"充分休息很重要"}}', 1, '2024-01-01 00:00:00'),

(2, '力量提升计划', '力量提升', '{"day1":{"exercise":"胸部+三头肌训练","note":"卧推、双杠臂屈伸等"},"day2":{"exercise":"背部+二头肌训练","note":"引体向上、划船等"},"day3":{"exercise":"腿部训练","note":"深蹲、硬拉、腿举"},"day4":{"exercise":"肩部+核心训练","note":"推举、侧平举、平板支撑"},"day5":{"exercise":"全身复合动作","note":"深蹲推举、硬拉等"},"day6":{"exercise":"有氧恢复训练","note":"轻度有氧，促进恢复"},"day7":{"exercise":"休息","note":"完全休息，肌肉生长"}}', 1, '2024-01-01 00:00:00');

-- 插入示例健身目标
INSERT INTO fitness_goal (userId, goalType, targetValue, startDate, endDate, progress, isAchieved) VALUES
(1, '减脂', '体脂率降到12%', '2024-01-01 00:00:00', '2024-06-01 00:00:00', '{"2024-01-01":"体脂率15.2%","2024-01-15":"体脂率14.8%","2024-01-29":"体脂率14.2%"}', 0),
(1, '增重', '体重增加到72kg', '2024-01-01 00:00:00', '2024-04-01 00:00:00', '{"2024-01-01":"体重70.5kg","2024-01-15":"体重69.8kg"}', 0),
(1, '力量提升', '卧推重量达到80kg', '2024-01-01 00:00:00', '2024-03-01 00:00:00', '{"2024-01-01":"卧推60kg","2024-01-15":"卧推65kg"}', 0);

-- 插入已完成的目标示例
INSERT INTO fitness_goal (userId, goalType, targetValue, startDate, endDate, progress, isAchieved) VALUES
(1, '体态改善', '改善圆肩驼背', '2023-10-01 00:00:00', '2023-12-31 00:00:00', '{"2023-10-01":"开始矫正训练","2023-11-01":"肩部灵活性提升","2023-12-01":"体态明显改善","2023-12-31":"目标达成"}', 1);

-- 创建视图：用户健身数据汇总
CREATE OR REPLACE VIEW user_fitness_summary AS
SELECT 
    u.id as userId,
    u.userName,
    COUNT(DISTINCT fd.id) as fitness_data_count,
    COUNT(DISTINCT el.id) as exercise_log_count,
    COUNT(DISTINCT tp.id) as training_plan_count,
    COUNT(DISTINCT fg.id) as fitness_goal_count,
    COUNT(DISTINCT CASE WHEN fg.isAchieved = 1 THEN fg.id END) as achieved_goal_count,
    COALESCE(SUM(el.caloriesBurned), 0) as total_calories_burned,
    MAX(fd.dateRecorded) as last_fitness_data_date,
    MAX(el.dateRecorded) as last_exercise_date
FROM user u
LEFT JOIN fitness_data fd ON u.id = fd.userId AND fd.isDelete = 0
LEFT JOIN exercise_log el ON u.id = el.userId AND el.isDelete = 0
LEFT JOIN training_plan tp ON u.id = tp.userId AND tp.isDelete = 0
LEFT JOIN fitness_goal fg ON u.id = fg.userId AND fg.isDelete = 0
WHERE u.isDelete = 0
GROUP BY u.id, u.userName;

-- 创建索引优化查询性能
CREATE INDEX idx_fitness_data_user_date ON fitness_data(userId, dateRecorded DESC);
CREATE INDEX idx_exercise_log_user_date ON exercise_log(userId, dateRecorded DESC);
CREATE INDEX idx_exercise_log_type ON exercise_log(exerciseType);
CREATE INDEX idx_training_plan_user_type ON training_plan(userId, planType);
CREATE INDEX idx_fitness_goal_user_status ON fitness_goal(userId, isAchieved);

-- 查询示例数据
SELECT '=== 用户健身数据汇总 ===' as info;
SELECT * FROM user_fitness_summary;

SELECT '=== 最近的健身数据 ===' as info;
SELECT fd.*, u.userName 
FROM fitness_data fd 
JOIN user u ON fd.userId = u.id 
ORDER BY fd.dateRecorded DESC 
LIMIT 5;

SELECT '=== 最近的运动记录 ===' as info;
SELECT el.*, u.userName 
FROM exercise_log el 
JOIN user u ON el.userId = u.id 
ORDER BY el.dateRecorded DESC 
LIMIT 5;

SELECT '=== 进行中的健身目标 ===' as info;
SELECT fg.*, u.userName 
FROM fitness_goal fg 
JOIN user u ON fg.userId = u.id 
WHERE fg.isAchieved = 0 
ORDER BY fg.startDate DESC;
