package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.Command;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 指挥中心调度指令数据访问层
 */
@Mapper
public interface CommandMapper extends BaseMapper<Command> {
    
    /**
     * 根据条件查询指令列表
     * @param status 状态
     * @param priority 优先级
     * @param commandType 指令类型
     * @return 指令列表
     */
    List<Command> selectCommandList(@Param("status") String status, 
                                   @Param("priority") String priority, 
                                   @Param("commandType") String commandType);
    
    /**
     * 获取待执行指令
     * @return 待执行指令列表
     */
    List<Command> selectPendingCommands();
    
    /**
     * 获取执行中指令
     * @return 执行中指令列表
     */
    List<Command> selectExecutingCommands();
    
    /**
     * 获取指令统计信息
     * @return 统计数据
     */
    Map<String, Object> selectCommandStats();
}
