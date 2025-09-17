package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.SchedulingRecord;
import com.disaster.emergency.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/scheduling")
@CrossOrigin
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createScheduling(@RequestBody Map<String, Object> request) {
        try {
            // 参数验证
            Long demandId = null;
            if (request.get("demandId") != null) {
                demandId = Long.valueOf(request.get("demandId").toString());
            }
            Long resourceId = null;
            if (request.get("resourceIds") != null) {
                String resourceIdsStr = request.get("resourceIds").toString().replaceAll("[\\[\\]]", "");
                resourceId = Long.valueOf(resourceIdsStr);
            }
            Integer allocatedQuantity = (Integer) request.get("allocatedQuantities");
            Long schedulerId = null;
            if (request.get("schedulerId") != null) {
                schedulerId = Long.valueOf(request.get("schedulerId").toString());
            }
            String schedulerName = (String) request.get("schedulerName");
            String remark = (String) request.get("remark");
            
            if (demandId == null || demandId <= 0) {
                return Result.error(60001, "需求ID不能为空或无效");
            }
            if (resourceId == null || resourceId <= 0) {
                return Result.error(60001, "资源ID不能为空或无效");
            }
            if (allocatedQuantity == null || allocatedQuantity <= 0) {
                return Result.error(60001, "分配数量必须大于0");
            }
            if (schedulerId == null || schedulerId <= 0) {
                return Result.error(60001, "调度员ID不能为空或无效");
            }
            if (schedulerName == null || schedulerName.trim().isEmpty()) {
                return Result.error(60001, "调度员姓名不能为空");
            }
            
            SchedulingRecord record = schedulingService.createScheduling(demandId, resourceId, allocatedQuantity, schedulerId, schedulerName, remark);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("demandId", demandId);
            result.put("totalAllocatedQuantity", allocatedQuantity);
            result.put("schedulerName", schedulerName);
            result.put("schedulingTime", record.getSchedulingTime());
            result.put("status", record.getStatus());
            
            return Result.success("调度方案创建成功", result);
        } catch (Exception e) {
            return Result.error(60001, "调度方案创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> getSchedulingRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long demandId,
            @RequestParam(required = false) Long schedulerId,
            @RequestParam(required = false) String status) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 25);
        result.put("pages", 3);
        result.put("current", page);
        result.put("size", size);
        result.put("records", schedulingService.list());
        
        return Result.success("查询成功", result);
    }

    @PutMapping("/{id}/status")
    public Result<Map<String, Object>> updateSchedulingStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (id == null || id <= 0) {
            return Result.error(60002, "调度记录ID无效");
        }
        
        String status = request.get("status");
        String remark = request.get("remark");
        
        if (status == null || status.trim().isEmpty()) {
            return Result.error(60001, "状态不能为空");
        }
        
        boolean success = schedulingService.updateStatus(id, status);
        if (!success) {
            return Result.error(60002, "调度记录不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", status);
        result.put("updateTime", java.time.LocalDateTime.now());
        
        return Result.success("状态更新成功", result);
    }

    @PutMapping("/{id}/complete")
    public Result<Map<String, Object>> completeScheduling(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (id == null || id <= 0) {
            return Result.error(60002, "调度记录ID无效");
        }
        
        String remark = request.get("remark");
        
        boolean success = schedulingService.completeScheduling(id, remark);
        if (!success) {
            return Result.error(60002, "调度记录不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", "completed");
        result.put("actualDeliveryTime", java.time.LocalDateTime.now());
        result.put("updateTime", java.time.LocalDateTime.now());
        
        return Result.success("调度完成", result);
    }
}