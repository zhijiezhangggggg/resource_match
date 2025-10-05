package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@TableName("resource_allocation")
@Schema(description = "资源分配记录实体")
public class ResourceAllocation {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "分配记录ID")
    private Long id;
    
    @NotNull(message = "资源ID不能为空")
    @Schema(description = "资源ID", example = "1")
    private Long resourceId;
    
    @NotNull(message = "需求ID不能为空")
    @Schema(description = "需求ID", example = "1")
    private Long demandId;
    
    @NotNull(message = "分配数量不能为空")
    @Min(value = 1, message = "分配数量必须大于0")
    @Schema(description = "分配数量", example = "10")
    private Integer allocatedQuantity;
    
    @NotBlank(message = "分配原因不能为空")
    @Size(max = 200, message = "分配原因不能超过200个字符")
    @Schema(description = "分配原因", example = "紧急救灾需要")
    private String allocationReason;
    
    @NotNull(message = "预计到达时间不能为空")
    @Schema(description = "预计到达时间", example = "2025-10-06 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrivalTime;
    
    @Schema(description = "分配状态", example = "allocated")
    private String status;
    
    @Schema(description = "分配人", example = "张三")
    private String allocator;
    
    @Schema(description = "分配时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime allocationTime;
    
    @Schema(description = "备注", example = "优先配送")
    private String remarks;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
