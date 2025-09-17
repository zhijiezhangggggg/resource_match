package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_relation")
public class KnowledgeRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sourceNodeId;
    private Long targetNodeId;
    private String relationType;
    private BigDecimal weight;
    private String properties;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
