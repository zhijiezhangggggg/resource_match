package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
@CrossOrigin
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/get")
    public Result<Map<String, String>> getSystemConfig(@RequestParam(required = false) String configKey) {
        if (configKey != null && !configKey.isEmpty()) {
            String value = systemConfigService.getConfigValue(configKey);
            Map<String, String> result = new HashMap<>();
            result.put(configKey, value != null ? value : "");
            return Result.success("查询成功", result);
        } else {
            Map<String, String> allConfigs = systemConfigService.getAllConfigs();
            return Result.success("查询成功", allConfigs);
        }
    }

    @PutMapping("/update")
    public Result<Map<String, Object>> updateSystemConfig(@RequestBody Map<String, String> request) {
        String configKey = request.get("configKey");
        String configValue = request.get("configValue");
        String configDesc = request.get("configDesc");
        
        boolean success = systemConfigService.updateConfig(configKey, configValue, configDesc);
        if (!success) {
            return Result.error(500, "配置更新失败");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("configKey", configKey);
        result.put("configValue", configValue);
        result.put("updateTime", java.time.LocalDateTime.now());
        
        return Result.success("配置更新成功", result);
    }
}
