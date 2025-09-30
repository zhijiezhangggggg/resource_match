package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Command;

import java.util.List;
import java.util.Map;

/**
 * 指挥中心调度指令服务接口
 */
public interface CommandService extends IService<Command> {
    
    /**
     * 创建调度指令
     * @param command 指令对象
     * @return 创建的指令
     */
    Command createCommand(Command command);
    
    /**
     * 执行指令
     * @param commandId 指令ID
     * @param executorId 执行人ID
     * @param executorName 执行人姓名
     * @return 是否执行成功
     */
    boolean executeCommand(Long commandId, String executorId, String executorName);
    
    /**
     * 完成指令
     * @param commandId 指令ID
     * @param remark 完成备注
     * @return 是否完成成功
     */
    boolean completeCommand(Long commandId, String remark);
    
    /**
     * 取消指令
     * @param commandId 指令ID
     * @param reason 取消原因
     * @return 是否取消成功
     */
    boolean cancelCommand(Long commandId, String reason);
    
    /**
     * 获取指令列表
     * @param status 状态筛选
     * @param priority 优先级筛选
     * @param commandType 指令类型筛选
     * @return 指令列表
     */
    List<Command> getCommandList(String status, String priority, String commandType);
    
    /**
     * 根据ID获取指令
     * @param commandId 指令ID
     * @return 指令对象
     */
    Command getCommandById(Long commandId);
    
    /**
     * 获取待执行指令
     * @return 待执行指令列表
     */
    List<Command> getPendingCommands();
    
    /**
     * 获取执行中指令
     * @return 执行中指令列表
     */
    List<Command> getExecutingCommands();
    
    /**
     * 获取指令统计
     * @return 统计数据
     */
    Map<String, Object> getCommandStats();
    
    /**
     * 紧急调度
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param content 指令内容
     * @param commanderId 指挥员ID
     * @param commanderName 指挥员姓名
     * @return 创建的指令
     */
    Command emergencyDispatch(String targetType, String targetId, String content, 
                             String commanderId, String commanderName);
    
    /**
     * 批量调度
     * @param dispatchList 调度列表
     * @param commanderId 指挥员ID
     * @param commanderName 指挥员姓名
     * @return 创建的指令列表
     */
    List<Command> batchDispatch(List<Map<String, Object>> dispatchList, 
                               String commanderId, String commanderName);
}
