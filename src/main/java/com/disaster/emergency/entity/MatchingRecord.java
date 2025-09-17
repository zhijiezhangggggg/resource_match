package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("matching_record")
public class MatchingRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long resourceId;
    private BigDecimal matchScore;
    private String matchReason;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
