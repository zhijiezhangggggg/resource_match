package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.ResourceAllocation;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.mapper.ResourceAllocationMapper;
import com.disaster.emergency.mapper.DemandMapper;
import com.disaster.emergency.service.ResourceService;
import com.disaster.emergency.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {
    
    @Autowired
    private ResourceAllocationMapper resourceAllocationMapper;
    
    @Autowired
    private DemandMapper demandMapper;
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

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
    
    @Override
    public List<Resource> getAvailableResources() {
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "available")
                   .gt("available_quantity", 0);
        return list(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> allocateResource(Long resourceId, Long demandId, Integer allocatedQuantity, 
                                               String allocationReason, LocalDateTime estimatedArrivalTime, 
                                               String allocator, String remarks) {
        
        // 1. 验证资源是否存在
        Resource resource = getById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }
        
        // 2. 验证需求是否存在
        Demand demand = demandMapper.selectById(demandId);
        if (demand == null) {
            throw new RuntimeException("需求不存在");
        }
        
        // 3. 验证可用数量是否足够
        if (resource.getAvailableQuantity() < allocatedQuantity) {
            throw new RuntimeException("可用数量不足，当前可用数量：" + resource.getAvailableQuantity());
        }
        
        // 4. 更新资源可用数量
        int newAvailableQuantity = resource.getAvailableQuantity() - allocatedQuantity;
        UpdateWrapper<Resource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", resourceId)
                    .set("available_quantity", newAvailableQuantity)
                    .set("update_time", LocalDateTime.now());
        
        if (newAvailableQuantity == 0) {
            updateWrapper.set("status", "allocated");
        }
        
        boolean resourceUpdated = update(updateWrapper);
        if (!resourceUpdated) {
            throw new RuntimeException("资源数量更新失败");
        }
        
        // 5. 创建资源分配记录
        ResourceAllocation allocation = new ResourceAllocation();
        allocation.setResourceId(resourceId);
        allocation.setDemandId(demandId);
        allocation.setAllocatedQuantity(allocatedQuantity);
        allocation.setAllocationReason(allocationReason);
        allocation.setEstimatedArrivalTime(estimatedArrivalTime);
        allocation.setStatus("allocated");
        allocation.setAllocator(allocator);
        allocation.setAllocationTime(LocalDateTime.now());
        allocation.setRemarks(remarks);
        allocation.setCreateTime(LocalDateTime.now());
        allocation.setUpdateTime(LocalDateTime.now());
        
        int allocationInserted = resourceAllocationMapper.insert(allocation);
        if (allocationInserted <= 0) {
            throw new RuntimeException("资源分配记录创建失败");
        }
        
        // 6. 更新需求状态（如果需求被完全满足）
        if (demand.getQuantity() <= allocatedQuantity) {
            UpdateWrapper<Demand> demandUpdateWrapper = new UpdateWrapper<>();
            demandUpdateWrapper.eq("id", demandId)
                              .set("status", "satisfied")
                              .set("update_time", LocalDateTime.now());
            demandMapper.update(null, demandUpdateWrapper);
        }
        
        // 7. 建立知识图谱关联
        try {
            // 获取或创建资源节点
            String resourceNodeType = "resource";
            Long resourceBusinessId = resourceId;
            String resourceNodeName = resource.getResourceName();
            
            // 检查资源节点是否已存在
            com.disaster.emergency.entity.KnowledgeNode resourceNode = knowledgeGraphService.getNodeByBusinessId(resourceNodeType, resourceBusinessId);
            if (resourceNode == null) {
                // 创建资源节点
                Map<String, Object> resourceProperties = new HashMap<>();
                resourceProperties.put("resourceType", resource.getResourceType());
                resourceProperties.put("totalQuantity", resource.getTotalQuantity());
                resourceProperties.put("availableQuantity", newAvailableQuantity);
                resourceProperties.put("unit", resource.getUnit());
                resourceProperties.put("location", resource.getProvince() + "-" + resource.getCity() + "-" + resource.getDistrict());
                resourceProperties.put("warehouseName", resource.getWarehouseName());
                resourceProperties.put("contactPerson", resource.getContactPerson());
                resourceProperties.put("contactPhone", resource.getContactPhone());
                resourceProperties.put("priorityLevel", resource.getPriorityLevel());
                resourceProperties.put("status", newAvailableQuantity == 0 ? "allocated" : "available");
                
                Long resourceNodeId = knowledgeGraphService.createNode(resourceNodeType, resourceBusinessId, resourceNodeName, resourceProperties);
                resourceNode = knowledgeGraphService.getNode(resourceNodeId);
            } else {
                // 更新资源节点属性
                Map<String, Object> resourceProperties = new HashMap<>();
                resourceProperties.put("availableQuantity", newAvailableQuantity);
                resourceProperties.put("status", newAvailableQuantity == 0 ? "allocated" : "available");
                knowledgeGraphService.updateNodeProperties(resourceNode.getId(), resourceProperties);
            }
            
            // 获取或创建需求节点
            String demandNodeType = "demand";
            Long demandBusinessId = demandId;
            String demandNodeName = demand.getDemandType();
            
            com.disaster.emergency.entity.KnowledgeNode demandNode = knowledgeGraphService.getNodeByBusinessId(demandNodeType, demandBusinessId);
            if (demandNode == null) {
                // 创建需求节点
                Map<String, Object> demandProperties = new HashMap<>();
                demandProperties.put("demandType", demand.getDemandType());
                demandProperties.put("quantity", demand.getQuantity());
                demandProperties.put("unit", demand.getUnit());
                demandProperties.put("urgency", demand.getUrgency());
                demandProperties.put("location", demand.getProvince() + "-" + demand.getCity() + "-" + demand.getDistrict());
                demandProperties.put("description", demand.getDescription());
                demandProperties.put("status", demand.getQuantity() <= allocatedQuantity ? "satisfied" : "pending");
                
                Long demandNodeId = knowledgeGraphService.createNode(demandNodeType, demandBusinessId, demandNodeName, demandProperties);
                demandNode = knowledgeGraphService.getNode(demandNodeId);
            } else {
                // 更新需求节点属性
                Map<String, Object> demandProperties = new HashMap<>();
                demandProperties.put("status", demand.getQuantity() <= allocatedQuantity ? "satisfied" : "pending");
                knowledgeGraphService.updateNodeProperties(demandNode.getId(), demandProperties);
            }
            
            // 创建资源分配关系
            Map<String, Object> relationProperties = new HashMap<>();
            relationProperties.put("allocatedQuantity", allocatedQuantity);
            relationProperties.put("allocationReason", allocationReason);
            relationProperties.put("estimatedArrivalTime", estimatedArrivalTime.toString());
            relationProperties.put("allocator", allocator);
            relationProperties.put("allocationTime", LocalDateTime.now().toString());
            relationProperties.put("remarks", remarks);
            
            knowledgeGraphService.createRelation(
                resourceNode.getId(), 
                demandNode.getId(), 
                "allocates", 
                1.0, 
                relationProperties
            );
            
        } catch (Exception e) {
            // 知识图谱操作失败不影响主流程，只记录日志
            System.err.println("知识图谱关联创建失败: " + e.getMessage());
        }
        
        // 8. 返回分配结果
        Map<String, Object> result = new HashMap<>();
        result.put("allocationId", allocation.getId());
        result.put("resourceId", resourceId);
        result.put("demandId", demandId);
        result.put("allocatedQuantity", allocatedQuantity);
        result.put("remainingQuantity", newAvailableQuantity);
        result.put("allocationTime", allocation.getAllocationTime());
        result.put("estimatedArrivalTime", estimatedArrivalTime);
        result.put("status", "allocated");
        
        return result;
    }
}
