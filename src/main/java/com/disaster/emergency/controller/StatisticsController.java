package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/disaster")
    public Result<Map<String, Object>> getDisasterStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
        
        Map<String, Object> statistics = statisticsService.getDisasterStatistics(startTime, endTime, province, city);
        return Result.success("查询成功", statistics);
    }

    @GetMapping("/resource")
    public Result<Map<String, Object>> getResourceStatistics() {
        Map<String, Object> statistics = statisticsService.getResourceStatistics();
        return Result.success("查询成功", statistics);
    }

    @GetMapping("/demand")
    public Result<Map<String, Object>> getDemandStatistics() {
        Map<String, Object> statistics = statisticsService.getDemandStatistics();
        return Result.success("查询成功", statistics);
    }
}
