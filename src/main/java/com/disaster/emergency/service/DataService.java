package com.disaster.emergency.service;

import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;

import java.util.List;
import java.util.Map;

public interface DataService {
    
    /**
     * 获取所有数据（资源、需求、灾情）
     */
    Map<String, Object> getAllData();
    
    /**
     * 分页获取所有数据
     */
    Map<String, Object> getAllDataPaginated(Integer page, Integer size);
    
    /**
     * 获取数据统计信息
     */
    Map<String, Object> getDataStatistics();
    
    /**
     * 按地区获取数据
     */
    Map<String, Object> getDataByRegion(String province, String city, String district);
    
    /**
     * 获取最新数据
     */
    Map<String, Object> getLatestData(Integer limit);
    
    /**
     * 获取所有资源数据
     */
    List<Resource> getAllResources();
    
    /**
     * 获取所有需求数据
     */
    List<Demand> getAllDemands();
    
    /**
     * 获取所有灾情数据
     */
    List<Disaster> getAllDisasters();
    
    /**
     * 按状态获取数据
     */
    Map<String, Object> getDataByStatus(String resourceStatus, String demandStatus, String disasterStatus);
    
    /**
     * 获取数据概览
     */
    Map<String, Object> getDataOverview();
}
