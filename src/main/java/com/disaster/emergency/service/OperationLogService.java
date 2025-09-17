package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    void saveLog(OperationLog log);
}
