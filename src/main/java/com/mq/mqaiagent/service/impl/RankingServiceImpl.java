package com.mq.mqaiagent.service.impl;

import com.mq.mqaiagent.mapper.ExerciseLogMapper;
import com.mq.mqaiagent.mapper.UserMapper;
import com.mq.mqaiagent.model.dto.ranking.*;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现类
 */
@Service
@Slf4j
public class RankingServiceImpl implements RankingService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ExerciseLogMapper exerciseLogMapper;

    @Resource
    private UserMapper userMapper;

    private static final long SCORE_MULTIPLIER = 10000000000000L; // 10^13
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public void updateRankingAfterAdd(Long userId, Date dateRecorded,
                                     LocalDate weekStartDate, LocalDate monthStartDate) {
        try {
            // 更新周榜
            updateRanking(userId, dateRecorded, "week", weekStartDate);

            // 更新月榜
            updateRanking(userId, dateRecorded, "month", monthStartDate);

            log.info("用户排行榜更新成功: userId={}", userId);
        } catch (Exception e) {
            log.error("更新排行榜失败: userId={}", userId, e);
        }
    }

    @Override
    public void updateRankingAfterDelete(Long userId, Date weekStartDate, Date monthStartDate) {
        try {
            LocalDate weekStart = weekStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate monthStart = monthStartDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // 重新计算周榜
            updateRanking(userId, new Date(), "week", weekStart);

            // 重新计算月榜
            updateRanking(userId, new Date(), "month", monthStart);

            // 如果运动次数变为0，从排行榜移除
            checkAndRemoveIfZero(userId, "week", weekStart);
            checkAndRemoveIfZero(userId, "month", monthStart);

            log.info("用户排行榜删除更新成功: userId={}", userId);
        } catch (Exception e) {
            log.error("删除更新排行榜失败: userId={}", userId, e);
        }
    }

    /**
     * 更新指定类型的排行榜
     */
    private void updateRanking(Long userId, Date dateRecorded,
                              String rankingType, LocalDate startDate) {
        String redisKey = "ranking:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
        String firstTimeKey = "ranking:first:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
        String userIdStr = String.valueOf(userId);

        // 1. 检查用户是否首次上榜
        Boolean hasFirstTime = redisTemplate.opsForHash().hasKey(firstTimeKey, userIdStr);
        long firstRecordTimestamp;

        if (hasFirstTime == null || !hasFirstTime) {
            // 首次上榜，记录时间
            firstRecordTimestamp = dateRecorded.getTime() / 1000; // 转为秒级时间戳
            redisTemplate.opsForHash().put(firstTimeKey, userIdStr,
                    String.valueOf(firstRecordTimestamp));
        } else {
            // 非首次，获取已记录的首次时间
            Object value = redisTemplate.opsForHash().get(firstTimeKey, userIdStr);
            firstRecordTimestamp = Long.parseLong(value.toString());
        }

        // 2. 查询当前用户在该周期内的运动记录总数
        int exerciseCount = countExerciseInPeriod(userId, rankingType, startDate);

        // 3. 计算 score = exerciseCount * 10^13 + (10^13 - firstRecordTimestamp)
        double score = exerciseCount * SCORE_MULTIPLIER + (SCORE_MULTIPLIER - firstRecordTimestamp);

        // 4. 更新 Redis Zset
        redisTemplate.opsForZSet().add(redisKey, userIdStr, score);

        // 5. 设置 Key 过期时间（周榜14天，月榜60天）
        long expireDays = "week".equals(rankingType) ? 14 : 60;
        redisTemplate.expire(redisKey, expireDays, TimeUnit.DAYS);
        redisTemplate.expire(firstTimeKey, expireDays, TimeUnit.DAYS);
    }

    /**
     * 统计用户在指定周期内的运动记录数
     */
    private int countExerciseInPeriod(Long userId, String rankingType, LocalDate startDate) {
        if ("week".equals(rankingType)) {
            return exerciseLogMapper.countByUserIdAndWeek(userId, startDate);
        } else {
            return exerciseLogMapper.countByUserIdAndMonth(userId, startDate);
        }
    }

    /**
     * 检查运动次数，如果为0则从排行榜移除
     */
    private void checkAndRemoveIfZero(Long userId, String rankingType, LocalDate startDate) {
        int count = countExerciseInPeriod(userId, rankingType, startDate);
        if (count == 0) {
            String redisKey = "ranking:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
            String firstTimeKey = "ranking:first:" + rankingType + ":" + startDate.format(DATE_FORMATTER);

            redisTemplate.opsForZSet().remove(redisKey, String.valueOf(userId));
            redisTemplate.opsForHash().delete(firstTimeKey, String.valueOf(userId));
        }
    }

    @Override
    public RankingListResponse getRankingList(String rankingType, Integer current, Integer pageSize) {
        // 1. 参数校验
        if (current == null || current < 1) {
            current = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100; // 最大100条
        }

        // 2. 计算 Redis Key
        LocalDate startDate = calculateStartDate(rankingType);
        String redisKey = "ranking:" + rankingType + ":" + startDate.format(DATE_FORMATTER);

        // 3. 从 Redis Zset 获取排行数据（按 score 降序）
        int start = (current - 1) * pageSize;
        int end = start + pageSize - 1;
        Set<ZSetOperations.TypedTuple<Object>> rankingSet =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, start, end);

        // 4. 获取总数
        Long total = redisTemplate.opsForZSet().zCard(redisKey);
        if (total == null) {
            total = 0L;
        }

        // 5. 如果没有数据，直接返回空结果
        if (rankingSet == null || rankingSet.isEmpty()) {
            return new RankingListResponse(total, current, pageSize, 0, new ArrayList<>(), null);
        }

        // 6. 批量获取用户信息
        List<Long> userIds = rankingSet.stream()
                .map(tuple -> Long.parseLong(tuple.getValue().toString()))
                .collect(Collectors.toList());
        Map<Long, User> userInfoMap = batchGetUserInfo(userIds);

        // 7. 获取首次记录时间
        String firstTimeKey = "ranking:first:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
        // 修复：将 Long 类型的 userId 转换为 String，避免 Redis 序列化错误
        List<Object> userIdObjects = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<Object> firstTimes = redisTemplate.opsForHash().multiGet(firstTimeKey, userIdObjects);

        // 8. 组装返回数据
        List<RankingRecord> records = new ArrayList<>();
        int rank = start + 1;
        int index = 0;
        for (ZSetOperations.TypedTuple<Object> tuple : rankingSet) {
            Long userId = Long.parseLong(tuple.getValue().toString());

            // 从 score 中解析出运动次数
            double score = tuple.getScore();
            int exerciseCount = (int) (score / SCORE_MULTIPLIER);

            // 获取用户详细统计信息
            ExerciseStats stats = getExerciseStats(userId, rankingType, startDate);

            RankingRecord record = new RankingRecord();
            record.setRank(rank++);
            record.setUserId(userId);

            User user = userInfoMap.get(userId);
            if (user != null) {
                record.setUserName(user.getUserName());
                record.setUserAvatar(user.getUserAvatar());
            } else {
                record.setUserName("用户" + userId);
                record.setUserAvatar("");
            }

            record.setScore(exerciseCount);

            // 处理首次记录时间
            if (firstTimes.get(index) != null) {
                long timestamp = Long.parseLong(firstTimes.get(index).toString());
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
                record.setFirstRecordTime(dateTime.format(DATETIME_FORMATTER));
            }

            record.setTotalMinutes(stats.getTotalMinutes());
            record.setTotalCalories(stats.getTotalCalories());
            record.setExerciseTypes(stats.getExerciseTypes());

            records.add(record);
            index++;
        }

        // 9. 构建统计信息
        StatisticInfo statisticInfo = buildStatisticInfo(total.intValue(), records);

        // 10. 计算总页数
        int pages = (int) Math.ceil((double) total / pageSize);

        return new RankingListResponse(total, current, pageSize, pages, records, statisticInfo);
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, User> batchGetUserInfo(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    /**
     * 获取用户在指定周期内的运动统计
     */
    private ExerciseStats getExerciseStats(Long userId, String rankingType, LocalDate startDate) {
        ExerciseStats stats;

        if ("week".equals(rankingType)) {
            stats = exerciseLogMapper.sumStatsByUserIdAndWeek(userId, startDate);
        } else {
            stats = exerciseLogMapper.sumStatsByUserIdAndMonth(userId, startDate);
        }

        if (stats == null) {
            stats = new ExerciseStats();
            stats.setTotalMinutes(0);
            stats.setTotalCalories(0.0f);
            stats.setExerciseCount(0);
        }

        // 查询运动类型（去重，最多3种）
        List<String> exerciseTypes = exerciseLogMapper.getDistinctExerciseTypes(userId, startDate, 3);
        stats.setExerciseTypes(exerciseTypes);

        return stats;
    }

    /**
     * 构建统计信息
     */
    private StatisticInfo buildStatisticInfo(Integer totalUsers, List<RankingRecord> records) {
        StatisticInfo info = new StatisticInfo();
        info.setTotalUsers(totalUsers);

        if (!records.isEmpty()) {
            // 计算平均分数
            double avgScore = records.stream()
                    .mapToInt(RankingRecord::getScore)
                    .average()
                    .orElse(0.0);
            info.setAverageScore(Math.round(avgScore * 10.0) / 10.0);

            // 最高分数
            info.setTopScore(records.get(0).getScore());
        } else {
            info.setAverageScore(0.0);
            info.setTopScore(0);
        }

        // 更新时间
        info.setUpdateTime(LocalDateTime.now().format(DATETIME_FORMATTER));

        return info;
    }

    /**
     * 计算周期起始日期
     */
    private LocalDate calculateStartDate(String rankingType) {
        LocalDate now = LocalDate.now();
        if ("week".equals(rankingType)) {
            // 本周一
            return now.with(DayOfWeek.MONDAY);
        } else {
            // 30天前
            return now.minusDays(29);
        }
    }

    @Override
    public MyRankingResponse getMyRanking(Long userId, String rankingType) {
        // 1. 计算 Redis Key
        LocalDate startDate = calculateStartDate(rankingType);
        String redisKey = "ranking:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
        String firstTimeKey = "ranking:first:" + rankingType + ":" + startDate.format(DATE_FORMATTER);
        String userIdStr = String.valueOf(userId);

        // 2. 获取用户 score（判断是否上榜）
        Double score = redisTemplate.opsForZSet().score(redisKey, userIdStr);

        // 3. 获取总人数
        Long totalUsers = redisTemplate.opsForZSet().zCard(redisKey);
        int total = totalUsers != null ? totalUsers.intValue() : 0;

        if (score == null) {
            // 未上榜
            return MyRankingResponse.notOnBoard(total);
        }

        // 4. 获取用户排名（reverseRank 从0开始，需要+1）
        Long rank = redisTemplate.opsForZSet().reverseRank(redisKey, userIdStr);
        int myRank = rank != null ? rank.intValue() + 1 : 0;

        // 5. 解析运动次数
        int exerciseCount = (int) (score / SCORE_MULTIPLIER);

        // 6. 计算超越百分比
        double beatPercent = total > 0 ? ((double) (total - myRank) / total) * 100 : 0;
        beatPercent = Math.round(beatPercent * 10.0) / 10.0;

        // 7. 获取用户详细统计
        ExerciseStats stats = getExerciseStats(userId, rankingType, startDate);

        // 8. 获取首次记录时间
        Object firstTimeObj = redisTemplate.opsForHash().get(firstTimeKey, userIdStr);
        String firstRecordTime = null;
        if (firstTimeObj != null) {
            long timestamp = Long.parseLong(firstTimeObj.toString());
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now()));
            firstRecordTime = dateTime.format(DATETIME_FORMATTER);
        }

        // 9. 获取下一名信息（如果不是第一名）
        NextRankInfo nextRankInfo = null;
        if (myRank > 1) {
            nextRankInfo = getNextRankInfo(redisKey, myRank);
        }

        // 10. 计算里程碑
        List<Milestone> milestones = calculateMilestones(myRank, exerciseCount);

        return MyRankingResponse.builder()
                .isOnBoard(true)
                .myRank(myRank)
                .myScore(exerciseCount)
                .firstRecordTime(firstRecordTime)
                .totalMinutes(stats.getTotalMinutes())
                .totalCalories(stats.getTotalCalories())
                .totalUsers(total)
                .beatPercent(beatPercent)
                .rankChange(0) // TODO: 可以通过快照表计算排名变化
                .rankChangeType("same")
                .nextRankInfo(nextRankInfo)
                .milestones(milestones)
                .build();
    }

    /**
     * 获取下一名信息
     */
    private NextRankInfo getNextRankInfo(String redisKey, int myRank) {
        // 获取前一名（排名更靠前）的信息
        int nextRankPosition = myRank - 2; // 因为Redis从0开始
        Set<ZSetOperations.TypedTuple<Object>> nextRankSet =
                redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, nextRankPosition, nextRankPosition);

        if (nextRankSet != null && !nextRankSet.isEmpty()) {
            ZSetOperations.TypedTuple<Object> tuple = nextRankSet.iterator().next();
            Long nextUserId = Long.parseLong(tuple.getValue().toString());
            double nextScore = tuple.getScore();
            int nextExerciseCount = (int) (nextScore / SCORE_MULTIPLIER);

            User nextUser = userMapper.selectById(nextUserId);

            NextRankInfo info = new NextRankInfo();
            info.setRank(myRank - 1);
            info.setUserName(nextUser != null ? nextUser.getUserName() : "用户" + nextUserId);
            info.setScore(nextExerciseCount);
            info.setScoreDiff(nextExerciseCount - 0); // TODO: 需要传入当前用户分数

            return info;
        }

        return null;
    }

    /**
     * 计算里程碑
     */
    private List<Milestone> calculateMilestones(int myRank, int exerciseCount) {
        List<Milestone> milestones = new ArrayList<>();

        // Top 100
        milestones.add(new Milestone("top100",
                myRank <= 100,
                myRank <= 100 ? "已进入 Top 100" : "距离 Top 100 还需努力",
                "🏆"));

        // Top 50
        milestones.add(new Milestone("top50",
                myRank <= 50,
                myRank <= 50 ? "已进入 Top 50" : "距离 Top 50 还需努力",
                "🥇"));

        // Top 10
        milestones.add(new Milestone("top10",
                myRank <= 10,
                myRank <= 10 ? "已进入 Top 10" : "距离 Top 10 还需努力",
                "⭐"));

        return milestones;
    }

    @Override
    public boolean refreshRanking(String rankingType) {
        try {
            LocalDate startDate = calculateStartDate(rankingType);
            String redisKey = "ranking:" + rankingType + ":" + startDate.format(DATE_FORMATTER);

            // 清除现有排行榜数据
            redisTemplate.delete(redisKey);

            // TODO: 可以从数据库重新构建排行榜

            log.info("排行榜刷新成功: rankingType={}", rankingType);
            return true;
        } catch (Exception e) {
            log.error("排行榜刷新失败: rankingType={}", rankingType, e);
            return false;
        }
    }
}
