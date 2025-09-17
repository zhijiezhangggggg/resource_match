package com.disaster.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.disaster.emergency.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
