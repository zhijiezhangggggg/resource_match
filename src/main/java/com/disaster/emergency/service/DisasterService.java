package com.disaster.emergency.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Disaster;

import java.util.Map;

public interface DisasterService extends IService<Disaster> {
    Disaster reportDisaster(Disaster disaster);
    
    /**
     * 分页查询灾情列表
     * @param page 页码
     * @param size 每页数量
     * @param disasterType 灾害类型
     * @param severity 严重程度
     * @param province 省份
     * @param city 城市
     * @param status 状态
     * @return 分页结果
     */
    IPage<Disaster> getDisasterList(int page, int size, String disasterType, String severity, 
                                   String province, String city, String status);
    
    /**
     * 获取灾情统计信息
     * @return 统计信息
     */
    Map<String, Object> getDisasterStatistics();
}
