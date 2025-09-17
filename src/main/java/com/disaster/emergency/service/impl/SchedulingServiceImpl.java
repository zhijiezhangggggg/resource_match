package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.SchedulingRecord;
import com.disaster.emergency.mapper.SchedulingRecordMapper;
import com.disaster.emergency.service.SchedulingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SchedulingServiceImpl extends ServiceImpl<SchedulingRecordMapper, SchedulingRecord> implements SchedulingService {

    @Override
    public SchedulingRecord createScheduling(Long demandId, Long resourceId, Integer allocatedQuantity, 
                                           Long schedulerId, String schedulerName, String remark) {
        SchedulingRecord record = new SchedulingRecord();
        record.setDemandId(demandId);
        record.setResourceId(resourceId);
        record.setAllocatedQuantity(allocatedQuantity);
        record.setSchedulerId(schedulerId);
        record.setSchedulerName(schedulerName);
        record.setSchedulingTime(LocalDateTime.now());
        record.setStatus("allocated");
        record.setRemark(remark);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        save(record);
        return record;
    }

    @Override
    public boolean updateStatus(Long schedulingId, String status) {
        UpdateWrapper<SchedulingRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", schedulingId)
                    .set("status", status)
                    .set("update_time", LocalDateTime.now());
        return update(updateWrapper);
    }

    @Override
    public boolean completeScheduling(Long schedulingId, String remark) {
        UpdateWrapper<SchedulingRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", schedulingId)
                    .set("status", "completed")
                    .set("actual_delivery_time", LocalDateTime.now())
                    .set("remark", remark)
                    .set("update_time", LocalDateTime.now());
        return update(updateWrapper);
    }
}
