package com.disaster.emergency.service;

import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;

import java.util.List;
import java.util.Map;

/**
 * 相似度计算服务接口
 * 
 * <p>提供多维度相似度计算功能，包括类型、数量、距离、时效性、优先级等维度的相似度计算，
 * 用于资源与需求的匹配分析。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
public interface SimilarityCalculationService {
    
    /**
     * 计算资源与需求的多维相似度
     * 
     * <p>综合考虑类型、数量、距离、时效性、优先级等多个维度，
     * 计算资源与需求的综合相似度分数。</p>
     * 
     * @param resource 资源对象
     * @param demand 需求对象
     * @return 相似度分数（0-100），分数越高表示匹配度越高
     */
    double calculateOverallSimilarity(Resource resource, Demand demand);
    
    /**
     * 计算类型相似度
     * 
     * <p>基于资源类型和需求类型的匹配程度计算相似度。</p>
     * 
     * @param resourceType 资源类型
     * @param demandType 需求类型
     * @return 类型相似度分数（0-100）
     */
    double calculateTypeSimilarity(String resourceType, String demandType);
    
    /**
     * 计算数量相似度
     * 
     * <p>基于资源数量和需求数量的匹配程度计算相似度。</p>
     * 
     * @param resourceQuantity 资源数量
     * @param demandQuantity 需求数量
     * @return 数量相似度分数（0-100）
     */
    double calculateQuantitySimilarity(Integer resourceQuantity, Integer demandQuantity);
    
    /**
     * 计算距离相似度
     * 
     * <p>基于资源位置和需求位置的地理距离计算相似度。</p>
     * 
     * @param resourceLocation 资源位置
     * @param demandLocation 需求位置
     * @return 距离相似度分数（0-100），距离越近分数越高
     */
    double calculateDistanceSimilarity(String resourceLocation, String demandLocation);
    
    /**
     * 计算时效性相似度
     * 
     * <p>基于资源可用时间和需求紧急程度计算时效性相似度。</p>
     * 
     * @param resourceAvailableTime 资源可用时间
     * @param demandUrgency 需求紧急程度
     * @param demandDeadline 需求截止时间
     * @return 时效性相似度分数（0-100）
     */
    double calculateTimelinessSimilarity(String resourceAvailableTime, String demandUrgency, String demandDeadline);
    
    /**
     * 计算优先级相似度
     * 
     * <p>基于资源优先级和需求优先级计算相似度。</p>
     * 
     * @param resourcePriority 资源优先级
     * @param demandPriority 需求优先级
     * @return 优先级相似度分数（0-100）
     */
    double calculatePrioritySimilarity(String resourcePriority, String demandPriority);
    
    /**
     * 批量计算相似度
     * 
     * <p>为多个资源与单个需求计算相似度，返回排序后的结果。</p>
     * 
     * @param resources 资源列表
     * @param demand 需求对象
     * @param limit 返回结果数量限制
     * @return 相似度结果列表，按相似度分数降序排列
     */
    List<Map<String, Object>> calculateBatchSimilarity(List<Resource> resources, Demand demand, Integer limit);
    
    /**
     * 获取相似度权重配置
     * 
     * <p>获取各维度相似度的权重配置，用于调整相似度计算的重点。</p>
     * 
     * @return 权重配置Map
     */
    Map<String, Double> getSimilarityWeights();
    
    /**
     * 更新相似度权重配置
     * 
     * <p>更新各维度相似度的权重配置。</p>
     * 
     * @param weights 新的权重配置
     * @return 更新是否成功
     */
    boolean updateSimilarityWeights(Map<String, Double> weights);
}
