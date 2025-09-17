package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("scheduling_record")
public class SchedulingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long resourceId;
    private Integer allocatedQuantity;
    private Long schedulerId;
    private String schedulerName;
    private LocalDateTime schedulingTime;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
