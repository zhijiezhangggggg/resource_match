package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SimilarityConfig;
import com.disaster.emergency.entity.SimilarityResult;
import com.disaster.emergency.service.SimilarityService;
import com.disaster.emergency.service.DemandService;
import com.disaster.emergency.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/similarity")
@CrossOrigin
public class SimilarityController {
    
    @Autowired
    private SimilarityService similarityService;
    
    @Autowired
    private DemandService demandService;
    
    @Autowired
    private ResourceService resourceService;
    
    @PostMapping("/calculate")
    public Result<SimilarityResult> calculateSimilarity(@RequestBody Map<String, Object> request) {
        try {
            Long demandId = Long.valueOf(request.get("demandId").toString());
            Long resourceId = Long.valueOf(request.get("resourceId").toString());
            
            Demand demand = demandService.getById(demandId);
            Resource resource = resourceService.getById(resourceId);
            
            if (demand == null) {
                return Result.error(20001, "需求不存在");
            }
            if (resource == null) {
                return Result.error(20001, "资源不存在");
            }
            
            SimilarityResult result = similarityService.calculateSimilarity(demand, resource);
            return Result.success("相似度计算成功", result);
        } catch (Exception e) {
            return Result.error(20001, "相似度计算失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/calculate-for-demand")
    public Result<List<SimilarityResult>> calculateSimilarityForDemand(@RequestBody Map<String, Object> request) {
        try {
            Long demandId = Long.valueOf(request.get("demandId").toString());
            
            List<SimilarityResult> results = similarityService.calculateSimilarityForDemand(demandId);
            return Result.success("相似度计算成功", results);
        } catch (Exception e) {
            return Result.error(20001, "相似度计算失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/recommendations/{demandId}")
    public Result<List<Map<String, Object>>> getRecommendations(
            @PathVariable Long demandId,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (demandId == null || demandId <= 0) {
                return Result.error(20001, "需求ID无效");
            }
            
            List<Map<String, Object>> recommendations = similarityService.getRecommendedResources(demandId, limit);
            return Result.success("获取推荐资源成功", recommendations);
        } catch (Exception e) {
            return Result.error(20001, "获取推荐资源失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/config")
    public Result<List<SimilarityConfig>> getSimilarityConfigs() {
        try {
            List<SimilarityConfig> configs = similarityService.getSimilarityConfigs();
            return Result.success("获取相似度配置成功", configs);
        } catch (Exception e) {
            return Result.error(20001, "获取相似度配置失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/config")
    public Result<Map<String, Object>> updateSimilarityConfigs(@RequestBody List<SimilarityConfig> configs) {
        try {
            boolean success = similarityService.updateSimilarityConfigs(configs);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("updatedCount", configs.size());
            
            return Result.success("相似度配置更新成功", result);
        } catch (Exception e) {
            return Result.error(20001, "相似度配置更新失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/dimension/type")
    public Result<Map<String, Object>> calculateTypeSimilarity(
            @RequestParam String demandType,
            @RequestParam String resourceType) {
        try {
            java.math.BigDecimal score = similarityService.calculateTypeSimilarity(demandType, resourceType);
            Map<String, Object> result = new HashMap<>();
            result.put("demandType", demandType);
            result.put("resourceType", resourceType);
            result.put("similarityScore", score);
            
            return Result.success("类型相似度计算成功", result);
        } catch (Exception e) {
            return Result.error(20001, "类型相似度计算失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/dimension/quantity")
    public Result<Map<String, Object>> calculateQuantitySimilarity(
            @RequestParam Integer demandQuantity,
            @RequestParam Integer availableQuantity) {
        try {
            java.math.BigDecimal score = similarityService.calculateQuantitySimilarity(demandQuantity, availableQuantity);
            Map<String, Object> result = new HashMap<>();
            result.put("demandQuantity", demandQuantity);
            result.put("availableQuantity", availableQuantity);
            result.put("similarityScore", score);
            
            return Result.success("数量相似度计算成功", result);
        } catch (Exception e) {
            return Result.error(20001, "数量相似度计算失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/dimension/distance")
    public Result<Map<String, Object>> calculateDistanceSimilarity(
            @RequestParam String demandLocation,
            @RequestParam String resourceLocation) {
        try {
            java.math.BigDecimal score = similarityService.calculateDistanceSimilarity(demandLocation, resourceLocation);
            Map<String, Object> result = new HashMap<>();
            result.put("demandLocation", demandLocation);
            result.put("resourceLocation", resourceLocation);
            result.put("similarityScore", score);
            
            return Result.success("距离相似度计算成功", result);
        } catch (Exception e) {
            return Result.error(20001, "距离相似度计算失败: " + e.getMessage());
        }
    }
}
