package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resourceType;
    private String resourceName;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String unit;
    private String province;
    private String city;
    private String district;
    private String warehouseName;
    private String contactPerson;
    private String contactPhone;
    private Long organizationId;
    private Integer priorityLevel;
    private String similarityCache;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
