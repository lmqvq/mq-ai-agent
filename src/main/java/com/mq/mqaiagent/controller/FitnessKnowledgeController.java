package com.mq.mqaiagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.model.dto.knowledge.*;
import com.mq.mqaiagent.model.entity.*;
import com.mq.mqaiagent.model.vo.*;
import com.mq.mqaiagent.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 健身知识相关接口
 */
@RestController
@RequestMapping("/knowledge")
@Slf4j
public class FitnessKnowledgeController {

    @Resource
    private KnowledgeBasicsService knowledgeBasicsService;

    @Resource
    private KnowledgeExerciseService knowledgeExerciseService;

    @Resource
    private KnowledgeNutrientService knowledgeNutrientService;

    @Resource
    private KnowledgeProgramService knowledgeProgramService;

    // region 基础知识相关接口

    /**
     * 分页获取基础知识列表
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/basics/list")
    public BaseResponse<Page<KnowledgeBasicsVO>> listBasicsKnowledge(
            @RequestBody KnowledgeBasicsQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();

        // 构建查询条件
        QueryWrapper<KnowledgeBasics> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 难度筛选
        if (StringUtils.isNotBlank(queryRequest.getDifficulty())) {
            queryWrapper.eq("difficulty", queryRequest.getDifficulty());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            queryWrapper.and(qw -> qw
                    .like("title", queryRequest.getSearchText())
                    .or()
                    .like("description", queryRequest.getSearchText())
            );
        }

        // 按排序字段和创建时间排序
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        // 分页查询
        Page<KnowledgeBasics> page = knowledgeBasicsService.page(new Page<>(current, size), queryWrapper);

        // 转换为VO
        Page<KnowledgeBasicsVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<KnowledgeBasicsVO> voList = page.getRecords().stream().map(entity -> {
            KnowledgeBasicsVO vo = new KnowledgeBasicsVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return ResultUtils.success(voPage);
    }

    /**
     * 根据ID获取基础知识详情
     *
     * @param id 知识ID
     * @return 知识详情
     */
    @GetMapping("/basics/{id}")
    public BaseResponse<KnowledgeBasicsVO> getBasicsKnowledgeById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        KnowledgeBasics entity = knowledgeBasicsService.getById(id);
        if (entity == null || entity.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        KnowledgeBasicsVO vo = new KnowledgeBasicsVO();
        BeanUtils.copyProperties(entity, vo);

        // 增加浏览次数
        entity.setViews(entity.getViews() + 1);
        knowledgeBasicsService.updateById(entity);

        return ResultUtils.success(vo);
    }

    // endregion

    // region 动作指导相关接口

    /**
     * 分页获取动作指导列表
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/exercises/list")
    public BaseResponse<Page<KnowledgeExerciseVO>> listExercises(
            @RequestBody KnowledgeExerciseQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();

        // 构建查询条件
        QueryWrapper<KnowledgeExercise> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 分类筛选
        if (StringUtils.isNotBlank(queryRequest.getCategory())) {
            queryWrapper.eq("category", queryRequest.getCategory());
        }

        // 难度筛选
        if (StringUtils.isNotBlank(queryRequest.getDifficulty())) {
            queryWrapper.eq("difficulty", queryRequest.getDifficulty());
        }

        // 肌群筛选
        if (StringUtils.isNotBlank(queryRequest.getMuscleGroup())) {
            queryWrapper.eq("muscleGroup", queryRequest.getMuscleGroup());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            queryWrapper.and(qw -> qw
                    .like("name", queryRequest.getSearchText())
                    .or()
                    .like("description", queryRequest.getSearchText())
            );
        }

        // 按排序字段和创建时间排序
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        // 分页查询
        Page<KnowledgeExercise> page = knowledgeExerciseService.page(new Page<>(current, size), queryWrapper);

        // 转换为VO
        Page<KnowledgeExerciseVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<KnowledgeExerciseVO> voList = page.getRecords().stream().map(entity -> {
            KnowledgeExerciseVO vo = new KnowledgeExerciseVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return ResultUtils.success(voPage);
    }

    /**
     * 根据ID获取动作指导详情
     *
     * @param id 动作ID
     * @return 动作详情
     */
    @GetMapping("/exercises/{id}")
    public BaseResponse<KnowledgeExerciseVO> getExerciseById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        KnowledgeExercise entity = knowledgeExerciseService.getById(id);
        if (entity == null || entity.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        KnowledgeExerciseVO vo = new KnowledgeExerciseVO();
        BeanUtils.copyProperties(entity, vo);

        return ResultUtils.success(vo);
    }

    // endregion

    // region 营养知识相关接口

    /**
     * 获取营养知识列表
     *
     * @param queryRequest 查询请求
     * @return 列表结果
     */
    @PostMapping("/nutrients/list")
    public BaseResponse<List<KnowledgeNutrientVO>> listNutrients(
            @RequestBody KnowledgeNutrientQueryRequest queryRequest) {
        if (queryRequest == null) {
            queryRequest = new KnowledgeNutrientQueryRequest();
        }

        // 构建查询条件
        QueryWrapper<KnowledgeNutrient> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 关键词搜索
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            KnowledgeNutrientQueryRequest finalQueryRequest = queryRequest;
            queryWrapper.and(qw -> qw
                    .like("name", finalQueryRequest.getSearchText())
                    .or()
                    .like("description", finalQueryRequest.getSearchText())
            );
        }

        // 按排序字段排序
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        // 查询列表
        List<KnowledgeNutrient> list = knowledgeNutrientService.list(queryWrapper);

        // 转换为VO
        List<KnowledgeNutrientVO> voList = list.stream().map(entity -> {
            KnowledgeNutrientVO vo = new KnowledgeNutrientVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    // endregion

    // region 训练计划相关接口

    /**
     * 获取训练计划列表
     *
     * @param queryRequest 查询请求
     * @return 列表结果
     */
    @PostMapping("/programs/list")
    public BaseResponse<List<KnowledgeProgramVO>> listPrograms(
            @RequestBody KnowledgeProgramQueryRequest queryRequest) {
        if (queryRequest == null) {
            queryRequest = new KnowledgeProgramQueryRequest();
        }

        // 构建查询条件
        QueryWrapper<KnowledgeProgram> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 类型筛选
        if (StringUtils.isNotBlank(queryRequest.getType())) {
            queryWrapper.eq("type", queryRequest.getType());
        }

        // 级别筛选
        if (StringUtils.isNotBlank(queryRequest.getLevel())) {
            queryWrapper.eq("level", queryRequest.getLevel());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            KnowledgeProgramQueryRequest finalQueryRequest = queryRequest;
            queryWrapper.and(qw -> qw
                    .like("name", finalQueryRequest.getSearchText())
                    .or()
                    .like("description", finalQueryRequest.getSearchText())
            );
        }

        // 按排序字段排序
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        // 查询列表
        List<KnowledgeProgram> list = knowledgeProgramService.list(queryWrapper);

        // 转换为VO
        List<KnowledgeProgramVO> voList = list.stream().map(entity -> {
            KnowledgeProgramVO vo = new KnowledgeProgramVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    // endregion
}

