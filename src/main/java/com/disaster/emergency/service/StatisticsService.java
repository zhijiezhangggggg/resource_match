package com.disaster.emergency.service;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getDisasterStatistics(String startTime, String endTime, String province, String city);
    Map<String, Object> getResourceStatistics();
    Map<String, Object> getDemandStatistics();
}
