package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Disaster;

public interface DisasterService extends IService<Disaster> {
    Disaster reportDisaster(Disaster disaster);
}
