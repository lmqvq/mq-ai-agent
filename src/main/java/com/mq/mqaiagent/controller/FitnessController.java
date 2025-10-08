package com.mq.mqaiagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mq.mqaiagent.annotation.AuthCheck;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.DeleteRequest;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.constant.UserConstant;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.model.dto.*;
import com.mq.mqaiagent.model.dto.exerciseLog.ExerciseLogAddRequest;
import com.mq.mqaiagent.model.dto.exerciseLog.ExerciseLogQueryRequest;
import com.mq.mqaiagent.model.dto.fitnessData.FitnessDataAddRequest;
import com.mq.mqaiagent.model.dto.fitnessData.FitnessDataQueryRequest;
import com.mq.mqaiagent.model.dto.fitnessData.FitnessDataUpdateRequest;
import com.mq.mqaiagent.model.dto.fitnessGoal.FitnessGoalAddRequest;
import com.mq.mqaiagent.model.dto.fitnessGoal.FitnessGoalQueryRequest;
import com.mq.mqaiagent.model.dto.fitnessGoal.FitnessGoalUpdateRequest;
import com.mq.mqaiagent.model.dto.trainingPlan.TrainingPlanAddRequest;
import com.mq.mqaiagent.model.dto.trainingPlan.TrainingPlanQueryRequest;
import com.mq.mqaiagent.model.entity.*;
import com.mq.mqaiagent.model.vo.*;
import com.mq.mqaiagent.service.*;
import com.mq.mqaiagent.utils.FitnessUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 健身相关接口
 */
@RestController
@RequestMapping("/fitness")
@Slf4j
public class FitnessController {

    @Resource
    private FitnessDataService fitnessDataService;

    @Resource
    private ExerciseLogService exerciseLogService;

    @Resource
    private TrainingPlanService trainingPlanService;

    @Resource
    private FitnessGoalService fitnessGoalService;

    @Resource
    private UserService userService;

    @Resource
    private RankingService rankingService;

    // region 健身数据相关接口

    /**
     * 创建健身数据
     *
     * @param fitnessDataAddRequest
     * @param request
     * @return
     */
    @PostMapping("/data/add")
    public BaseResponse<Long> addFitnessData(@RequestBody FitnessDataAddRequest fitnessDataAddRequest,
            HttpServletRequest request) {
        if (fitnessDataAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FitnessData fitnessData = new FitnessData();
        BeanUtils.copyProperties(fitnessDataAddRequest, fitnessData);

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        fitnessData.setUserId(loginUser.getId());

        // 计算BMI
        Float bmi = fitnessDataService.calculateBMI(fitnessData.getWeight(), fitnessData.getHeight());
        fitnessData.setBmi(bmi);

        // 如果没有指定记录时间，使用当前时间
        if (fitnessData.getDateRecorded() == null) {
            fitnessData.setDateRecorded(new Date());
        }

        fitnessDataService.validFitnessData(fitnessData, true);
        boolean result = fitnessDataService.save(fitnessData);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(fitnessData.getId());
    }

    /**
     * 删除健身数据
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/data/delete")
    public BaseResponse<Boolean> deleteFitnessData(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        FitnessData oldFitnessData = fitnessDataService.getById(id);
        ThrowUtils.throwIf(oldFitnessData == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldFitnessData.getUserId().equals(user.getId()) && !userService.isAdmin((User) request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = fitnessDataService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新健身数据（仅管理员）
     *
     * @param fitnessDataUpdateRequest
     * @return
     */
    @PostMapping("/data/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateFitnessData(@RequestBody FitnessDataUpdateRequest fitnessDataUpdateRequest) {
        if (fitnessDataUpdateRequest == null || fitnessDataUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FitnessData fitnessData = new FitnessData();
        BeanUtils.copyProperties(fitnessDataUpdateRequest, fitnessData);

        // 重新计算BMI
        Float bmi = fitnessDataService.calculateBMI(fitnessData.getWeight(), fitnessData.getHeight());
        fitnessData.setBmi(bmi);

        fitnessDataService.validFitnessData(fitnessData, false);
        long id = fitnessDataUpdateRequest.getId();
        // 判断是否存在
        FitnessData oldFitnessData = fitnessDataService.getById(id);
        ThrowUtils.throwIf(oldFitnessData == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = fitnessDataService.updateById(fitnessData);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取健身数据
     *
     * @param id
     * @return
     */
    @GetMapping("/data/get")
    public BaseResponse<FitnessDataVO> getFitnessDataById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FitnessData fitnessData = fitnessDataService.getById(id);
        if (fitnessData == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(fitnessDataService.getFitnessDataVO(fitnessData));
    }

    /**
     * 分页获取健身数据列表（仅管理员）
     *
     * @param fitnessDataQueryRequest
     * @return
     */
    @PostMapping("/data/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<FitnessDataVO>> listFitnessDataByPage(
            @RequestBody FitnessDataQueryRequest fitnessDataQueryRequest) {
        long current = fitnessDataQueryRequest.getCurrent();
        long size = fitnessDataQueryRequest.getPageSize();
        Page<FitnessData> fitnessDataPage = fitnessDataService.page(new Page<>(current, size),
                fitnessDataService.getQueryWrapper(fitnessDataQueryRequest));
        return ResultUtils.success(fitnessDataService.getFitnessDataVOPage(fitnessDataPage));
    }

    /**
     * 分页获取当前用户创建的健身数据列表
     *
     * @param fitnessDataQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/data/my/list/page")
    public BaseResponse<Page<FitnessDataVO>> listMyFitnessDataByPage(
            @RequestBody FitnessDataQueryRequest fitnessDataQueryRequest,
            HttpServletRequest request) {
        if (fitnessDataQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        fitnessDataQueryRequest.setUserId(loginUser.getId());
        long current = fitnessDataQueryRequest.getCurrent();
        long size = fitnessDataQueryRequest.getPageSize();
        Page<FitnessData> fitnessDataPage = fitnessDataService.page(new Page<>(current, size),
                fitnessDataService.getQueryWrapper(fitnessDataQueryRequest));
        return ResultUtils.success(fitnessDataService.getFitnessDataVOPage(fitnessDataPage));
    }

    /**
     * 获取健身数据趋势
     *
     * @param days    天数
     * @param request
     * @return
     */
    @GetMapping("/data/trends")
    public BaseResponse<List<FitnessDataVO>> getFitnessTrends(@RequestParam(defaultValue = "30") Integer days,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FitnessDataVO> trends = fitnessDataService.getFitnessTrends(loginUser.getId(), days);
        return ResultUtils.success(trends);
    }

    // endregion

    // region 运动记录相关接口

    /**
     * 创建运动记录
     *
     * @param exerciseLogAddRequest
     * @param request
     * @return
     */
    @PostMapping("/exercise/add")
    public BaseResponse<Long> addExerciseLog(@RequestBody ExerciseLogAddRequest exerciseLogAddRequest,
            HttpServletRequest request) {
        if (exerciseLogAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ExerciseLog exerciseLog = new ExerciseLog();
        BeanUtils.copyProperties(exerciseLogAddRequest, exerciseLog);

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        exerciseLog.setUserId(loginUser.getId());

        // 如果没有指定记录时间，使用当前时间
        if (exerciseLog.getDateRecorded() == null) {
            exerciseLog.setDateRecorded(new Date());
        }

        exerciseLogService.validExerciseLog(exerciseLog, true);
        boolean result = exerciseLogService.save(exerciseLog);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(exerciseLog.getId());
    }

    /**
     * 删除运动记录
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/exercise/delete")
    public BaseResponse<Boolean> deleteExerciseLog(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        ExerciseLog oldExerciseLog = exerciseLogService.getById(id);
        ThrowUtils.throwIf(oldExerciseLog == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldExerciseLog.getUserId().equals(user.getId()) && !userService.isAdmin((User) request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = exerciseLogService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取当前用户的运动记录列表
     *
     * @param exerciseLogQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/exercise/my/list/page")
    public BaseResponse<Page<ExerciseLogVO>> listMyExerciseLogByPage(
            @RequestBody ExerciseLogQueryRequest exerciseLogQueryRequest,
            HttpServletRequest request) {
        if (exerciseLogQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        exerciseLogQueryRequest.setUserId(loginUser.getId());
        long current = exerciseLogQueryRequest.getCurrent();
        long size = exerciseLogQueryRequest.getPageSize();
        Page<ExerciseLog> exerciseLogPage = exerciseLogService.page(new Page<>(current, size),
                exerciseLogService.getQueryWrapper(exerciseLogQueryRequest));
        return ResultUtils.success(exerciseLogService.getExerciseLogVOPage(exerciseLogPage));
    }

    /**
     * 获取运动统计
     *
     * @param days    天数
     * @param request
     * @return
     */
    @GetMapping("/exercise/stats")
    public BaseResponse<List<ExerciseLogVO>> getExerciseStats(@RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<ExerciseLogVO> stats = exerciseLogService.getExerciseStats(loginUser.getId(), days);
        return ResultUtils.success(stats);
    }

    /**
     * 获取总卡路里消耗
     *
     * @param days    天数
     * @param request
     * @return
     */
    @GetMapping("/exercise/calories")
    public BaseResponse<Float> getTotalCaloriesBurned(@RequestParam(defaultValue = "7") Integer days,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Float totalCalories = exerciseLogService.getTotalCaloriesBurned(loginUser.getId(), days);
        return ResultUtils.success(totalCalories);
    }

    // endregion

    // region 训练计划相关接口

    /**
     * 创建训练计划
     *
     * @param trainingPlanAddRequest
     * @param request
     * @return
     */
    @PostMapping("/plan/add")
    public BaseResponse<Long> addTrainingPlan(@RequestBody TrainingPlanAddRequest trainingPlanAddRequest,
            HttpServletRequest request) {
        if (trainingPlanAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TrainingPlan trainingPlan = new TrainingPlan();
        BeanUtils.copyProperties(trainingPlanAddRequest, trainingPlan);

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        trainingPlan.setUserId(loginUser.getId());

        trainingPlanService.validTrainingPlan(trainingPlan, true);
        boolean result = trainingPlanService.save(trainingPlan);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(trainingPlan.getId());
    }

    /**
     * 删除训练计划
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/plan/delete")
    public BaseResponse<Boolean> deleteTrainingPlan(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        TrainingPlan oldTrainingPlan = trainingPlanService.getById(id);
        ThrowUtils.throwIf(oldTrainingPlan == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldTrainingPlan.getUserId().equals(user.getId()) && !userService.isAdmin((User) request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = trainingPlanService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取当前用户的训练计划列表
     *
     * @param trainingPlanQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/plan/my/list/page")
    public BaseResponse<Page<TrainingPlanVO>> listMyTrainingPlanByPage(
            @RequestBody TrainingPlanQueryRequest trainingPlanQueryRequest,
            HttpServletRequest request) {
        if (trainingPlanQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        trainingPlanQueryRequest.setUserId(loginUser.getId());
        long current = trainingPlanQueryRequest.getCurrent();
        long size = trainingPlanQueryRequest.getPageSize();
        Page<TrainingPlan> trainingPlanPage = trainingPlanService.page(new Page<>(current, size),
                trainingPlanService.getQueryWrapper(trainingPlanQueryRequest));
        return ResultUtils.success(trainingPlanService.getTrainingPlanVOPage(trainingPlanPage));
    }

    /**
     * 获取默认训练计划
     *
     * @return
     */
    @GetMapping("/plan/default")
    public BaseResponse<List<TrainingPlanVO>> getDefaultTrainingPlans() {
        List<TrainingPlanVO> defaultPlans = trainingPlanService.getDefaultTrainingPlans();
        return ResultUtils.success(defaultPlans);
    }

    // endregion

    // region 健身目标相关接口

    /**
     * 创建健身目标
     *
     * @param fitnessGoalAddRequest
     * @param request
     * @return
     */
    @PostMapping("/goal/add")
    public BaseResponse<Long> addFitnessGoal(@RequestBody FitnessGoalAddRequest fitnessGoalAddRequest,
            HttpServletRequest request) {
        if (fitnessGoalAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        FitnessGoal fitnessGoal = new FitnessGoal();
        BeanUtils.copyProperties(fitnessGoalAddRequest, fitnessGoal);

        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        fitnessGoal.setUserId(loginUser.getId());

        fitnessGoalService.validFitnessGoal(fitnessGoal, true);
        boolean result = fitnessGoalService.save(fitnessGoal);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(fitnessGoal.getId());
    }

    /**
     * 删除健身目标
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/goal/delete")
    public BaseResponse<Boolean> deleteFitnessGoal(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        FitnessGoal oldFitnessGoal = fitnessGoalService.getById(id);
        ThrowUtils.throwIf(oldFitnessGoal == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldFitnessGoal.getUserId().equals(user.getId()) && !userService.isAdmin((User) request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = fitnessGoalService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新健身目标
     *
     * @param fitnessGoalUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/goal/update")
    public BaseResponse<Boolean> updateFitnessGoal(@RequestBody FitnessGoalUpdateRequest fitnessGoalUpdateRequest,
            HttpServletRequest request) {
        if (fitnessGoalUpdateRequest == null || fitnessGoalUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        FitnessGoal fitnessGoal = new FitnessGoal();
        BeanUtils.copyProperties(fitnessGoalUpdateRequest, fitnessGoal);

        // 判断是否存在
        long id = fitnessGoalUpdateRequest.getId();
        FitnessGoal oldFitnessGoal = fitnessGoalService.getById(id);
        ThrowUtils.throwIf(oldFitnessGoal == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可修改
        if (!oldFitnessGoal.getUserId().equals(user.getId()) && !userService.isAdmin((User) request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        fitnessGoalService.validFitnessGoal(fitnessGoal, false);
        boolean result = fitnessGoalService.updateById(fitnessGoal);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取当前用户的健身目标列表
     *
     * @param fitnessGoalQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/goal/my/list/page")
    public BaseResponse<Page<FitnessGoalVO>> listMyFitnessGoalByPage(
            @RequestBody FitnessGoalQueryRequest fitnessGoalQueryRequest,
            HttpServletRequest request) {
        if (fitnessGoalQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        fitnessGoalQueryRequest.setUserId(loginUser.getId());
        long current = fitnessGoalQueryRequest.getCurrent();
        long size = fitnessGoalQueryRequest.getPageSize();
        Page<FitnessGoal> fitnessGoalPage = fitnessGoalService.page(new Page<>(current, size),
                fitnessGoalService.getQueryWrapper(fitnessGoalQueryRequest));
        return ResultUtils.success(fitnessGoalService.getFitnessGoalVOPage(fitnessGoalPage));
    }

    /**
     * 获取当前用户进行中的健身目标
     *
     * @param request
     * @return
     */
    @GetMapping("/goal/active")
    public BaseResponse<List<FitnessGoalVO>> getActiveFitnessGoals(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<FitnessGoalVO> activeGoals = fitnessGoalService.getActiveFitnessGoals(loginUser.getId());
        return ResultUtils.success(activeGoals);
    }

    // endregion

    // region BMI计算相关接口

    /**
     * 计算BMI指数
     *
     * @param bmiCalculateRequest BMI计算请求参数
     * @return BMI计算结果
     */
    @PostMapping("/bmi/calculate")
    public BaseResponse<BMICalculateVO> calculateBMI(@RequestBody BMICalculateRequest bmiCalculateRequest) {
        if (bmiCalculateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Float weight = bmiCalculateRequest.getWeight();
        Float height = bmiCalculateRequest.getHeight();

        // 参数验证
        if (weight == null || height == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "体重和身高不能为空");
        }

        if (weight <= 0 || height <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "体重和身高必须大于0");
        }

        // 使用工具类计算BMI
        Float bmi = FitnessUtils.calculateBMI(weight, height);
        if (bmi == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "BMI计算失败");
        }

        // 获取BMI分类描述
        String category = FitnessUtils.getBMICategory(bmi);

        // 获取理想体重范围
        float[] idealWeightRange = FitnessUtils.getIdealWeightRange(height);

        // 生成健康建议
        String healthAdvice = generateHealthAdvice(bmi, weight, idealWeightRange);

        // 构建返回结果
        BMICalculateVO result = new BMICalculateVO();
        result.setBmi(bmi);
        result.setCategory(category);
        result.setIdealWeightMin(idealWeightRange[0]);
        result.setIdealWeightMax(idealWeightRange[1]);
        result.setHealthAdvice(healthAdvice);
        result.setInputWeight(weight);
        result.setInputHeight(height);

        log.info("BMI计算完成 - 体重: {}kg, 身高: {}cm, BMI: {}, 分类: {}", weight, height, bmi, category);

        return ResultUtils.success(result);
    }

    /**
     * 生成健康建议
     *
     * @param bmi              BMI值
     * @param currentWeight    当前体重
     * @param idealWeightRange 理想体重范围
     * @return 健康建议
     */
    private String generateHealthAdvice(Float bmi, Float currentWeight, float[] idealWeightRange) {
        if (bmi == null) {
            return "无法生成建议，请检查输入数据";
        }

        StringBuilder advice = new StringBuilder();

        if (bmi < 18.5f) {
            advice.append("您的BMI偏低，建议适当增重。");
            float weightToGain = idealWeightRange[0] - currentWeight;
            if (weightToGain > 0) {
                advice.append(String.format("建议增重%.1fkg以达到健康体重范围。", weightToGain));
            }
            advice.append("建议增加营养摄入，进行适量的力量训练来增加肌肉量。");
        } else if (bmi >= 18.5f && bmi <= 24.9f) {
            advice.append("恭喜！您的BMI在正常范围内，请继续保持健康的生活方式。");
            advice.append("建议保持均衡饮食和规律运动，维持当前体重。");
        } else if (bmi >= 25.0f && bmi <= 29.9f) {
            advice.append("您的BMI偏高，建议适当减重。");
            float weightToLose = currentWeight - idealWeightRange[1];
            if (weightToLose > 0) {
                advice.append(String.format("建议减重%.1fkg以达到健康体重范围。", weightToLose));
            }
            advice.append("建议控制饮食热量摄入，增加有氧运动。");
        } else {
            advice.append("您的BMI过高，强烈建议减重。");
            float weightToLose = currentWeight - idealWeightRange[1];
            if (weightToLose > 0) {
                advice.append(String.format("建议减重%.1fkg以达到健康体重范围。", weightToLose));
            }
            advice.append("建议咨询专业医生或营养师，制定科学的减重计划。");
        }

        return advice.toString();
    }

    // endregion

    // region 排行榜相关接口

    /**
     * 获取排行榜列表
     *
     * @param rankingQueryRequest 排行榜查询请求
     * @return 排行榜列表
     */
    @PostMapping("/ranking/list")
    public BaseResponse<com.mq.mqaiagent.model.dto.ranking.RankingListResponse> getRankingList(
            @RequestBody com.mq.mqaiagent.model.dto.ranking.RankingQueryRequest rankingQueryRequest) {
        if (rankingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String rankingType = rankingQueryRequest.getRankingType();
        if (!"week".equals(rankingType) && !"month".equals(rankingType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排行类型只能是 week 或 month");
        }

        Integer current = rankingQueryRequest.getCurrent();
        Integer pageSize = rankingQueryRequest.getPageSize();

        com.mq.mqaiagent.model.dto.ranking.RankingListResponse response = 
                rankingService.getRankingList(rankingType, current, pageSize);
        return ResultUtils.success(response);
    }

    /**
     * 获取我的排名
     *
     * @param rankingType 排行类型（week/month）
     * @param request     请求
     * @return 我的排名信息
     */
    @GetMapping("/ranking/my")
    public BaseResponse<com.mq.mqaiagent.model.dto.ranking.MyRankingResponse> getMyRanking(
            @RequestParam String rankingType,
            HttpServletRequest request) {
        if (!"week".equals(rankingType) && !"month".equals(rankingType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排行类型只能是 week 或 month");
        }

        User loginUser = userService.getLoginUser(request);
        com.mq.mqaiagent.model.dto.ranking.MyRankingResponse response = 
                rankingService.getMyRanking(loginUser.getId(), rankingType);
        return ResultUtils.success(response);
    }

    /**
     * 刷新排行榜缓存（管理员接口）
     *
     * @param rankingType 排行类型（week/month）
     * @return 是否刷新成功
     */
    @PostMapping("/ranking/refresh")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> refreshRanking(@RequestParam String rankingType) {
        if (!"week".equals(rankingType) && !"month".equals(rankingType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排行类型只能是 week 或 month");
        }

        boolean result = rankingService.refreshRanking(rankingType);
        return ResultUtils.success(result);
    }

    // endregion
}
