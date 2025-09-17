package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("similarity_config")
public class SimilarityConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dimension;
    private BigDecimal weight;
    private String algorithm;
    private BigDecimal threshold;
    private Integer isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
