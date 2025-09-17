package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("similarity_result")
public class SimilarityResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long resourceId;
    private BigDecimal totalScore;
    private String dimensionScores;
    private String matchReason;
    private LocalDateTime calculationTime;
    private LocalDateTime createTime;
}
