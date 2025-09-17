package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.DisasterService;
import com.disaster.emergency.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/map")
@CrossOrigin
public class MapController {

    @Autowired
    private DisasterService disasterService;

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/disasters")
    public Result<List<Map<String, Object>>> getDisasterDistribution(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        
        List<Disaster> disasters = disasterService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Disaster disaster : disasters) {
            // 模拟经纬度数据
            Map<String, Object> disasterMap = new HashMap<>();
            disasterMap.put("id", disaster.getId());
            disasterMap.put("disasterType", disaster.getDisasterType());
            disasterMap.put("severity", disaster.getSeverity());
            disasterMap.put("province", disaster.getProvince());
            disasterMap.put("city", disaster.getCity());
            disasterMap.put("district", disaster.getDistrict());
            disasterMap.put("longitude", 103.6276 + Math.random() * 0.1); // 模拟成都附近坐标
            disasterMap.put("latitude", 31.1311 + Math.random() * 0.1);
            disasterMap.put("status", disaster.getStatus());
            disasterMap.put("occurTime", disaster.getOccurTime());
            disasterMap.put("demandCount", (int)(Math.random() * 10) + 1); // 模拟需求数量
            
            result.add(disasterMap);
        }
        
        return Result.success("查询成功", result);
    }

    @GetMapping("/resources")
    public Result<List<Map<String, Object>>> getResourceDistribution(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
        
        List<Resource> resources = resourceService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Resource resource : resources) {
            // 模拟经纬度数据
            Map<String, Object> resourceMap = new HashMap<>();
            resourceMap.put("id", resource.getId());
            resourceMap.put("resourceType", resource.getResourceType());
            resourceMap.put("resourceName", resource.getResourceName());
            resourceMap.put("availableQuantity", resource.getAvailableQuantity());
            resourceMap.put("province", resource.getProvince());
            resourceMap.put("city", resource.getCity());
            resourceMap.put("district", resource.getDistrict());
            resourceMap.put("longitude", 103.9238 + Math.random() * 0.1); // 模拟成都附近坐标
            resourceMap.put("latitude", 30.5728 + Math.random() * 0.1);
            resourceMap.put("warehouseName", resource.getWarehouseName());
            resourceMap.put("status", resource.getStatus());
            
            result.add(resourceMap);
        }
        
        return Result.success("查询成功", result);
    }
}
