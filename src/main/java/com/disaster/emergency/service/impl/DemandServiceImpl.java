package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.mapper.DemandMapper;
import com.disaster.emergency.service.DemandService;
import org.springframework.stereotype.Service;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    @Override
    public Demand submitDemand(Demand demand) {
        demand.setStatus("pending");
        demand.setCreateTime(java.time.LocalDateTime.now());
        demand.setUpdateTime(java.time.LocalDateTime.now());
        save(demand);
        return demand;
    }
}
