package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.SystemConfig;

import java.util.Map;

public interface SystemConfigService extends IService<SystemConfig> {
    Map<String, String> getAllConfigs();
    String getConfigValue(String configKey);
    boolean updateConfig(String configKey, String configValue, String configDesc);
}
