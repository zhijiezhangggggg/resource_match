package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.SchedulingRecord;

public interface SchedulingService extends IService<SchedulingRecord> {
    SchedulingRecord createScheduling(Long demandId, Long resourceId, Integer allocatedQuantity, 
                                    Long schedulerId, String schedulerName, String remark);
    boolean updateStatus(Long schedulingId, String status);
    boolean completeScheduling(Long schedulingId, String remark);
}
