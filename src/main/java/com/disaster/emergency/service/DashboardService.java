package com.disaster.emergency.service;

import java.util.Map;

/**
 * 大屏数据服务接口
 * 
 * <p>提供大屏展示所需的各种数据服务，包括实时状态、资源分布、调度控制等。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
public interface DashboardService {
    
    /**
     * 获取实时匹配状态概览
     * 
     * @return 匹配状态统计数据
     */
    Map<String, Object> getMatchingOverview();
    
    /**
     * 获取资源分布数据
     * 
     * @return 资源分布统计信息
     */
    Map<String, Object> getResourceDistribution();
    
    /**
     * 获取实时调度状态
     * 
     * @return 调度状态和指令信息
     */
    Map<String, Object> getSchedulingStatus();
    
    /**
     * 获取实时匹配进度
     * 
     * @return 当前匹配进度信息
     */
    Map<String, Object> getMatchingProgress();
    
    /**
     * 获取地图数据
     * 
     * @return 地图展示所需的数据
     */
    Map<String, Object> getMapData();
    
    /**
     * 修改调度指令
     * 
     * @param request 调度指令修改请求
     * @return 修改结果
     */
    Map<String, Object> modifyScheduling(Map<String, Object> request);
    
    /**
     * 确认调度指令
     * 
     * @param request 确认请求
     * @return 确认结果
     */
    Map<String, Object> confirmScheduling(Map<String, Object> request);
    
    /**
     * 取消调度指令
     * 
     * @param request 取消请求
     * @return 取消结果
     */
    Map<String, Object> cancelScheduling(Map<String, Object> request);
    
    /**
     * 获取实时告警信息
     * 
     * @return 告警信息列表
     */
    Map<String, Object> getAlerts();
    
    /**
     * 推送实时数据到WebSocket
     * 
     * @param data 要推送的数据
     */
    void pushRealtimeData(Map<String, Object> data);
}
