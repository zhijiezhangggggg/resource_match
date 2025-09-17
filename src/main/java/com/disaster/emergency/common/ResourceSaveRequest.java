package com.disaster.emergency.common;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Data
public class ResourceSaveRequest {
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;
    
    @NotBlank(message = "资源名称不能为空")
    private String resourceName;
    
    @NotNull(message = "总数量不能为空")
    @Min(value = 0, message = "总数量不能为负数")
    private Integer totalQuantity;
    
    @NotNull(message = "可用数量不能为空")
    @Min(value = 0, message = "可用数量不能为负数")
    private Integer availableQuantity;
    
    @NotBlank(message = "单位不能为空")
    private String unit;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    private String district;
    
    private String warehouseName;
    private String contactPerson;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
    
    private Long organizationId;
}
