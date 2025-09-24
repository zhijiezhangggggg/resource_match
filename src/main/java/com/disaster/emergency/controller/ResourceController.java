package com.disaster.emergency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resource")
@CrossOrigin
@Tag(name = "资源管理", description = "资源信息管理、查询、更新")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Operation(summary = "保存资源", description = "新增或更新资源信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "资源保存成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
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

    @Operation(summary = "查询资源列表", description = "分页查询资源信息，支持多条件筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @GetMapping("/list")
    public Result<Map<String, Object>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String resourceName) {
        
        try {
            // 参数验证
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            
            Page<Resource> resourcePage = resourceService.getResourceList(page, size, resourceType, status, 
                    province, city, district, organizationId, resourceName);
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", resourcePage.getTotal());
            result.put("pages", resourcePage.getPages());
            result.put("current", resourcePage.getCurrent());
            result.put("size", resourcePage.getSize());
            result.put("records", resourcePage.getRecords());
            
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error(40001, "查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询资源详情", description = "根据ID查询资源详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "40002", description = "资源不存在")
    })
    @GetMapping("/{id}")
    public Result<Resource> getResourceDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(40002, "资源不存在");
        }
        return Result.success("查询成功", resource);
    }

    @Operation(summary = "更新资源信息", description = "更新资源的完整信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误"),
            @ApiResponse(responseCode = "40002", description = "资源不存在")
    })
    @PutMapping("/{id}")
    public Result<Resource> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        try {
            // 检查资源是否存在
            Resource existingResource = resourceService.getById(id);
            if (existingResource == null) {
                return Result.error(40002, "资源不存在");
            }
            
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
            
            resource.setId(id);
            resource.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = resourceService.updateById(resource);
            
            if (!success) {
                return Result.error(40001, "资源更新失败");
            }
            
            return Result.success("资源更新成功", resource);
        } catch (Exception e) {
            return Result.error(40001, "资源更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新资源数量", description = "更新资源的可用数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误"),
            @ApiResponse(responseCode = "40002", description = "资源不存在")
    })
    @PutMapping("/{id}/quantity")
    public Result<Map<String, Object>> updateResourceQuantity(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        Integer availableQuantity = (Integer) request.get("availableQuantity");
        
        if (availableQuantity == null || availableQuantity < 0) {
            return Result.error(40001, "可用数量不能为负数");
        }
        
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(40002, "资源不存在");
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

    @Operation(summary = "删除资源", description = "根据ID删除资源")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "40002", description = "资源不存在")
    })
    @DeleteMapping("/{id}")
    public Result<String> deleteResource(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(40002, "资源ID无效");
        }
        
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error(40002, "资源不存在");
        }
        
        boolean success = resourceService.removeById(id);
        if (!success) {
            return Result.error(40001, "资源删除失败");
        }
        
        return Result.success("资源删除成功", null);
    }

    @Operation(summary = "批量删除资源", description = "批量删除多个资源")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @DeleteMapping("/batch")
    public Result<String> batchDeleteResources(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(40001, "资源ID列表不能为空");
        }
        
        boolean success = resourceService.removeByIds(ids);
        if (!success) {
            return Result.error(40001, "批量删除失败");
        }
        
        return Result.success("批量删除成功", null);
    }

    @Operation(summary = "资源统计", description = "获取资源统计信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getResourceStatistics() {
        try {
            Map<String, Object> statistics = resourceService.getResourceStatistics();
            return Result.success("统计成功", statistics);
        } catch (Exception e) {
            return Result.error(40001, "统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按类型统计资源", description = "按资源类型统计资源数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功")
    })
    @GetMapping("/statistics/by-type")
    public Result<List<Map<String, Object>>> getResourceStatisticsByType() {
        try {
            List<Map<String, Object>> statistics = resourceService.getResourceStatisticsByType();
            return Result.success("统计成功", statistics);
        } catch (Exception e) {
            return Result.error(40001, "统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按地区统计资源", description = "按地区统计资源数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功")
    })
    @GetMapping("/statistics/by-region")
    public Result<List<Map<String, Object>>> getResourceStatisticsByRegion() {
        try {
            List<Map<String, Object>> statistics = resourceService.getResourceStatisticsByRegion();
            return Result.success("统计成功", statistics);
        } catch (Exception e) {
            return Result.error(40001, "统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按状态统计资源", description = "按资源状态统计资源数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计成功")
    })
    @GetMapping("/statistics/by-status")
    public Result<List<Map<String, Object>>> getResourceStatisticsByStatus() {
        try {
            List<Map<String, Object>> statistics = resourceService.getResourceStatisticsByStatus();
            return Result.success("统计成功", statistics);
        } catch (Exception e) {
            return Result.error(40001, "统计失败: " + e.getMessage());
        }
    }
}