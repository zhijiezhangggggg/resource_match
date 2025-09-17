package com.disaster.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.disaster.emergency.entity.MatchingRecord;

import java.util.List;

public interface MatchingService extends IService<MatchingRecord> {
    List<MatchingRecord> matchResources(Long demandId, String demandType, Integer quantity, 
                                      String province, String city, String urgency);
    boolean confirmMatching(Long matchingId, String status);
}
