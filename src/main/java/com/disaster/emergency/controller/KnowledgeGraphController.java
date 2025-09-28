package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.KnowledgeNode;
import com.disaster.emergency.entity.KnowledgeRelation;
import com.disaster.emergency.service.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/knowledge-graph")
@CrossOrigin
@Tag(name = "知识图谱管理", description = "知识图谱节点和关系的创建、查询、更新、删除等操作")
public class KnowledgeGraphController {
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;
    
    @Operation(summary = "创建知识节点", description = "在知识图谱中创建新的节点")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "知识节点创建成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点已存在")
    })
    @PostMapping("/node")
    public Result<Map<String, Object>> createNode(@RequestBody Map<String, Object> request) {
        try {
            String nodeType = (String) request.get("nodeType");
            Long businessId = Long.valueOf(request.get("businessId").toString());
            String nodeName = (String) request.get("nodeName");
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");
            
            if (nodeType == null || businessId == null || nodeName == null) {
                return Result.error(20001, "节点类型、业务ID和节点名称不能为空");
            }
            
            Long nodeId = knowledgeGraphService.createNode(nodeType, businessId, nodeName, properties);
            
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", nodeId);
            result.put("nodeType", nodeType);
            result.put("businessId", businessId);
            result.put("nodeName", nodeName);
            
            return Result.success("知识节点创建成功", result);
        } catch (Exception e) {
            return Result.error(20001, "知识节点创建失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "创建知识关系", description = "在两个节点之间创建关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "知识关系创建成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点不存在")
    })
    @PostMapping("/relation")
    public Result<Map<String, Object>> createRelation(@RequestBody Map<String, Object> request) {
        try {
            Long sourceNodeId = Long.valueOf(request.get("sourceNodeId").toString());
            Long targetNodeId = Long.valueOf(request.get("targetNodeId").toString());
            String relationType = (String) request.get("relationType");
            Double weight = request.get("weight") != null ? Double.valueOf(request.get("weight").toString()) : 1.0;
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) request.get("properties");
            
            if (sourceNodeId == null || targetNodeId == null || relationType == null) {
                return Result.error(20001, "源节点ID、目标节点ID和关系类型不能为空");
            }
            
            Long relationId = knowledgeGraphService.createRelation(sourceNodeId, targetNodeId, relationType, weight, properties);
            
            Map<String, Object> result = new HashMap<>();
            result.put("relationId", relationId);
            result.put("sourceNodeId", sourceNodeId);
            result.put("targetNodeId", targetNodeId);
            result.put("relationType", relationType);
            result.put("weight", weight);
            
            return Result.success("知识关系创建成功", result);
        } catch (Exception e) {
            return Result.error(20001, "知识关系创建失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取节点信息", description = "根据节点ID获取节点的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取节点成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点不存在")
    })
    @GetMapping("/node/{nodeId}")
    public Result<KnowledgeNode> getNode(@PathVariable Long nodeId) {
        try {
            KnowledgeNode node = knowledgeGraphService.getNode(nodeId);
            if (node == null) {
                return Result.error(20002, "节点不存在");
            }
            return Result.success("获取节点成功", node);
        } catch (Exception e) {
            return Result.error(20001, "获取节点失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "根据业务ID获取节点", description = "通过节点类型和业务ID查询节点信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取节点成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点不存在")
    })
    @GetMapping("/node/business/{nodeType}/{businessId}")
    public Result<KnowledgeNode> getNodeByBusinessId(@PathVariable String nodeType, @PathVariable Long businessId) {
        try {
            KnowledgeNode node = knowledgeGraphService.getNodeByBusinessId(nodeType, businessId);
            if (node == null) {
                return Result.error(20002, "节点不存在");
            }
            return Result.success("获取节点成功", node);
        } catch (Exception e) {
            return Result.error(20001, "获取节点失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取节点关系", description = "查询指定节点的所有关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取节点关系成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/node/{nodeId}/relations")
    public Result<List<KnowledgeRelation>> getNodeRelations(@PathVariable Long nodeId) {
        try {
            List<KnowledgeRelation> relations = knowledgeGraphService.getNodeRelations(nodeId);
            return Result.success("获取节点关系成功", relations);
        } catch (Exception e) {
            return Result.error(20001, "获取节点关系失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取邻居节点", description = "查询指定节点的所有邻居节点")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取邻居节点成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/node/{nodeId}/neighbors")
    public Result<List<KnowledgeNode>> getNeighborNodes(
            @PathVariable Long nodeId,
            @RequestParam(required = false) String relationType) {
        try {
            List<KnowledgeNode> neighbors = knowledgeGraphService.getNeighborNodes(nodeId, relationType);
            return Result.success("获取邻居节点成功", neighbors);
        } catch (Exception e) {
            return Result.error(20001, "获取邻居节点失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "图谱遍历", description = "从指定节点开始进行深度优先遍历")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "图谱遍历成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/traverse/{startNodeId}")
    public Result<Map<String, Object>> traverseGraph(
            @PathVariable Long startNodeId,
            @RequestParam(defaultValue = "3") Integer maxDepth) {
        try {
            Map<String, Object> result = knowledgeGraphService.traverseGraph(startNodeId, maxDepth);
            return Result.success("图谱遍历成功", result);
        } catch (Exception e) {
            return Result.error(20001, "图谱遍历失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "查找路径", description = "查找两个节点之间的所有可能路径")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "路径查找成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/path/{startNodeId}/{endNodeId}")
    public Result<List<List<Long>>> findPaths(
            @PathVariable Long startNodeId,
            @PathVariable Long endNodeId,
            @RequestParam(defaultValue = "5") Integer maxDepth) {
        try {
            List<List<Long>> paths = knowledgeGraphService.findPaths(startNodeId, endNodeId, maxDepth);
            return Result.success("路径查找成功", paths);
        } catch (Exception e) {
            return Result.error(20001, "路径查找失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新节点属性", description = "更新指定节点的属性信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "节点属性更新成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点不存在")
    })
    @PutMapping("/node/{nodeId}/properties")
    public Result<Map<String, Object>> updateNodeProperties(
            @PathVariable Long nodeId,
            @RequestBody Map<String, Object> properties) {
        try {
            boolean success = knowledgeGraphService.updateNodeProperties(nodeId, properties);
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", nodeId);
            result.put("success", success);
            
            return Result.success("节点属性更新成功", result);
        } catch (Exception e) {
            return Result.error(20001, "节点属性更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除节点", description = "删除指定的节点及其所有相关关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "节点删除成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "节点不存在")
    })
    @DeleteMapping("/node/{nodeId}")
    public Result<Map<String, Object>> deleteNode(@PathVariable Long nodeId) {
        try {
            boolean success = knowledgeGraphService.deleteNode(nodeId);
            Map<String, Object> result = new HashMap<>();
            result.put("nodeId", nodeId);
            result.put("success", success);
            
            return Result.success("节点删除成功", result);
        } catch (Exception e) {
            return Result.error(20001, "节点删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除关系", description = "删除指定的关系")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "关系删除成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "关系不存在")
    })
    @DeleteMapping("/relation/{relationId}")
    public Result<Map<String, Object>> deleteRelation(@PathVariable Long relationId) {
        try {
            boolean success = knowledgeGraphService.deleteRelation(relationId);
            Map<String, Object> result = new HashMap<>();
            result.put("relationId", relationId);
            result.put("success", success);
            
            return Result.success("关系删除成功", result);
        } catch (Exception e) {
            return Result.error(20001, "关系删除失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取图谱统计", description = "获取知识图谱的整体统计信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取图谱统计成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getGraphStatistics() {
        try {
            Map<String, Object> statistics = knowledgeGraphService.getGraphStatistics();
            return Result.success("获取图谱统计成功", statistics);
        } catch (Exception e) {
            return Result.error(20001, "获取图谱统计失败: " + e.getMessage());
        }
    }
}
