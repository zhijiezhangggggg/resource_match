package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.ResourceAllocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResourceAllocationMapper extends BaseMapper<ResourceAllocation> {
    
    /**
     * 按资源ID统计分配记录
     */
    @Select("SELECT resource_id as resourceId, COUNT(*) as allocationCount, " +
            "SUM(allocated_quantity) as totalAllocatedQuantity " +
            "FROM resource_allocation GROUP BY resource_id")
    List<Map<String, Object>> getAllocationStatisticsByResource();
    
    /**
     * 按需求ID统计分配记录
     */
    @Select("SELECT demand_id as demandId, COUNT(*) as allocationCount, " +
            "SUM(allocated_quantity) as totalAllocatedQuantity " +
            "FROM resource_allocation GROUP BY demand_id")
    List<Map<String, Object>> getAllocationStatisticsByDemand();
    
    /**
     * 按状态统计分配记录
     */
    @Select("SELECT status, COUNT(*) as count, SUM(allocated_quantity) as totalAllocatedQuantity " +
            "FROM resource_allocation GROUP BY status")
    List<Map<String, Object>> getAllocationStatisticsByStatus();
}
