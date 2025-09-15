package com.admin.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数
 * 
 * @author admin
 * @date 2024
 */
@Data
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式（asc/desc）
     */
    private String orderType = "desc";
}
