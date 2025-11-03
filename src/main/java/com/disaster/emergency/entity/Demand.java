package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Double latitude;
    private Double longitude;
    private String description;
    private String status;
    
    @TableField(exist = false)
    private String submitterName; // 提交人姓名（从关联的disaster表获取）
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
