package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.*;
import com.disaster.emergency.mapper.*;
import com.disaster.emergency.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimilarityServiceImpl implements SimilarityService {
    
    @Autowired
    private SimilarityConfigMapper similarityConfigMapper;
    
    @Autowired
    private DemandMapper demandMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Override
    public SimilarityResult calculateSimilarity(Demand demand, Resource resource) {
        // 获取相似度配置
        List<SimilarityConfig> configs = getSimilarityConfigs();
        Map<String, SimilarityConfig> configMap = configs.stream()
            .collect(Collectors.toMap(SimilarityConfig::getDimension, config -> config));
        
        // 计算各维度相似度
        BigDecimal typeScore = calculateTypeSimilarity(demand.getDemandType(), resource.getResourceType());
        BigDecimal quantityScore = calculateQuantitySimilarity(demand.getQuantity(), resource.getAvailableQuantity());
        BigDecimal distanceScore = calculateDistanceSimilarity(
            demand.getProvince() + demand.getCity() + demand.getDistrict(),
            resource.getProvince() + resource.getCity() + resource.getDistrict()
        );
        BigDecimal timeScore = calculateTimeSimilarity(demand.getUrgency(), resource.getPriorityLevel() != null ? resource.getPriorityLevel() : 1);
        
        // 计算加权总分
        BigDecimal totalScore = BigDecimal.ZERO;
        if (configMap.containsKey("type")) {
            totalScore = totalScore.add(typeScore.multiply(configMap.get("type").getWeight()));
        }
        if (configMap.containsKey("quantity")) {
            totalScore = totalScore.add(quantityScore.multiply(configMap.get("quantity").getWeight()));
        }
        if (configMap.containsKey("distance")) {
            totalScore = totalScore.add(distanceScore.multiply(configMap.get("distance").getWeight()));
        }
        if (configMap.containsKey("time")) {
            totalScore = totalScore.add(timeScore.multiply(configMap.get("time").getWeight()));
        }
        
        // 构建维度评分JSON
        Map<String, Object> dimensionScores = new HashMap<>();
        dimensionScores.put("type", typeScore);
        dimensionScores.put("quantity", quantityScore);
        dimensionScores.put("distance", distanceScore);
        dimensionScores.put("time", timeScore);
        
        // 构建匹配原因
        StringBuilder matchReason = new StringBuilder();
        matchReason.append("类型匹配度: ").append(typeScore).append("; ");
        matchReason.append("数量匹配度: ").append(quantityScore).append("; ");
        matchReason.append("距离匹配度: ").append(distanceScore).append("; ");
        matchReason.append("时效匹配度: ").append(timeScore);
        
        // 创建相似度结果
        SimilarityResult result = new SimilarityResult();
        result.setDemandId(demand.getId());
        result.setResourceId(resource.getId());
        result.setTotalScore(totalScore.multiply(new BigDecimal("100"))); // 转换为0-100分
        result.setDimensionScores(mapToJson(dimensionScores));
        result.setMatchReason(matchReason.toString());
        result.setCalculationTime(LocalDateTime.now());
        result.setCreateTime(LocalDateTime.now());
        
        return result;
    }
    
    @Override
    public List<SimilarityResult> calculateSimilarityForDemand(Long demandId) {
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            return Collections.emptyList();
        }
        
        // 获取所有可用资源
        List<Resource> resources = resourceMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Resource>()
                .eq("status", "available")
                .gt("available_quantity", 0)
        );
        
        List<SimilarityResult> results = new ArrayList<>();
        for (Resource resource : resources) {
            SimilarityResult result = calculateSimilarity(demand, resource);
            results.add(result);
        }
        
        // 按总分排序
        results.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        
        return results;
    }
    
    @Override
    public List<SimilarityConfig> getSimilarityConfigs() {
        return similarityConfigMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SimilarityConfig>()
                .eq("is_active", 1)
        );
    }
    
    @Override
    public boolean updateSimilarityConfigs(List<SimilarityConfig> configs) {
        for (SimilarityConfig config : configs) {
            similarityConfigMapper.updateById(config);
        }
        return true;
    }
    
    @Override
    public BigDecimal calculateTypeSimilarity(String demandType, String resourceType) {
        if (demandType == null || resourceType == null) {
            return BigDecimal.ZERO;
        }
        
        if (demandType.equals(resourceType)) {
            return new BigDecimal("1.0");
        }
        
        // 部分匹配
        if (demandType.contains(resourceType) || resourceType.contains(demandType)) {
            return new BigDecimal("0.8");
        }
        
        // 相关类型匹配
        Map<String, Set<String>> relatedTypes = new HashMap<>();
        relatedTypes.put("帐篷", new HashSet<>(Arrays.asList("临时住所", "避难所")));
        relatedTypes.put("食品", new HashSet<>(Arrays.asList("食物", "粮食", "干粮")));
        relatedTypes.put("药品", new HashSet<>(Arrays.asList("药物", "医疗", "急救")));
        
        for (Map.Entry<String, Set<String>> entry : relatedTypes.entrySet()) {
            if ((demandType.equals(entry.getKey()) && entry.getValue().contains(resourceType)) ||
                (resourceType.equals(entry.getKey()) && entry.getValue().contains(demandType))) {
                return new BigDecimal("0.6");
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal calculateQuantitySimilarity(Integer demandQuantity, Integer availableQuantity) {
        if (demandQuantity == null || availableQuantity == null || demandQuantity <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (availableQuantity >= demandQuantity) {
            return new BigDecimal("1.0");
        }
        
        // 计算满足度
        double satisfaction = (double) availableQuantity / demandQuantity;
        return new BigDecimal(String.valueOf(satisfaction));
    }
    
    @Override
    public BigDecimal calculateDistanceSimilarity(String demandLocation, String resourceLocation) {
        if (demandLocation == null || resourceLocation == null) {
            return BigDecimal.ZERO;
        }
        
        // 完全相同
        if (demandLocation.equals(resourceLocation)) {
            return new BigDecimal("1.0");
        }
        
        // 同市不同区
        if (demandLocation.contains("市") && resourceLocation.contains("市") &&
            demandLocation.split("市")[0].equals(resourceLocation.split("市")[0])) {
            return new BigDecimal("0.8");
        }
        
        // 同省不同市
        if (demandLocation.contains("省") && resourceLocation.contains("省") &&
            demandLocation.split("省")[0].equals(resourceLocation.split("省")[0])) {
            return new BigDecimal("0.6");
        }
        
        // 不同省
        return new BigDecimal("0.2");
    }
    
    @Override
    public BigDecimal calculateTimeSimilarity(String urgency, Integer priorityLevel) {
        if (urgency == null || priorityLevel == null) {
            return new BigDecimal("0.5");
        }
        
        Map<String, Integer> urgencyMap = new HashMap<>();
        urgencyMap.put("紧急", 4);
        urgencyMap.put("高", 3);
        urgencyMap.put("中", 2);
        urgencyMap.put("低", 1);
        
        Integer urgencyValue = urgencyMap.getOrDefault(urgency, 2);
        
        // 计算匹配度
        double similarity = 1.0 - Math.abs(urgencyValue - priorityLevel) / 4.0;
        return new BigDecimal(String.valueOf(Math.max(0, similarity)));
    }
    
    @Override
    public List<Map<String, Object>> getRecommendedResources(Long demandId, Integer limit) {
        List<SimilarityResult> results = calculateSimilarityForDemand(demandId);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        int count = 0;
        
        for (SimilarityResult result : results) {
            if (count >= limit) break;
            
            Resource resource = resourceMapper.selectById(result.getResourceId());
            if (resource != null) {
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("resourceId", resource.getId());
                recommendation.put("resourceName", resource.getResourceName());
                recommendation.put("resourceType", resource.getResourceType());
                recommendation.put("availableQuantity", resource.getAvailableQuantity());
                recommendation.put("unit", resource.getUnit());
                recommendation.put("location", resource.getProvince() + resource.getCity() + resource.getDistrict());
                recommendation.put("similarityScore", result.getTotalScore());
                recommendation.put("matchReason", result.getMatchReason());
                
                recommendations.add(recommendation);
                count++;
            }
        }
        
        return recommendations;
    }
    
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":")
                .append(entry.getValue()).append(",");
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
