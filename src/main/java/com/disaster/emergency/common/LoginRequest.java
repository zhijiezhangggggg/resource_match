package com.disaster.emergency.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录请求类
 * 
 * @author 系统管理员
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
    @Schema(description = "用户名", example = "admin", required = true)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "密码", example = "admin123", required = true)
    private String password;
}