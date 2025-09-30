package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Command;
import com.disaster.emergency.service.CommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指挥中心调度指令控制器
 */
@RestController
@RequestMapping("/command")
@CrossOrigin
@Tag(name = "指挥中心调度", description = "指挥中心协调调度指令管理")
public class CommandController {
    
    @Autowired
    private CommandService commandService;
    
    /**
     * 创建调度指令
     */
    @Operation(summary = "创建调度指令", description = "创建新的调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/create")
    public Result<Command> createCommand(@RequestBody Command command) {
        try {
            if (command.getContent() == null || command.getContent().trim().isEmpty()) {
                return Result.error(400, "指令内容不能为空");
            }
            if (command.getCommanderId() == null || command.getCommanderId().trim().isEmpty()) {
                return Result.error(400, "指挥员ID不能为空");
            }
            if (command.getCommanderName() == null || command.getCommanderName().trim().isEmpty()) {
                return Result.error(400, "指挥员姓名不能为空");
            }
            
            Command createdCommand = commandService.createCommand(command);
            return Result.success("指令创建成功", createdCommand);
        } catch (Exception e) {
            return Result.error(500, "指令创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行指令
     */
    @Operation(summary = "执行指令", description = "开始执行指定的调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "执行成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/{commandId}/execute")
    public Result<Map<String, Object>> executeCommand(@PathVariable Long commandId, 
                                                     @RequestBody Map<String, String> request) {
        try {
            String executorId = request.get("executorId");
            String executorName = request.get("executorName");
            
            if (executorId == null || executorId.trim().isEmpty()) {
                return Result.error(400, "执行人ID不能为空");
            }
            if (executorName == null || executorName.trim().isEmpty()) {
                return Result.error(400, "执行人姓名不能为空");
            }
            
            boolean success = commandService.executeCommand(commandId, executorId, executorName);
            if (success) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("commandId", commandId);
                result.put("status", "executing");
                result.put("executorId", executorId);
                result.put("executorName", executorName);
                return Result.success("指令执行成功", result);
            } else {
                return Result.error(400, "指令执行失败，请检查指令状态");
            }
        } catch (Exception e) {
            return Result.error(500, "指令执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成指令
     */
    @Operation(summary = "完成指令", description = "标记指令为已完成")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "完成成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/{commandId}/complete")
    public Result<Map<String, Object>> completeCommand(@PathVariable Long commandId, 
                                                      @RequestBody Map<String, String> request) {
        try {
            String remark = request.get("remark");
            
            boolean success = commandService.completeCommand(commandId, remark);
            if (success) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("commandId", commandId);
                result.put("status", "completed");
                result.put("remark", remark);
                return Result.success("指令完成成功", result);
            } else {
                return Result.error(400, "指令完成失败，请检查指令状态");
            }
        } catch (Exception e) {
            return Result.error(500, "指令完成失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消指令
     */
    @Operation(summary = "取消指令", description = "取消指定的调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/{commandId}/cancel")
    public Result<Map<String, Object>> cancelCommand(@PathVariable Long commandId, 
                                                    @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error(400, "取消原因不能为空");
            }
            
            boolean success = commandService.cancelCommand(commandId, reason);
            if (success) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("commandId", commandId);
                result.put("status", "cancelled");
                result.put("reason", reason);
                return Result.success("指令取消成功", result);
            } else {
                return Result.error(400, "指令取消失败，请检查指令状态");
            }
        } catch (Exception e) {
            return Result.error(500, "指令取消失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指令列表
     */
    @Operation(summary = "获取指令列表", description = "获取调度指令列表，支持按状态、优先级、类型筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/list")
    public Result<List<Command>> getCommandList(@RequestParam(required = false) String status,
                                               @RequestParam(required = false) String priority,
                                               @RequestParam(required = false) String commandType) {
        try {
            List<Command> commands = commandService.getCommandList(status, priority, commandType);
            return Result.success("获取指令列表成功", commands);
        } catch (Exception e) {
            return Result.error(500, "获取指令列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指令详情
     */
    @Operation(summary = "获取指令详情", description = "获取指定指令的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "指令不存在"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/{commandId}")
    public Result<Command> getCommandById(@PathVariable Long commandId) {
        try {
            Command command = commandService.getCommandById(commandId);
            if (command == null) {
                return Result.error(404, "指令不存在");
            }
            return Result.success("获取指令详情成功", command);
        } catch (Exception e) {
            return Result.error(500, "获取指令详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待执行指令
     */
    @Operation(summary = "获取待执行指令", description = "获取所有待执行的调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/pending")
    public Result<List<Command>> getPendingCommands() {
        try {
            List<Command> commands = commandService.getPendingCommands();
            return Result.success("获取待执行指令成功", commands);
        } catch (Exception e) {
            return Result.error(500, "获取待执行指令失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取执行中指令
     */
    @Operation(summary = "获取执行中指令", description = "获取所有执行中的调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/executing")
    public Result<List<Command>> getExecutingCommands() {
        try {
            List<Command> commands = commandService.getExecutingCommands();
            return Result.success("获取执行中指令成功", commands);
        } catch (Exception e) {
            return Result.error(500, "获取执行中指令失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指令统计
     */
    @Operation(summary = "获取指令统计", description = "获取指令相关的统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getCommandStats() {
        try {
            Map<String, Object> stats = commandService.getCommandStats();
            return Result.success("获取指令统计成功", stats);
        } catch (Exception e) {
            return Result.error(500, "获取指令统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 紧急调度
     */
    @Operation(summary = "紧急调度", description = "创建紧急调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/emergency-dispatch")
    public Result<Command> emergencyDispatch(@RequestBody Map<String, String> request) {
        try {
            String targetType = request.get("targetType");
            String targetId = request.get("targetId");
            String content = request.get("content");
            String commanderId = request.get("commanderId");
            String commanderName = request.get("commanderName");
            
            if (targetType == null || targetType.trim().isEmpty()) {
                return Result.error(400, "目标类型不能为空");
            }
            if (targetId == null || targetId.trim().isEmpty()) {
                return Result.error(400, "目标ID不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return Result.error(400, "指令内容不能为空");
            }
            if (commanderId == null || commanderId.trim().isEmpty()) {
                return Result.error(400, "指挥员ID不能为空");
            }
            if (commanderName == null || commanderName.trim().isEmpty()) {
                return Result.error(400, "指挥员姓名不能为空");
            }
            
            Command command = commandService.emergencyDispatch(targetType, targetId, content, commanderId, commanderName);
            return Result.success("紧急调度指令创建成功", command);
        } catch (Exception e) {
            return Result.error(500, "紧急调度失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量调度
     */
    @Operation(summary = "批量调度", description = "创建批量调度指令")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @PostMapping("/batch-dispatch")
    public Result<List<Command>> batchDispatch(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dispatchList = (List<Map<String, Object>>) request.get("dispatchList");
            String commanderId = (String) request.get("commanderId");
            String commanderName = (String) request.get("commanderName");
            
            if (dispatchList == null || dispatchList.isEmpty()) {
                return Result.error(400, "调度列表不能为空");
            }
            if (commanderId == null || commanderId.trim().isEmpty()) {
                return Result.error(400, "指挥员ID不能为空");
            }
            if (commanderName == null || commanderName.trim().isEmpty()) {
                return Result.error(400, "指挥员姓名不能为空");
            }
            
            List<Command> commands = commandService.batchDispatch(dispatchList, commanderId, commanderName);
            return Result.success("批量调度指令创建成功", commands);
        } catch (Exception e) {
            return Result.error(500, "批量调度失败: " + e.getMessage());
        }
    }
}
