package com.mq.mqaiagent.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mq.mqaiagent.mapper.KeepReportMapper;
import com.mq.mqaiagent.model.dto.keepReport.KeepReport;
import com.mq.mqaiagent.model.dto.ChatHistoryDetailDTO;
import com.mq.mqaiagent.model.dto.ChatHistoryListDTO;
import com.mq.mqaiagent.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天历史服务实现类
 * 
 * @author MQQQ
 * @create 2025-08-07
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Resource
    private KeepReportMapper keepReportMapper;

    @Override
    public List<ChatHistoryListDTO> getChatHistoryList(Long userId) {
        try {
            // 构建查询条件：根据用户ID查询，按更新时间倒序排列
            LambdaQueryWrapper<KeepReport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeepReport::getUserId, userId)
                        .eq(KeepReport::getIsDelete, 0)
                        .orderByDesc(KeepReport::getUpdateTime);

            // 查询数据库
            List<KeepReport> keepReports = keepReportMapper.selectList(queryWrapper);

            // 转换为DTO
            return keepReports.stream()
                    .map(report -> ChatHistoryListDTO.builder()
                            .chatId(report.getChatId())
                            .lastMessage(report.getLastMessage())
                            .createTime(report.getCreateTime())
                            .updateTime(report.getUpdateTime())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户历史对话列表失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ChatHistoryDetailDTO getChatHistoryDetail(Long userId, String chatId) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<KeepReport> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KeepReport::getUserId, userId)
                        .eq(KeepReport::getChatId, chatId)
                        .eq(KeepReport::getIsDelete, 0);

            // 查询数据库
            KeepReport keepReport = keepReportMapper.selectOne(queryWrapper);

            if (keepReport == null || keepReport.getMessages() == null || keepReport.getMessages().isBlank()) {
                log.warn("未找到对话记录或消息为空，用户ID: {}, 对话ID: {}", userId, chatId);
                return null;
            }

            // 解析消息JSON
            List<ChatHistoryDetailDTO.ChatMessageDTO> messages = parseMessages(keepReport.getMessages());

            return ChatHistoryDetailDTO.builder()
                    .chatId(chatId)
                    .messages(messages)
                    .build();

        } catch (Exception e) {
            log.error("获取对话详情失败，用户ID: {}, 对话ID: {}, 错误: {}", userId, chatId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteChatHistory(Long userId, String chatId) {
        try {
            // 构建更新条件：逻辑删除
            LambdaUpdateWrapper<KeepReport> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KeepReport::getUserId, userId)
                        .eq(KeepReport::getChatId, chatId)
                        .set(KeepReport::getIsDelete, 1);

            // 执行逻辑删除
            int updatedRows = keepReportMapper.update(null, updateWrapper);
            
            if (updatedRows > 0) {
                log.info("成功删除对话记录，用户ID: {}, 对话ID: {}", userId, chatId);
                return true;
            } else {
                log.warn("删除对话记录失败，记录不存在，用户ID: {}, 对话ID: {}", userId, chatId);
                return false;
            }

        } catch (Exception e) {
            log.error("删除对话记录失败，用户ID: {}, 对话ID: {}, 错误: {}", userId, chatId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 解析消息JSON为DTO列表
     *
     * @param messagesJson 消息JSON字符串
     * @return 消息DTO列表
     */
    private List<ChatHistoryDetailDTO.ChatMessageDTO> parseMessages(String messagesJson) {
        List<ChatHistoryDetailDTO.ChatMessageDTO> messages = new ArrayList<>();
        
        try {
            JSONArray jsonArray = JSON.parseArray(messagesJson);
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String messageType = jsonObject.getString("messageType");
                
                // 处理消息内容
                Object messageObj = jsonObject.get("message");
                String messageContent;
                
                if (messageObj instanceof JSONObject) {
                    // 如果是JSON对象，转换为字符串
                    messageContent = messageObj.toString();
                } else {
                    // 否则直接使用字符串值
                    messageContent = jsonObject.getString("message");
                }
                
                messages.add(ChatHistoryDetailDTO.ChatMessageDTO.builder()
                        .messageType(messageType)
                        .message(messageContent)
                        .build());
            }
            
        } catch (Exception e) {
            log.error("解析消息JSON失败: {}", e.getMessage(), e);
        }
        
        return messages;
    }
}
