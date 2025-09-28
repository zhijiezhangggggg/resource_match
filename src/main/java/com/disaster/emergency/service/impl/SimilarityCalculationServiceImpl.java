package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.service.SimilarityCalculationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 相似度计算服务实现类
 * 
 * <p>实现多维度相似度计算功能，包括类型、数量、距离、时效性、优先级等维度的相似度计算。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class SimilarityCalculationServiceImpl implements SimilarityCalculationService {
    
    // 默认权重配置
    private Map<String, Double> defaultWeights = new HashMap<String, Double>() {{
        put("type", 0.3);      // 类型权重
        put("quantity", 0.25); // 数量权重
        put("distance", 0.2);  // 距离权重
        put("timeliness", 0.15); // 时效性权重
        put("priority", 0.1);  // 优先级权重
    }};
    
    // 类型相似度映射表
    private Map<String, Set<String>> typeMapping = new HashMap<String, Set<String>>() {{
        put("帐篷", new HashSet<String>() {{ add("帐篷"); add("临时住所"); add("避难所"); }});
        put("食品", new HashSet<String>() {{ add("食品"); add("食物"); add("粮食"); add("干粮"); add("方便面"); }});
        put("药品", new HashSet<String>() {{ add("药品"); add("药物"); add("医疗用品"); add("急救包"); }});
        put("饮用水", new HashSet<String>() {{ add("饮用水"); add("水"); add("纯净水"); add("矿泉水"); }});
        put("衣物", new HashSet<String>() {{ add("衣物"); add("服装"); add("棉衣"); add("保暖用品"); }});
        put("通讯设备", new HashSet<String>() {{ add("通讯设备"); add("对讲机"); add("卫星电话"); add("手机"); }});
        put("照明设备", new HashSet<String>() {{ add("照明设备"); add("手电筒"); add("应急灯"); add("蜡烛"); }});
    }};
    
    @Override
    public double calculateOverallSimilarity(Resource resource, Demand demand) {
        // 获取各维度相似度
        double typeSim = calculateTypeSimilarity(resource.getResourceType(), demand.getDemandType());
        double quantitySim = calculateQuantitySimilarity(resource.getAvailableQuantity(), demand.getQuantity());
        double distanceSim = calculateDistanceSimilarity(
            resource.getProvince() + resource.getCity() + resource.getDistrict(), 
            demand.getProvince() + demand.getCity() + demand.getDistrict()
        );
        double timelinessSim = calculateTimelinessSimilarity(
            resource.getCreateTime() != null ? resource.getCreateTime().toString() : null, 
            demand.getUrgency(), 
            null // Demand实体没有deadline字段
        );
        double prioritySim = calculatePrioritySimilarity(
            resource.getPriorityLevel() != null ? resource.getPriorityLevel().toString() : "中", 
            "中" // Demand实体没有priority字段，使用默认值
        );
        
        // 加权计算综合相似度
        Map<String, Double> weights = getSimilarityWeights();
        double overallSim = typeSim * weights.get("type") +
                           quantitySim * weights.get("quantity") +
                           distanceSim * weights.get("distance") +
                           timelinessSim * weights.get("timeliness") +
                           prioritySim * weights.get("priority");
        
        return Math.round(overallSim * 100.0) / 100.0; // 保留两位小数
    }
    
    @Override
    public double calculateTypeSimilarity(String resourceType, String demandType) {
        if (resourceType == null || demandType == null) {
            return 0.0;
        }
        
        // 完全匹配
        if (resourceType.equals(demandType)) {
            return 100.0;
        }
        
        // 查找类型映射
        for (Map.Entry<String, Set<String>> entry : typeMapping.entrySet()) {
            if (entry.getValue().contains(resourceType) && entry.getValue().contains(demandType)) {
                return 90.0; // 同类别不同具体类型
            }
        }
        
        // 检查是否包含关键词
        if (resourceType.contains(demandType) || demandType.contains(resourceType)) {
            return 70.0;
        }
        
        return 0.0;
    }
    
    @Override
    public double calculateQuantitySimilarity(Integer resourceQuantity, Integer demandQuantity) {
        if (resourceQuantity == null || demandQuantity == null || resourceQuantity <= 0 || demandQuantity <= 0) {
            return 0.0;
        }
        
        // 完全匹配
        if (resourceQuantity.equals(demandQuantity)) {
            return 100.0;
        }
        
        // 计算数量比例
        double ratio = (double) Math.min(resourceQuantity, demandQuantity) / Math.max(resourceQuantity, demandQuantity);
        
        // 根据比例计算相似度
        if (ratio >= 0.9) return 95.0;
        if (ratio >= 0.8) return 90.0;
        if (ratio >= 0.7) return 80.0;
        if (ratio >= 0.6) return 70.0;
        if (ratio >= 0.5) return 60.0;
        if (ratio >= 0.3) return 40.0;
        return 20.0;
    }
    
    @Override
    public double calculateDistanceSimilarity(String resourceLocation, String demandLocation) {
        if (resourceLocation == null || demandLocation == null) {
            return 0.0;
        }
        
        // 完全匹配
        if (resourceLocation.equals(demandLocation)) {
            return 100.0;
        }
        
        // 检查是否在同一地区
        if (isSameRegion(resourceLocation, demandLocation)) {
            return 80.0;
        }
        
        // 检查是否在同一省份
        if (isSameProvince(resourceLocation, demandLocation)) {
            return 60.0;
        }
        
        // 检查是否在同一大区
        if (isSameRegionGroup(resourceLocation, demandLocation)) {
            return 40.0;
        }
        
        return 10.0; // 不同地区
    }
    
    @Override
    public double calculateTimelinessSimilarity(String resourceAvailableTime, String demandUrgency, String demandDeadline) {
        if (resourceAvailableTime == null || demandUrgency == null) {
            return 50.0; // 默认中等相似度
        }
        
        try {
            LocalDateTime availableTime = LocalDateTime.parse(resourceAvailableTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            
            // 计算资源可用时间与当前时间的差值（小时）
            long hoursDiff = java.time.Duration.between(now, availableTime).toHours();
            
            // 根据需求紧急程度和资源可用时间计算相似度
            switch (demandUrgency.toLowerCase()) {
                case "紧急":
                    if (hoursDiff <= 2) return 100.0;
                    if (hoursDiff <= 6) return 90.0;
                    if (hoursDiff <= 12) return 80.0;
                    if (hoursDiff <= 24) return 60.0;
                    return 30.0;
                case "高":
                    if (hoursDiff <= 6) return 100.0;
                    if (hoursDiff <= 12) return 90.0;
                    if (hoursDiff <= 24) return 80.0;
                    if (hoursDiff <= 48) return 60.0;
                    return 40.0;
                case "中":
                    if (hoursDiff <= 24) return 100.0;
                    if (hoursDiff <= 48) return 90.0;
                    if (hoursDiff <= 72) return 80.0;
                    if (hoursDiff <= 120) return 60.0;
                    return 50.0;
                case "低":
                    if (hoursDiff <= 48) return 100.0;
                    if (hoursDiff <= 120) return 90.0;
                    if (hoursDiff <= 240) return 80.0;
                    return 70.0;
                default:
                    return 50.0;
            }
        } catch (Exception e) {
            return 50.0; // 解析失败时返回中等相似度
        }
    }
    
    @Override
    public double calculatePrioritySimilarity(String resourcePriority, String demandPriority) {
        if (resourcePriority == null || demandPriority == null) {
            return 50.0;
        }
        
        // 完全匹配
        if (resourcePriority.equals(demandPriority)) {
            return 100.0;
        }
        
        // 优先级映射
        Map<String, Integer> priorityMap = new HashMap<String, Integer>() {{
            put("高", 3);
            put("中", 2);
            put("低", 1);
        }};
        
        Integer resourceLevel = priorityMap.get(resourcePriority);
        Integer demandLevel = priorityMap.get(demandPriority);
        
        if (resourceLevel == null || demandLevel == null) {
            return 50.0;
        }
        
        // 根据优先级差异计算相似度
        int diff = Math.abs(resourceLevel - demandLevel);
        switch (diff) {
            case 0: return 100.0;
            case 1: return 80.0;
            case 2: return 60.0;
            default: return 40.0;
        }
    }
    
    @Override
    public List<Map<String, Object>> calculateBatchSimilarity(List<Resource> resources, Demand demand, Integer limit) {
        if (resources == null || resources.isEmpty() || demand == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> results = resources.stream()
            .map(resource -> {
                Map<String, Object> result = new HashMap<>();
                result.put("resource", resource);
                result.put("similarity", calculateOverallSimilarity(resource, demand));
                result.put("typeSimilarity", calculateTypeSimilarity(resource.getResourceType(), demand.getDemandType()));
                result.put("quantitySimilarity", calculateQuantitySimilarity(resource.getAvailableQuantity(), demand.getQuantity()));
                result.put("distanceSimilarity", calculateDistanceSimilarity(
                    resource.getProvince() + resource.getCity() + resource.getDistrict(), 
                    demand.getProvince() + demand.getCity() + demand.getDistrict()
                ));
                result.put("timelinessSimilarity", calculateTimelinessSimilarity(
                    resource.getCreateTime() != null ? resource.getCreateTime().toString() : null, 
                    demand.getUrgency(), 
                    null
                ));
                result.put("prioritySimilarity", calculatePrioritySimilarity(
                    resource.getPriorityLevel() != null ? resource.getPriorityLevel().toString() : "中", 
                    "中"
                ));
                return result;
            })
            .sorted((a, b) -> Double.compare(
                (Double) b.get("similarity"), 
                (Double) a.get("similarity")
            ))
            .collect(Collectors.toList());
        
        // 限制返回结果数量
        if (limit != null && limit > 0 && results.size() > limit) {
            return results.subList(0, limit);
        }
        
        return results;
    }
    
    @Override
    public Map<String, Double> getSimilarityWeights() {
        return new HashMap<>(defaultWeights);
    }
    
    @Override
    public boolean updateSimilarityWeights(Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return false;
        }
        
        // 验证权重总和是否为1.0
        double totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalWeight - 1.0) > 0.01) {
            return false;
        }
        
        defaultWeights.putAll(weights);
        return true;
    }
    
    /**
     * 检查是否在同一地区（市/县级别）
     */
    private boolean isSameRegion(String location1, String location2) {
        if (location1 == null || location2 == null) return false;
        
        // 提取市/县信息
        String region1 = extractRegion(location1);
        String region2 = extractRegion(location2);
        
        return region1 != null && region2 != null && region1.equals(region2);
    }
    
    /**
     * 检查是否在同一省份
     */
    private boolean isSameProvince(String location1, String location2) {
        if (location1 == null || location2 == null) return false;
        
        // 提取省份信息
        String province1 = extractProvince(location1);
        String province2 = extractProvince(location2);
        
        return province1 != null && province2 != null && province1.equals(province2);
    }
    
    /**
     * 检查是否在同一大区
     */
    private boolean isSameRegionGroup(String location1, String location2) {
        if (location1 == null || location2 == null) return false;
        
        // 简化的地区分组
        String region1 = extractRegionGroup(location1);
        String region2 = extractRegionGroup(location2);
        
        return region1 != null && region2 != null && region1.equals(region2);
    }
    
    /**
     * 提取地区信息（市/县）
     */
    private String extractRegion(String location) {
        if (location == null) return null;
        
        // 匹配市/县/区
        if (location.contains("市")) {
            int index = location.indexOf("市");
            return location.substring(0, index + 1);
        }
        if (location.contains("县")) {
            int index = location.indexOf("县");
            return location.substring(0, index + 1);
        }
        if (location.contains("区")) {
            int index = location.indexOf("区");
            return location.substring(0, index + 1);
        }
        
        return null;
    }
    
    /**
     * 提取省份信息
     */
    private String extractProvince(String location) {
        if (location == null) return null;
        
        // 匹配省份
        String[] provinces = {"北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江", 
                            "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", 
                            "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾", 
                            "内蒙古", "广西", "西藏", "宁夏", "新疆", "香港", "澳门"};
        
        for (String province : provinces) {
            if (location.contains(province)) {
                return province;
            }
        }
        
        return null;
    }
    
    /**
     * 提取地区分组信息
     */
    private String extractRegionGroup(String location) {
        String province = extractProvince(location);
        if (province == null) return null;
        
        // 简化的地区分组
        if (Arrays.asList("北京", "天津", "河北", "山西", "内蒙古").contains(province)) {
            return "华北";
        }
        if (Arrays.asList("辽宁", "吉林", "黑龙江").contains(province)) {
            return "东北";
        }
        if (Arrays.asList("上海", "江苏", "浙江", "安徽", "福建", "江西", "山东").contains(province)) {
            return "华东";
        }
        if (Arrays.asList("河南", "湖北", "湖南", "广东", "广西", "海南").contains(province)) {
            return "中南";
        }
        if (Arrays.asList("重庆", "四川", "贵州", "云南", "西藏").contains(province)) {
            return "西南";
        }
        if (Arrays.asList("陕西", "甘肃", "青海", "宁夏", "新疆").contains(province)) {
            return "西北";
        }
        
        return "其他";
    }
}
