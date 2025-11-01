package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.*;
import com.disaster.emergency.service.*;
import com.disaster.emergency.websocket.DashboardWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大屏数据服务实现类
 * 
 * <p>提供大屏展示所需的各种数据服务实现。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private MatchingService matchingService;
    
    @Autowired
    private SchedulingService schedulingService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private DemandService demandService;
    
    @Autowired
    private DisasterService disasterService;
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private DashboardWebSocketHandler webSocketHandler;

    @Override
    public Map<String, Object> getMatchingOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // 获取匹配记录统计
            List<MatchingRecord> matchingRecords = matchingService.list();
            
            // 按状态统计
            Map<String, Long> statusStats = matchingRecords.stream()
                .collect(Collectors.groupingBy(
                    MatchingRecord::getStatus,
                    Collectors.counting()
                ));
            
            // 按匹配分数统计
            long highScoreCount = matchingRecords.stream()
                .filter(record -> record.getMatchScore().doubleValue() >= 80)
                .count();
            
            long mediumScoreCount = matchingRecords.stream()
                .filter(record -> record.getMatchScore().doubleValue() >= 60 && record.getMatchScore().doubleValue() < 80)
                .count();
            
            long lowScoreCount = matchingRecords.stream()
                .filter(record -> record.getMatchScore().doubleValue() < 60)
                .count();
            
            // 今日匹配统计
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long todayMatches = matchingRecords.stream()
                .filter(record -> record.getCreateTime().isAfter(today))
                .count();
            
            overview.put("totalMatches", matchingRecords.size());
            overview.put("statusStats", statusStats);
            Map<String, Long> scoreStats = new java.util.HashMap<>();
            scoreStats.put("high", highScoreCount);
            scoreStats.put("medium", mediumScoreCount);
            scoreStats.put("low", lowScoreCount);
            overview.put("scoreStats", scoreStats);
            overview.put("todayMatches", todayMatches);
            overview.put("averageScore", matchingRecords.stream()
                .mapToDouble(record -> record.getMatchScore().doubleValue())
                .average()
                .orElse(0.0));
            overview.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            overview.put("error", "获取匹配概览数据失败: " + e.getMessage());
        }
        
        return overview;
    }

    @Override
    public Map<String, Object> getResourceDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        try {
            // 获取所有资源
            List<Resource> resources = resourceService.list();
            
            // 按地区统计
            Map<String, Long> regionStats = resources.stream()
                .collect(Collectors.groupingBy(
                    resource -> resource.getProvince() + "-" + resource.getCity(),
                    Collectors.counting()
                ));
            
            // 按类型统计
            Map<String, Long> typeStats = resources.stream()
                .collect(Collectors.groupingBy(
                    Resource::getResourceType,
                    Collectors.counting()
                ));
            
            // 按状态统计
            Map<String, Long> statusStats = resources.stream()
                .collect(Collectors.groupingBy(
                    Resource::getStatus,
                    Collectors.counting()
                ));
            
            // 可用资源统计
            long availableCount = resources.stream()
                .filter(resource -> "available".equals(resource.getStatus()))
                .count();
            
            // 总数量统计
            int totalQuantity = resources.stream()
                .mapToInt(Resource::getTotalQuantity)
                .sum();
            
            int availableQuantity = resources.stream()
                .mapToInt(Resource::getAvailableQuantity)
                .sum();
            
            distribution.put("totalResources", resources.size());
            distribution.put("regionStats", regionStats);
            distribution.put("typeStats", typeStats);
            distribution.put("statusStats", statusStats);
            distribution.put("availableCount", availableCount);
            distribution.put("totalQuantity", totalQuantity);
            distribution.put("availableQuantity", availableQuantity);
            distribution.put("utilizationRate", totalQuantity > 0 ? (double) (totalQuantity - availableQuantity) / totalQuantity * 100 : 0.0);
            distribution.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            distribution.put("error", "获取资源分布数据失败: " + e.getMessage());
        }
        
        return distribution;
    }

    @Override
    public Map<String, Object> getSchedulingStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 获取调度记录
            List<SchedulingRecord> schedulingRecords = schedulingService.list();
            
            // 按状态统计
            Map<String, Long> statusStats = schedulingRecords.stream()
                .collect(Collectors.groupingBy(
                    SchedulingRecord::getStatus,
                    Collectors.counting()
                ));
            
            // 今日调度统计
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long todaySchedules = schedulingRecords.stream()
                .filter(record -> record.getCreateTime().isAfter(today))
                .count();
            
            // 待处理调度
            List<SchedulingRecord> pendingSchedules = schedulingRecords.stream()
                .filter(record -> "pending".equals(record.getStatus()))
                .collect(Collectors.toList());
            
            // 进行中调度
            List<SchedulingRecord> inProgressSchedules = schedulingRecords.stream()
                .filter(record -> "in_progress".equals(record.getStatus()))
                .collect(Collectors.toList());
            
            status.put("totalSchedules", schedulingRecords.size());
            status.put("statusStats", statusStats);
            status.put("todaySchedules", todaySchedules);
            status.put("pendingCount", pendingSchedules.size());
            status.put("inProgressCount", inProgressSchedules.size());
            status.put("pendingSchedules", pendingSchedules.stream()
                .map(this::convertSchedulingRecordToMap)
                .collect(Collectors.toList()));
            status.put("inProgressSchedules", inProgressSchedules.stream()
                .map(this::convertSchedulingRecordToMap)
                .collect(Collectors.toList()));
            status.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            status.put("error", "获取调度状态数据失败: " + e.getMessage());
        }
        
        return status;
    }

    @Override
    public Map<String, Object> getMatchingProgress() {
        Map<String, Object> progress = new HashMap<>();
        
        try {
            // 获取当前正在进行的匹配任务
            List<MatchingRecord> recentMatches = matchingService.list().stream()
                .filter(record -> record.getCreateTime().isAfter(LocalDateTime.now().minusHours(1)))
                .collect(Collectors.toList());
            
            // 按时间分组统计
            Map<String, Long> hourlyStats = recentMatches.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getCreateTime().format(DateTimeFormatter.ofPattern("HH:00")),
                    Collectors.counting()
                ));
            
            // 匹配成功率
            long successCount = recentMatches.stream()
                .filter(record -> "approved".equals(record.getStatus()))
                .count();
            
            double successRate = recentMatches.size() > 0 ? (double) successCount / recentMatches.size() * 100 : 0.0;
            
            progress.put("recentMatches", recentMatches.size());
            progress.put("hourlyStats", hourlyStats);
            progress.put("successCount", successCount);
            progress.put("successRate", successRate);
            progress.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            progress.put("error", "获取匹配进度数据失败: " + e.getMessage());
        }
        
        return progress;
    }

    @Override
    public Map<String, Object> getMapData() {
        Map<String, Object> mapData = new HashMap<>();
        
        try {
            // 获取资源位置数据
            List<Resource> resources = resourceService.list();
            List<Map<String, Object>> resourcePoints = resources.stream()
                .map(this::convertResourceToMapPoint)
                .collect(Collectors.toList());
            
            // 获取需求位置数据
            List<Demand> demands = demandService.list();
            List<Map<String, Object>> demandPoints = demands.stream()
                .map(this::convertDemandToMapPoint)
                .collect(Collectors.toList());
            
            // 获取灾情位置数据
            List<Disaster> disasters = disasterService.list();
            List<Map<String, Object>> disasterPoints = disasters.stream()
                .map(this::convertDisasterToMapPoint)
                .collect(Collectors.toList());
            
            mapData.put("resources", resourcePoints);
            mapData.put("demands", demandPoints);
            mapData.put("disasters", disasterPoints);
            mapData.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            mapData.put("error", "获取地图数据失败: " + e.getMessage());
        }
        
        return mapData;
    }

    @Override
    public Map<String, Object> modifyScheduling(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long schedulingId = Long.valueOf(request.get("schedulingId").toString());
            String newStatus = (String) request.get("newStatus");
            String remark = (String) request.get("remark");
            String operator = (String) request.get("operator");
            
            // 更新调度记录
            boolean success = schedulingService.updateStatus(schedulingId, newStatus);
            
            if (success) {
                result.put("success", true);
                result.put("message", "调度指令修改成功");
                result.put("schedulingId", schedulingId);
                result.put("newStatus", newStatus);
                result.put("operator", operator);
                result.put("modifyTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                // 推送实时更新
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("type", "scheduling_modified");
                pushData.put("data", result);
                pushRealtimeData(pushData);
            } else {
                result.put("success", false);
                result.put("message", "调度指令修改失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "调度指令修改失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> confirmScheduling(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long schedulingId = Long.valueOf(request.get("schedulingId").toString());
            String remark = (String) request.get("remark");
            String operator = (String) request.get("operator");
            
            // 确认调度
            boolean success = schedulingService.completeScheduling(schedulingId, remark);
            
            if (success) {
                result.put("success", true);
                result.put("message", "调度指令确认成功");
                result.put("schedulingId", schedulingId);
                result.put("operator", operator);
                result.put("confirmTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                // 推送实时更新
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("type", "scheduling_confirmed");
                pushData.put("data", result);
                pushRealtimeData(pushData);
            } else {
                result.put("success", false);
                result.put("message", "调度指令确认失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "调度指令确认失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> cancelScheduling(Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Long schedulingId = Long.valueOf(request.get("schedulingId").toString());
            String reason = (String) request.get("reason");
            String operator = (String) request.get("operator");
            
            // 取消调度
            boolean success = schedulingService.updateStatus(schedulingId, "cancelled");
            
            if (success) {
                result.put("success", true);
                result.put("message", "调度指令取消成功");
                result.put("schedulingId", schedulingId);
                result.put("reason", reason);
                result.put("operator", operator);
                result.put("cancelTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                // 推送实时更新
                Map<String, Object> pushData = new HashMap<>();
                pushData.put("type", "scheduling_cancelled");
                pushData.put("data", result);
                pushRealtimeData(pushData);
            } else {
                result.put("success", false);
                result.put("message", "调度指令取消失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "调度指令取消失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        
        try {
            List<Map<String, Object>> alertList = new ArrayList<>();
            
            // 检查资源不足告警
            List<Resource> lowStockResources = resourceService.list().stream()
                .filter(resource -> resource.getAvailableQuantity() < 10)
                .collect(Collectors.toList());
            
            for (Resource resource : lowStockResources) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "resource_low_stock");
                alert.put("level", "warning");
                alert.put("title", "资源库存不足");
                alert.put("message", resource.getResourceName() + " 库存不足，当前可用数量: " + resource.getAvailableQuantity());
                alert.put("resourceId", resource.getId());
                alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                alertList.add(alert);
            }
            
            // 检查长时间待处理的调度
            List<SchedulingRecord> longPendingSchedules = schedulingService.list().stream()
                .filter(record -> "pending".equals(record.getStatus()) && 
                        record.getCreateTime().isBefore(LocalDateTime.now().minusHours(2)))
                .collect(Collectors.toList());
            
            for (SchedulingRecord schedule : longPendingSchedules) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "scheduling_pending");
                alert.put("level", "info");
                alert.put("title", "调度指令待处理");
                alert.put("message", "调度记录ID " + schedule.getId() + " 已等待处理超过2小时");
                alert.put("schedulingId", schedule.getId());
                alert.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                alertList.add(alert);
            }
            
            alerts.put("alerts", alertList);
            alerts.put("totalCount", alertList.size());
            alerts.put("warningCount", alertList.stream().filter(a -> "warning".equals(a.get("level"))).count());
            alerts.put("infoCount", alertList.stream().filter(a -> "info".equals(a.get("level"))).count());
            alerts.put("lastUpdateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        } catch (Exception e) {
            alerts.put("error", "获取告警信息失败: " + e.getMessage());
        }
        
        return alerts;
    }

    @Override
    public void pushRealtimeData(Map<String, Object> data) {
        // 通过WebSocket向所有连接的客户端推送数据
        webSocketHandler.broadcastMessage(data);
        System.out.println("推送实时数据: " + data);
    }

    // 辅助方法
    private Map<String, Object> convertSchedulingRecordToMap(SchedulingRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("demandId", record.getDemandId());
        map.put("resourceId", record.getResourceId());
        map.put("allocatedQuantity", record.getAllocatedQuantity());
        map.put("schedulerName", record.getSchedulerName());
        map.put("status", record.getStatus());
        map.put("schedulingTime", record.getSchedulingTime());
        map.put("remark", record.getRemark());
        return map;
    }

    private Map<String, Object> convertResourceToMapPoint(Resource resource) {
        Map<String, Object> point = new HashMap<>();
        point.put("id", resource.getId());
        point.put("name", resource.getResourceName());
        point.put("type", resource.getResourceType());
        point.put("province", resource.getProvince());
        point.put("city", resource.getCity());
        point.put("district", resource.getDistrict());
        point.put("latitude", resource.getLatitude()); // 添加经纬度字段
        point.put("longitude", resource.getLongitude());
        point.put("availableQuantity", resource.getAvailableQuantity());
        point.put("totalQuantity", resource.getTotalQuantity());
        point.put("status", resource.getStatus());
        point.put("warehouseName", resource.getWarehouseName());
        point.put("contactPerson", resource.getContactPerson());
        return point;
    }

    private Map<String, Object> convertDemandToMapPoint(Demand demand) {
        Map<String, Object> point = new HashMap<>();
        point.put("id", demand.getId());
        point.put("type", demand.getDemandType());
        point.put("quantity", demand.getQuantity());
        point.put("unit", demand.getUnit());
        point.put("province", demand.getProvince());
        point.put("city", demand.getCity());
        point.put("district", demand.getDistrict());
        point.put("latitude", demand.getLatitude()); // 添加经纬度字段
        point.put("longitude", demand.getLongitude());
        point.put("urgency", demand.getUrgency());
        point.put("status", demand.getStatus());
        point.put("description", demand.getDescription());
        point.put("createTime", demand.getCreateTime());
        return point;
    }

    private Map<String, Object> convertDisasterToMapPoint(Disaster disaster) {
        Map<String, Object> point = new HashMap<>();
        point.put("id", disaster.getId());
        point.put("type", disaster.getDisasterType());
        point.put("level", disaster.getSeverity());
        point.put("province", disaster.getProvince());
        point.put("city", disaster.getCity());
        point.put("district", disaster.getDistrict());
        point.put("latitude", disaster.getLatitude()); // 添加经纬度字段
        point.put("longitude", disaster.getLongitude());
        point.put("status", disaster.getStatus());
        point.put("occurTime", disaster.getOccurTime());
        point.put("description", disaster.getDescription());
        return point;
    }
}
