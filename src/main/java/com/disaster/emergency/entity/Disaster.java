package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("disaster")
public class Disaster {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String disasterType;
    private LocalDateTime occurTime;
    private String province;
    private String city;
    private String district;
    private String severity;
    private String description;
    private String originalText;
    private String parsedData;
    private String reporterName;
    private String reporterPhone;
    private Long reporterId;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
