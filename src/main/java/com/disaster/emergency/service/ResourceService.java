package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Resource;

public interface ResourceService extends IService<Resource> {
    Resource saveResource(Resource resource);
    boolean updateQuantity(Long resourceId, Integer quantity);
}
