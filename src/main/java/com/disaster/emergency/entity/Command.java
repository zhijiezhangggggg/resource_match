package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指挥中心调度指令实体
 */
@Data
@TableName("command")
public class Command {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 指令内容
     */
    private String content;
    
    /**
     * 指挥员ID
     */
    private String commanderId;
    
    /**
     * 指挥员姓名
     */
    private String commanderName;
    
    /**
     * 执行人ID
     */
    private String executorId;
    
    /**
     * 执行人姓名
     */
    private String executorName;
    
    /**
     * 指令状态：pending-待执行，executing-执行中，completed-已完成，cancelled-已取消
     */
    private String status;
    
    /**
     * 优先级：high-高，medium-中，low-低
     */
    private String priority;
    
    /**
     * 指令类型：emergency-紧急调度，normal-常规调度，batch-批量调度
     */
    private String commandType;
    
    /**
     * 目标类型：resource-资源，organization-组织，disaster-灾害
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private String targetId;
    
    /**
     * 执行备注
     */
    private String remark;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 执行开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime executeTime;
    
    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime completeTime;
}
