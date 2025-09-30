package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Command;
import com.disaster.emergency.mapper.CommandMapper;
import com.disaster.emergency.service.CommandService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指挥中心调度指令服务实现类
 */
@Service
public class CommandServiceImpl extends ServiceImpl<CommandMapper, Command> implements CommandService {
    
    @Override
    public Command createCommand(Command command) {
        command.setStatus("pending");
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        this.save(command);
        return command;
    }
    
    @Override
    public boolean executeCommand(Long commandId, String executorId, String executorName) {
        Command command = this.getById(commandId);
        if (command == null || !"pending".equals(command.getStatus())) {
            return false;
        }
        
        command.setStatus("executing");
        command.setExecutorId(executorId);
        command.setExecutorName(executorName);
        command.setExecuteTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(command);
    }
    
    @Override
    public boolean completeCommand(Long commandId, String remark) {
        Command command = this.getById(commandId);
        if (command == null || !"executing".equals(command.getStatus())) {
            return false;
        }
        
        command.setStatus("completed");
        command.setRemark(remark);
        command.setCompleteTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(command);
    }
    
    @Override
    public boolean cancelCommand(Long commandId, String reason) {
        Command command = this.getById(commandId);
        if (command == null || "completed".equals(command.getStatus()) || "cancelled".equals(command.getStatus())) {
            return false;
        }
        
        command.setStatus("cancelled");
        command.setCancelReason(reason);
        command.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(command);
    }
    
    @Override
    public List<Command> getCommandList(String status, String priority, String commandType) {
        QueryWrapper<Command> queryWrapper = new QueryWrapper<>();
        
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (priority != null && !priority.trim().isEmpty()) {
            queryWrapper.eq("priority", priority);
        }
        if (commandType != null && !commandType.trim().isEmpty()) {
            queryWrapper.eq("command_type", commandType);
        }
        
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper);
    }
    
    @Override
    public Command getCommandById(Long commandId) {
        return this.getById(commandId);
    }
    
    @Override
    public List<Command> getPendingCommands() {
        QueryWrapper<Command> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending");
        queryWrapper.orderByDesc("create_time");
        return this.list(queryWrapper);
    }
    
    @Override
    public List<Command> getExecutingCommands() {
        QueryWrapper<Command> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "executing");
        queryWrapper.orderByDesc("execute_time");
        return this.list(queryWrapper);
    }
    
    @Override
    public Map<String, Object> getCommandStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总指令数
        long totalCount = this.count();
        stats.put("totalCount", totalCount);
        
        // 待执行指令数
        QueryWrapper<Command> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("status", "pending");
        long pendingCount = this.count(pendingWrapper);
        stats.put("pendingCount", pendingCount);
        
        // 执行中指令数
        QueryWrapper<Command> executingWrapper = new QueryWrapper<>();
        executingWrapper.eq("status", "executing");
        long executingCount = this.count(executingWrapper);
        stats.put("executingCount", executingCount);
        
        // 已完成指令数
        QueryWrapper<Command> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("status", "completed");
        long completedCount = this.count(completedWrapper);
        stats.put("completedCount", completedCount);
        
        // 已取消指令数
        QueryWrapper<Command> cancelledWrapper = new QueryWrapper<>();
        cancelledWrapper.eq("status", "cancelled");
        long cancelledCount = this.count(cancelledWrapper);
        stats.put("cancelledCount", cancelledCount);
        
        // 完成率
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return stats;
    }
    
    @Override
    public Command emergencyDispatch(String targetType, String targetId, String content, 
                                   String commanderId, String commanderName) {
        Command command = new Command();
        command.setContent(content);
        command.setCommanderId(commanderId);
        command.setCommanderName(commanderName);
        command.setTargetType(targetType);
        command.setTargetId(targetId);
        command.setCommandType("emergency");
        command.setPriority("high");
        command.setStatus("pending");
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        
        this.save(command);
        return command;
    }
    
    @Override
    public List<Command> batchDispatch(List<Map<String, Object>> dispatchList, 
                                     String commanderId, String commanderName) {
        List<Command> commands = new java.util.ArrayList<>();
        
        for (Map<String, Object> dispatch : dispatchList) {
            Command command = new Command();
            command.setContent((String) dispatch.get("content"));
            command.setCommanderId(commanderId);
            command.setCommanderName(commanderName);
            command.setTargetType((String) dispatch.get("targetType"));
            command.setTargetId((String) dispatch.get("targetId"));
            command.setCommandType("batch");
            command.setPriority((String) dispatch.getOrDefault("priority", "medium"));
            command.setStatus("pending");
            command.setCreateTime(LocalDateTime.now());
            command.setUpdateTime(LocalDateTime.now());
            
            this.save(command);
            commands.add(command);
        }
        
        return commands;
    }
}
