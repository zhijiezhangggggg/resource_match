package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disaster.emergency.entity.OperationLog;
import com.disaster.emergency.mapper.OperationLogMapper;
import com.disaster.emergency.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void saveLog(OperationLog log) {
        log.setOperationTime(LocalDateTime.now());
        save(log);
    }
}
