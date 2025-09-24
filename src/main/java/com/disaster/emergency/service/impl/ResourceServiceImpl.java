package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Page<Resource> getResourceList(Integer page, Integer size, String resourceType, String status, 
                                         String province, String city, String district, Long organizationId, String resourceName) {
        Page<Resource> pageParam = new Page<>(page, size);
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.hasText(resourceType)) {
            queryWrapper.eq("resource_type", resourceType);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(province)) {
            queryWrapper.eq("province", province);
        }
        if (StringUtils.hasText(city)) {
            queryWrapper.eq("city", city);
        }
        if (StringUtils.hasText(district)) {
            queryWrapper.eq("district", district);
        }
        if (organizationId != null) {
            queryWrapper.eq("organization_id", organizationId);
        }
        if (StringUtils.hasText(resourceName)) {
            queryWrapper.like("resource_name", resourceName);
        }
        
        // 按更新时间倒序排列
        queryWrapper.orderByDesc("update_time");
        
        return page(pageParam, queryWrapper);
    }

    @Override
    public Map<String, Object> getResourceStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总资源数量
        long totalResources = count();
        statistics.put("totalResources", totalResources);
        
        // 可用资源数量
        QueryWrapper<Resource> availableWrapper = new QueryWrapper<>();
        availableWrapper.eq("status", "available");
        long availableResources = count(availableWrapper);
        statistics.put("availableResources", availableResources);
        
        // 已分配资源数量
        QueryWrapper<Resource> allocatedWrapper = new QueryWrapper<>();
        allocatedWrapper.eq("status", "allocated");
        long allocatedResources = count(allocatedWrapper);
        statistics.put("allocatedResources", allocatedResources);
        
        // 维护中资源数量
        QueryWrapper<Resource> maintenanceWrapper = new QueryWrapper<>();
        maintenanceWrapper.eq("status", "maintenance");
        long maintenanceResources = count(maintenanceWrapper);
        statistics.put("maintenanceResources", maintenanceResources);
        
        // 总数量统计
        List<Resource> allResources = list();
        int totalQuantity = allResources.stream().mapToInt(Resource::getTotalQuantity).sum();
        int availableQuantity = allResources.stream().mapToInt(Resource::getAvailableQuantity).sum();
        int usedQuantity = totalQuantity - availableQuantity;
        
        statistics.put("totalQuantity", totalQuantity);
        statistics.put("availableQuantity", availableQuantity);
        statistics.put("usedQuantity", usedQuantity);
        statistics.put("utilizationRate", totalQuantity > 0 ? (double) usedQuantity / totalQuantity * 100 : 0);
        
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getResourceStatisticsByType() {
        return baseMapper.getResourceStatisticsByType();
    }

    @Override
    public List<Map<String, Object>> getResourceStatisticsByRegion() {
        return baseMapper.getResourceStatisticsByRegion();
    }

    @Override
    public List<Map<String, Object>> getResourceStatisticsByStatus() {
        return baseMapper.getResourceStatisticsByStatus();
    }
}
