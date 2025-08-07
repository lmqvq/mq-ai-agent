package com.mq.mqaiagent.controller;

import com.mq.mqaiagent.common.BaseResponse;
import com.mq.mqaiagent.common.ErrorCode;
import com.mq.mqaiagent.common.ResultUtils;
import com.mq.mqaiagent.exception.BusinessException;
import com.mq.mqaiagent.model.dto.ChatHistoryDetailDTO;
import com.mq.mqaiagent.model.dto.ChatHistoryListDTO;
import com.mq.mqaiagent.model.entity.User;
import com.mq.mqaiagent.service.ChatHistoryService;
import com.mq.mqaiagent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天历史接口
 * 
 * @author MQQQ
 * @create 2025-08-07
 */
@RestController
@RequestMapping("/chat/history")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 获取用户的历史对话列表
     *
     * @param request HTTP请求对象
     * @return 历史对话列表
     */
    @GetMapping("/list")
    public BaseResponse<List<ChatHistoryListDTO>> getChatHistoryList(HttpServletRequest request) {
        try {
            // 获取当前登录用户
            User currentUser = userService.getLoginUser(request);
            
            // 获取历史对话列表
            List<ChatHistoryListDTO> historyList = chatHistoryService.getChatHistoryList(currentUser.getId());
            
            return ResultUtils.success(historyList);
            
        } catch (BusinessException e) {
            log.warn("获取历史对话列表失败: {}", e.getMessage());
            return ResultUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取历史对话列表异常: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
        }
    }

    /**
     * 获取单个对话的完整消息
     *
     * @param chatId  对话ID
     * @param request HTTP请求对象
     * @return 对话详情
     */
    @GetMapping("/detail")
    public BaseResponse<ChatHistoryDetailDTO> getChatHistoryDetail(@RequestParam String chatId, 
                                                                   HttpServletRequest request) {
        try {
            // 参数校验
            if (chatId == null || chatId.trim().isEmpty()) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "对话ID不能为空");
            }
            
            // 获取当前登录用户
            User currentUser = userService.getLoginUser(request);
            
            // 获取对话详情
            ChatHistoryDetailDTO historyDetail = chatHistoryService.getChatHistoryDetail(currentUser.getId(), chatId);
            
            if (historyDetail == null) {
                return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "对话记录不存在");
            }
            
            return ResultUtils.success(historyDetail);
            
        } catch (BusinessException e) {
            log.warn("获取对话详情失败: {}", e.getMessage());
            return ResultUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取对话详情异常: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
        }
    }

    /**
     * 删除用户的某个对话
     *
     * @param chatId  对话ID
     * @param request HTTP请求对象
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteChatHistory(@RequestParam String chatId, 
                                                   HttpServletRequest request) {
        try {
            // 参数校验
            if (chatId == null || chatId.trim().isEmpty()) {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "对话ID不能为空");
            }
            
            // 获取当前登录用户
            User currentUser = userService.getLoginUser(request);
            
            // 删除对话
            boolean success = chatHistoryService.deleteChatHistory(currentUser.getId(), chatId);
            
            if (success) {
                return ResultUtils.success(true);
            } else {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR, "删除失败，对话记录不存在");
            }
            
        } catch (BusinessException e) {
            log.warn("删除对话失败: {}", e.getMessage());
            return ResultUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("删除对话异常: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
        }
    }
}
