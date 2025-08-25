package com.mq.mqaiagent.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本相似度服务测试类
 * 
 * @author MQQQ
 * @version v1.0
 * @since 2025/1/25
 */
@SpringBootTest
@ActiveProfiles("test")
class TextSimilarityServiceTest {

    @Resource
    private TextSimilarityService textSimilarityService;

    @Test
    void testCalculateCosineSimilarity() {
        // 测试完全相同的文本
        String text1 = "我想增肌，请给我建议";
        String text2 = "我想增肌，请给我建议";
        double similarity1 = textSimilarityService.calculateCosineSimilarity(text1, text2);
        assertEquals(1.0, similarity1, 0.01);

        // 测试相似的文本
        String text3 = "我想增肌，请给我一些建议";
        double similarity2 = textSimilarityService.calculateCosineSimilarity(text1, text3);
        assertTrue(similarity2 > 0.7);

        // 测试不相关的文本
        String text4 = "今天天气怎么样";
        double similarity3 = textSimilarityService.calculateCosineSimilarity(text1, text4);
        assertTrue(similarity3 < 0.3);
    }

    @Test
    void testCalculateCosineSimilarityWithEmptyText() {
        // 测试空文本
        String text1 = "我想增肌";
        String emptyText = "";
        String nullText = null;

        double similarity1 = textSimilarityService.calculateCosineSimilarity(text1, emptyText);
        assertEquals(0.0, similarity1);

        double similarity2 = textSimilarityService.calculateCosineSimilarity(text1, nullText);
        assertEquals(0.0, similarity2);

        double similarity3 = textSimilarityService.calculateCosineSimilarity(emptyText, nullText);
        assertEquals(0.0, similarity3);
    }

    @Test
    void testCalculateCosineSimilarityWithShortText() {
        // 测试短文本
        String shortText1 = "增肌";
        String shortText2 = "减脂";

        double similarity = textSimilarityService.calculateCosineSimilarity(shortText1, shortText2);
        assertEquals(0.0, similarity); // 短文本应该返回0
    }

    @Test
    void testIsSimilar() {
        // 测试相似判断
        String text1 = "我想增肌，请给我建议";
        String text2 = "我想增肌，请给我一些建议";
        String text3 = "今天天气怎么样";

        assertTrue(textSimilarityService.isSimilar(text1, text2));
        assertFalse(textSimilarityService.isSimilar(text1, text3));
    }

    @Test
    void testGenerateTextHash() {
        // 测试哈希生成
        String text1 = "我想增肌，请给我建议";
        String text2 = "我想增肌，请给我建议"; // 相同文本
        String text3 = "我想增肌，请给我一些建议"; // 不同文本

        String hash1 = textSimilarityService.generateTextHash(text1);
        String hash2 = textSimilarityService.generateTextHash(text2);
        String hash3 = textSimilarityService.generateTextHash(text3);

        // 相同文本应该生成相同哈希
        assertEquals(hash1, hash2);
        // 不同文本应该生成不同哈希
        assertNotEquals(hash1, hash3);

        // 测试空文本
        String emptyHash = textSimilarityService.generateTextHash("");
        assertEquals("empty", emptyHash);

        String nullHash = textSimilarityService.generateTextHash(null);
        assertEquals("empty", nullHash);
    }

    @Test
    void testCalculateSimilarities() {
        // 准备测试数据
        String targetText = "我想增肌，请给我建议";
        List<String> candidates = Arrays.asList(
                "我想增肌，请给我一些建议",  // 高相似度
                "如何进行力量训练",         // 中等相似度
                "今天天气怎么样",           // 低相似度
                "我想减脂，请给我建议"      // 中等相似度
        );

        // 执行测试
        List<TextSimilarityService.SimilarityResult> results = 
                textSimilarityService.calculateSimilarities(targetText, candidates);

        // 验证结果
        assertFalse(results.isEmpty());
        
        // 结果应该按相似度降序排列
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getSimilarity() >= results.get(i + 1).getSimilarity());
        }

        // 第一个结果应该是最相似的
        TextSimilarityService.SimilarityResult topResult = results.get(0);
        assertEquals("我想增肌，请给我一些建议", topResult.getText());
        assertTrue(topResult.getSimilarity() > 0.7);
    }

    @Test
    void testCalculateSimilaritiesWithEmptyInput() {
        // 测试空输入
        List<TextSimilarityService.SimilarityResult> results1 = 
                textSimilarityService.calculateSimilarities("", Arrays.asList("test"));
        assertTrue(results1.isEmpty());

        List<TextSimilarityService.SimilarityResult> results2 = 
                textSimilarityService.calculateSimilarities("test", Arrays.asList());
        assertTrue(results2.isEmpty());

        List<TextSimilarityService.SimilarityResult> results3 = 
                textSimilarityService.calculateSimilarities(null, null);
        assertTrue(results3.isEmpty());
    }

    @Test
    void testSimilarityResult() {
        // 测试SimilarityResult类
        String text = "测试文本";
        double similarity = 0.85;
        
        TextSimilarityService.SimilarityResult result = 
                new TextSimilarityService.SimilarityResult(text, similarity);

        assertEquals(text, result.getText());
        assertEquals(similarity, result.getSimilarity(), 0.001);
        assertTrue(result.isSimilar(0.8));
        assertFalse(result.isSimilar(0.9));

        // 测试toString方法
        String toString = result.toString();
        assertTrue(toString.contains("测试文本"));
        assertTrue(toString.contains("0.850"));
    }

    @Test
    void testSimilarityResultWithLongText() {
        // 测试长文本的toString方法
        String longText = "这是一个非常长的文本，用来测试toString方法是否会正确截断文本内容，确保输出的可读性";
        double similarity = 0.75;
        
        TextSimilarityService.SimilarityResult result = 
                new TextSimilarityService.SimilarityResult(longText, similarity);

        String toString = result.toString();
        assertTrue(toString.contains("..."));
        assertTrue(toString.length() < longText.length() + 50); // 确保被截断了
    }

    @Test
    void testGetSimilarityThreshold() {
        // 测试获取相似度阈值
        double threshold = textSimilarityService.getSimilarityThreshold();
        assertTrue(threshold > 0.0 && threshold <= 1.0);
    }

    @Test
    void testChineseTextSimilarity() {
        // 测试中文文本相似度计算
        String text1 = "我想学习健身知识";
        String text2 = "我想了解健身相关知识";
        String text3 = "健身知识学习指南";

        double similarity1 = textSimilarityService.calculateCosineSimilarity(text1, text2);
        double similarity2 = textSimilarityService.calculateCosineSimilarity(text1, text3);

        assertTrue(similarity1 > 0.5);
        assertTrue(similarity2 > 0.3);
    }

    @Test
    void testMixedLanguageText() {
        // 测试中英文混合文本
        String text1 = "我想学习fitness知识";
        String text2 = "我想了解fitness相关内容";

        double similarity = textSimilarityService.calculateCosineSimilarity(text1, text2);
        assertTrue(similarity > 0.5);
    }
}
