package com.mq.mqaiagent.utils;

import com.mq.mqaiagent.constant.FitnessConstant;

/**
 * 健身相关工具类
 */
public class FitnessUtils {

    /**
     * 计算BMI指数
     *
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @return BMI值，保留两位小数
     */
    public static Float calculateBMI(Float weight, Float height) {
        if (weight == null || height == null || weight <= 0 || height <= 0) {
            return null;
        }
        
        // 身高转换为米
        float heightInMeters = height / 100;
        float bmi = weight / (heightInMeters * heightInMeters);
        
        // 保留两位小数
        return Math.round(bmi * 100) / 100.0f;
    }

    /**
     * 获取BMI分类描述
     *
     * @param bmi BMI值
     * @return BMI分类描述
     */
    public static String getBMICategory(Float bmi) {
        if (bmi == null) {
            return "未知";
        }
        
        if (bmi < FitnessConstant.BMI.UNDERWEIGHT) {
            return "偏瘦";
        } else if (bmi >= FitnessConstant.BMI.NORMAL_MIN && bmi <= FitnessConstant.BMI.NORMAL_MAX) {
            return "正常";
        } else if (bmi >= FitnessConstant.BMI.OVERWEIGHT_MIN && bmi <= FitnessConstant.BMI.OVERWEIGHT_MAX) {
            return "超重";
        } else if (bmi >= FitnessConstant.BMI.OBESE) {
            return "肥胖";
        } else {
            return "未知";
        }
    }

    /**
     * 计算理想体重范围
     *
     * @param height 身高(cm)
     * @return 理想体重范围数组 [最小值, 最大值]
     */
    public static float[] getIdealWeightRange(Float height) {
        if (height == null || height <= 0) {
            return new float[]{0, 0};
        }
        
        float heightInMeters = height / 100;
        float minWeight = FitnessConstant.BMI.NORMAL_MIN * heightInMeters * heightInMeters;
        float maxWeight = FitnessConstant.BMI.NORMAL_MAX * heightInMeters * heightInMeters;
        
        return new float[]{
            Math.round(minWeight * 10) / 10.0f,
            Math.round(maxWeight * 10) / 10.0f
        };
    }

    /**
     * 估算基础代谢率 (BMR) - 使用Harris-Benedict公式
     *
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @param age    年龄
     * @param isMale 是否为男性
     * @return 基础代谢率(卡路里/天)
     */
    public static Float calculateBMR(Float weight, Float height, Integer age, Boolean isMale) {
        if (weight == null || height == null || age == null || isMale == null) {
            return null;
        }
        
        float bmr;
        if (isMale) {
            // 男性: BMR = 88.362 + (13.397 × 体重kg) + (4.799 × 身高cm) - (5.677 × 年龄)
            bmr = 88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * age);
        } else {
            // 女性: BMR = 447.593 + (9.247 × 体重kg) + (3.098 × 身高cm) - (4.330 × 年龄)
            bmr = 447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * age);
        }
        
        return Math.round(bmr * 10) / 10.0f;
    }

    /**
     * 根据活动水平计算每日总消耗 (TDEE)
     *
     * @param bmr           基础代谢率
     * @param activityLevel 活动水平 (1.2-久坐, 1.375-轻度活动, 1.55-中度活动, 1.725-重度活动, 1.9-极重度活动)
     * @return 每日总消耗(卡路里/天)
     */
    public static Float calculateTDEE(Float bmr, Float activityLevel) {
        if (bmr == null || activityLevel == null) {
            return null;
        }
        
        float tdee = bmr * activityLevel;
        return Math.round(tdee * 10) / 10.0f;
    }

    /**
     * 估算运动消耗卡路里
     *
     * @param weight       体重(kg)
     * @param duration     运动时长(分钟)
     * @param exerciseType 运动类型
     * @return 消耗卡路里
     */
    public static Float estimateCaloriesBurned(Float weight, Integer duration, String exerciseType) {
        if (weight == null || duration == null || exerciseType == null) {
            return null;
        }
        
        // MET值 (代谢当量)
        float met = getMETValue(exerciseType);
        
        // 卡路里消耗 = MET × 体重(kg) × 时间(小时)
        float hours = duration / 60.0f;
        float calories = met * weight * hours;
        
        return Math.round(calories * 10) / 10.0f;
    }

    /**
     * 获取不同运动类型的MET值
     *
     * @param exerciseType 运动类型
     * @return MET值
     */
    private static float getMETValue(String exerciseType) {
        return switch (exerciseType) {
            case FitnessConstant.ExerciseType.WALKING -> 3.5f;
            case FitnessConstant.ExerciseType.RUNNING -> 8.0f;
            case FitnessConstant.ExerciseType.CYCLING -> 6.0f;
            case FitnessConstant.ExerciseType.SWIMMING -> 7.0f;
            case FitnessConstant.ExerciseType.STRENGTH_TRAINING -> 5.0f;
            case FitnessConstant.ExerciseType.YOGA -> 2.5f;
            case FitnessConstant.ExerciseType.PILATES -> 3.0f;
            case FitnessConstant.ExerciseType.DANCING -> 4.5f;
            case FitnessConstant.ExerciseType.MARTIAL_ARTS -> 6.0f;
            default -> 4.0f; // 默认中等强度
        };
    }

    /**
     * 验证健身数据的合理性
     *
     * @param weight  体重
     * @param height  身高
     * @param bodyFat 体脂率
     * @return 验证结果
     */
    public static boolean validateFitnessData(Float weight, Float height, Float bodyFat) {
        if (weight != null && (weight < FitnessConstant.Validation.MIN_WEIGHT || weight > FitnessConstant.Validation.MAX_WEIGHT)) {
            return false;
        }
        if (height != null && (height < FitnessConstant.Validation.MIN_HEIGHT || height > FitnessConstant.Validation.MAX_HEIGHT)) {
            return false;
        }
        if (bodyFat != null && (bodyFat < FitnessConstant.Validation.MIN_BODY_FAT || bodyFat > FitnessConstant.Validation.MAX_BODY_FAT)) {
            return false;
        }
        return true;
    }

    /**
     * 验证运动数据的合理性
     *
     * @param duration       运动时长
     * @param caloriesBurned 消耗卡路里
     * @return 验证结果
     */
    public static boolean validateExerciseData(Integer duration, Float caloriesBurned) {
        if (duration != null && (duration < FitnessConstant.Validation.MIN_DURATION || duration > FitnessConstant.Validation.MAX_DURATION)) {
            return false;
        }
        if (caloriesBurned != null && (caloriesBurned < FitnessConstant.Validation.MIN_CALORIES || caloriesBurned > FitnessConstant.Validation.MAX_CALORIES)) {
            return false;
        }
        return true;
    }
}
