package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.Demand;

import java.util.List;
import java.util.Map;

public interface DemandService extends IService<Demand> {
    Demand submitDemand(Demand demand);
    
    Page<Demand> getDemandList(Integer page, Integer size, Long disasterId, String demandType, 
                               String urgency, String status, String province, String city);
    
    boolean updateDemandStatus(Long id, String status);
    
    Map<String, Object> getDemandStatistics(String startTime, String endTime, String province, String city);
    
    List<Map<String, Object>> getDemandStatisticsByType(String startTime, String endTime);
    
    List<Map<String, Object>> getDemandStatisticsByUrgency(String startTime, String endTime);
}
