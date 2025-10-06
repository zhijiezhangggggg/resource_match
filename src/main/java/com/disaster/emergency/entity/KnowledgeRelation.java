package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonRawValue;
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
    
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    @JsonRawValue
    private String properties;
    
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
