package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.mapper.DisasterMapper;
import com.disaster.emergency.mapper.DemandMapper;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private DisasterMapper disasterMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DemandMapper demandMapper;

    @Override
    public Map<String, Object> getDisasterStatistics(String startTime, String endTime, String province, String city) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有灾情
        List<Disaster> disasters = disasterMapper.selectList(null);
        
        int totalDisasters = disasters.size();
        int activeDisasters = 0;
        int resolvedDisasters = 0;
        int closedDisasters = 0;
        
        Map<String, Integer> byType = new HashMap<>();
        Map<String, Integer> bySeverity = new HashMap<>();
        
        for (Disaster disaster : disasters) {
            // 统计状态
            switch (disaster.getStatus()) {
                case "active":
                    activeDisasters++;
                    break;
                case "resolved":
                    resolvedDisasters++;
                    break;
                case "closed":
                    closedDisasters++;
                    break;
            }
            
            // 统计类型
            byType.put(disaster.getDisasterType(), byType.getOrDefault(disaster.getDisasterType(), 0) + 1);
            
            // 统计严重程度
            bySeverity.put(disaster.getSeverity(), bySeverity.getOrDefault(disaster.getSeverity(), 0) + 1);
        }
        
        result.put("totalDisasters", totalDisasters);
        result.put("activeDisasters", activeDisasters);
        result.put("resolvedDisasters", resolvedDisasters);
        result.put("closedDisasters", closedDisasters);
        
        // 转换为列表格式
        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("disasterType", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", totalDisasters > 0 ? (entry.getValue() * 100.0 / totalDisasters) : 0);
            byTypeList.add(item);
        }
        result.put("byType", byTypeList);
        
        List<Map<String, Object>> bySeverityList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : bySeverity.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("severity", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", totalDisasters > 0 ? (entry.getValue() * 100.0 / totalDisasters) : 0);
            bySeverityList.add(item);
        }
        result.put("bySeverity", bySeverityList);
        
        return result;
    }

    @Override
    public Map<String, Object> getResourceStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        List<Resource> resources = resourceMapper.selectList(null);
        
        int totalResources = resources.size();
        int availableResources = 0;
        int allocatedResources = 0;
        int depletedResources = 0;
        
        Map<String, Map<String, Integer>> byType = new HashMap<>();
        
        for (Resource resource : resources) {
            // 统计状态
            switch (resource.getStatus()) {
                case "available":
                    availableResources++;
                    break;
                case "allocated":
                    allocatedResources++;
                    break;
                case "depleted":
                    depletedResources++;
                    break;
            }
            
            // 统计类型
            String type = resource.getResourceType();
            Map<String, Integer> typeStats = byType.computeIfAbsent(type, k -> new HashMap<>());
            typeStats.put("totalQuantity", typeStats.getOrDefault("totalQuantity", 0) + resource.getTotalQuantity());
            typeStats.put("availableQuantity", typeStats.getOrDefault("availableQuantity", 0) + resource.getAvailableQuantity());
            typeStats.put("allocatedQuantity", typeStats.getOrDefault("allocatedQuantity", 0) + (resource.getTotalQuantity() - resource.getAvailableQuantity()));
            typeStats.put("depletedQuantity", typeStats.getOrDefault("depletedQuantity", 0) + (resource.getTotalQuantity() - resource.getAvailableQuantity()));
        }
        
        result.put("totalResources", totalResources);
        result.put("availableResources", availableResources);
        result.put("allocatedResources", allocatedResources);
        result.put("depletedResources", depletedResources);
        
        // 转换为列表格式
        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("resourceType", entry.getKey());
            item.putAll(entry.getValue());
            byTypeList.add(item);
        }
        result.put("byType", byTypeList);
        
        return result;
    }

    @Override
    public Map<String, Object> getDemandStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        List<Demand> demands = demandMapper.selectList(null);
        
        int totalDemands = demands.size();
        int pendingDemands = 0;
        int matchedDemands = 0;
        int allocatedDemands = 0;
        int completedDemands = 0;
        
        Map<String, Integer> byType = new HashMap<>();
        Map<String, Integer> byUrgency = new HashMap<>();
        
        for (Demand demand : demands) {
            // 统计状态
            switch (demand.getStatus()) {
                case "pending":
                    pendingDemands++;
                    break;
                case "matched":
                    matchedDemands++;
                    break;
                case "allocated":
                    allocatedDemands++;
                    break;
                case "completed":
                    completedDemands++;
                    break;
            }
            
            // 统计类型
            byType.put(demand.getDemandType(), byType.getOrDefault(demand.getDemandType(), 0) + 1);
            
            // 统计紧急程度
            byUrgency.put(demand.getUrgency(), byUrgency.getOrDefault(demand.getUrgency(), 0) + 1);
        }
        
        result.put("totalDemands", totalDemands);
        result.put("pendingDemands", pendingDemands);
        result.put("matchedDemands", matchedDemands);
        result.put("allocatedDemands", allocatedDemands);
        result.put("completedDemands", completedDemands);
        
        // 转换为列表格式
        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("demandType", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", totalDemands > 0 ? (entry.getValue() * 100.0 / totalDemands) : 0);
            byTypeList.add(item);
        }
        result.put("byType", byTypeList);
        
        List<Map<String, Object>> byUrgencyList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : byUrgency.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("urgency", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", totalDemands > 0 ? (entry.getValue() * 100.0 / totalDemands) : 0);
            byUrgencyList.add(item);
        }
        result.put("byUrgency", byUrgencyList);
        
        return result;
    }
}
