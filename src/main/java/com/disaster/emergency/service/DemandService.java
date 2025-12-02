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
    
    /**
     * 统计已匹配和待匹配需求数量
     * 已匹配需求：在MatchingRecord表中存在记录的需求（去重demandId）
     * 待匹配需求：在Demand表中但不在MatchingRecord表中的需求
     * @return 包含已匹配和待匹配需求数量的Map
     */
    Map<String, Object> getMatchingDemandCount();
    
    /**
     * 检查并更新需求状态
     * 根据已分配资源总量检查需求状态，如果累计分配已满足需求则更新为allocated
     * @param demandId 需求ID
     * @return 更新后的状态信息
     */
    Map<String, Object> checkAndUpdateDemandStatus(Long demandId);
}
