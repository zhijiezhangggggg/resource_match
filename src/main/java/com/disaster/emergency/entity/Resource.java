package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@TableName("resource")
@Schema(description = "资源信息实体")
public class Resource {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "资源ID")
    private Long id;
    
    @NotBlank(message = "资源类型不能为空")
    @Schema(description = "资源类型", example = "医疗设备")
    private String resourceType;
    
    @NotBlank(message = "资源名称不能为空")
    @Schema(description = "资源名称", example = "呼吸机")
    private String resourceName;
    
    @NotNull(message = "总数量不能为空")
    @Min(value = 0, message = "总数量不能为负数")
    @Schema(description = "总数量", example = "100")
    private Integer totalQuantity;
    
    @NotNull(message = "可用数量不能为空")
    @Min(value = 0, message = "可用数量不能为负数")
    @Schema(description = "可用数量", example = "80")
    private Integer availableQuantity;
    
    @NotBlank(message = "单位不能为空")
    @Schema(description = "单位", example = "台")
    private String unit;
    
    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份", example = "北京市")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", example = "北京市")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    @Schema(description = "区县", example = "朝阳区")
    private String district;
    
    @DecimalMin(value = "-90.0", message = "纬度不能小于-90度")
    @DecimalMax(value = "90.0", message = "纬度不能大于90度")
    @Schema(description = "纬度", example = "39.9042")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "经度不能小于-180度")
    @DecimalMax(value = "180.0", message = "经度不能大于180度")
    @Schema(description = "经度", example = "116.4074")
    private Double longitude;
    
    @Schema(description = "仓库名称", example = "中央仓库")
    private String warehouseName;
    
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;
    
    @Schema(description = "所属组织ID", example = "1")
    private Long organizationId;
    
    @Min(value = 1, message = "优先级不能小于1")
    @Max(value = 5, message = "优先级不能大于5")
    @Schema(description = "优先级等级(1-5)", example = "3")
    private Integer priorityLevel;
    
    @TableField(exist = false)
    @Schema(description = "相似度缓存")
    private String similarityCache;
    
    @Schema(description = "资源状态", example = "available")
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
