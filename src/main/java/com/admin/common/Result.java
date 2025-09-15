package com.admin.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * 
 * @author admin
 * @date 2024
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String msg) {
        this();
        this.code = code;
        this.msg = msg;
    }

    public Result(Integer code, String msg, T data) {
        this(code, msg);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功");
    }

    /**
     * 成功响应带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "操作失败");
    }

    /**
     * 失败响应带消息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg);
    }

    /**
     * 失败响应带码和消息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg);
    }
}
