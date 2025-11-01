package com.disaster.emergency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.service.DemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demand")
@CrossOrigin
@Tag(name = "需求管理接口", description = "需求管理相关接口")
public class DemandController {

    @Autowired
    private DemandService demandService;

    @PostMapping("/submit")
    @Operation(summary = "提交需求", description = "提交新的救援需求")
    public Result<Demand> submitDemand(@Parameter(description = "需求信息", required = true) @RequestBody Demand demand) {
        try {
            // 参数验证
            if (demand.getDisasterId() == null || demand.getDisasterId() <= 0) {
                return Result.error(30001, "灾情ID不能为空或无效");
            }
            if (demand.getDemandType() == null || demand.getDemandType().trim().isEmpty()) {
                return Result.error(30001, "需求类型不能为空");
            }
            if (demand.getQuantity() == null || demand.getQuantity() <= 0) {
                return Result.error(30002, "需求数量必须大于0");
            }
            if (demand.getUnit() == null || demand.getUnit().trim().isEmpty()) {
                return Result.error(30001, "单位不能为空");
            }
            if (demand.getUrgency() == null || demand.getUrgency().trim().isEmpty()) {
                return Result.error(30001, "紧急程度不能为空");
            }
            if (demand.getProvince() == null || demand.getProvince().trim().isEmpty()) {
                return Result.error(30001, "省份不能为空");
            }
            if (demand.getCity() == null || demand.getCity().trim().isEmpty()) {
                return Result.error(30001, "城市不能为空");
            }
            if (demand.getDistrict() == null || demand.getDistrict().trim().isEmpty()) {
                return Result.error(30001, "区县不能为空");
            }
            
            // 验证经纬度
            if (demand.getLatitude() != null && (demand.getLatitude() < -90.0 || demand.getLatitude() > 90.0)) {
                return Result.error(30001, "纬度必须在-90到90度之间");
            }
            if (demand.getLongitude() != null && (demand.getLongitude() < -180.0 || demand.getLongitude() > 180.0)) {
                return Result.error(30001, "经度必须在-180到180度之间");
            }
            
            Demand submittedDemand = demandService.submitDemand(demand);
            return Result.success("需求提交成功", submittedDemand);
        } catch (Exception e) {
            return Result.error(30001, "需求提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询需求列表", description = "分页查询需求列表，支持多条件筛选")
    public Result<Map<String, Object>> getDemandList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "灾情ID") @RequestParam(required = false) Long disasterId,
            @Parameter(description = "需求类型") @RequestParam(required = false) String demandType,
            @Parameter(description = "紧急程度") @RequestParam(required = false) String urgency,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "省份") @RequestParam(required = false) String province,
            @Parameter(description = "城市") @RequestParam(required = false) String city) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        try {
            Page<Demand> demandPage = demandService.getDemandList(page, size, disasterId, demandType, urgency, status, province, city);
            Map<String, Object> result = new HashMap<>();
            result.put("total", demandPage.getTotal());
            result.put("pages", demandPage.getPages());
            result.put("current", demandPage.getCurrent());
            result.put("size", demandPage.getSize());
            result.put("records", demandPage.getRecords());
            
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error(30001, "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "查询需求详情", description = "根据ID查询需求详细信息")
    public Result<Demand> getDemandDetail(@Parameter(description = "需求ID", required = true) @PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(30002, "需求ID无效");
        }
        
        try {
            Demand demand = demandService.getById(id);
            if (demand == null) {
                return Result.error(30002, "需求不存在");
            }
            return Result.success("查询成功", demand);
        } catch (Exception e) {
            return Result.error(30001, "查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "更新需求", description = "更新需求信息")
    public Result<Demand> updateDemand(@Parameter(description = "需求信息", required = true) @RequestBody Demand demand) {
        try {
            if (demand.getId() == null || demand.getId() <= 0) {
                return Result.error(30002, "需求ID不能为空或无效");
            }
            
            // 检查需求是否存在
            Demand existingDemand = demandService.getById(demand.getId());
            if (existingDemand == null) {
                return Result.error(30002, "需求不存在");
            }
            
            // 合并现有数据和新数据，只更新非空字段
            if (demand.getDisasterId() != null) {
                existingDemand.setDisasterId(demand.getDisasterId());
            }
            if (demand.getDemandType() != null) {
                existingDemand.setDemandType(demand.getDemandType());
            }
            if (demand.getQuantity() != null) {
                existingDemand.setQuantity(demand.getQuantity());
            }
            if (demand.getUnit() != null) {
                existingDemand.setUnit(demand.getUnit());
            }
            if (demand.getUrgency() != null) {
                existingDemand.setUrgency(demand.getUrgency());
            }
            if (demand.getProvince() != null) {
                existingDemand.setProvince(demand.getProvince());
            }
            if (demand.getCity() != null) {
                existingDemand.setCity(demand.getCity());
            }
            if (demand.getDistrict() != null) {
                existingDemand.setDistrict(demand.getDistrict());
            }
            if (demand.getLatitude() != null) {
                existingDemand.setLatitude(demand.getLatitude());
            }
            if (demand.getLongitude() != null) {
                existingDemand.setLongitude(demand.getLongitude());
            }
            if (demand.getDescription() != null) {
                existingDemand.setDescription(demand.getDescription());
            }
            if (demand.getStatus() != null) {
                existingDemand.setStatus(demand.getStatus());
            }
            // 始终更新update_time
            existingDemand.setUpdateTime(LocalDateTime.now());
            
            // 使用updateById确保执行UPDATE操作，不会误判为INSERT
            boolean success = demandService.updateById(existingDemand);
            if (success) {
                // 返回更新后的完整对象
                Demand updatedDemand = demandService.getById(demand.getId());
                return Result.success("更新成功", updatedDemand);
            } else {
                return Result.error(30001, "更新失败");
            }
        } catch (Exception e) {
            return Result.error(30001, "更新失败: " + e.getMessage());
        }
    }

    @PutMapping("/status")
    @Operation(summary = "更新需求状态", description = "更新需求处理状态，支持请求参数和请求体两种方式")
    public Result<String> updateDemandStatus(
            @Parameter(description = "需求ID") @RequestParam(required = false) Long id,
            @Parameter(description = "新状态") @RequestParam(required = false) String status,
            @Parameter(description = "请求体（可选，如果提供则使用请求体中的参数）") @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            Long demandId;
            String newStatus;
            
            // 优先使用请求体，如果请求体为空则使用请求参数
            if (requestBody != null && !requestBody.isEmpty()) {
                Object idObj = requestBody.get("id");
                Object statusObj = requestBody.get("status");
                
                if (idObj != null) {
                    demandId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
                } else {
                    demandId = id;
                }
                
                if (statusObj != null) {
                    newStatus = statusObj.toString();
                } else {
                    newStatus = status;
                }
            } else {
                demandId = id;
                newStatus = status;
            }
            
            if (demandId == null || demandId <= 0) {
                return Result.error(30002, "需求ID不能为空或无效");
            }
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return Result.error(30001, "状态不能为空");
            }
            
            boolean success = demandService.updateDemandStatus(demandId, newStatus);
            if (success) {
                return Result.success("状态更新成功", null);
            } else {
                return Result.error(30001, "状态更新失败");
            }
        } catch (Exception e) {
            return Result.error(30001, "状态更新失败: " + e.getMessage());
        }
    }

    @PutMapping("/status/{id:\\d+}")
    @Operation(summary = "更新需求状态(RESTful)", description = "根据ID更新需求处理状态")
    public Result<String> updateDemandStatusById(
            @Parameter(description = "需求ID", required = true) @PathVariable Long id,
            @Parameter(description = "新状态", required = true) @RequestParam String status) {
        // 委托给主方法处理，传递null作为请求体（使用请求参数）
        return updateDemandStatus(id, status, null);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "更新需求(RESTful)", description = "根据ID更新需求信息")
    public Result<Demand> updateDemandById(
            @Parameter(description = "需求ID", required = true) @PathVariable Long id,
            @Parameter(description = "需求信息", required = true) @RequestBody Demand demand) {
        // 设置ID，确保使用路径中的ID
        demand.setId(id);
        return updateDemand(demand);
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "删除需求", description = "根据ID删除需求")
    public Result<String> deleteDemand(@Parameter(description = "需求ID", required = true) @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(30002, "需求ID不能为空或无效");
            }
            
            // 检查需求是否存在
            Demand demand = demandService.getById(id);
            if (demand == null) {
                return Result.error(30002, "需求不存在");
            }
            
            boolean success = demandService.removeById(id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error(30001, "删除失败");
            }
        } catch (Exception e) {
            return Result.error(30001, "删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "需求统计", description = "获取需求统计信息")
    public Result<Map<String, Object>> getDemandStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "省份") @RequestParam(required = false) String province,
            @Parameter(description = "城市") @RequestParam(required = false) String city) {
        try {
            Map<String, Object> statistics = demandService.getDemandStatistics(startTime, endTime, province, city);
            return Result.success("统计查询成功", statistics);
        } catch (Exception e) {
            return Result.error(30001, "统计查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/by-type")
    @Operation(summary = "按类型统计需求", description = "按需求类型统计需求数量")
    public Result<List<Map<String, Object>>> getDemandStatisticsByType(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        try {
            List<Map<String, Object>> statistics = demandService.getDemandStatisticsByType(startTime, endTime);
            return Result.success("统计查询成功", statistics);
        } catch (Exception e) {
            return Result.error(30001, "统计查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/by-urgency")
    @Operation(summary = "按紧急程度统计需求", description = "按紧急程度统计需求数量")
    public Result<List<Map<String, Object>>> getDemandStatisticsByUrgency(
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        try {
            List<Map<String, Object>> statistics = demandService.getDemandStatisticsByUrgency(startTime, endTime);
            return Result.success("统计查询成功", statistics);
        } catch (Exception e) {
            return Result.error(30001, "统计查询失败: " + e.getMessage());
        }
    }
}