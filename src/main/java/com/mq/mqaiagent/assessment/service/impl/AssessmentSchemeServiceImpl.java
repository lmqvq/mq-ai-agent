package com.mq.mqaiagent.assessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mq.mqaiagent.assessment.mapper.AssessmentSchemeItemMapper;
import com.mq.mqaiagent.assessment.mapper.AssessmentSchemeMapper;
import com.mq.mqaiagent.assessment.model.dto.scheme.AssessmentSchemeQueryRequest;
import com.mq.mqaiagent.assessment.model.entity.AssessmentScheme;
import com.mq.mqaiagent.assessment.model.entity.AssessmentSchemeItem;
import com.mq.mqaiagent.assessment.model.vo.AssessmentSchemeItemVO;
import com.mq.mqaiagent.assessment.model.vo.AssessmentSchemeVO;
import com.mq.mqaiagent.assessment.service.AssessmentSchemeService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Assessment scheme service implementation.
 */
@Service
public class AssessmentSchemeServiceImpl extends ServiceImpl<AssessmentSchemeMapper, AssessmentScheme>
        implements AssessmentSchemeService {

    @Resource
    private AssessmentSchemeItemMapper assessmentSchemeItemMapper;

    @Override
    public List<AssessmentSchemeVO> listSchemeVO(AssessmentSchemeQueryRequest schemeQueryRequest) {
        QueryWrapper<AssessmentScheme> queryWrapper = new QueryWrapper<>();
        if (schemeQueryRequest != null) {
            queryWrapper.eq(StringUtils.isNotBlank(schemeQueryRequest.getSchemeCode()), "schemeCode",
                    schemeQueryRequest.getSchemeCode());
            queryWrapper.eq(StringUtils.isNotBlank(schemeQueryRequest.getSceneType()), "sceneType",
                    schemeQueryRequest.getSceneType());
            queryWrapper.eq("status", ObjectUtils.isNotEmpty(schemeQueryRequest.getStatus())
                    ? schemeQueryRequest.getStatus() : 1);
        } else {
            queryWrapper.eq("status", 1);
        }
        queryWrapper.orderByAsc("createTime");
        return this.list(queryWrapper).stream().map(this::getSchemeVO).collect(Collectors.toList());
    }

    @Override
    public AssessmentSchemeVO getSchemeDetail(String schemeCode) {
        if (StringUtils.isBlank(schemeCode)) {
            return null;
        }
        QueryWrapper<AssessmentScheme> schemeQueryWrapper = new QueryWrapper<>();
        schemeQueryWrapper.eq("schemeCode", schemeCode).last("limit 1");
        AssessmentScheme assessmentScheme = this.getOne(schemeQueryWrapper);
        if (assessmentScheme == null) {
            return null;
        }
        AssessmentSchemeVO schemeVO = getSchemeVO(assessmentScheme);
        QueryWrapper<AssessmentSchemeItem> itemQueryWrapper = new QueryWrapper<>();
        itemQueryWrapper.eq("schemeCode", schemeCode).orderByAsc("displayOrder");
        List<AssessmentSchemeItemVO> itemVOList = assessmentSchemeItemMapper.selectList(itemQueryWrapper).stream()
                .map(this::getSchemeItemVO)
                .collect(Collectors.toList());
        schemeVO.setItemList(itemVOList);
        return schemeVO;
    }

    private AssessmentSchemeVO getSchemeVO(AssessmentScheme assessmentScheme) {
        if (assessmentScheme == null) {
            return null;
        }
        AssessmentSchemeVO schemeVO = new AssessmentSchemeVO();
        BeanUtils.copyProperties(assessmentScheme, schemeVO);
        schemeVO.setItemList(Collections.emptyList());
        return schemeVO;
    }

    private AssessmentSchemeItemVO getSchemeItemVO(AssessmentSchemeItem assessmentSchemeItem) {
        if (assessmentSchemeItem == null) {
            return null;
        }
        AssessmentSchemeItemVO itemVO = new AssessmentSchemeItemVO();
        BeanUtils.copyProperties(assessmentSchemeItem, itemVO);
        return itemVO;
    }
}
