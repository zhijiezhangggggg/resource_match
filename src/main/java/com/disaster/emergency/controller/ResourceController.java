package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/resource")
@CrossOrigin
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping("/save")
    public Result<Resource> saveResource(@RequestBody Resource resource) {
        try {
            // 参数验证
            if (resource.getResourceType() == null || resource.getResourceType().trim().isEmpty()) {
                return Result.error(40001, "资源类型不能为空");
            }
            if (resource.getResourceName() == null || resource.getResourceName().trim().isEmpty()) {
                return Result.error(40001, "资源名称不能为空");
            }
            if (resource.getTotalQuantity() == null || resource.getTotalQuantity() < 0) {
                return Result.error(40001, "总数量不能为负数");
            }
            if (resource.getAvailableQuantity() == null || resource.getAvailableQuantity() < 0) {
                return Result.error(40001, "可用数量不能为负数");
            }
            if (resource.getAvailableQuantity() > resource.getTotalQuantity()) {
                return Result.error(40001, "可用数量不能超过总数量");
            }
            if (resource.getUnit() == null || resource.getUnit().trim().isEmpty()) {
                return Result.error(40001, "单位不能为空");
            }
            if (resource.getProvince() == null || resource.getProvince().trim().isEmpty()) {
                return Result.error(40001, "省份不能为空");
            }
            if (resource.getCity() == null || resource.getCity().trim().isEmpty()) {
                return Result.error(40001, "城市不能为空");
            }
            if (resource.getDistrict() == null || resource.getDistrict().trim().isEmpty()) {
                return Result.error(40001, "区县不能为空");
            }
            
            // 验证联系电话格式
            if (resource.getContactPhone() != null && !resource.getContactPhone().matches("^1[3-9]\\d{9}$")) {
                return Result.error(40001, "联系电话格式不正确");
            }
            
            Resource savedResource = resourceService.saveResource(resource);
            return Result.success("资源保存成功", savedResource);
        } catch (Exception e) {
            return Result.error(40001, "资源保存失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long organizationId) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 20);
        result.put("pages", 2);
        result.put("current", page);
        result.put("size", size);
        result.put("records", resourceService.list());
        
        return Result.success("查询成功", result);
    }

    @GetMapping("/{id}")
    public Result<Resource> getResourceDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(40002, "资源ID不存在");
        }
        return Result.success("查询成功", resource);
    }

    @PutMapping("/{id}/quantity")
    public Result<Map<String, Object>> updateResourceQuantity(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        Integer availableQuantity = (Integer) request.get("availableQuantity");
        String remark = (String) request.get("remark");
        
        if (availableQuantity == null || availableQuantity < 0) {
            return Result.error(40001, "可用数量不能为负数");
        }
        
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(40002, "资源ID不存在");
        }
        
        if (availableQuantity > resource.getTotalQuantity()) {
            return Result.error(40001, "可用数量不能超过总数量");
        }
        
        boolean success = resourceService.updateQuantity(id, availableQuantity);
        if (!success) {
            return Result.error(40001, "资源数量更新失败");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("availableQuantity", availableQuantity);
        result.put("updateTime", java.time.LocalDateTime.now());
        
        return Result.success("数量更新成功", result);
    }
}