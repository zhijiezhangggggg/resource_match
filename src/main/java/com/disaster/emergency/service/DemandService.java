package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Demand;

public interface DemandService extends IService<Demand> {
    Demand submitDemand(Demand demand);
}
