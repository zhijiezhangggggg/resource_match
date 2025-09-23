package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.mapper.DisasterMapper;
import com.disaster.emergency.service.DisasterService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DisasterServiceImpl extends ServiceImpl<DisasterMapper, Disaster> implements DisasterService {

    @Override
    public Disaster reportDisaster(Disaster disaster) {
        disaster.setStatus("active");
        disaster.setCreateTime(LocalDateTime.now());
        disaster.setUpdateTime(LocalDateTime.now());
        save(disaster);
        return disaster;
    }

    @Override
    public IPage<Disaster> getDisasterList(int page, int size, String disasterType, String severity, 
                                          String province, String city, String status) {
        // 创建分页对象
        Page<Disaster> pageParam = new Page<>(page, size);
        
        // 构建查询条件
        QueryWrapper<Disaster> queryWrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(disasterType)) {
            queryWrapper.eq("disaster_type", disasterType);
        }
        if (StringUtils.hasText(severity)) {
            queryWrapper.eq("severity", severity);
        }
        if (StringUtils.hasText(province)) {
            queryWrapper.eq("province", province);
        }
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");
        
        return page(pageParam, queryWrapper);
    }

    @Override
    public Map<String, Object> getDisasterStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总灾情数
        long totalDisasters = count();
        statistics.put("totalDisasters", totalDisasters);
        
        // 活跃灾情数
        long activeDisasters = count(new QueryWrapper<Disaster>().eq("status", "active"));
        statistics.put("activeDisasters", activeDisasters);
        
        // 已解决灾情数
        long resolvedDisasters = count(new QueryWrapper<Disaster>().eq("status", "resolved"));
        statistics.put("resolvedDisasters", resolvedDisasters);
        
        // 已关闭灾情数
        long closedDisasters = count(new QueryWrapper<Disaster>().eq("status", "closed"));
        statistics.put("closedDisasters", closedDisasters);
        
        // 按灾害类型统计
        Map<String, Long> disasterTypeStats = new HashMap<>();
        List<Map<String, Object>> typeStats = baseMapper.getDisasterTypeStatistics();
        for (Map<String, Object> stat : typeStats) {
            disasterTypeStats.put((String) stat.get("disasterType"), (Long) stat.get("count"));
        }
        statistics.put("disasterTypeStats", disasterTypeStats);
        
        // 按严重程度统计
        Map<String, Long> severityStats = new HashMap<>();
        List<Map<String, Object>> severityStatList = baseMapper.getSeverityStatistics();
        for (Map<String, Object> stat : severityStatList) {
            severityStats.put((String) stat.get("severity"), (Long) stat.get("count"));
        }
        statistics.put("severityStats", severityStats);
        
        return statistics;
    }
}
