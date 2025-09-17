package com.disaster.emergency.common;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class DisasterReportRequest {
    @NotBlank(message = "灾害类型不能为空")
    private String disasterType;
    
    @NotNull(message = "发生时间不能为空")
    private String occurTime;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    private String district;
    
    @NotBlank(message = "严重程度不能为空")
    private String severity;
    
    private String description;
    
    private String reporterName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String reporterPhone;
}
