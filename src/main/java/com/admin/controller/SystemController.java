package com.admin.controller;

import com.disaster.emergency.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统控制器
 * 
 * @author admin
 * @date 2024
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("系统运行正常");
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public Result<String> info() {
        return Result.success("后台管理系统 v1.0.0");
    }
}
