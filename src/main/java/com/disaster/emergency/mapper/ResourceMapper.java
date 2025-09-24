package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    
    /**
     * 按资源类型统计
     */
    @Select("SELECT resource_type as type, COUNT(*) as count, SUM(total_quantity) as totalQuantity, " +
            "SUM(available_quantity) as availableQuantity FROM resource GROUP BY resource_type")
    List<Map<String, Object>> getResourceStatisticsByType();
    
    /**
     * 按地区统计
     */
    @Select("SELECT CONCAT(province, '-', city, '-', district) as region, COUNT(*) as count, " +
            "SUM(total_quantity) as totalQuantity, SUM(available_quantity) as availableQuantity " +
            "FROM resource GROUP BY province, city, district ORDER BY count DESC")
    List<Map<String, Object>> getResourceStatisticsByRegion();
    
    /**
     * 按状态统计
     */
    @Select("SELECT status, COUNT(*) as count, SUM(total_quantity) as totalQuantity, " +
            "SUM(available_quantity) as availableQuantity FROM resource GROUP BY status")
    List<Map<String, Object>> getResourceStatisticsByStatus();
}
