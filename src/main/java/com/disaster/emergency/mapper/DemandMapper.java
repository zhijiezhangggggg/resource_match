package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.Demand;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandMapper extends BaseMapper<Demand> {
}
