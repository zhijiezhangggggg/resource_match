package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.DataService;
import com.disaster.emergency.service.DisasterService;
import com.disaster.emergency.service.DemandService;
import com.disaster.emergency.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private DemandService demandService;
    
    @Autowired
    private DisasterService disasterService;

    @Override
    public Map<String, Object> getAllData() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有资源数据
        List<Resource> resources = resourceService.list();
        result.put("resources", resources);
        result.put("resourceCount", resources.size());
        
        // 获取所有需求数据
        List<Demand> demands = demandService.list();
        result.put("demands", demands);
        result.put("demandCount", demands.size());
        
        // 获取所有灾情数据
        List<Disaster> disasters = disasterService.list();
        result.put("disasters", disasters);
        result.put("disasterCount", disasters.size());
        
        // 计算总数据量
        int totalCount = resources.size() + demands.size() + disasters.size();
        result.put("totalCount", totalCount);
        
        return result;
    }

    @Override
    public Map<String, Object> getAllDataPaginated(Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取分页资源数据
        List<Resource> resources = resourceService.list();
        int resourceStart = (page - 1) * size;
        int resourceEnd = Math.min(resourceStart + size, resources.size());
        List<Resource> paginatedResources = resources.subList(resourceStart, resourceEnd);
        result.put("resources", paginatedResources);
        result.put("resourceCount", resources.size());
        result.put("resourcePage", page);
        result.put("resourceSize", size);
        
        // 获取分页需求数据
        List<Demand> demands = demandService.list();
        int demandStart = (page - 1) * size;
        int demandEnd = Math.min(demandStart + size, demands.size());
        List<Demand> paginatedDemands = demands.subList(demandStart, demandEnd);
        result.put("demands", paginatedDemands);
        result.put("demandCount", demands.size());
        result.put("demandPage", page);
        result.put("demandSize", size);
        
        // 获取分页灾情数据
        List<Disaster> disasters = disasterService.list();
        int disasterStart = (page - 1) * size;
        int disasterEnd = Math.min(disasterStart + size, disasters.size());
        List<Disaster> paginatedDisasters = disasters.subList(disasterStart, disasterEnd);
        result.put("disasters", paginatedDisasters);
        result.put("disasterCount", disasters.size());
        result.put("disasterPage", page);
        result.put("disasterSize", size);
        
        // 计算总数据量
        int totalCount = resources.size() + demands.size() + disasters.size();
        result.put("totalCount", totalCount);
        result.put("currentPage", page);
        result.put("pageSize", size);
        
        return result;
    }

    @Override
    public Map<String, Object> getDataStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取资源统计信息
            Map<String, Object> resourceStats = resourceService.getResourceStatistics();
            result.put("resourceStatistics", resourceStats);
            
            // 获取需求统计信息
            Map<String, Object> demandStats = demandService.getDemandStatistics(null, null, null, null);
            result.put("demandStatistics", demandStats);
            
            // 获取灾情统计信息
            Map<String, Object> disasterStats = disasterService.getDisasterStatistics();
            result.put("disasterStatistics", disasterStats);
            
            // 计算总体统计
            Map<String, Object> overallStats = new HashMap<>();
            overallStats.put("totalResources", resourceService.count());
            overallStats.put("totalDemands", demandService.count());
            overallStats.put("totalDisasters", disasterService.count());
            overallStats.put("totalRecords", resourceService.count() + demandService.count() + disasterService.count());
            result.put("overallStatistics", overallStats);
            
        } catch (Exception e) {
            // 如果统计方法失败，提供基础统计
            Map<String, Object> overallStats = new HashMap<>();
            overallStats.put("totalResources", resourceService.count());
            overallStats.put("totalDemands", demandService.count());
            overallStats.put("totalDisasters", disasterService.count());
            overallStats.put("totalRecords", resourceService.count() + demandService.count() + disasterService.count());
            result.put("overallStatistics", overallStats);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getDataByRegion(String province, String city, String district) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取指定地区的资源数据
        List<Resource> resources = resourceService.list();
        if (province != null || city != null || district != null) {
            resources = resources.stream()
                .filter(resource -> 
                    (province == null || province.equals(resource.getProvince())) &&
                    (city == null || city.equals(resource.getCity())) &&
                    (district == null || district.equals(resource.getDistrict()))
                )
                .collect(Collectors.toList());
        }
        result.put("resources", resources);
        result.put("resourceCount", resources.size());
        
        // 获取指定地区的需求数据
        List<Demand> demands = demandService.list();
        if (province != null || city != null || district != null) {
            demands = demands.stream()
                .filter(demand -> 
                    (province == null || province.equals(demand.getProvince())) &&
                    (city == null || city.equals(demand.getCity())) &&
                    (district == null || district.equals(demand.getDistrict()))
                )
                .collect(Collectors.toList());
        }
        result.put("demands", demands);
        result.put("demandCount", demands.size());
        
        // 获取指定地区的灾情数据
        List<Disaster> disasters = disasterService.list();
        if (province != null || city != null || district != null) {
            disasters = disasters.stream()
                .filter(disaster -> 
                    (province == null || province.equals(disaster.getProvince())) &&
                    (city == null || city.equals(disaster.getCity())) &&
                    (district == null || district.equals(disaster.getDistrict()))
                )
                .collect(Collectors.toList());
        }
        result.put("disasters", disasters);
        result.put("disasterCount", disasters.size());
        
        // 计算总数据量
        int totalCount = resources.size() + demands.size() + disasters.size();
        result.put("totalCount", totalCount);
        
        // 添加筛选条件
        Map<String, String> filter = new HashMap<>();
        if (province != null) filter.put("province", province);
        if (city != null) filter.put("city", city);
        if (district != null) filter.put("district", district);
        result.put("filter", filter);
        
        return result;
    }

    @Override
    public Map<String, Object> getLatestData(Integer limit) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最新的资源数据
        List<Resource> allResources = resourceService.list();
        List<Resource> latestResources = allResources.stream()
            .sorted((r1, r2) -> r2.getCreateTime().compareTo(r1.getCreateTime()))
            .limit(limit)
            .collect(Collectors.toList());
        result.put("latestResources", latestResources);
        
        // 获取最新的需求数据
        List<Demand> allDemands = demandService.list();
        List<Demand> latestDemands = allDemands.stream()
            .sorted((d1, d2) -> d2.getCreateTime().compareTo(d1.getCreateTime()))
            .limit(limit)
            .collect(Collectors.toList());
        result.put("latestDemands", latestDemands);
        
        // 获取最新的灾情数据
        List<Disaster> allDisasters = disasterService.list();
        List<Disaster> latestDisasters = allDisasters.stream()
            .sorted((d1, d2) -> d2.getCreateTime().compareTo(d1.getCreateTime()))
            .limit(limit)
            .collect(Collectors.toList());
        result.put("latestDisasters", latestDisasters);
        
        result.put("limit", limit);
        result.put("totalLatestCount", latestResources.size() + latestDemands.size() + latestDisasters.size());
        
        return result;
    }

    @Override
    public List<Resource> getAllResources() {
        return resourceService.list();
    }

    @Override
    public List<Demand> getAllDemands() {
        return demandService.list();
    }

    @Override
    public List<Disaster> getAllDisasters() {
        return disasterService.list();
    }

    @Override
    public Map<String, Object> getDataByStatus(String resourceStatus, String demandStatus, String disasterStatus) {
        Map<String, Object> result = new HashMap<>();
        
        // 按状态筛选资源
        List<Resource> resources = resourceService.list();
        if (resourceStatus != null && !resourceStatus.trim().isEmpty()) {
            resources = resources.stream()
                .filter(resource -> resourceStatus.equals(resource.getStatus()))
                .collect(Collectors.toList());
        }
        result.put("resources", resources);
        result.put("resourceCount", resources.size());
        
        // 按状态筛选需求
        List<Demand> demands = demandService.list();
        if (demandStatus != null && !demandStatus.trim().isEmpty()) {
            demands = demands.stream()
                .filter(demand -> demandStatus.equals(demand.getStatus()))
                .collect(Collectors.toList());
        }
        result.put("demands", demands);
        result.put("demandCount", demands.size());
        
        // 按状态筛选灾情
        List<Disaster> disasters = disasterService.list();
        if (disasterStatus != null && !disasterStatus.trim().isEmpty()) {
            disasters = disasters.stream()
                .filter(disaster -> disasterStatus.equals(disaster.getStatus()))
                .collect(Collectors.toList());
        }
        result.put("disasters", disasters);
        result.put("disasterCount", disasters.size());
        
        // 计算总数据量
        int totalCount = resources.size() + demands.size() + disasters.size();
        result.put("totalCount", totalCount);
        
        // 添加筛选条件
        Map<String, String> filter = new HashMap<>();
        if (resourceStatus != null) filter.put("resourceStatus", resourceStatus);
        if (demandStatus != null) filter.put("demandStatus", demandStatus);
        if (disasterStatus != null) filter.put("disasterStatus", disasterStatus);
        result.put("filter", filter);
        
        return result;
    }

    @Override
    public Map<String, Object> getDataOverview() {
        Map<String, Object> result = new HashMap<>();
        
        // 基础统计信息
        long resourceCount = resourceService.count();
        long demandCount = demandService.count();
        long disasterCount = disasterService.count();
        
        result.put("totalResources", resourceCount);
        result.put("totalDemands", demandCount);
        result.put("totalDisasters", disasterCount);
        result.put("totalRecords", resourceCount + demandCount + disasterCount);
        
        // 获取可用资源数量
        List<Resource> availableResources = resourceService.list().stream()
            .filter(resource -> "available".equals(resource.getStatus()) && resource.getAvailableQuantity() > 0)
            .collect(Collectors.toList());
        result.put("availableResources", availableResources.size());
        
        // 获取紧急需求数量
        List<Demand> urgentDemands = demandService.list().stream()
            .filter(demand -> "urgent".equals(demand.getUrgency()) || "high".equals(demand.getUrgency()))
            .collect(Collectors.toList());
        result.put("urgentDemands", urgentDemands.size());
        
        // 获取活跃灾情数量
        List<Disaster> activeDisasters = disasterService.list().stream()
            .filter(disaster -> "active".equals(disaster.getStatus()) || "ongoing".equals(disaster.getStatus()))
            .collect(Collectors.toList());
        result.put("activeDisasters", activeDisasters.size());
        
        return result;
    }
}
