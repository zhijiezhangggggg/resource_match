package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.mapper.DemandMapper;
import com.disaster.emergency.service.DemandService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    @Override
    public Demand submitDemand(Demand demand) {
        demand.setStatus("pending");
        demand.setCreateTime(LocalDateTime.now());
        demand.setUpdateTime(LocalDateTime.now());
        save(demand);
        return demand;
    }

    @Override
    public Page<Demand> getDemandList(Integer page, Integer size, Long disasterId, String demandType, 
                                      String urgency, String status, String province, String city) {
        Page<Demand> pageParam = new Page<>(page, size);
        QueryWrapper<Demand> queryWrapper = new QueryWrapper<>();
        
        if (disasterId != null) {
            queryWrapper.eq("disaster_id", disasterId);
        }
        if (StringUtils.hasText(demandType)) {
            queryWrapper.eq("demand_type", demandType);
        }
        if (StringUtils.hasText(urgency)) {
            queryWrapper.eq("urgency", urgency);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(province)) {
            queryWrapper.eq("province", province);
        }
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        
        queryWrapper.orderByDesc("create_time");
        return page(pageParam, queryWrapper);
    }

    @Override
    public boolean updateDemandStatus(Long id, String status) {
        Demand demand = new Demand();
        demand.setId(id);
        demand.setStatus(status);
        demand.setUpdateTime(LocalDateTime.now());
        return updateById(demand);
    }

    @Override
    public Map<String, Object> getDemandStatistics(String startTime, String endTime, String province, String city) {
        QueryWrapper<Demand> queryWrapper = new QueryWrapper<>();
        
        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge("create_time", start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le("create_time", end);
        }
        
        // 地区筛选
        if (StringUtils.hasText(province)) {
            queryWrapper.eq("province", province);
        }
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        
        // 统计总数
        long totalCount = count(queryWrapper);
        
        // 按状态统计
        Map<String, Object> statusStats = new HashMap<>();
        for (String status : new String[]{"pending", "processing", "completed", "cancelled"}) {
            QueryWrapper<Demand> statusWrapper = new QueryWrapper<>();
            statusWrapper.eq("status", status);
            // 复制原查询条件
            if (StringUtils.hasText(startTime)) {
                LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                statusWrapper.ge("create_time", start);
            }
            if (StringUtils.hasText(endTime)) {
                LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                statusWrapper.le("create_time", end);
            }
            if (StringUtils.hasText(province)) {
                statusWrapper.eq("province", province);
            }
            if (StringUtils.hasText(city)) {
                statusWrapper.eq("city", city);
            }
            long count = count(statusWrapper);
            statusStats.put(status, count);
        }
        
        // 按紧急程度统计
        Map<String, Object> urgencyStats = new HashMap<>();
        for (String urgency : new String[]{"low", "medium", "high", "urgent"}) {
            QueryWrapper<Demand> urgencyWrapper = new QueryWrapper<>();
            urgencyWrapper.eq("urgency", urgency);
            // 复制原查询条件
            if (StringUtils.hasText(startTime)) {
                LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                urgencyWrapper.ge("create_time", start);
            }
            if (StringUtils.hasText(endTime)) {
                LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                urgencyWrapper.le("create_time", end);
            }
            if (StringUtils.hasText(province)) {
                urgencyWrapper.eq("province", province);
            }
            if (StringUtils.hasText(city)) {
                urgencyWrapper.eq("city", city);
            }
            long count = count(urgencyWrapper);
            urgencyStats.put(urgency, count);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("statusStats", statusStats);
        result.put("urgencyStats", urgencyStats);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getDemandStatisticsByType(String startTime, String endTime) {
        QueryWrapper<Demand> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge("create_time", start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le("create_time", end);
        }
        
        queryWrapper.select("demand_type", "count(*) as count")
                   .groupBy("demand_type")
                   .orderByDesc("count");
        
        return listMaps(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getDemandStatisticsByUrgency(String startTime, String endTime) {
        QueryWrapper<Demand> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge("create_time", start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le("create_time", end);
        }
        
        queryWrapper.select("urgency", "count(*) as count")
                   .groupBy("urgency")
                   .orderByDesc("count");
        
        return listMaps(queryWrapper);
    }
}
