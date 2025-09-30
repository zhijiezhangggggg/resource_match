package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 大屏数据接口控制器
 * 
 * <p>提供大屏展示所需的各种数据接口，包括实时匹配状态、资源分布、调度指令等。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
@Tag(name = "大屏数据接口", description = "大屏展示数据接口，包括实时状态、资源分布、调度控制等")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取实时匹配状态概览
     * 
     * @return 匹配状态统计数据
     */
    @Operation(summary = "获取实时匹配状态概览", description = "获取当前匹配状态的整体统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/matching-overview")
    public Result<Map<String, Object>> getMatchingOverview() {
        try {
            Map<String, Object> overview = dashboardService.getMatchingOverview();
            return Result.success("获取匹配状态概览成功", overview);
        } catch (Exception e) {
            return Result.error(500, "获取匹配状态概览失败: " + e.getMessage());
        }
    }

    /**
     * 获取资源分布数据
     * 
     * @return 资源分布统计信息
     */
    @Operation(summary = "获取资源分布数据", description = "获取按地区、类型等维度的资源分布统计")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/resource-distribution")
    public Result<Map<String, Object>> getResourceDistribution() {
        try {
            Map<String, Object> distribution = dashboardService.getResourceDistribution();
            return Result.success("获取资源分布数据成功", distribution);
        } catch (Exception e) {
            return Result.error(500, "获取资源分布数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时调度状态
     * 
     * @return 调度状态和指令信息
     */
    @Operation(summary = "获取实时调度状态", description = "获取当前调度状态和待处理指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/scheduling-status")
    public Result<Map<String, Object>> getSchedulingStatus() {
        try {
            Map<String, Object> status = dashboardService.getSchedulingStatus();
            return Result.success("获取调度状态成功", status);
        } catch (Exception e) {
            return Result.error(500, "获取调度状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时匹配进度
     * 
     * @return 当前匹配进度信息
     */
    @Operation(summary = "获取实时匹配进度", description = "获取当前正在进行的匹配任务进度")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/matching-progress")
    public Result<Map<String, Object>> getMatchingProgress() {
        try {
            Map<String, Object> progress = dashboardService.getMatchingProgress();
            return Result.success("获取匹配进度成功", progress);
        } catch (Exception e) {
            return Result.error(500, "获取匹配进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取地图数据
     * 
     * @return 地图展示所需的数据
     */
    @Operation(summary = "获取地图数据", description = "获取地图展示所需的资源、需求、灾情等位置数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/map-data")
    public Result<Map<String, Object>> getMapData() {
        try {
            Map<String, Object> mapData = dashboardService.getMapData();
            return Result.success("获取地图数据成功", mapData);
        } catch (Exception e) {
            return Result.error(500, "获取地图数据失败: " + e.getMessage());
        }
    }

    /**
     * 修改调度指令
     * 
     * @param request 调度指令修改请求
     * @return 修改结果
     */
    @Operation(summary = "修改调度指令", description = "指挥中心修改调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/modify-scheduling")
    public Result<Map<String, Object>> modifyScheduling(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = dashboardService.modifyScheduling(request);
            return Result.success("调度指令修改成功", result);
        } catch (Exception e) {
            return Result.error(500, "调度指令修改失败: " + e.getMessage());
        }
    }

    /**
     * 确认调度指令
     * 
     * @param request 确认请求
     * @return 确认结果
     */
    @Operation(summary = "确认调度指令", description = "确认执行调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "确认成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/confirm-scheduling")
    public Result<Map<String, Object>> confirmScheduling(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = dashboardService.confirmScheduling(request);
            return Result.success("调度指令确认成功", result);
        } catch (Exception e) {
            return Result.error(500, "调度指令确认失败: " + e.getMessage());
        }
    }

    /**
     * 取消调度指令
     * 
     * @param request 取消请求
     * @return 取消结果
     */
    @Operation(summary = "取消调度指令", description = "取消调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/cancel-scheduling")
    public Result<Map<String, Object>> cancelScheduling(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = dashboardService.cancelScheduling(request);
            return Result.success("调度指令取消成功", result);
        } catch (Exception e) {
            return Result.error(500, "调度指令取消失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时告警信息
     * 
     * @return 告警信息列表
     */
    @Operation(summary = "获取实时告警信息", description = "获取系统告警和异常信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/alerts")
    public Result<Map<String, Object>> getAlerts() {
        try {
            Map<String, Object> alerts = dashboardService.getAlerts();
            return Result.success("获取告警信息成功", alerts);
        } catch (Exception e) {
            return Result.error(500, "获取告警信息失败: " + e.getMessage());
        }
    }
}
