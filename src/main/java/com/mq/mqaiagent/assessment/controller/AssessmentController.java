package com.mq.mqaiagent.assessment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mq.mqaiagent.assessment.constant.AssessmentConstant;
import com.mq.mqaiagent.assessment.model.dto.profile.AssessmentProfileSaveRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordAddRequest;
import com.mq.mqaiagent.assessment.model.dto.record.AssessmentRecordQueryRequest;
import com.mq.mqaiagent.assessment.model.dto.report.AssessmentReportGenerateRequest;
import com.mq.mqaiagent.assessment.model.dto.scheme.AssessmentSchemeQueryRequest;
import com.mq.mqaiagent.assessment.model.vo.AssessmentProfileVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordDetailVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentRecordVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentReportVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentSchemeVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentTrendVO;
import com.mq.mqaiagent.assessment.service.AssessmentProfileService;
import com.mq.mqaiagent.assessment.service.AssessmentRecordService;
import com.mq.mqaiagent.assessment.service.AssessmentReportService;
import com.mq.mqaiagent.assessment.service.AssessmentSchemeService;
import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.exception.ThrowUtils;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Assessment controller.
 */
@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    @Resource
    private AssessmentSchemeService assessmentSchemeService;

    @Resource
    private AssessmentProfileService assessmentProfileService;

    @Resource
    private AssessmentRecordService assessmentRecordService;

    @Resource
    private AssessmentReportService assessmentReportService;

    @Resource
    private UserService userService;

    @PostMapping("/scheme/list")
    public BaseResponse<List<AssessmentSchemeVO>> listScheme(
            @RequestBody(required = false) AssessmentSchemeQueryRequest schemeQueryRequest) {
        return ResultUtils.success(assessmentSchemeService.listSchemeVO(schemeQueryRequest));
    }

    @GetMapping("/scheme/get")
    public BaseResponse<AssessmentSchemeVO> getScheme(@RequestParam String schemeCode) {
        AssessmentSchemeVO schemeVO = assessmentSchemeService.getSchemeDetail(schemeCode);
        ThrowUtils.throwIf(schemeVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(schemeVO);
    }

    @PostMapping("/profile/save")
    public BaseResponse<AssessmentProfileVO> saveMyProfile(
            @RequestBody AssessmentProfileSaveRequest profileSaveRequest,
            HttpServletRequest request) {
        if (profileSaveRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentProfileService.saveOrUpdateProfile(loginUser.getId(), profileSaveRequest));
    }

    @GetMapping("/profile/my/get")
    public BaseResponse<AssessmentProfileVO> getMyProfile(
            @RequestParam(defaultValue = AssessmentConstant.DEFAULT_SCHEME_CODE) String schemeCode,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        AssessmentProfileVO profileVO = assessmentProfileService.getUserProfile(loginUser.getId(), schemeCode);
        ThrowUtils.throwIf(profileVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(profileVO);
    }

    @PostMapping("/record/add")
    public BaseResponse<Long> addRecord(@RequestBody AssessmentRecordAddRequest recordAddRequest,
            HttpServletRequest request) {
        if (recordAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentRecordService.createRecord(loginUser.getId(), recordAddRequest));
    }

    @PostMapping("/record/my/list/page")
    public BaseResponse<Page<AssessmentRecordVO>> listMyRecordByPage(
            @RequestBody AssessmentRecordQueryRequest recordQueryRequest,
            HttpServletRequest request) {
        if (recordQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentRecordService.listUserRecordByPage(loginUser.getId(), recordQueryRequest));
    }

    @GetMapping("/record/my/get")
    public BaseResponse<AssessmentRecordDetailVO> getMyRecord(@RequestParam Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentRecordService.getUserRecordDetail(loginUser.getId(), id));
    }

    @GetMapping("/record/my/trends")
    public BaseResponse<List<AssessmentTrendVO>> getMyTrends(
            @RequestParam(defaultValue = AssessmentConstant.DEFAULT_SCHEME_CODE) String schemeCode,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentRecordService.getUserTrends(loginUser.getId(), schemeCode, limit));
    }

    @PostMapping("/report/generate")
    public BaseResponse<AssessmentReportVO> generateMyReport(
            @RequestBody AssessmentReportGenerateRequest generateRequest,
            HttpServletRequest request) {
        if (generateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(assessmentReportService.generateUserReport(loginUser.getId(), generateRequest));
    }

    @GetMapping("/report/my/get")
    public BaseResponse<AssessmentReportVO> getMyReport(@RequestParam Long recordId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        AssessmentReportVO reportVO = assessmentReportService.getUserReport(loginUser.getId(), recordId);
        ThrowUtils.throwIf(reportVO == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(reportVO);
    }
}
