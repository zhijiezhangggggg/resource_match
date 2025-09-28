package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SchedulingRecord;
import com.disaster.emergency.mapper.SchedulingRecordMapper;
import com.disaster.emergency.service.SchedulingService;
import com.disaster.emergency.service.SimilarityCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 资源调度服务实现类
 */
@Service
public class SchedulingServiceImpl implements SchedulingService {
    
    @Autowired
    private SchedulingRecordMapper schedulingRecordMapper;
    
    @Autowired
    private SimilarityCalculationService similarityCalculationService;
    
    @Override
    public Map<String, Object> optimizeResourceAllocation(List<Demand> demands, List<Resource> resources, String algorithm) {
        if (demands == null || demands.isEmpty() || resources == null || resources.isEmpty()) {
            return createEmptyResult();
        }
        
        switch (algorithm.toLowerCase()) {
            case "greedy":
                return greedyScheduling(demands, resources);
            case "genetic":
                return geneticScheduling(demands, resources, 50, 100);
            case "simulated_annealing":
                return simulatedAnnealingScheduling(demands, resources, 1000.0, 0.95);
            default:
                return greedyScheduling(demands, resources);
        }
    }
    
    @Override
    public Map<String, Object> greedyScheduling(List<Demand> demands, List<Resource> resources) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> allocations = new ArrayList<>();
        Set<Long> usedResources = new HashSet<>();
        Set<Long> satisfiedDemands = new HashSet<>();
        
        // 为每个需求找到最佳匹配的资源
        for (Demand demand : demands) {
            if (satisfiedDemands.contains(demand.getId())) {
                continue;
            }
            
            Map<String, Object> bestMatch = findBestResourceForDemand(demand, resources, usedResources);
            if (bestMatch != null) {
                allocations.add(bestMatch);
                usedResources.add((Long) bestMatch.get("resourceId"));
                satisfiedDemands.add(demand.getId());
            }
        }
        
        result.put("allocations", allocations);
        result.put("totalAllocations", allocations.size());
        result.put("satisfiedDemands", satisfiedDemands.size());
        result.put("totalDemands", demands.size());
        result.put("usedResources", usedResources.size());
        result.put("totalResources", resources.size());
        result.put("algorithm", "greedy");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return result;
    }
    
    @Override
    public Map<String, Object> geneticScheduling(List<Demand> demands, List<Resource> resources, 
                                                Integer populationSize, Integer generations) {
        // 简化实现，实际应该使用遗传算法
        return greedyScheduling(demands, resources);
    }
    
    @Override
    public Map<String, Object> simulatedAnnealingScheduling(List<Demand> demands, List<Resource> resources,
                                                           Double initialTemperature, Double coolingRate) {
        // 简化实现，实际应该使用模拟退火算法
        return greedyScheduling(demands, resources);
    }
    
    @Override
    public boolean saveSchedulingRecord(SchedulingRecord schedulingRecord) {
        return schedulingRecordMapper.insert(schedulingRecord) > 0;
    }
    
    @Override
    public SchedulingRecord getSchedulingRecord(Long recordId) {
        return schedulingRecordMapper.selectById(recordId);
    }
    
    @Override
    public List<SchedulingRecord> getSchedulingHistory(String startTime, String endTime, Integer limit) {
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> calculateSchedulingMetrics(Map<String, Object> allocationResult) {
        Map<String, Object> metrics = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allocations = (List<Map<String, Object>>) allocationResult.get("allocations");
        
        if (allocations == null || allocations.isEmpty()) {
            metrics.put("satisfactionRate", 0.0);
            metrics.put("resourceUtilizationRate", 0.0);
            metrics.put("averageSimilarity", 0.0);
            metrics.put("efficiency", 0.0);
            return metrics;
        }
        
        // 计算满意度
        int totalDemands = (Integer) allocationResult.get("totalDemands");
        int satisfiedDemands = (Integer) allocationResult.get("satisfiedDemands");
        double satisfactionRate = totalDemands > 0 ? (double) satisfiedDemands / totalDemands : 0.0;
        
        // 计算资源利用率
        int totalResources = (Integer) allocationResult.get("totalResources");
        int usedResources = (Integer) allocationResult.get("usedResources");
        double resourceUtilizationRate = totalResources > 0 ? (double) usedResources / totalResources : 0.0;
        
        // 计算平均相似度
        double averageSimilarity = allocations.stream()
            .mapToDouble(allocation -> (Double) allocation.get("similarity"))
            .average()
            .orElse(0.0);
        
        // 计算效率（综合指标）
        double efficiency = (satisfactionRate * 0.4 + resourceUtilizationRate * 0.3 + averageSimilarity / 100.0 * 0.3) * 100.0;
        
        metrics.put("satisfactionRate", Math.round(satisfactionRate * 10000.0) / 100.0);
        metrics.put("resourceUtilizationRate", Math.round(resourceUtilizationRate * 10000.0) / 100.0);
        metrics.put("averageSimilarity", Math.round(averageSimilarity * 100.0) / 100.0);
        metrics.put("efficiency", Math.round(efficiency * 100.0) / 100.0);
        
        return metrics;
    }
    
    @Override
    public boolean updateResourceStatus(Long resourceId, String newStatus) {
        return true;
    }
    
    @Override
    public boolean updateDemandStatus(Long demandId, String newStatus) {
        return true;
    }
    
    @Override
    public Long createScheduling(Long demandId, Long resourceId, Integer allocatedQuantity, 
                                Long schedulerId, String schedulerName, String remark) {
        SchedulingRecord record = new SchedulingRecord();
        record.setDemandId(demandId);
        record.setResourceId(resourceId);
        record.setAllocatedQuantity(allocatedQuantity);
        record.setSchedulerId(schedulerId);
        record.setSchedulerName(schedulerName);
        record.setRemark(remark);
        record.setSchedulingTime(LocalDateTime.now());
        record.setStatus("pending");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        if (saveSchedulingRecord(record)) {
            return record.getId();
        }
        return null;
    }
    
    @Override
    public List<SchedulingRecord> list() {
        return schedulingRecordMapper.selectList(null);
    }
    
    @Override
    public boolean updateStatus(Long recordId, String status) {
        SchedulingRecord record = schedulingRecordMapper.selectById(recordId);
        if (record == null) {
            return false;
        }
        
        record.setStatus(status);
        record.setUpdateTime(LocalDateTime.now());
        return schedulingRecordMapper.updateById(record) > 0;
    }
    
    @Override
    public boolean completeScheduling(Long recordId, String remark) {
        SchedulingRecord record = schedulingRecordMapper.selectById(recordId);
        if (record == null) {
            return false;
        }
        
        record.setStatus("completed");
        record.setActualDeliveryTime(LocalDateTime.now());
        if (remark != null && !remark.trim().isEmpty()) {
            record.setRemark(record.getRemark() + " | 完成备注: " + remark);
        }
        record.setUpdateTime(LocalDateTime.now());
        return schedulingRecordMapper.updateById(record) > 0;
    }
    
    private Map<String, Object> findBestResourceForDemand(Demand demand, List<Resource> resources, Set<Long> usedResources) {
        Resource bestResource = null;
        double bestSimilarity = 0.0;
        
        for (Resource resource : resources) {
            if (usedResources.contains(resource.getId())) {
                continue;
            }
            
            double similarity = similarityCalculationService.calculateOverallSimilarity(resource, demand);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestResource = resource;
            }
        }
        
        if (bestResource != null) {
            Map<String, Object> allocation = new HashMap<>();
            allocation.put("demandId", demand.getId());
            allocation.put("resourceId", bestResource.getId());
            allocation.put("similarity", bestSimilarity);
            allocation.put("allocationTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return allocation;
        }
        
        return null;
    }
    
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("allocations", new ArrayList<>());
        result.put("totalAllocations", 0);
        result.put("satisfiedDemands", 0);
        result.put("totalDemands", 0);
        result.put("usedResources", 0);
        result.put("totalResources", 0);
        result.put("algorithm", "none");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }
}