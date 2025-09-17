package com.disaster.emergency.common;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Data
public class DemandSubmitRequest {
    @NotNull(message = "灾情ID不能为空")
    private Long disasterId;
    
    @NotBlank(message = "需求类型不能为空")
    private String demandType;
    
    @NotNull(message = "需求数量不能为空")
    @Min(value = 1, message = "需求数量必须大于0")
    private Integer quantity;
    
    @NotBlank(message = "单位不能为空")
    private String unit;
    
    @NotBlank(message = "紧急程度不能为空")
    private String urgency;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    private String district;
    
    private String description;
}
