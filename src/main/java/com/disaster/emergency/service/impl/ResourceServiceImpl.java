package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.service.ResourceService;
import org.springframework.stereotype.Service;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Override
    public Resource saveResource(Resource resource) {
        resource.setStatus("available");
        resource.setCreateTime(java.time.LocalDateTime.now());
        resource.setUpdateTime(java.time.LocalDateTime.now());
        save(resource);
        return resource;
    }

    @Override
    public boolean updateQuantity(Long resourceId, Integer quantity) {
        UpdateWrapper<Resource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", resourceId)
                    .set("available_quantity", quantity)
                    .set("update_time", java.time.LocalDateTime.now());
        return update(updateWrapper);
    }
}
