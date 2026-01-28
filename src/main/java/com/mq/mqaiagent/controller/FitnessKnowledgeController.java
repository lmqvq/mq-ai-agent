package com.mq.mqaiagent.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mq.mqaiagent.annotation.AuthCheck;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.DeleteRequest;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.constant.UserConstant;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.manager.CosManager;
import com.mq.mqaiagent.model.dto.knowledge.*;
import com.mq.mqaiagent.model.entity.*;
import com.mq.mqaiagent.model.vo.*;
import com.mq.mqaiagent.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 健身知识相关接口
 */
@RestController
@RequestMapping("/knowledge")
@Slf4j
public class FitnessKnowledgeController {

    @Value("${cos.client.bucket-url:}")
    private String bucketUrl;

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private KnowledgeBasicsService knowledgeBasicsService;

    @Resource
    private KnowledgeExerciseService knowledgeExerciseService;

    @Resource
    private KnowledgeNutrientService knowledgeNutrientService;

    @Resource
    private KnowledgeProgramService knowledgeProgramService;

    @Resource
    private KnowledgeMealService knowledgeMealService;

    // region 图片上传接口

    /**
     * 上传知识库图片（管理员）
     *
     * @param file 图片文件
     * @param type 图片类型 (basics/exercise/meal)
     * @return 图片URL
     */
    @PostMapping("/image/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadKnowledgeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            HttpServletRequest request) {
        // 校验文件
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR, "类型不能为空");

        // 校验文件大小（不超过5MB）
        long fileSize = file.getSize();
        final long FIVE_MB = 5 * 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > FIVE_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过5M");

        // 校验文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> validSuffixList = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
        ThrowUtils.throwIf(!validSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件类型错误");

        // 生成存储路径：fitness_knowledge/{type}/{uuid}.{suffix}
        String uuid = IdUtil.simpleUUID();
        String filepath = String.format("fitness_knowledge/%s/%s.%s", type, uuid, suffix);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uuid, "." + suffix);
            file.transferTo(tempFile);
            cosManager.putObject(filepath, tempFile);
            return ResultUtils.success(bucketUrl + "/" + filepath);
        } catch (Exception e) {
            log.error("知识库图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    // endregion

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

    // region 饮食计划相关接口

    /**
     * 获取饮食计划列表
     *
     * @param queryRequest 查询请求
     * @return 列表结果
     */
    @PostMapping("/meals/list")
    public BaseResponse<List<KnowledgeMealVO>> listMeals(
            @RequestBody KnowledgeMealQueryRequest queryRequest) {
        if (queryRequest == null) {
            queryRequest = new KnowledgeMealQueryRequest();
        }

        QueryWrapper<KnowledgeMeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isDelete", 0);

        // 餐食类型筛选
        if (StringUtils.isNotBlank(queryRequest.getMealType())) {
            queryWrapper.eq("mealType", queryRequest.getMealType());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(queryRequest.getSearchText())) {
            KnowledgeMealQueryRequest finalQueryRequest = queryRequest;
            queryWrapper.and(qw -> qw
                    .like("name", finalQueryRequest.getSearchText())
                    .or()
                    .like("description", finalQueryRequest.getSearchText())
            );
        }

        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        List<KnowledgeMeal> list = knowledgeMealService.list(queryWrapper);

        List<KnowledgeMealVO> voList = list.stream().map(entity -> {
            KnowledgeMealVO vo = new KnowledgeMealVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    // endregion

    // region 管理接口（仅管理员）

    /**
     * 添加基础知识（管理员）
     */
    @PostMapping("/basics/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addBasics(@RequestBody KnowledgeBasics entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(entity.getTitle()), ErrorCode.PARAMS_ERROR, "标题不能为空");
        entity.setViews(0);
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        boolean result = knowledgeBasicsService.save(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 更新基础知识（管理员）
     */
    @PostMapping("/basics/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateBasics(@RequestBody KnowledgeBasics entity) {
        ThrowUtils.throwIf(entity == null || entity.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeBasicsService.updateById(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除基础知识（管理员）
     */
    @PostMapping("/basics/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteBasics(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeBasicsService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 添加动作指导（管理员）
     */
    @PostMapping("/exercises/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addExercise(@RequestBody KnowledgeExercise entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(entity.getName()), ErrorCode.PARAMS_ERROR, "名称不能为空");
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        boolean result = knowledgeExerciseService.save(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 更新动作指导（管理员）
     */
    @PostMapping("/exercises/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateExercise(@RequestBody KnowledgeExercise entity) {
        ThrowUtils.throwIf(entity == null || entity.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeExerciseService.updateById(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除动作指导（管理员）
     */
    @PostMapping("/exercises/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteExercise(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeExerciseService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 添加营养知识（管理员）
     */
    @PostMapping("/nutrients/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addNutrient(@RequestBody KnowledgeNutrient entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(entity.getName()), ErrorCode.PARAMS_ERROR, "名称不能为空");
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        boolean result = knowledgeNutrientService.save(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 更新营养知识（管理员）
     */
    @PostMapping("/nutrients/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateNutrient(@RequestBody KnowledgeNutrient entity) {
        ThrowUtils.throwIf(entity == null || entity.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeNutrientService.updateById(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除营养知识（管理员）
     */
    @PostMapping("/nutrients/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteNutrient(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeNutrientService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 添加训练计划（管理员）
     */
    @PostMapping("/programs/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addProgram(@RequestBody KnowledgeProgram entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(entity.getName()), ErrorCode.PARAMS_ERROR, "名称不能为空");
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        boolean result = knowledgeProgramService.save(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 更新训练计划（管理员）
     */
    @PostMapping("/programs/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateProgram(@RequestBody KnowledgeProgram entity) {
        ThrowUtils.throwIf(entity == null || entity.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeProgramService.updateById(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除训练计划（管理员）
     */
    @PostMapping("/programs/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteProgram(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeProgramService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 添加饮食计划（管理员）
     */
    @PostMapping("/meals/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addMeal(@RequestBody KnowledgeMeal entity) {
        ThrowUtils.throwIf(entity == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(entity.getName()), ErrorCode.PARAMS_ERROR, "名称不能为空");
        entity.setSortOrder(entity.getSortOrder() == null ? 0 : entity.getSortOrder());
        boolean result = knowledgeMealService.save(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 更新饮食计划（管理员）
     */
    @PostMapping("/meals/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMeal(@RequestBody KnowledgeMeal entity) {
        ThrowUtils.throwIf(entity == null || entity.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeMealService.updateById(entity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除饮食计划（管理员）
     */
    @PostMapping("/meals/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMeal(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = knowledgeMealService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}

