package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.MatchingRecord;
import com.disaster.emergency.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matching")
@CrossOrigin
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    @PostMapping("/search")
    public Result<Map<String, Object>> searchMatching(@RequestBody Map<String, Object> request) {
        try {
            // 参数验证
            Long demandId = null;
            if (request.get("demandId") != null) {
                demandId = Long.valueOf(request.get("demandId").toString());
            }
            String demandType = (String) request.get("demandType");
            Integer quantity = (Integer) request.get("quantity");
            String province = (String) request.get("province");
            String city = (String) request.get("city");
            String urgency = (String) request.get("urgency");
            
            if (demandId == null || demandId <= 0) {
                return Result.error(50001, "需求ID不能为空或无效");
            }
            if (demandType == null || demandType.trim().isEmpty()) {
                return Result.error(50001, "需求类型不能为空");
            }
            if (quantity == null || quantity <= 0) {
                return Result.error(50001, "需求数量必须大于0");
            }
            if (province == null || province.trim().isEmpty()) {
                return Result.error(50001, "省份不能为空");
            }
            if (city == null || city.trim().isEmpty()) {
                return Result.error(50001, "城市不能为空");
            }
            if (urgency == null || urgency.trim().isEmpty()) {
                return Result.error(50001, "紧急程度不能为空");
            }
            
            List<MatchingRecord> matches = matchingService.matchResources(demandId, demandType, quantity, province, city, urgency);
            
            Map<String, Object> result = new HashMap<>();
            result.put("demandId", demandId);
            result.put("totalMatches", matches.size());
            result.put("matches", matches);
            
            return Result.success("匹配成功", result);
        } catch (Exception e) {
            return Result.error(50001, "匹配失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public Result<Map<String, Object>> getMatchingRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long demandId,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) String status) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 15);
        result.put("pages", 2);
        result.put("current", page);
        result.put("size", size);
        result.put("records", matchingService.list());
        
        return Result.success("查询成功", result);
    }

    @PutMapping("/{id}/confirm")
    public Result<Map<String, Object>> confirmMatching(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (id == null || id <= 0) {
            return Result.error(50002, "匹配记录ID无效");
        }
        
        String status = request.get("status");
        String remark = request.get("remark");
        
        if (status == null || status.trim().isEmpty()) {
            return Result.error(50001, "状态不能为空");
        }
        
        boolean success = matchingService.confirmMatching(id, status);
        if (!success) {
            return Result.error(50002, "匹配记录不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", status);
        result.put("updateTime", java.time.LocalDateTime.now());
        
        return Result.success("确认成功", result);
    }
}