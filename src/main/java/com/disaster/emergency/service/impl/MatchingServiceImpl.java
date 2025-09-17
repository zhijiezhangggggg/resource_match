package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.MatchingRecord;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.mapper.MatchingRecordMapper;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingServiceImpl extends ServiceImpl<MatchingRecordMapper, MatchingRecord> implements MatchingService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public List<MatchingRecord> matchResources(Long demandId, String demandType, Integer quantity, 
                                             String province, String city, String urgency) {
        // 查询匹配的资源
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_type", demandType)
                   .ge("available_quantity", quantity)
                   .eq("status", "available");
        
        List<Resource> resources = resourceMapper.selectList(queryWrapper);
        List<MatchingRecord> matchingRecords = new ArrayList<>();
        
        for (Resource resource : resources) {
            MatchingRecord record = new MatchingRecord();
            record.setDemandId(demandId);
            record.setResourceId(resource.getId());
            
            // 计算匹配度评分
            BigDecimal score = calculateMatchScore(resource, demandType, quantity, province, city, urgency);
            record.setMatchScore(score);
            
            // 设置匹配原因
            String reason = generateMatchReason(resource, province, city);
            record.setMatchReason(reason);
            
            record.setStatus("pending");
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            
            save(record);
            matchingRecords.add(record);
        }
        
        return matchingRecords;
    }

    @Override
    public boolean confirmMatching(Long matchingId, String status) {
        UpdateWrapper<MatchingRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", matchingId)
                    .set("status", status)
                    .set("update_time", LocalDateTime.now());
        return update(updateWrapper);
    }

    private BigDecimal calculateMatchScore(Resource resource, String demandType, Integer quantity, 
                                         String province, String city, String urgency) {
        BigDecimal score = BigDecimal.ZERO;
        
        // 类型匹配 (40分)
        if (resource.getResourceType().equals(demandType)) {
            score = score.add(new BigDecimal("40"));
        }
        
        // 数量匹配 (30分)
        if (resource.getAvailableQuantity() >= quantity) {
            score = score.add(new BigDecimal("30"));
        }
        
        // 距离匹配 (20分)
        if (resource.getProvince().equals(province) && resource.getCity().equals(city)) {
            score = score.add(new BigDecimal("20"));
        } else if (resource.getProvince().equals(province)) {
            score = score.add(new BigDecimal("10"));
        }
        
        // 紧急程度匹配 (10分)
        if ("紧急".equals(urgency)) {
            score = score.add(new BigDecimal("10"));
        } else if ("高".equals(urgency)) {
            score = score.add(new BigDecimal("8"));
        } else if ("中".equals(urgency)) {
            score = score.add(new BigDecimal("5"));
        }
        
        return score;
    }

    private String generateMatchReason(Resource resource, String province, String city) {
        StringBuilder reason = new StringBuilder();
        
        reason.append("类型匹配度:高");
        
        if (resource.getProvince().equals(province) && resource.getCity().equals(city)) {
            reason.append(", 距离匹配度:高");
        } else if (resource.getProvince().equals(province)) {
            reason.append(", 距离匹配度:中");
        } else {
            reason.append(", 距离匹配度:低");
        }
        
        return reason.toString();
    }
}
