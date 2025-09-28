package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.MatchingRecord;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SchedulingRecord;
import com.disaster.emergency.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源匹配调度控制器
 * 
 * <p>整合文本解析、知识图谱操作、相似度计算和调度算法的完整业务流程。</p>
 * 
 * @author 系统
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/resource-matching")
@CrossOrigin
@Tag(name = "资源匹配调度", description = "灾情需求解析、知识图谱操作、相似度计算和资源调度优化")
public class ResourceMatchingController {
    
    @Autowired
    private TextParseService textParseService;
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;
    
    @Autowired
    private SimilarityCalculationService similarityCalculationService;
    
    @Autowired
    private SchedulingService schedulingService;
    
    @Autowired
    private DemandService demandService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private MatchingService matchingService;
    
    /**
     * 处理灾情/需求报告
     * 
     * <p>接收灾民或救援队的灾情/需求报告，解析文本内容，在知识图谱中创建或更新节点，
     * 并自动进行资源匹配和调度优化。</p>
     * 
     * @param request 报告请求，包含报告类型、文本内容、位置等信息
     * @return 处理结果，包含解析结果、图谱操作结果、匹配结果等
     */
    @Operation(summary = "处理灾情/需求报告", description = "解析灾情需求文本，创建知识图谱节点，进行资源匹配调度")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "处理成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "解析失败")
    })
    @PostMapping("/process-report")
    public Result<Map<String, Object>> processReport(@RequestBody Map<String, Object> request) {
        try {
            String reportType = (String) request.get("reportType"); // disaster 或 demand
            String text = (String) request.get("text");
            String location = (String) request.get("location");
            String reporter = (String) request.get("reporter");
            
            if (reportType == null || text == null || text.trim().isEmpty()) {
                return Result.error(20001, "报告类型和文本内容不能为空");
            }
            
            Map<String, Object> result = new HashMap<>();
            
            // 1. 解析文本
            Map<String, Object> parseResult = textParseService.parseText(text, reportType);
            result.put("parseResult", parseResult);
            
            // 2. 在知识图谱中创建或更新节点
            Map<String, Object> graphResult = processKnowledgeGraph(reportType, parseResult, location, reporter);
            result.put("graphResult", graphResult);
            
            // 3. 如果是需求报告，进行资源匹配
            if ("demand".equals(reportType)) {
                Map<String, Object> matchingResult = performResourceMatching(parseResult, location);
                result.put("matchingResult", matchingResult);
            }
            
            result.put("status", "success");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return Result.success("报告处理成功", result);
            
        } catch (Exception e) {
            return Result.error(20001, "报告处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行资源匹配
     * 
     * <p>根据需求信息，计算与可用资源的相似度，并执行调度优化算法。</p>
     * 
     * @param request 匹配请求，包含需求信息和匹配参数
     * @return 匹配结果，包含相似度计算和调度方案
     */
    @Operation(summary = "执行资源匹配", description = "计算需求与资源的相似度，执行调度优化算法")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "匹配成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @PostMapping("/match-resources")
    public Result<Map<String, Object>> matchResources(@RequestBody Map<String, Object> request) {
        try {
            Long demandId = Long.valueOf(request.get("demandId").toString());
            String algorithm = (String) request.getOrDefault("algorithm", "greedy");
            Integer limit = (Integer) request.getOrDefault("limit", 10);
            
            // 获取需求信息
            Demand demand = demandService.getById(demandId);
            if (demand == null) {
                return Result.error(20002, "需求不存在");
            }
            
            // 获取可用资源
            List<Resource> resources = resourceService.getAvailableResources();
            
            // 计算相似度
            List<Map<String, Object>> similarityResults = similarityCalculationService
                .calculateBatchSimilarity(resources, demand, limit);
            
            // 执行调度优化
            Map<String, Object> schedulingResult = schedulingService
                .optimizeResourceAllocation(Arrays.asList(demand), resources, algorithm);
            
            // 计算调度效果评估
            Map<String, Object> metrics = schedulingService.calculateSchedulingMetrics(schedulingResult);
            
            // 保存匹配记录到数据库
            saveMatchingRecords(demandId, similarityResults);
            
            // 保存调度记录到数据库
            saveSchedulingRecords(demandId, schedulingResult);
            
            // 更新需求状态为已匹配
            updateDemandStatus(demandId, "matched");
            
            Map<String, Object> result = new HashMap<>();
            result.put("similarityResults", similarityResults);
            result.put("schedulingResult", schedulingResult);
            result.put("metrics", metrics);
            result.put("algorithm", algorithm);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return Result.success("资源匹配成功", result);
            
        } catch (Exception e) {
            return Result.error(20001, "资源匹配失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量资源调度
     * 
     * <p>对多个需求和资源进行批量调度优化。</p>
     * 
     * @param request 批量调度请求
     * @return 调度结果
     */
    @Operation(summary = "批量资源调度", description = "对多个需求和资源进行批量调度优化")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "调度成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @PostMapping("/batch-scheduling")
    public Result<Map<String, Object>> batchScheduling(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> demandIds = (List<Long>) request.get("demandIds");
            @SuppressWarnings("unchecked")
            List<Long> resourceIds = (List<Long>) request.get("resourceIds");
            String algorithm = (String) request.getOrDefault("algorithm", "greedy");
            
            if (demandIds == null || demandIds.isEmpty() || resourceIds == null || resourceIds.isEmpty()) {
                return Result.error(20001, "需求和资源ID列表不能为空");
            }
            
            // 获取需求和资源
            List<Demand> demands = demandIds.stream()
                .map(demandService::getById)
                .filter(demand -> demand != null)
                .collect(java.util.stream.Collectors.toList());
            
            List<Resource> resources = resourceIds.stream()
                .map(resourceService::getById)
                .filter(resource -> resource != null)
                .collect(java.util.stream.Collectors.toList());
            
            // 执行调度优化
            Map<String, Object> schedulingResult = schedulingService
                .optimizeResourceAllocation(demands, resources, algorithm);
            
            // 计算调度效果评估
            Map<String, Object> metrics = schedulingService.calculateSchedulingMetrics(schedulingResult);
            
            Map<String, Object> result = new HashMap<>();
            result.put("schedulingResult", schedulingResult);
            result.put("metrics", metrics);
            result.put("algorithm", algorithm);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return Result.success("批量调度成功", result);
            
        } catch (Exception e) {
            return Result.error(20001, "批量调度失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取相似度权重配置
     * 
     * @return 权重配置
     */
    @Operation(summary = "获取相似度权重配置", description = "获取各维度相似度的权重配置")
    @GetMapping("/similarity-weights")
    public Result<Map<String, Double>> getSimilarityWeights() {
        try {
            Map<String, Double> weights = similarityCalculationService.getSimilarityWeights();
            return Result.success("获取权重配置成功", weights);
        } catch (Exception e) {
            return Result.error(20001, "获取权重配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新相似度权重配置
     * 
     * @param weights 新的权重配置
     * @return 更新结果
     */
    @Operation(summary = "更新相似度权重配置", description = "更新各维度相似度的权重配置")
    @PutMapping("/similarity-weights")
    public Result<Map<String, Object>> updateSimilarityWeights(@RequestBody Map<String, Double> weights) {
        try {
            boolean success = similarityCalculationService.updateSimilarityWeights(weights);
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("weights", weights);
            
            if (success) {
                return Result.success("权重配置更新成功", result);
            } else {
                return Result.error(20001, "权重配置更新失败，请检查权重总和是否为1.0");
            }
        } catch (Exception e) {
            return Result.error(20001, "权重配置更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取调度历史
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量限制
     * @return 调度历史
     */
    @Operation(summary = "获取调度历史", description = "获取指定时间范围内的调度历史记录")
    @GetMapping("/scheduling-history")
    public Result<List<Map<String, Object>>> getSchedulingHistory(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            List<com.disaster.emergency.entity.SchedulingRecord> records = 
                schedulingService.getSchedulingHistory(startTime, endTime, limit);
            
            List<Map<String, Object>> result = records.stream()
                .map(record -> {
                    Map<String, Object> recordMap = new HashMap<>();
                    recordMap.put("id", record.getId());
                    recordMap.put("algorithm", "unknown"); // SchedulingRecord没有algorithm字段
                    recordMap.put("status", record.getStatus());
                    recordMap.put("createTime", record.getCreateTime());
                    return recordMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return Result.success("获取调度历史成功", result);
        } catch (Exception e) {
            return Result.error(20001, "获取调度历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理知识图谱操作
     */
    private Map<String, Object> processKnowledgeGraph(String reportType, Map<String, Object> parseResult, 
                                                     String location, String reporter) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据解析结果创建或更新节点
            String nodeType = "demand".equals(reportType) ? "demand" : "disaster";
            String nodeName = generateNodeName(parseResult, location);
            Map<String, Object> properties = new HashMap<>(parseResult);
            properties.put("location", location);
            properties.put("reporter", reporter);
            
            // 检查是否已存在相同节点
            Long businessId = generateBusinessId(reportType, parseResult);
            com.disaster.emergency.entity.KnowledgeNode existingNode = knowledgeGraphService.getNodeByBusinessId(nodeType, businessId);
            
            if (existingNode != null) {
                // 更新现有节点
                knowledgeGraphService.updateNodeProperties(existingNode.getId(), properties);
                result.put("action", "updated");
                result.put("nodeId", existingNode.getId());
            } else {
                // 创建新节点
                Long nodeId = knowledgeGraphService.createNode(nodeType, businessId, nodeName, properties);
                result.put("action", "created");
                result.put("nodeId", nodeId);
            }
            
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 执行资源匹配
     */
    private Map<String, Object> performResourceMatching(Map<String, Object> parseResult, String location) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建真实需求对象并保存到数据库
            Demand demand = createRealDemand(parseResult, location);
            demand = demandService.submitDemand(demand);
            Long demandId = demand.getId();
            
            // 获取可用资源
            List<Resource> resources = resourceService.getAvailableResources();
            
            // 计算相似度
            List<Map<String, Object>> similarityResults = similarityCalculationService
                .calculateBatchSimilarity(resources, demand, 10);
            
            // 执行调度优化
            Map<String, Object> schedulingResult = schedulingService
                .optimizeResourceAllocation(Arrays.asList(demand), resources, "greedy");
            
            // 保存匹配记录到数据库
            saveMatchingRecords(demandId, similarityResults);
            
            // 保存调度记录到数据库
            saveSchedulingRecords(demandId, schedulingResult);
            
            // 更新需求状态为已匹配
            updateDemandStatus(demandId, "matched");
            
            result.put("demandId", demandId);
            result.put("similarityResults", similarityResults);
            result.put("schedulingResult", schedulingResult);
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 生成节点名称
     */
    private String generateNodeName(Map<String, Object> parseResult, String location) {
        StringBuilder name = new StringBuilder();
        
        if (parseResult.containsKey("disaster_type")) {
            name.append(parseResult.get("disaster_type"));
        }
        if (parseResult.containsKey("demand_type")) {
            name.append(parseResult.get("demand_type"));
        }
        if (location != null) {
            name.append("-").append(location);
        }
        
        return name.length() > 0 ? name.toString() : "未知类型";
    }
    
    /**
     * 生成业务ID
     */
    private Long generateBusinessId(String reportType, Map<String, Object> parseResult) {
        // 简单的业务ID生成逻辑，实际应该根据具体业务规则
        return System.currentTimeMillis() % 1000000;
    }
    
    
    /**
     * 创建真实需求对象
     */
    private Demand createRealDemand(Map<String, Object> parseResult, String location) {
        Demand demand = new Demand();
        
        // 设置需求类型
        String demandType = (String) parseResult.getOrDefault("demand_type", "未知");
        demand.setDemandType(demandType != null && !demandType.trim().isEmpty() ? demandType : "未知");
        
        // 设置需求数量
        Integer quantity = (Integer) parseResult.getOrDefault("quantity", 1);
        demand.setQuantity(quantity != null && quantity > 0 ? quantity : 1);
        
        // 设置单位
        String unit = (String) parseResult.getOrDefault("unit", "个");
        demand.setUnit(unit != null && !unit.trim().isEmpty() ? unit : "个");
        
        // 设置紧急程度
        String urgency = (String) parseResult.getOrDefault("urgency", "中");
        demand.setUrgency(urgency != null && !urgency.trim().isEmpty() ? urgency : "中");
        
        // 解析location到province, city, district
        if (location != null && !location.trim().isEmpty()) {
            String[] locationParts = location.split("省|市|区|县");
            if (locationParts.length > 0) {
                demand.setProvince(locationParts[0] + "省");
            } else {
                demand.setProvince("未知");
            }
            if (locationParts.length > 1) {
                demand.setCity(locationParts[1] + "市");
            } else {
                demand.setCity("未知");
            }
            if (locationParts.length > 2) {
                demand.setDistrict(locationParts[2] + "区");
            } else {
                demand.setDistrict("未知");
            }
        } else {
            // 默认位置
            demand.setProvince("未知");
            demand.setCity("未知");
            demand.setDistrict("未知");
        }
        
        // 确保所有必填字段都有值
        if (demand.getProvince() == null || demand.getProvince().trim().isEmpty()) {
            demand.setProvince("未知");
        }
        if (demand.getCity() == null || demand.getCity().trim().isEmpty()) {
            demand.setCity("未知");
        }
        if (demand.getDistrict() == null || demand.getDistrict().trim().isEmpty()) {
            demand.setDistrict("未知");
        }
        
        // 设置描述信息
        StringBuilder description = new StringBuilder();
        description.append("需求描述: ").append(parseResult.getOrDefault("description", "无"));
        description.append("; 优先级: ").append(parseResult.getOrDefault("priority", "中"));
        if (parseResult.containsKey("contact")) {
            description.append("; 联系方式: ").append(parseResult.get("contact"));
        }
        demand.setDescription(description.toString());
        
        // 设置初始状态
        demand.setStatus("pending");
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        demand.setCreateTime(now);
        demand.setUpdateTime(now);
        
        // 设置灾情ID（如果有的话）
        if (parseResult.containsKey("disaster_id")) {
            try {
                Long disasterId = Long.valueOf(parseResult.get("disaster_id").toString());
                demand.setDisasterId(disasterId != null && disasterId > 0 ? disasterId : 1L);
            } catch (Exception e) {
                demand.setDisasterId(1L);
            }
        } else {
            // 默认关联到第一个灾情记录，实际应用中应该根据业务逻辑确定
            demand.setDisasterId(1L);
        }
        
        return demand;
    }
    
    /**
     * 保存匹配记录到数据库
     */
    private void saveMatchingRecords(Long demandId, List<Map<String, Object>> similarityResults) {
        try {
            System.out.println("开始保存匹配记录，需求ID: " + demandId + ", 相似度结果数量: " + similarityResults.size());
            
            for (Map<String, Object> similarityResult : similarityResults) {
                MatchingRecord matchingRecord = new MatchingRecord();
                matchingRecord.setDemandId(demandId);
                
                // 从similarityResult中获取resource信息
                @SuppressWarnings("unchecked")
                Map<String, Object> resource = (Map<String, Object>) similarityResult.get("resource");
                if (resource != null) {
                    Long resourceId = Long.valueOf(resource.get("id").toString());
                    matchingRecord.setResourceId(resourceId);
                    System.out.println("处理资源ID: " + resourceId);
                } else {
                    System.err.println("相似度结果中缺少resource信息");
                    continue;
                }
                
                // 设置匹配分数
                Double similarity = (Double) similarityResult.get("similarity");
                if (similarity != null) {
                    matchingRecord.setMatchScore(new BigDecimal(similarity));
                } else {
                    matchingRecord.setMatchScore(new BigDecimal(0));
                }
                
                // 生成匹配原因
                String matchReason = generateMatchReason(similarityResult);
                matchingRecord.setMatchReason(matchReason);
                
                matchingRecord.setStatus("pending");
                matchingRecord.setCreateTime(LocalDateTime.now());
                matchingRecord.setUpdateTime(LocalDateTime.now());
                
                // 保存到数据库
                boolean saved = matchingService.save(matchingRecord);
                if (saved) {
                    System.out.println("匹配记录保存成功，资源ID: " + matchingRecord.getResourceId() + 
                                     ", 匹配分数: " + matchingRecord.getMatchScore());
                } else {
                    System.err.println("匹配记录保存失败，资源ID: " + matchingRecord.getResourceId());
                }
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("保存匹配记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 保存调度记录到数据库
     */
    private void saveSchedulingRecords(Long demandId, Map<String, Object> schedulingResult) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocations = (List<Map<String, Object>>) schedulingResult.get("allocations");
            
            System.out.println("开始保存调度记录，需求ID: " + demandId + ", 分配数量: " + (allocations != null ? allocations.size() : 0));
            
            if (allocations != null && !allocations.isEmpty()) {
                for (Map<String, Object> allocation : allocations) {
                    SchedulingRecord schedulingRecord = new SchedulingRecord();
                    schedulingRecord.setDemandId(demandId);
                    
                    Long resourceId = Long.valueOf(allocation.get("resourceId").toString());
                    schedulingRecord.setResourceId(resourceId);
                    System.out.println("处理调度记录，资源ID: " + resourceId);
                    
                    // 获取需求信息以确定分配数量
                    Demand demand = demandService.getById(demandId);
                    if (demand != null) {
                        schedulingRecord.setAllocatedQuantity(demand.getQuantity());
                        System.out.println("分配数量: " + demand.getQuantity());
                    } else {
                        schedulingRecord.setAllocatedQuantity(1); // 默认数量
                        System.out.println("使用默认分配数量: 1");
                    }
                    
                    // 系统自动调度
                    schedulingRecord.setSchedulerId(1L);
                    schedulingRecord.setSchedulerName("系统自动调度");
                    schedulingRecord.setRemark("自动匹配生成");
                    schedulingRecord.setSchedulingTime(LocalDateTime.now());
                    schedulingRecord.setStatus("pending");
                    schedulingRecord.setCreateTime(LocalDateTime.now());
                    schedulingRecord.setUpdateTime(LocalDateTime.now());
                    
                    // 保存到数据库
                    boolean saved = schedulingService.saveSchedulingRecord(schedulingRecord);
                    if (saved) {
                        System.out.println("调度记录保存成功，资源ID: " + resourceId + 
                                         ", 分配数量: " + schedulingRecord.getAllocatedQuantity());
                        
                        // 更新资源数量
                        updateResourceQuantity(schedulingRecord.getResourceId(), schedulingRecord.getAllocatedQuantity());
                        
                        // 调度记录保存成功后，更新需求状态为已分配
                        updateDemandStatus(demandId, "allocated");
                    } else {
                        System.err.println("调度记录保存失败，资源ID: " + resourceId);
                    }
                }
            } else {
                System.out.println("没有分配记录需要保存");
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("保存调度记录失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成匹配原因
     */
    private String generateMatchReason(Map<String, Object> similarityResult) {
        StringBuilder reason = new StringBuilder();
        
        // 类型相似度
        Double typeSimilarity = (Double) similarityResult.get("typeSimilarity");
        if (typeSimilarity != null && typeSimilarity > 0) {
            reason.append("类型匹配度: ").append(typeSimilarity).append("%; ");
        }
        
        // 数量相似度
        Double quantitySimilarity = (Double) similarityResult.get("quantitySimilarity");
        if (quantitySimilarity != null) {
            reason.append("数量匹配度: ").append(quantitySimilarity).append("%; ");
        }
        
        // 距离相似度
        Double distanceSimilarity = (Double) similarityResult.get("distanceSimilarity");
        if (distanceSimilarity != null) {
            reason.append("距离匹配度: ").append(distanceSimilarity).append("%; ");
        }
        
        // 优先级相似度
        Double prioritySimilarity = (Double) similarityResult.get("prioritySimilarity");
        if (prioritySimilarity != null) {
            reason.append("优先级匹配度: ").append(prioritySimilarity).append("%; ");
        }
        
        // 时效性相似度
        Double timelinessSimilarity = (Double) similarityResult.get("timelinessSimilarity");
        if (timelinessSimilarity != null) {
            reason.append("时效性匹配度: ").append(timelinessSimilarity).append("%; ");
        }
        
        // 总体相似度
        Double totalSimilarity = (Double) similarityResult.get("similarity");
        if (totalSimilarity != null) {
            reason.append("总体相似度: ").append(totalSimilarity).append("%");
        }
        
        return reason.length() > 0 ? reason.toString() : "自动匹配";
    }
    
    /**
     * 更新需求状态
     */
    private void updateDemandStatus(Long demandId, String status) {
        try {
            boolean success = demandService.updateDemandStatus(demandId, status);
            if (success) {
                System.out.println("需求ID " + demandId + " 状态已更新为: " + status);
            } else {
                System.err.println("需求ID " + demandId + " 状态更新失败");
            }
        } catch (Exception e) {
            System.err.println("更新需求状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新资源数量
     */
    private void updateResourceQuantity(Long resourceId, Integer allocatedQuantity) {
        try {
            // 获取当前资源信息
            Resource resource = resourceService.getById(resourceId);
            if (resource != null) {
                // 计算新的可用数量
                Integer currentAvailable = resource.getAvailableQuantity();
                Integer newAvailable = currentAvailable - allocatedQuantity;
                
                // 确保可用数量不为负数
                if (newAvailable < 0) {
                    newAvailable = 0;
                }
                
                // 更新资源数量
                boolean success = resourceService.updateQuantity(resourceId, newAvailable);
                if (success) {
                    System.out.println("资源ID " + resourceId + " 数量已更新: " + currentAvailable + " -> " + newAvailable);
                } else {
                    System.err.println("资源ID " + resourceId + " 数量更新失败");
                }
            } else {
                System.err.println("资源ID " + resourceId + " 不存在");
            }
        } catch (Exception e) {
            System.err.println("更新资源数量失败: " + e.getMessage());
        }
    }
}
