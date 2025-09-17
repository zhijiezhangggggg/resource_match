package com.disaster.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_node")
public class KnowledgeNode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String nodeType;
    private Long businessId;
    private String nodeName;
    private String properties;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
