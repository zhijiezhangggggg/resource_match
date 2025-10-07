package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.service.DataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/data")
@CrossOrigin
@Tag(name = "综合数据接口", description = "获取资源、需求和灾情的所有数据信息")
public class DataController {

    @Autowired
    private DataService dataService;

    @Operation(summary = "获取所有数据", description = "返回资源、需求和灾情的所有数据信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "数据获取成功"),
            @ApiResponse(responseCode = "40001", description = "数据获取失败")
    })
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllData() {
        try {
            Map<String, Object> result = dataService.getAllData();
            return Result.success("数据获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "数据获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取分页数据", description = "分页返回资源、需求和灾情数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "数据获取成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @GetMapping("/all/paginated")
    public Result<Map<String, Object>> getAllDataPaginated(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 参数验证
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            
            Map<String, Object> result = dataService.getAllDataPaginated(page, size);
            return Result.success("分页数据获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "分页数据获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取数据统计信息", description = "返回资源、需求和灾情的统计信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "统计信息获取成功"),
            @ApiResponse(responseCode = "40001", description = "统计信息获取失败")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDataStatistics() {
        try {
            Map<String, Object> result = dataService.getDataStatistics();
            return Result.success("统计信息获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "统计信息获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按地区获取数据", description = "根据地区筛选返回资源、需求和灾情数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "数据获取成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @GetMapping("/by-region")
    public Result<Map<String, Object>> getDataByRegion(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district) {
        try {
            Map<String, Object> result = dataService.getDataByRegion(province, city, district);
            return Result.success("地区数据获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "地区数据获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取最新数据", description = "返回最近创建的资源、需求和灾情数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "最新数据获取成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @GetMapping("/latest")
    public Result<Map<String, Object>> getLatestData(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            // 参数验证
            if (limit < 1 || limit > 100) limit = 10;
            
            Map<String, Object> result = dataService.getLatestData(limit);
            return Result.success("最新数据获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "最新数据获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "按状态获取数据", description = "根据状态筛选返回资源、需求和灾情数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "数据获取成功"),
            @ApiResponse(responseCode = "40001", description = "参数错误")
    })
    @GetMapping("/by-status")
    public Result<Map<String, Object>> getDataByStatus(
            @RequestParam(required = false) String resourceStatus,
            @RequestParam(required = false) String demandStatus,
            @RequestParam(required = false) String disasterStatus) {
        try {
            Map<String, Object> result = dataService.getDataByStatus(resourceStatus, demandStatus, disasterStatus);
            return Result.success("状态数据获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "状态数据获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取数据概览", description = "返回系统数据概览信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "概览信息获取成功"),
            @ApiResponse(responseCode = "40001", description = "概览信息获取失败")
    })
    @GetMapping("/overview")
    public Result<Map<String, Object>> getDataOverview() {
        try {
            Map<String, Object> result = dataService.getDataOverview();
            return Result.success("概览信息获取成功", result);
        } catch (Exception e) {
            return Result.error(40001, "概览信息获取失败: " + e.getMessage());
        }
    }
}
