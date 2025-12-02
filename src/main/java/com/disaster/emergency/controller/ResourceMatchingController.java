package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.MatchingRecord;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.SchedulingRecord;
import com.disaster.emergency.mapper.SchedulingRecordMapper;
import com.disaster.emergency.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    @Autowired
    private DisasterService disasterService;
    
    @Autowired
    private SchedulingRecordMapper schedulingRecordMapper;
    
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
            Double latitude = null;
            Double longitude = null;
            Long disasterId = null;
            
            if (request.get("latitude") != null) {
                try { latitude = Double.valueOf(request.get("latitude").toString()); } catch (Exception ignore) {}
            }
            if (request.get("longitude") != null) {
                try { longitude = Double.valueOf(request.get("longitude").toString()); } catch (Exception ignore) {}
            }
            if (request.get("disasterId") != null) {
                try { disasterId = Long.valueOf(request.get("disasterId").toString()); } catch (Exception ignore) {}
            }
            
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
            
            // 3. 如果是需求报告，进行资源匹配（并保存经纬度）
            if ("demand".equals(reportType)) {
                Map<String, Object> matchingResult = performResourceMatching(parseResult, location, latitude, longitude, disasterId);
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
            
            boolean hasMatches = similarityResults != null && !similarityResults.isEmpty();
            
            // 执行调度优化（仅当存在候选匹配时）
            Map<String, Object> schedulingResult = schedulingService
                .optimizeResourceAllocation(Arrays.asList(demand), resources, algorithm);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocations = (List<Map<String, Object>>) schedulingResult.get("allocations");
            boolean hasAllocations = allocations != null && !allocations.isEmpty();
            
            // 计算调度效果评估
            Map<String, Object> metrics = schedulingService.calculateSchedulingMetrics(schedulingResult);
            
            // 仅当有匹配结果时才保存匹配记录
            if (hasMatches) {
                saveMatchingRecords(demandId, similarityResults);
                // 有匹配结果，更新为matched
                updateDemandStatus(demandId, "matched");
            } else {
                // 无匹配结果，更新为match_failed
                updateDemandStatus(demandId, "match_failed");
            }
            
            // 仅当有调度分配时才保存调度记录，具体状态在保存逻辑中根据分配数量判断
            if (hasAllocations) {
                saveSchedulingRecords(demandId, schedulingResult);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("similarityResults", similarityResults);
            result.put("schedulingResult", schedulingResult);
            result.put("metrics", metrics);
            result.put("algorithm", algorithm);
            result.put("hasMatches", hasMatches);
            result.put("hasAllocations", hasAllocations);
            result.put("schedulingStatus", "ended");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if (!hasMatches) {
                return Result.error(20001, "资源匹配失败: 无可用资源");
            }
            
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
     * 测试知识图谱关系创建功能
     * 
     * @param request 测试请求
     * @return 测试结果
     */
    @Operation(summary = "测试知识图谱关系创建", description = "测试process-report接口中的知识图谱关系创建功能")
    @PostMapping("/test-knowledge-graph-relations")
    public Result<Map<String, Object>> testKnowledgeGraphRelations(@RequestBody Map<String, Object> request) {
        try {
            String reportType = (String) request.get("reportType");
            String text = (String) request.get("text");
            String location = (String) request.get("location");
            String reporter = (String) request.get("reporter");
            
            if (reportType == null || text == null || text.trim().isEmpty()) {
                return Result.error(20001, "报告类型和文本内容不能为空");
            }
            
            // 解析文本
            Map<String, Object> parseResult = textParseService.parseText(text, reportType);
            
            // 处理知识图谱操作（包含关系创建）
            Map<String, Object> graphResult = processKnowledgeGraph(reportType, parseResult, location, reporter);
            
            Map<String, Object> result = new HashMap<>();
            result.put("parseResult", parseResult);
            result.put("graphResult", graphResult);
            result.put("testTime", java.time.LocalDateTime.now().toString());
            
            return Result.success("知识图谱关系创建测试成功", result);
            
        } catch (Exception e) {
            return Result.error(20001, "知识图谱关系创建测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理知识图谱操作
     */
    private Map<String, Object> processKnowledgeGraph(String reportType, Map<String, Object> parseResult, 
                                                     String location, String reporter) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> createdRelations = new ArrayList<>();
        
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
            
            Long currentNodeId;
            if (existingNode != null) {
                // 更新现有节点
                knowledgeGraphService.updateNodeProperties(existingNode.getId(), properties);
                result.put("action", "updated");
                result.put("nodeId", existingNode.getId());
                currentNodeId = existingNode.getId();
            } else {
                // 创建新节点
                Long nodeId = knowledgeGraphService.createNode(nodeType, businessId, nodeName, properties);
                result.put("action", "created");
                result.put("nodeId", nodeId);
                currentNodeId = nodeId;
            }
            
            // 创建知识图谱关系
            createdRelations = createKnowledgeGraphRelations(reportType, parseResult, currentNodeId, location);
            
            result.put("status", "success");
            result.put("createdRelations", createdRelations);
            result.put("relationCount", createdRelations.size());
            
        } catch (Exception e) {
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("createdRelations", createdRelations);
        }
        
        return result;
    }
    
    /**
     * 执行资源匹配
     */
    private Map<String, Object> performResourceMatching(Map<String, Object> parseResult, String location, Double latitude, Double longitude, Long disasterId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建真实需求对象并保存到数据库
            Demand demand = createRealDemand(parseResult, location, latitude, longitude, disasterId);
            if (demand == null) {
                result.put("status", "failed");
                result.put("error", "无法找到关联的灾情，请先选择或创建灾情");
                return result;
            }
            demand = demandService.submitDemand(demand);
            Long demandId = demand.getId();
            
            // 获取可用资源
            List<Resource> resources = resourceService.getAvailableResources();
            
            // 计算相似度
            List<Map<String, Object>> similarityResults = similarityCalculationService
                .calculateBatchSimilarity(resources, demand, 10);
            boolean hasMatches = similarityResults != null && !similarityResults.isEmpty();
            
            // 执行调度优化
            Map<String, Object> schedulingResult = schedulingService
                .optimizeResourceAllocation(Arrays.asList(demand), resources, "greedy");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allocations = (List<Map<String, Object>>) schedulingResult.get("allocations");
            boolean hasAllocations = allocations != null && !allocations.isEmpty();
            
            // 仅当有匹配结果时才保存匹配记录
            if (hasMatches) {
                saveMatchingRecords(demandId, similarityResults);
                updateDemandStatus(demandId, "matched");
            } else {
                updateDemandStatus(demandId, "match_failed");
            }
            
            // 仅当有调度分配时才保存调度记录，具体状态在保存逻辑中根据分配数量判断
            if (hasAllocations) {
                saveSchedulingRecords(demandId, schedulingResult);
            }
            
            result.put("demandId", demandId);
            result.put("similarityResults", similarityResults);
            result.put("schedulingResult", schedulingResult);
            result.put("hasMatches", hasMatches);
            result.put("hasAllocations", hasAllocations);
            result.put("schedulingStatus", "ended");
            result.put("status", hasMatches ? "success" : "failed");
            
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
     * 创建知识图谱关系
     */
    private List<Map<String, Object>> createKnowledgeGraphRelations(String reportType, Map<String, Object> parseResult, 
                                                                   Long currentNodeId, String location) {
        List<Map<String, Object>> createdRelations = new ArrayList<>();
        
        try {
            // 1. 创建需求与灾情的关系
            if ("demand".equals(reportType)) {
                createDemandDisasterRelation(parseResult, currentNodeId, createdRelations);
            }
            
            // 2. 创建需求与资源类型的关系
            if ("demand".equals(reportType)) {
                createDemandResourceTypeRelation(parseResult, currentNodeId, createdRelations);
            }
            
            // 3. 创建灾情与地理位置的关系
            if ("disaster".equals(reportType)) {
                createDisasterLocationRelation(parseResult, currentNodeId, location, createdRelations);
            }
            
            // 4. 创建需求与地理位置的关系
            if ("demand".equals(reportType)) {
                createDemandLocationRelation(parseResult, currentNodeId, location, createdRelations);
            }
            
            // 5. 创建需求与紧急程度的关系
            if ("demand".equals(reportType)) {
                createDemandUrgencyRelation(parseResult, currentNodeId, createdRelations);
            }
            
            // 6. 创建灾情与严重程度的关系
            if ("disaster".equals(reportType)) {
                createDisasterSeverityRelation(parseResult, currentNodeId, createdRelations);
            }
            
        } catch (Exception e) {
            System.err.println("创建知识图谱关系时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return createdRelations;
    }
    
    /**
     * 创建需求与灾情的关系
     */
    private void createDemandDisasterRelation(Map<String, Object> parseResult, Long demandNodeId, 
                                            List<Map<String, Object>> createdRelations) {
        try {
            // 查找相关的灾情节点
            String disasterType = (String) parseResult.get("disaster_type");
            if (disasterType != null && !disasterType.trim().isEmpty()) {
                // 查找相同类型的灾情节点
                List<com.disaster.emergency.entity.KnowledgeNode> disasterNodes = 
                    findNodesByTypeAndProperty("disaster", "disaster_type", disasterType);
                
                for (com.disaster.emergency.entity.KnowledgeNode disasterNode : disasterNodes) {
                    if (!disasterNode.getId().equals(demandNodeId)) {
                        Map<String, Object> relationProperties = new HashMap<>();
                        relationProperties.put("relationReason", "需求由灾情引发");
                        relationProperties.put("disasterType", disasterType);
                        relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                        
                        Long relationId = knowledgeGraphService.createRelation(
                            disasterNode.getId(), 
                            demandNodeId, 
                            "triggers", 
                            0.8, 
                            relationProperties
                        );
                        
                        Map<String, Object> relationInfo = new HashMap<>();
                        relationInfo.put("relationId", relationId);
                        relationInfo.put("relationType", "triggers");
                        relationInfo.put("sourceNodeId", disasterNode.getId());
                        relationInfo.put("targetNodeId", demandNodeId);
                        relationInfo.put("weight", 0.8);
                        createdRelations.add(relationInfo);
                        
                        System.out.println("创建需求-灾情关系: " + disasterNode.getId() + " -> " + demandNodeId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("创建需求-灾情关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建需求与资源类型的关系
     */
    private void createDemandResourceTypeRelation(Map<String, Object> parseResult, Long demandNodeId, 
                                                List<Map<String, Object>> createdRelations) {
        try {
            String demandType = (String) parseResult.get("demand_type");
            if (demandType != null && !demandType.trim().isEmpty()) {
                // 查找相同类型的资源节点
                List<com.disaster.emergency.entity.KnowledgeNode> resourceNodes = 
                    findNodesByTypeAndProperty("resource", "resource_type", demandType);
                
                for (com.disaster.emergency.entity.KnowledgeNode resourceNode : resourceNodes) {
                    Map<String, Object> relationProperties = new HashMap<>();
                    relationProperties.put("relationReason", "需求匹配资源类型");
                    relationProperties.put("demandType", demandType);
                    relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                    
                    Long relationId = knowledgeGraphService.createRelation(
                        demandNodeId, 
                        resourceNode.getId(), 
                        "requires", 
                        0.9, 
                        relationProperties
                    );
                    
                    Map<String, Object> relationInfo = new HashMap<>();
                    relationInfo.put("relationId", relationId);
                    relationInfo.put("relationType", "requires");
                    relationInfo.put("sourceNodeId", demandNodeId);
                    relationInfo.put("targetNodeId", resourceNode.getId());
                    relationInfo.put("weight", 0.9);
                    createdRelations.add(relationInfo);
                    
                    System.out.println("创建需求-资源关系: " + demandNodeId + " -> " + resourceNode.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("创建需求-资源关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建灾情与地理位置的关系
     */
    private void createDisasterLocationRelation(Map<String, Object> parseResult, Long disasterNodeId, 
                                              String location, List<Map<String, Object>> createdRelations) {
        try {
            if (location != null && !location.trim().isEmpty()) {
                // 查找相同地理位置的节点
                List<com.disaster.emergency.entity.KnowledgeNode> locationNodes = 
                    findNodesByProperty("location", location);
                
                for (com.disaster.emergency.entity.KnowledgeNode locationNode : locationNodes) {
                    if (!locationNode.getId().equals(disasterNodeId)) {
                        Map<String, Object> relationProperties = new HashMap<>();
                        relationProperties.put("relationReason", "相同地理位置");
                        relationProperties.put("location", location);
                        relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                        
                        Long relationId = knowledgeGraphService.createRelation(
                            disasterNodeId, 
                            locationNode.getId(), 
                            "located_in", 
                            0.7, 
                            relationProperties
                        );
                        
                        Map<String, Object> relationInfo = new HashMap<>();
                        relationInfo.put("relationId", relationId);
                        relationInfo.put("relationType", "located_in");
                        relationInfo.put("sourceNodeId", disasterNodeId);
                        relationInfo.put("targetNodeId", locationNode.getId());
                        relationInfo.put("weight", 0.7);
                        createdRelations.add(relationInfo);
                        
                        System.out.println("创建灾情-地理位置关系: " + disasterNodeId + " -> " + locationNode.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("创建灾情-地理位置关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建需求与地理位置的关系
     */
    private void createDemandLocationRelation(Map<String, Object> parseResult, Long demandNodeId, 
                                            String location, List<Map<String, Object>> createdRelations) {
        try {
            if (location != null && !location.trim().isEmpty()) {
                // 查找相同地理位置的需求节点
                List<com.disaster.emergency.entity.KnowledgeNode> locationNodes = 
                    findNodesByTypeAndProperty("demand", "location", location);
                
                for (com.disaster.emergency.entity.KnowledgeNode locationNode : locationNodes) {
                    if (!locationNode.getId().equals(demandNodeId)) {
                        Map<String, Object> relationProperties = new HashMap<>();
                        relationProperties.put("relationReason", "相同地理位置需求");
                        relationProperties.put("location", location);
                        relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                        
                        Long relationId = knowledgeGraphService.createRelation(
                            demandNodeId, 
                            locationNode.getId(), 
                            "co_located", 
                            0.6, 
                            relationProperties
                        );
                        
                        Map<String, Object> relationInfo = new HashMap<>();
                        relationInfo.put("relationId", relationId);
                        relationInfo.put("relationType", "co_located");
                        relationInfo.put("sourceNodeId", demandNodeId);
                        relationInfo.put("targetNodeId", locationNode.getId());
                        relationInfo.put("weight", 0.6);
                        createdRelations.add(relationInfo);
                        
                        System.out.println("创建需求-地理位置关系: " + demandNodeId + " -> " + locationNode.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("创建需求-地理位置关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建需求与紧急程度的关系
     */
    private void createDemandUrgencyRelation(Map<String, Object> parseResult, Long demandNodeId, 
                                           List<Map<String, Object>> createdRelations) {
        try {
            String urgency = (String) parseResult.get("urgency");
            if (urgency != null && !urgency.trim().isEmpty()) {
                // 查找相同紧急程度的需求节点
                List<com.disaster.emergency.entity.KnowledgeNode> urgencyNodes = 
                    findNodesByTypeAndProperty("demand", "urgency", urgency);
                
                for (com.disaster.emergency.entity.KnowledgeNode urgencyNode : urgencyNodes) {
                    if (!urgencyNode.getId().equals(demandNodeId)) {
                        Map<String, Object> relationProperties = new HashMap<>();
                        relationProperties.put("relationReason", "相同紧急程度");
                        relationProperties.put("urgency", urgency);
                        relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                        
                        Long relationId = knowledgeGraphService.createRelation(
                            demandNodeId, 
                            urgencyNode.getId(), 
                            "same_urgency", 
                            0.5, 
                            relationProperties
                        );
                        
                        Map<String, Object> relationInfo = new HashMap<>();
                        relationInfo.put("relationId", relationId);
                        relationInfo.put("relationType", "same_urgency");
                        relationInfo.put("sourceNodeId", demandNodeId);
                        relationInfo.put("targetNodeId", urgencyNode.getId());
                        relationInfo.put("weight", 0.5);
                        createdRelations.add(relationInfo);
                        
                        System.out.println("创建需求-紧急程度关系: " + demandNodeId + " -> " + urgencyNode.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("创建需求-紧急程度关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建灾情与严重程度的关系
     */
    private void createDisasterSeverityRelation(Map<String, Object> parseResult, Long disasterNodeId, 
                                              List<Map<String, Object>> createdRelations) {
        try {
            String severity = (String) parseResult.get("severity");
            if (severity != null && !severity.trim().isEmpty()) {
                // 查找相同严重程度的灾情节点
                List<com.disaster.emergency.entity.KnowledgeNode> severityNodes = 
                    findNodesByTypeAndProperty("disaster", "severity", severity);
                
                for (com.disaster.emergency.entity.KnowledgeNode severityNode : severityNodes) {
                    if (!severityNode.getId().equals(disasterNodeId)) {
                        Map<String, Object> relationProperties = new HashMap<>();
                        relationProperties.put("relationReason", "相同严重程度");
                        relationProperties.put("severity", severity);
                        relationProperties.put("createTime", java.time.LocalDateTime.now().toString());
                        
                        Long relationId = knowledgeGraphService.createRelation(
                            disasterNodeId, 
                            severityNode.getId(), 
                            "same_severity", 
                            0.5, 
                            relationProperties
                        );
                        
                        Map<String, Object> relationInfo = new HashMap<>();
                        relationInfo.put("relationId", relationId);
                        relationInfo.put("relationType", "same_severity");
                        relationInfo.put("sourceNodeId", disasterNodeId);
                        relationInfo.put("targetNodeId", severityNode.getId());
                        relationInfo.put("weight", 0.5);
                        createdRelations.add(relationInfo);
                        
                        System.out.println("创建灾情-严重程度关系: " + disasterNodeId + " -> " + severityNode.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("创建灾情-严重程度关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据节点类型和属性查找节点
     */
    private List<com.disaster.emergency.entity.KnowledgeNode> findNodesByTypeAndProperty(String nodeType, 
                                                                                        String propertyKey, 
                                                                                        String propertyValue) {
        try {
            return knowledgeGraphService.findNodesByTypeAndProperty(nodeType, propertyKey, propertyValue);
        } catch (Exception e) {
            System.err.println("查找节点失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据属性查找节点
     */
    private List<com.disaster.emergency.entity.KnowledgeNode> findNodesByProperty(String propertyKey, 
                                                                                 String propertyValue) {
        try {
            return knowledgeGraphService.findNodesByProperty(propertyKey, propertyValue);
        } catch (Exception e) {
            System.err.println("查找节点失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    
    /**
     * 创建真实需求对象
     * @param parseResult 解析结果
     * @param location 位置信息
     * @param latitude 纬度
     * @param longitude 经度
     * @param disasterId 前端传入的灾情ID（可选）
     * @return 需求对象，如果无法找到关联的灾情则返回null
     */
    private Demand createRealDemand(Map<String, Object> parseResult, String location, Double latitude, Double longitude, Long disasterId) {
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
        String province = null;
        String city = null;
        String district = null;
        if (location != null && !location.trim().isEmpty()) {
            String[] locationParts = location.split("省|市|区|县");
            if (locationParts.length > 0) {
                province = locationParts[0] + "省";
            }
            if (locationParts.length > 1) {
                city = locationParts[1] + "市";
            }
            if (locationParts.length > 2) {
                district = locationParts[2] + "区";
            }
        }
        
        // 设置位置信息
        demand.setProvince(province != null && !province.trim().isEmpty() ? province : "未知");
        demand.setCity(city != null && !city.trim().isEmpty() ? city : "未知");
        demand.setDistrict(district != null && !district.trim().isEmpty() ? district : "未知");
        
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
        
        // 经纬度
        if (latitude != null && latitude >= -90.0 && latitude <= 90.0) {
            demand.setLatitude(latitude);
        }
        if (longitude != null && longitude >= -180.0 && longitude <= 180.0) {
            demand.setLongitude(longitude);
        }
        
        // 设置初始状态
        demand.setStatus("pending");
        
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        demand.setCreateTime(now);
        demand.setUpdateTime(now);
        
        // 关联灾情ID：优先使用前端传入的disasterId，否则根据位置和灾害类型查找已有灾情
        Long finalDisasterId = null;
        
        // 1. 优先使用前端传入的disasterId
        if (disasterId != null && disasterId > 0) {
            Disaster specifiedDisaster = disasterService.getById(disasterId);
            if (specifiedDisaster != null) {
                finalDisasterId = disasterId;
            }
        }
        
        // 2. 如果前端没有传入或传入的无效，尝试从parseResult中获取
        if (finalDisasterId == null && parseResult.containsKey("disaster_id")) {
            try {
                Long parsedId = Long.valueOf(parseResult.get("disaster_id").toString());
                if (parsedId != null && parsedId > 0) {
                    Disaster parsedDisaster = disasterService.getById(parsedId);
                    if (parsedDisaster != null) {
                        finalDisasterId = parsedId;
                    }
                }
            } catch (Exception ignore) {
                // 忽略解析错误
            }
        }
        
        // 3. 如果还没有找到，根据位置和灾害类型查找已有的灾情
        if (finalDisasterId == null) {
            String disasterType = (String) parseResult.getOrDefault("disaster_type", null);
            finalDisasterId = findExistingDisaster(province, city, district, disasterType);
        }
        
        // 4. 如果仍然找不到，返回null，让调用方处理
        if (finalDisasterId == null) {
            return null;
        }
        
        demand.setDisasterId(finalDisasterId);
        
        return demand;
    }
    
    /**
     * 根据位置和灾害类型查找已有的灾情
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param disasterType 灾害类型
     * @return 找到的灾情ID，如果找不到则返回null
     */
    private Long findExistingDisaster(String province, String city, String district, String disasterType) {
        try {
            // 使用QueryWrapper查询
            QueryWrapper<Disaster> queryWrapper = new QueryWrapper<>();
            
            // 优先匹配精确位置
            if (province != null && !province.trim().isEmpty() && !province.equals("未知")) {
                queryWrapper.eq("province", province);
            }
            if (city != null && !city.trim().isEmpty() && !city.equals("未知")) {
                queryWrapper.eq("city", city);
            }
            if (district != null && !district.trim().isEmpty() && !district.equals("未知")) {
                queryWrapper.eq("district", district);
            }
            
            // 如果指定了灾害类型，也作为查询条件
            if (disasterType != null && !disasterType.trim().isEmpty() && !disasterType.equals("未知")) {
                queryWrapper.eq("disaster_type", disasterType);
            }
            
            // 只查询活跃状态的灾情
            queryWrapper.eq("status", "active");
            
            // 按创建时间倒序，取最新的
            queryWrapper.orderByDesc("create_time");
            queryWrapper.last("LIMIT 1");
            
            List<Disaster> disasters = disasterService.list(queryWrapper);
            if (disasters != null && !disasters.isEmpty()) {
                return disasters.get(0).getId();
            }
            
            // 如果精确匹配没找到，放宽条件：只匹配省份和城市
            if (province != null && !province.trim().isEmpty() && !province.equals("未知") &&
                city != null && !city.trim().isEmpty() && !city.equals("未知")) {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("province", province);
                queryWrapper.eq("city", city);
                if (disasterType != null && !disasterType.trim().isEmpty() && !disasterType.equals("未知")) {
                    queryWrapper.eq("disaster_type", disasterType);
                }
                queryWrapper.eq("status", "active");
                queryWrapper.orderByDesc("create_time");
                queryWrapper.last("LIMIT 1");
                
                disasters = disasterService.list(queryWrapper);
                if (disasters != null && !disasters.isEmpty()) {
                    return disasters.get(0).getId();
                }
            }
            
        } catch (Exception e) {
            System.err.println("查找已有灾情时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
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
            
            System.out.println("开始保存调度记录，需求ID: " + demandId + ", 分配条目数量: " + (allocations != null ? allocations.size() : 0));
            
            if (allocations != null && !allocations.isEmpty()) {
                // 获取需求信息，用于确定目标数量
                Demand demand = demandService.getById(demandId);
                int demandQuantity = (demand != null && demand.getQuantity() != null && demand.getQuantity() > 0)
                        ? demand.getQuantity()
                        : 1;
                
                int totalAllocated = 0;
                
                for (Map<String, Object> allocation : allocations) {
                    SchedulingRecord schedulingRecord = new SchedulingRecord();
                    schedulingRecord.setDemandId(demandId);
                    
                    Long resourceId = Long.valueOf(allocation.get("resourceId").toString());
                    schedulingRecord.setResourceId(resourceId);
                    System.out.println("处理调度记录，资源ID: " + resourceId);
                    
                    // 根据资源可用数量和剩余需求量确定本次分配数量
                    Resource resource = resourceService.getById(resourceId);
                    if (resource == null) {
                        System.err.println("资源ID " + resourceId + " 不存在，跳过分配");
                        continue;
                    }
                    
                    // 强制类型匹配校验：资源类型必须与需求类型完全一致
                    String demandType = demand.getDemandType();
                    if (demandType != null && !demandType.trim().isEmpty()) {
                        String resourceType = resource.getResourceType();
                        if (resourceType == null || !resourceType.equals(demandType)) {
                            System.err.println("资源ID " + resourceId + " 类型不匹配（需求类型: " + demandType + 
                                             ", 资源类型: " + resourceType + "），跳过分配");
                            continue;
                        }
                    }
                    
                    int resourceAvailable = (resource.getAvailableQuantity() != null)
                            ? resource.getAvailableQuantity()
                            : 0;
                    int remainingDemand = demandQuantity - totalAllocated;
                    int allocatedQuantity = Math.max(0, Math.min(remainingDemand, resourceAvailable));
                    
                    // 如果没有可分配数量，则跳过该记录
                    if (allocatedQuantity <= 0) {
                        System.out.println("资源ID " + resourceId + " 可用数量不足，跳过分配");
                        continue;
                    }
                    
                    schedulingRecord.setAllocatedQuantity(allocatedQuantity);
                    System.out.println("分配数量: " + allocatedQuantity + " (需求剩余: " + remainingDemand + ", 资源可用: " + resourceAvailable + ")");
                    
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
                        
                        totalAllocated += allocatedQuantity;
                    } else {
                        System.err.println("调度记录保存失败，资源ID: " + resourceId);
                    }
                }
                
                // 查询该需求的所有历史分配记录，计算累计已分配总量（只计算类型匹配的资源）
                QueryWrapper<SchedulingRecord> historyQuery = new QueryWrapper<>();
                historyQuery.eq("demand_id", demandId);
                List<SchedulingRecord> historyRecords = schedulingRecordMapper.selectList(historyQuery);
                
                // 获取需求类型，用于过滤类型不匹配的分配记录
                String demandType = demand.getDemandType();
                int totalHistoryAllocated = 0;
                
                for (SchedulingRecord record : historyRecords) {
                    // 只计算类型匹配的资源分配
                    if (demandType != null && !demandType.trim().isEmpty()) {
                        Resource recordResource = resourceService.getById(record.getResourceId());
                        if (recordResource != null) {
                            String resourceType = recordResource.getResourceType();
                            // 类型不匹配的记录不计入累计分配
                            if (resourceType == null || !resourceType.equals(demandType)) {
                                System.out.println("跳过类型不匹配的分配记录：资源ID " + record.getResourceId() + 
                                                 " (需求类型: " + demandType + ", 资源类型: " + resourceType + ")");
                                continue;
                            }
                        }
                    }
                    totalHistoryAllocated += (record.getAllocatedQuantity() != null ? record.getAllocatedQuantity() : 0);
                }
                
                System.out.println("需求ID " + demandId + " 累计已分配总量: " + totalHistoryAllocated + ", 需求数量: " + demandQuantity);
                
                // 根据累计分配总量与需求数量的关系，更新需求状态
                if (totalHistoryAllocated >= demandQuantity) {
                    // 需求已完全满足（累计分配总量已满足需求）
                    updateDemandStatus(demandId, "allocated");
                    System.out.println("需求ID " + demandId + " 累计分配已满足需求，状态更新为 allocated");
                } else if (totalHistoryAllocated > 0) {
                    // 只满足了部分需求
                    updateDemandStatus(demandId, "processing");
                    System.out.println("需求ID " + demandId + " 累计分配部分满足，状态更新为 processing");
                } else {
                    // 没有成功分配任何资源，保持原状态（通常是 matched）
                    System.out.println("需求ID " + demandId + " 未成功分配任何资源，保持原状态");
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
