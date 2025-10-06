package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.DisasterService;
import com.disaster.emergency.service.DemandService;
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

    @Autowired
    private DemandService demandService;

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
            disasterMap.put("longitude", disaster.getLongitude() != null ? disaster.getLongitude() : 103.6276); // 使用真实坐标，如果没有则使用默认值
            disasterMap.put("latitude", disaster.getLatitude() != null ? disaster.getLatitude() : 31.1311);
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
            resourceMap.put("longitude", resource.getLongitude() != null ? resource.getLongitude() : 103.9238); // 使用真实坐标，如果没有则使用默认值
            resourceMap.put("latitude", resource.getLatitude() != null ? resource.getLatitude() : 30.5728);
            resourceMap.put("warehouseName", resource.getWarehouseName());
            resourceMap.put("status", resource.getStatus());
            
            result.add(resourceMap);
        }
        
        return Result.success("查询成功", result);
    }

    @GetMapping("/demands")
    public Result<List<Map<String, Object>>> getDemandDistribution(
            @RequestParam(required = false) String demandType,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        
        List<Demand> demands = demandService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Demand demand : demands) {
            Map<String, Object> demandMap = new HashMap<>();
            demandMap.put("id", demand.getId());
            demandMap.put("demandType", demand.getDemandType());
            demandMap.put("quantity", demand.getQuantity());
            demandMap.put("unit", demand.getUnit());
            demandMap.put("urgency", demand.getUrgency());
            demandMap.put("province", demand.getProvince());
            demandMap.put("city", demand.getCity());
            demandMap.put("district", demand.getDistrict());
            demandMap.put("longitude", demand.getLongitude() != null ? demand.getLongitude() : 103.6276); // 使用真实坐标，如果没有则使用默认值
            demandMap.put("latitude", demand.getLatitude() != null ? demand.getLatitude() : 31.1311);
            demandMap.put("status", demand.getStatus());
            demandMap.put("createTime", demand.getCreateTime());
            
            result.add(demandMap);
        }
        
        return Result.success("查询成功", result);
    }
}
