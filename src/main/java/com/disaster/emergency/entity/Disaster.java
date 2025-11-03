package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("disaster")
public class Disaster {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String disasterType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime occurTime;
    
    private String province;
    private String city;
    private String district;
    private Double latitude;
    private Double longitude;
    private String severity;
    private String description;
    private String originalText;
    private String parsedData;
    
    @TableField("reporter_name")
    private String reporterName;
    
    @TableField("reporter_phone")
    private String reporterPhone;
    // 暂时注释掉不存在的字段
    // private Long reporterId;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
