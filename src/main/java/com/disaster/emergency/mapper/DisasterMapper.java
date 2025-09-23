package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.Disaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DisasterMapper extends BaseMapper<Disaster> {
    
    /**
     * 获取灾害类型统计
     * @return 统计结果
     */
    @Select("SELECT disaster_type as disasterType, COUNT(*) as count FROM disaster GROUP BY disaster_type")
    List<Map<String, Object>> getDisasterTypeStatistics();
    
    /**
     * 获取严重程度统计
     * @return 统计结果
     */
    @Select("SELECT severity, COUNT(*) as count FROM disaster GROUP BY severity")
    List<Map<String, Object>> getSeverityStatistics();
}
