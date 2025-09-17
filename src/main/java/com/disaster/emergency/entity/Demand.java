package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("demand")
public class Demand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long disasterId;
    private String demandType;
    private Integer quantity;
    private String unit;
    private String urgency;
    private String province;
    private String city;
    private String district;
    private String description;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
