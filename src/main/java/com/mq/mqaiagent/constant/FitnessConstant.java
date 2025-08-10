package com.mq.mqaiagent.constant;

/**
 * 健身相关常量
 */
public interface FitnessConstant {

    /**
     * BMI分类常量
     */
    interface BMI {
        /**
         * 偏瘦
         */
        float UNDERWEIGHT = 18.5f;
        
        /**
         * 正常
         */
        float NORMAL_MIN = 18.5f;
        float NORMAL_MAX = 24.9f;
        
        /**
         * 超重
         */
        float OVERWEIGHT_MIN = 25.0f;
        float OVERWEIGHT_MAX = 29.9f;
        
        /**
         * 肥胖
         */
        float OBESE = 30.0f;
    }

    /**
     * 运动类型常量
     */
    interface ExerciseType {
        String STRENGTH_TRAINING = "力量训练";
        String CARDIO = "有氧运动";
        String YOGA = "瑜伽";
        String PILATES = "普拉提";
        String RUNNING = "跑步";
        String CYCLING = "骑行";
        String SWIMMING = "游泳";
        String WALKING = "步行";
        String DANCING = "舞蹈";
        String MARTIAL_ARTS = "武术";
    }

    /**
     * 训练计划类型常量
     */
    interface PlanType {
        String MUSCLE_GAIN = "增肌";
        String FAT_LOSS = "减脂";
        String BODY_SHAPING = "塑形";
        String STRENGTH = "力量提升";
        String ENDURANCE = "耐力提升";
        String FLEXIBILITY = "柔韧性";
        String REHABILITATION = "康复训练";
    }

    /**
     * 健身目标类型常量
     */
    interface GoalType {
        String WEIGHT_LOSS = "减重";
        String WEIGHT_GAIN = "增重";
        String MUSCLE_GAIN = "增肌";
        String FAT_LOSS = "减脂";
        String BODY_SHAPING = "塑形";
        String STRENGTH_IMPROVEMENT = "力量提升";
        String ENDURANCE_IMPROVEMENT = "耐力提升";
        String FLEXIBILITY_IMPROVEMENT = "柔韧性提升";
        String POSTURE_IMPROVEMENT = "体态改善";
        String HEALTH_MAINTENANCE = "健康维持";
    }

    /**
     * 数据验证常量
     */
    interface Validation {
        // 体重范围 (kg)
        float MIN_WEIGHT = 20.0f;
        float MAX_WEIGHT = 500.0f;
        
        // 身高范围 (cm)
        float MIN_HEIGHT = 50.0f;
        float MAX_HEIGHT = 300.0f;
        
        // 体脂率范围 (%)
        float MIN_BODY_FAT = 0.0f;
        float MAX_BODY_FAT = 100.0f;
        
        // 运动时长范围 (分钟)
        int MIN_DURATION = 1;
        int MAX_DURATION = 1440; // 24小时
        
        // 卡路里消耗范围
        float MIN_CALORIES = 0.0f;
        float MAX_CALORIES = 10000.0f;
    }

    /**
     * 默认值常量
     */
    interface Default {
        // 默认查询天数
        int DEFAULT_TREND_DAYS = 30;
        int DEFAULT_STATS_DAYS = 7;
        
        // 默认分页大小
        int DEFAULT_PAGE_SIZE = 10;
        int MAX_PAGE_SIZE = 100;
    }
}
