package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.SystemConfig;
import com.disaster.emergency.mapper.SystemConfigMapper;
import com.disaster.emergency.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = list();
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    public String getConfigValue(String configKey) {
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", configKey);
        SystemConfig config = getOne(queryWrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public boolean updateConfig(String configKey, String configValue, String configDesc) {
        UpdateWrapper<SystemConfig> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("config_key", configKey)
                    .set("config_value", configValue)
                    .set("config_desc", configDesc)
                    .set("update_time", LocalDateTime.now());
        return update(updateWrapper);
    }
}
