/**
 * 心情数据存储服务
 * 
 * 当前实现：localStorage 本地存储
 * 后续优化：可扩展为 API 接口同步到后端
 * 
 * ============================================================
 * 【后续 API 优化建议】
 * 
 * 1. 添加心情记录 API:
 *    POST /api/mood
 *    Body: { date: "2026-01-30", mood: 5, emoji: "😊" }
 * 
 * 2. 获取用户心情记录 API:
 *    GET /api/mood?startDate=2026-01-24&endDate=2026-01-30
 *    Response: [{ date, mood, emoji, createdAt }]
 * 
 * 3. 更新心情记录 API:
 *    PUT /api/mood/{date}
 *    Body: { mood: 4, emoji: "🙂" }
 * 
 * 4. 删除心情记录 API:
 *    DELETE /api/mood/{date}
 * 
 * 5. 心情统计 API:
 *    GET /api/mood/stats?days=30
 *    Response: { avgMood, moodCount, streakDays, moodDistribution }
 * 
 * 数据库表设计建议:
 * CREATE TABLE user_mood (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   user_id BIGINT NOT NULL,
 *   record_date DATE NOT NULL,
 *   mood_level TINYINT NOT NULL COMMENT '心情等级 1-5',
 *   emoji VARCHAR(10) COMMENT '心情 emoji',
 *   note VARCHAR(200) COMMENT '心情备注（可选）',
 *   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 *   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   UNIQUE KEY uk_user_date (user_id, record_date)
 * );
 * ============================================================
 */

// localStorage 键名
const MOOD_STORAGE_KEY = 'fitness_mood_records';

// 心情等级定义
export const MOOD_LEVELS = [
  { level: 5, emoji: '😊', label: '非常好', color: '#52c41a' },
  { level: 4, emoji: '🙂', label: '不错', color: '#73d13d' },
  { level: 3, emoji: '😐', label: '一般', color: '#faad14' },
  { level: 2, emoji: '😔', label: '不太好', color: '#ff7a45' },
  { level: 1, emoji: '😢', label: '很差', color: '#ff4d4f' }
];

/**
 * 获取所有心情记录
 * @returns {Object} 格式: { "2026-01-30": { mood: 5, emoji: "😊", updatedAt: "..." }, ... }
 */
export const getAllMoodRecords = () => {
  try {
    const data = localStorage.getItem(MOOD_STORAGE_KEY);
    return data ? JSON.parse(data) : {};
  } catch (error) {
    console.error('读取心情记录失败:', error);
    return {};
  }
};

/**
 * 获取指定日期的心情记录
 * @param {string} date 日期字符串 YYYY-MM-DD
 * @returns {Object|null} { mood: 5, emoji: "😊", updatedAt: "..." } 或 null
 */
export const getMoodByDate = (date) => {
  const records = getAllMoodRecords();
  return records[date] || null;
};

/**
 * 获取日期范围内的心情记录
 * @param {string} startDate 开始日期 YYYY-MM-DD
 * @param {string} endDate 结束日期 YYYY-MM-DD
 * @returns {Object} 范围内的心情记录
 */
export const getMoodsByDateRange = (startDate, endDate) => {
  const records = getAllMoodRecords();
  const result = {};
  
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  Object.keys(records).forEach(date => {
    const recordDate = new Date(date);
    if (recordDate >= start && recordDate <= end) {
      result[date] = records[date];
    }
  });
  
  return result;
};

/**
 * 保存心情记录
 * @param {string} date 日期字符串 YYYY-MM-DD
 * @param {number} mood 心情等级 1-5
 * @param {string} emoji 心情 emoji
 * @returns {boolean} 是否保存成功
 */
export const saveMood = (date, mood, emoji) => {
  try {
    const records = getAllMoodRecords();
    records[date] = {
      mood,
      emoji,
      updatedAt: new Date().toISOString()
    };
    localStorage.setItem(MOOD_STORAGE_KEY, JSON.stringify(records));
    return true;
  } catch (error) {
    console.error('保存心情记录失败:', error);
    return false;
  }
};

/**
 * 删除指定日期的心情记录
 * @param {string} date 日期字符串 YYYY-MM-DD
 * @returns {boolean} 是否删除成功
 */
export const deleteMood = (date) => {
  try {
    const records = getAllMoodRecords();
    delete records[date];
    localStorage.setItem(MOOD_STORAGE_KEY, JSON.stringify(records));
    return true;
  } catch (error) {
    console.error('删除心情记录失败:', error);
    return false;
  }
};

/**
 * 格式化日期为 YYYY-MM-DD（本地时间）
 * @param {Date} date 
 * @returns {string}
 */
const formatDateToLocal = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/**
 * 获取本周心情记录（周一到周日）
 * @returns {Array} [{ date, dateDisplay, dayOfWeek, mood, emoji }, ...]
 */
export const getWeekMoods = () => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dayOfWeek = today.getDay(); // 0=周日, 1=周一, ...
  
  // 计算本周一的日期
  const monday = new Date(today);
  const daysFromMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
  monday.setDate(today.getDate() - daysFromMonday);
  monday.setHours(0, 0, 0, 0);
  
  const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const todayStr = formatDateToLocal(today);
  const result = [];
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday);
    date.setDate(monday.getDate() + i);
    date.setHours(0, 0, 0, 0);
    const dateStr = formatDateToLocal(date);
    const moodRecord = getMoodByDate(dateStr);
    
    // 格式化显示日期：YYYY/MM/DD
    const dateDisplay = `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`;
    
    result.push({
      date: dateStr,
      dateDisplay: dateDisplay,
      year: date.getFullYear(),
      month: date.getMonth() + 1,
      day: date.getDate(),
      dayOfWeek: weekDays[i],
      isToday: dateStr === todayStr,
      isFuture: date > today,
      mood: moodRecord?.mood || null,
      emoji: moodRecord?.emoji || null
    });
  }
  
  return result;
};

/**
 * 计算平均心情（指定天数内）
 * @param {number} days 天数
 * @returns {number} 平均心情值，保留1位小数
 */
export const getAverageMood = (days = 7) => {
  const records = getAllMoodRecords();
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const startDate = new Date(today);
  startDate.setDate(today.getDate() - days + 1);
  
  let totalMood = 0;
  let count = 0;
  
  Object.keys(records).forEach(date => {
    const recordDate = new Date(date);
    if (recordDate >= startDate && recordDate <= today) {
      totalMood += records[date].mood;
      count++;
    }
  });
  
  return count > 0 ? Math.round((totalMood / count) * 10) / 10 : 0;
};

/**
 * 计算连续打卡天数
 * @returns {number} 连续天数
 */
export const getMoodStreakDays = () => {
  const records = getAllMoodRecords();
  const dates = Object.keys(records).sort().reverse(); // 降序排列
  
  if (dates.length === 0) return 0;
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const todayStr = today.toISOString().split('T')[0];
  
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);
  const yesterdayStr = yesterday.toISOString().split('T')[0];
  
  // 检查最近一次打卡是否是今天或昨天
  const latestDate = dates[0];
  if (latestDate !== todayStr && latestDate !== yesterdayStr) {
    return 0;
  }
  
  let streak = 1;
  let currentDate = new Date(latestDate);
  
  for (let i = 1; i < dates.length; i++) {
    const expectedDate = new Date(currentDate);
    expectedDate.setDate(currentDate.getDate() - 1);
    const expectedDateStr = expectedDate.toISOString().split('T')[0];
    
    if (dates[i] === expectedDateStr) {
      streak++;
      currentDate = expectedDate;
    } else {
      break;
    }
  }
  
  return streak;
};

/**
 * 根据心情等级获取对应的 emoji
 * @param {number} level 心情等级 1-5
 * @returns {string} emoji
 */
export const getEmojiByLevel = (level) => {
  const moodInfo = MOOD_LEVELS.find(m => m.level === level);
  return moodInfo?.emoji || '😐';
};

/**
 * 根据心情等级获取完整信息
 * @param {number} level 心情等级 1-5
 * @returns {Object} { level, emoji, label, color }
 */
export const getMoodInfoByLevel = (level) => {
  return MOOD_LEVELS.find(m => m.level === level) || MOOD_LEVELS[2]; // 默认返回"一般"
};

export default {
  MOOD_LEVELS,
  getAllMoodRecords,
  getMoodByDate,
  getMoodsByDateRange,
  saveMood,
  deleteMood,
  getWeekMoods,
  getAverageMood,
  getMoodStreakDays,
  getEmojiByLevel,
  getMoodInfoByLevel
};
