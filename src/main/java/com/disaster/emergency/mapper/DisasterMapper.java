package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.Disaster;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DisasterMapper extends BaseMapper<Disaster> {
}
