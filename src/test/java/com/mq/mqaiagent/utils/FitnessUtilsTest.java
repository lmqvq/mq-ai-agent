package com.mq.mqaiagent.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 健身工具类测试
 */
public class FitnessUtilsTest {

    @Test
    public void testCalculateBMI() {
        // 测试正常情况
        Float bmi = FitnessUtils.calculateBMI(70.0f, 175.0f);
        assertNotNull(bmi);
        assertEquals(22.86f, bmi, 0.01f);
        
        // 测试边界情况
        assertNull(FitnessUtils.calculateBMI(null, 175.0f));
        assertNull(FitnessUtils.calculateBMI(70.0f, null));
        assertNull(FitnessUtils.calculateBMI(0.0f, 175.0f));
        assertNull(FitnessUtils.calculateBMI(70.0f, 0.0f));
    }

    @Test
    public void testGetBMICategory() {
        assertEquals("偏瘦", FitnessUtils.getBMICategory(17.0f));
        assertEquals("正常", FitnessUtils.getBMICategory(22.0f));
        assertEquals("超重", FitnessUtils.getBMICategory(27.0f));
        assertEquals("肥胖", FitnessUtils.getBMICategory(32.0f));
        assertEquals("未知", FitnessUtils.getBMICategory(null));
    }

    @Test
    public void testGetIdealWeightRange() {
        float[] range = FitnessUtils.getIdealWeightRange(175.0f);
        assertNotNull(range);
        assertEquals(2, range.length);
        assertTrue(range[0] > 0);
        assertTrue(range[1] > range[0]);
        
        // 测试边界情况
        float[] invalidRange = FitnessUtils.getIdealWeightRange(null);
        assertEquals(0.0f, invalidRange[0]);
        assertEquals(0.0f, invalidRange[1]);
    }

    @Test
    public void testCalculateBMR() {
        // 测试男性BMR计算
        Float maleBMR = FitnessUtils.calculateBMR(70.0f, 175.0f, 25, true);
        assertNotNull(maleBMR);
        assertTrue(maleBMR > 0);
        
        // 测试女性BMR计算
        Float femaleBMR = FitnessUtils.calculateBMR(60.0f, 165.0f, 25, false);
        assertNotNull(femaleBMR);
        assertTrue(femaleBMR > 0);
        assertTrue(maleBMR > femaleBMR); // 一般情况下男性BMR更高
        
        // 测试边界情况
        assertNull(FitnessUtils.calculateBMR(null, 175.0f, 25, true));
    }

    @Test
    public void testCalculateTDEE() {
        Float bmr = 1500.0f;
        Float tdee = FitnessUtils.calculateTDEE(bmr, 1.375f);
        assertNotNull(tdee);
        assertEquals(2062.5f, tdee, 0.1f);
        
        // 测试边界情况
        assertNull(FitnessUtils.calculateTDEE(null, 1.375f));
        assertNull(FitnessUtils.calculateTDEE(bmr, null));
    }

    @Test
    public void testEstimateCaloriesBurned() {
        // 测试跑步消耗
        Float calories = FitnessUtils.estimateCaloriesBurned(70.0f, 30, "跑步");
        assertNotNull(calories);
        assertTrue(calories > 0);
        
        // 测试瑜伽消耗
        Float yogaCalories = FitnessUtils.estimateCaloriesBurned(60.0f, 60, "瑜伽");
        assertNotNull(yogaCalories);
        assertTrue(yogaCalories > 0);
        assertTrue(calories > yogaCalories); // 跑步消耗应该比瑜伽高
        
        // 测试边界情况
        assertNull(FitnessUtils.estimateCaloriesBurned(null, 30, "跑步"));
    }

    @Test
    public void testValidateFitnessData() {
        // 测试正常数据
        assertTrue(FitnessUtils.validateFitnessData(70.0f, 175.0f, 15.0f));
        
        // 测试异常数据
        assertFalse(FitnessUtils.validateFitnessData(10.0f, 175.0f, 15.0f)); // 体重过轻
        assertFalse(FitnessUtils.validateFitnessData(70.0f, 10.0f, 15.0f)); // 身高过低
        assertFalse(FitnessUtils.validateFitnessData(70.0f, 175.0f, 150.0f)); // 体脂率过高
        
        // 测试null值
        assertTrue(FitnessUtils.validateFitnessData(null, 175.0f, 15.0f));
    }

    @Test
    public void testValidateExerciseData() {
        // 测试正常数据
        assertTrue(FitnessUtils.validateExerciseData(30, 300.0f));
        
        // 测试异常数据
        assertFalse(FitnessUtils.validateExerciseData(0, 300.0f)); // 时长为0
        assertFalse(FitnessUtils.validateExerciseData(30, -100.0f)); // 负卡路里
        assertFalse(FitnessUtils.validateExerciseData(2000, 300.0f)); // 时长过长
        
        // 测试null值
        assertTrue(FitnessUtils.validateExerciseData(null, 300.0f));
    }
}
