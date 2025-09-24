package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Resource;

import java.util.List;
import java.util.Map;

public interface ResourceService extends IService<Resource> {
    /**
     * 保存资源信息
     */
    Resource saveResource(Resource resource);
    
    /**
     * 更新资源数量
     */
    boolean updateQuantity(Long resourceId, Integer quantity);
    
    /**
     * 分页查询资源列表
     */
    Page<Resource> getResourceList(Integer page, Integer size, String resourceType, String status, 
                                  String province, String city, String district, Long organizationId, String resourceName);
    
    /**
     * 获取资源统计信息
     */
    Map<String, Object> getResourceStatistics();
    
    /**
     * 按类型统计资源
     */
    List<Map<String, Object>> getResourceStatisticsByType();
    
    /**
     * 按地区统计资源
     */
    List<Map<String, Object>> getResourceStatisticsByRegion();
    
    /**
     * 按状态统计资源
     */
    List<Map<String, Object>> getResourceStatisticsByStatus();
}
