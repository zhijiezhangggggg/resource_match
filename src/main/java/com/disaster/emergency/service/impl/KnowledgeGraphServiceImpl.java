package com.disaster.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.disaster.emergency.entity.KnowledgeNode;
import com.disaster.emergency.entity.KnowledgeRelation;
import com.disaster.emergency.entity.Resource;
import com.disaster.emergency.entity.Demand;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.entity.MatchingRecord;
import com.disaster.emergency.mapper.KnowledgeNodeMapper;
import com.disaster.emergency.mapper.KnowledgeRelationMapper;
import com.disaster.emergency.mapper.ResourceMapper;
import com.disaster.emergency.mapper.DemandMapper;
import com.disaster.emergency.mapper.DisasterMapper;
import com.disaster.emergency.mapper.MatchingRecordMapper;
import com.disaster.emergency.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {
    
    @Autowired
    private KnowledgeNodeMapper knowledgeNodeMapper;
    
    @Autowired
    private KnowledgeRelationMapper knowledgeRelationMapper;
    
    @Autowired
    private ResourceMapper resourceMapper;
    
    @Autowired
    private DemandMapper demandMapper;
    
    @Autowired
    private DisasterMapper disasterMapper;
    
    @Autowired
    private MatchingRecordMapper matchingRecordMapper;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createNode(String nodeType, Long businessId, String nodeName, Map<String, Object> properties) {
        try {
            KnowledgeNode node = new KnowledgeNode();
            node.setNodeType(nodeType);
            node.setBusinessId(businessId);
            node.setNodeName(nodeName);
            
            String propertiesJson = mapToJson(properties);
            System.out.println("创建节点 - 类型: " + nodeType + ", 业务ID: " + businessId + ", 名称: " + nodeName);
            System.out.println("节点属性JSON: " + propertiesJson);
            
            node.setProperties(propertiesJson);
            node.setStatus("active");
            node.setCreateTime(LocalDateTime.now());
            node.setUpdateTime(LocalDateTime.now());
            
            int result = knowledgeNodeMapper.insert(node);
            System.out.println("节点插入结果: " + result + ", 节点ID: " + node.getId());
            
            if (result <= 0) {
                throw new RuntimeException("节点插入失败，影响行数: " + result);
            }
            
            return node.getId();
        } catch (Exception e) {
            System.err.println("创建节点时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createRelation(Long sourceNodeId, Long targetNodeId, String relationType, 
                              Double weight, Map<String, Object> properties) {
        try {
            KnowledgeRelation relation = new KnowledgeRelation();
            relation.setSourceNodeId(sourceNodeId);
            relation.setTargetNodeId(targetNodeId);
            relation.setRelationType(relationType);
            relation.setWeight(new BigDecimal(weight != null ? weight : 1.0));
            
            String propertiesJson = mapToJson(properties);
            System.out.println("创建关系 - 源节点ID: " + sourceNodeId + ", 目标节点ID: " + targetNodeId + ", 关系类型: " + relationType);
            System.out.println("关系属性JSON: " + propertiesJson);
            
            relation.setProperties(propertiesJson);
            relation.setStatus("active");
            relation.setCreateTime(LocalDateTime.now());
            relation.setUpdateTime(LocalDateTime.now());
            
            int result = knowledgeRelationMapper.insert(relation);
            System.out.println("关系插入结果: " + result + ", 关系ID: " + relation.getId());
            
            if (result <= 0) {
                throw new RuntimeException("关系插入失败，影响行数: " + result);
            }
            
            return relation.getId();
        } catch (Exception e) {
            System.err.println("创建关系时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    public KnowledgeNode getNode(Long nodeId) {
        return knowledgeNodeMapper.selectById(nodeId);
    }
    
    @Override
    public KnowledgeNode getNodeByBusinessId(String nodeType, Long businessId) {
        return knowledgeNodeMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeNode>()
                .eq("node_type", nodeType)
                .eq("business_id", businessId)
        );
    }
    
    @Override
    public List<KnowledgeRelation> getNodeRelations(Long nodeId) {
        List<KnowledgeRelation> relations = new ArrayList<>();
        
        // 获取作为源节点的关系
        List<KnowledgeRelation> sourceRelations = knowledgeRelationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeRelation>()
                .eq("source_node_id", nodeId)
        );
        relations.addAll(sourceRelations);
        
        // 获取作为目标节点的关系
        List<KnowledgeRelation> targetRelations = knowledgeRelationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeRelation>()
                .eq("target_node_id", nodeId)
        );
        relations.addAll(targetRelations);
        
        return relations;
    }
    
    @Override
    public List<KnowledgeNode> getNeighborNodes(Long nodeId, String relationType) {
        List<KnowledgeRelation> relations = getNodeRelations(nodeId);
        
        if (relationType != null) {
            relations = relations.stream()
                .filter(relation -> relationType.equals(relation.getRelationType()))
                .collect(Collectors.toList());
        }
        
        Set<Long> neighborIds = new HashSet<>();
        for (KnowledgeRelation relation : relations) {
            if (!nodeId.equals(relation.getSourceNodeId())) {
                neighborIds.add(relation.getSourceNodeId());
            }
            if (!nodeId.equals(relation.getTargetNodeId())) {
                neighborIds.add(relation.getTargetNodeId());
            }
        }
        
        List<KnowledgeNode> neighbors = new ArrayList<>();
        for (Long neighborId : neighborIds) {
            KnowledgeNode neighbor = getNode(neighborId);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        
        return neighbors;
    }
    
    @Override
    public Map<String, Object> traverseGraph(Long startNodeId, Integer maxDepth) {
        Map<String, Object> result = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        List<Map<String, Object>> traversalPath = new ArrayList<>();
        
        traverseNode(startNodeId, 0, maxDepth, visited, traversalPath);
        
        result.put("startNodeId", startNodeId);
        result.put("maxDepth", maxDepth);
        result.put("traversalPath", traversalPath);
        result.put("visitedNodes", visited.size());
        
        return result;
    }
    
    private void traverseNode(Long nodeId, int currentDepth, int maxDepth, 
                             Set<Long> visited, List<Map<String, Object>> path) {
        if (currentDepth >= maxDepth || visited.contains(nodeId)) {
            return;
        }
        
        visited.add(nodeId);
        KnowledgeNode node = getNode(nodeId);
        if (node == null) return;
        
        Map<String, Object> nodeInfo = new HashMap<>();
        nodeInfo.put("nodeId", nodeId);
        nodeInfo.put("nodeName", node.getNodeName());
        nodeInfo.put("nodeType", node.getNodeType());
        nodeInfo.put("depth", currentDepth);
        path.add(nodeInfo);
        
        List<KnowledgeNode> neighbors = getNeighborNodes(nodeId, null);
        for (KnowledgeNode neighbor : neighbors) {
            traverseNode(neighbor.getId(), currentDepth + 1, maxDepth, visited, path);
        }
    }
    
    @Override
    public List<List<Long>> findPaths(Long startNodeId, Long endNodeId, Integer maxDepth) {
        List<List<Long>> allPaths = new ArrayList<>();
        List<Long> currentPath = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        
        findPathsRecursive(startNodeId, endNodeId, maxDepth, currentPath, visited, allPaths);
        
        return allPaths;
    }
    
    private void findPathsRecursive(Long currentNodeId, Long endNodeId, Integer maxDepth,
                                   List<Long> currentPath, Set<Long> visited, List<List<Long>> allPaths) {
        if (currentPath.size() > maxDepth) {
            return;
        }
        
        currentPath.add(currentNodeId);
        visited.add(currentNodeId);
        
        if (currentNodeId.equals(endNodeId)) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            List<KnowledgeNode> neighbors = getNeighborNodes(currentNodeId, null);
            for (KnowledgeNode neighbor : neighbors) {
                if (!visited.contains(neighbor.getId())) {
                    findPathsRecursive(neighbor.getId(), endNodeId, maxDepth, currentPath, visited, allPaths);
                }
            }
        }
        
        currentPath.remove(currentPath.size() - 1);
        visited.remove(currentNodeId);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateNodeProperties(Long nodeId, Map<String, Object> properties) {
        KnowledgeNode node = getNode(nodeId);
        if (node == null) {
            return false;
        }
        
        // 合并属性而不是覆盖
        Map<String, Object> existingProperties = jsonToMap(node.getProperties());
        if (existingProperties == null) {
            existingProperties = new HashMap<>();
        }
        existingProperties.putAll(properties);
        
        node.setProperties(mapToJson(existingProperties));
        node.setUpdateTime(LocalDateTime.now());
        
        return knowledgeNodeMapper.updateById(node) > 0;
    }
    
    @Override
    public boolean deleteNode(Long nodeId) {
        // 先删除相关关系
        List<KnowledgeRelation> relations = getNodeRelations(nodeId);
        for (KnowledgeRelation relation : relations) {
            knowledgeRelationMapper.deleteById(relation.getId());
        }
        
        // 再删除节点
        return knowledgeNodeMapper.deleteById(nodeId) > 0;
    }
    
    @Override
    public boolean deleteRelation(Long relationId) {
        return knowledgeRelationMapper.deleteById(relationId) > 0;
    }
    
    @Override
    public Map<String, Object> getGraphStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 节点统计
        long totalNodes = knowledgeNodeMapper.selectCount(null);
        stats.put("totalNodes", totalNodes);
        
        // 按类型统计节点
        List<KnowledgeNode> allNodes = knowledgeNodeMapper.selectList(null);
        Map<String, Long> nodeTypeStats = allNodes.stream()
            .collect(Collectors.groupingBy(KnowledgeNode::getNodeType, Collectors.counting()));
        stats.put("nodeTypeStats", nodeTypeStats);
        
        // 关系统计
        long totalRelations = knowledgeRelationMapper.selectCount(null);
        stats.put("totalRelations", totalRelations);
        
        // 按类型统计关系
        List<KnowledgeRelation> allRelations = knowledgeRelationMapper.selectList(null);
        Map<String, Long> relationTypeStats = allRelations.stream()
            .collect(Collectors.groupingBy(KnowledgeRelation::getRelationType, Collectors.counting()));
        stats.put("relationTypeStats", relationTypeStats);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getFrontendKnowledgeGraphData() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有灾情数据
        List<Disaster> disasters = disasterMapper.selectList(null);
        List<Map<String, Object>> disasterNodes = new ArrayList<>();
        
        for (Disaster disaster : disasters) {
            Map<String, Object> disasterNode = new HashMap<>();
            disasterNode.put("id", disaster.getId());
            disasterNode.put("type", "disaster");
            disasterNode.put("name", disaster.getDisasterType());
            disasterNode.put("label", "灾情");
            disasterNode.put("data", disaster);
            
            // 获取该灾情下的所有需求
            List<Map<String, Object>> demandNodes = new ArrayList<>();
            List<Demand> demands = demandMapper.selectList(
                new QueryWrapper<Demand>().eq("disaster_id", disaster.getId())
            );
            
            for (Demand demand : demands) {
                Map<String, Object> demandNode = new HashMap<>();
                demandNode.put("id", demand.getId());
                demandNode.put("type", "demand");
                demandNode.put("name", demand.getDemandType());
                demandNode.put("label", "需求");
                demandNode.put("data", demand);
                
                // 获取该需求关联的所有资源
                List<Map<String, Object>> resourceNodes = new ArrayList<>();
                List<MatchingRecord> matchingRecords = matchingRecordMapper.selectList(
                    new QueryWrapper<MatchingRecord>().eq("demand_id", demand.getId())
                );
                
                // 如果没有匹配记录，尝试根据需求类型匹配资源
                if (matchingRecords.isEmpty()) {
                    List<Resource> availableResources = resourceMapper.selectList(
                        new QueryWrapper<Resource>()
                            .eq("resource_type", demand.getDemandType())
                            .eq("status", "available")
                    );
                    
                    for (Resource resource : availableResources) {
                        Map<String, Object> resourceNode = new HashMap<>();
                        resourceNode.put("id", resource.getId());
                        resourceNode.put("type", "resource");
                        resourceNode.put("name", resource.getResourceName());
                        resourceNode.put("label", resource.getResourceType());
                        resourceNode.put("data", resource);
                        resourceNode.put("matchingInfo", null); // 没有匹配记录
                        resourceNodes.add(resourceNode);
                    }
                } else {
                    for (MatchingRecord matchingRecord : matchingRecords) {
                        Resource resource = resourceMapper.selectById(matchingRecord.getResourceId());
                        if (resource != null) {
                            Map<String, Object> resourceNode = new HashMap<>();
                            resourceNode.put("id", resource.getId());
                            resourceNode.put("type", "resource");
                            resourceNode.put("name", resource.getResourceName());
                            resourceNode.put("label", resource.getResourceType());
                            resourceNode.put("data", resource);
                            resourceNode.put("matchingInfo", matchingRecord);
                            resourceNodes.add(resourceNode);
                        }
                    }
                }
                
                demandNode.put("resources", resourceNodes);
                demandNodes.add(demandNode);
            }
            
            disasterNode.put("demands", demandNodes);
            disasterNodes.add(disasterNode);
        }
        
        result.put("disasters", disasterNodes);
        result.put("statistics", getGraphStatistics());
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getNodesByType(String nodeType) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        
        switch (nodeType.toLowerCase()) {
            case "resource":
                List<Resource> resources = resourceMapper.selectList(null);
                for (Resource resource : resources) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", "resource_" + resource.getId());
                    node.put("type", "resource");
                    node.put("name", resource.getResourceName());
                    node.put("label", resource.getResourceType());
                    node.put("data", resource);
                    nodes.add(node);
                }
                break;
            case "demand":
                List<Demand> demands = demandMapper.selectList(null);
                for (Demand demand : demands) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", "demand_" + demand.getId());
                    node.put("type", "demand");
                    node.put("name", demand.getDemandType());
                    node.put("label", "需求");
                    node.put("data", demand);
                    nodes.add(node);
                }
                break;
            case "disaster":
                List<Disaster> disasters = disasterMapper.selectList(null);
                for (Disaster disaster : disasters) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", "disaster_" + disaster.getId());
                    node.put("type", "disaster");
                    node.put("name", disaster.getDisasterType());
                    node.put("label", "灾情");
                    node.put("data", disaster);
                    nodes.add(node);
                }
                break;
        }
        
        return nodes;
    }
    
    @Override
    public Map<String, Object> getNodeConnections(Long nodeId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取节点信息
        KnowledgeNode node = getNode(nodeId);
        if (node == null) {
            return result;
        }
        
        result.put("node", node);
        
        // 获取所有关系
        List<KnowledgeRelation> relations = getNodeRelations(nodeId);
        result.put("relations", relations);
        
        // 获取邻居节点
        List<KnowledgeNode> neighbors = getNeighborNodes(nodeId, null);
        result.put("neighbors", neighbors);
        
        // 统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("relationCount", relations.size());
        stats.put("neighborCount", neighbors.size());
        
        // 按关系类型统计
        Map<String, Long> relationTypeStats = relations.stream()
            .collect(Collectors.groupingBy(KnowledgeRelation::getRelationType, Collectors.counting()));
        stats.put("relationTypeStats", relationTypeStats);
        
        result.put("statistics", stats);
        
        return result;
    }
    
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value.toString());
            } else {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            }
            json.append(",");
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
    
    private Map<String, Object> jsonToMap(String json) {
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return new HashMap<>();
        }
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 简单的JSON解析，处理基本的key-value对
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] pairs = json.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim();
                        
                        // 处理不同类型的值
                        if ("null".equals(value)) {
                            map.put(key, null);
                        } else if (value.startsWith("\"") && value.endsWith("\"")) {
                            map.put(key, value.substring(1, value.length() - 1));
                        } else if ("true".equals(value) || "false".equals(value)) {
                            map.put(key, Boolean.parseBoolean(value));
                        } else {
                            try {
                                // 尝试解析为数字
                                if (value.contains(".")) {
                                    map.put(key, Double.parseDouble(value));
                                } else {
                                    map.put(key, Long.parseLong(value));
                                }
                            } catch (NumberFormatException e) {
                                map.put(key, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果解析失败，返回空Map
            System.err.println("JSON解析失败: " + e.getMessage());
        }
        
        return map;
    }
    
    @Override
    public List<KnowledgeNode> findNodesByTypeAndProperty(String nodeType, String propertyKey, String propertyValue) {
        try {
            List<KnowledgeNode> allNodes = knowledgeNodeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KnowledgeNode>()
                    .eq("node_type", nodeType)
            );
            
            List<KnowledgeNode> matchedNodes = new ArrayList<>();
            for (KnowledgeNode node : allNodes) {
                Map<String, Object> properties = jsonToMap(node.getProperties());
                if (properties != null && propertyValue.equals(properties.get(propertyKey))) {
                    matchedNodes.add(node);
                }
            }
            
            return matchedNodes;
        } catch (Exception e) {
            System.err.println("根据类型和属性查找节点失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<KnowledgeNode> findNodesByProperty(String propertyKey, String propertyValue) {
        try {
            List<KnowledgeNode> allNodes = knowledgeNodeMapper.selectList(null);
            
            List<KnowledgeNode> matchedNodes = new ArrayList<>();
            for (KnowledgeNode node : allNodes) {
                Map<String, Object> properties = jsonToMap(node.getProperties());
                if (properties != null && propertyValue.equals(properties.get(propertyKey))) {
                    matchedNodes.add(node);
                }
            }
            
            return matchedNodes;
        } catch (Exception e) {
            System.err.println("根据属性查找节点失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
