package com.disaster.emergency.service;

import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SimilarityConfig;
import com.disaster.emergency.entity.SimilarityResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SimilarityService {
    
    /**
     * 计算需求与资源的相似度
     * @param demand 需求
     * @param resource 资源
     * @return 相似度结果
     */
    SimilarityResult calculateSimilarity(Demand demand, Resource resource);
    
    /**
     * 批量计算相似度
     * @param demandId 需求ID
     * @return 相似度结果列表
     */
    List<SimilarityResult> calculateSimilarityForDemand(Long demandId);
    
    /**
     * 获取相似度配置
     * @return 配置列表
     */
    List<SimilarityConfig> getSimilarityConfigs();
    
    /**
     * 更新相似度配置
     * @param configs 配置列表
     * @return 更新结果
     */
    boolean updateSimilarityConfigs(List<SimilarityConfig> configs);
    
    /**
     * 计算类型相似度
     * @param demandType 需求类型
     * @param resourceType 资源类型
     * @return 相似度分数
     */
    BigDecimal calculateTypeSimilarity(String demandType, String resourceType);
    
    /**
     * 计算数量相似度
     * @param demandQuantity 需求数量
     * @param availableQuantity 可用数量
     * @return 相似度分数
     */
    BigDecimal calculateQuantitySimilarity(Integer demandQuantity, Integer availableQuantity);
    
    /**
     * 计算距离相似度
     * @param demandLocation 需求位置
     * @param resourceLocation 资源位置
     * @return 相似度分数
     */
    BigDecimal calculateDistanceSimilarity(String demandLocation, String resourceLocation);
    
    /**
     * 计算时效相似度
     * @param urgency 紧急程度
     * @param priorityLevel 优先级
     * @return 相似度分数
     */
    BigDecimal calculateTimeSimilarity(String urgency, Integer priorityLevel);
    
    /**
     * 获取推荐资源列表
     * @param demandId 需求ID
     * @param limit 限制数量
     * @return 推荐资源列表
     */
    List<Map<String, Object>> getRecommendedResources(Long demandId, Integer limit);
}
