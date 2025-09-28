package com.disaster.emergency.service;

import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SchedulingRecord;

import java.util.List;
import java.util.Map;

/**
 * 资源调度服务接口
 * 
 * <p>提供资源调度优化算法，包括贪心算法、遗传算法等多种调度策略，
 * 用于优化资源分配方案，提高资源利用效率。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
public interface SchedulingService {
    
    /**
     * 优化资源分配方案
     * 
     * <p>基于多个需求和资源，使用优化算法计算最优的资源分配方案。</p>
     * 
     * @param demands 需求列表
     * @param resources 资源列表
     * @param algorithm 调度算法类型（greedy、genetic、simulated_annealing）
     * @return 调度结果，包含分配方案和统计信息
     */
    Map<String, Object> optimizeResourceAllocation(List<Demand> demands, List<Resource> resources, String algorithm);
    
    /**
     * 贪心算法调度
     * 
     * <p>使用贪心算法进行资源分配，优先满足相似度最高的需求-资源对。</p>
     * 
     * @param demands 需求列表
     * @param resources 资源列表
     * @return 调度结果
     */
    Map<String, Object> greedyScheduling(List<Demand> demands, List<Resource> resources);
    
    /**
     * 遗传算法调度
     * 
     * <p>使用遗传算法进行资源分配，通过进化过程寻找全局最优解。</p>
     * 
     * @param demands 需求列表
     * @param resources 资源列表
     * @param populationSize 种群大小
     * @param generations 进化代数
     * @return 调度结果
     */
    Map<String, Object> geneticScheduling(List<Demand> demands, List<Resource> resources, 
                                         Integer populationSize, Integer generations);
    
    /**
     * 模拟退火算法调度
     * 
     * <p>使用模拟退火算法进行资源分配，通过温度控制避免局部最优。</p>
     * 
     * @param demands 需求列表
     * @param resources 资源列表
     * @param initialTemperature 初始温度
     * @param coolingRate 冷却速率
     * @return 调度结果
     */
    Map<String, Object> simulatedAnnealingScheduling(List<Demand> demands, List<Resource> resources,
                                                    Double initialTemperature, Double coolingRate);
    
    /**
     * 保存调度记录
     * 
     * <p>将调度结果保存到数据库。</p>
     * 
     * @param schedulingRecord 调度记录
     * @return 保存是否成功
     */
    boolean saveSchedulingRecord(SchedulingRecord schedulingRecord);
    
    /**
     * 获取调度记录
     * 
     * <p>根据ID获取调度记录。</p>
     * 
     * @param recordId 记录ID
     * @return 调度记录
     */
    SchedulingRecord getSchedulingRecord(Long recordId);
    
    /**
     * 获取调度历史
     * 
     * <p>获取指定时间范围内的调度历史记录。</p>
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量限制
     * @return 调度记录列表
     */
    List<SchedulingRecord> getSchedulingHistory(String startTime, String endTime, Integer limit);
    
    /**
     * 计算调度效果评估
     * 
     * <p>计算调度方案的效果评估指标。</p>
     * 
     * @param allocationResult 分配结果
     * @return 评估指标
     */
    Map<String, Object> calculateSchedulingMetrics(Map<String, Object> allocationResult);
    
    /**
     * 更新资源状态
     * 
     * <p>根据调度结果更新资源的状态。</p>
     * 
     * @param resourceId 资源ID
     * @param newStatus 新状态
     * @return 更新是否成功
     */
    boolean updateResourceStatus(Long resourceId, String newStatus);
    
    /**
     * 更新需求状态
     * 
     * <p>根据调度结果更新需求的状态。</p>
     * 
     * @param demandId 需求ID
     * @param newStatus 新状态
     * @return 更新是否成功
     */
    boolean updateDemandStatus(Long demandId, String newStatus);
    
    /**
     * 创建调度记录
     * 
     * <p>创建新的调度记录。</p>
     * 
     * @param demandId 需求ID
     * @param resourceId 资源ID
     * @param allocatedQuantity 分配数量
     * @param schedulerId 调度员ID
     * @param schedulerName 调度员姓名
     * @param remark 备注
     * @return 调度记录ID
     */
    Long createScheduling(Long demandId, Long resourceId, Integer allocatedQuantity, 
                         Long schedulerId, String schedulerName, String remark);
    
    /**
     * 获取调度记录列表
     * 
     * <p>获取所有调度记录。</p>
     * 
     * @return 调度记录列表
     */
    List<SchedulingRecord> list();
    
    /**
     * 更新调度记录状态
     * 
     * <p>更新指定调度记录的状态。</p>
     * 
     * @param recordId 记录ID
     * @param status 新状态
     * @return 更新是否成功
     */
    boolean updateStatus(Long recordId, String status);
    
    /**
     * 完成调度
     * 
     * <p>标记调度为完成状态。</p>
     * 
     * @param recordId 记录ID
     * @param remark 完成备注
     * @return 更新是否成功
     */
    boolean completeScheduling(Long recordId, String remark);
}