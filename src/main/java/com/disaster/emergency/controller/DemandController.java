package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.service.DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/demand")
@CrossOrigin
public class DemandController {

    @Autowired
    private DemandService demandService;

    @PostMapping("/submit")
    public Result<Demand> submitDemand(@RequestBody Demand demand) {
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
            
            Demand submittedDemand = demandService.submitDemand(demand);
            return Result.success("需求提交成功", submittedDemand);
        } catch (Exception e) {
            return Result.error(30001, "需求提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getDemandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long disasterId,
            @RequestParam(required = false) String demandType,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 30);
        result.put("pages", 3);
        result.put("current", page);
        result.put("size", size);
        result.put("records", demandService.list());
        
        return Result.success("查询成功", result);
    }

    @GetMapping("/{id}")
    public Result<Demand> getDemandDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(30002, "需求ID无效");
        }
        
        Demand demand = demandService.getById(id);
        if (demand == null) {
            return Result.error(30002, "需求ID不存在");
        }
        return Result.success("查询成功", demand);
    }
}