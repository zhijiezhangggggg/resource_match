package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.mapper.DisasterMapper;
import com.disaster.emergency.service.DisasterService;
import org.springframework.stereotype.Service;

@Service
public class DisasterServiceImpl extends ServiceImpl<DisasterMapper, Disaster> implements DisasterService {

    @Override
    public Disaster reportDisaster(Disaster disaster) {
        disaster.setStatus("active");
        disaster.setCreateTime(java.time.LocalDateTime.now());
        disaster.setUpdateTime(java.time.LocalDateTime.now());
        save(disaster);
        return disaster;
    }
}
