package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.KnowledgeNode;
import com.disaster.emergency.entity.KnowledgeRelation;
import com.disaster.emergency.mapper.KnowledgeNodeMapper;
import com.disaster.emergency.mapper.KnowledgeRelationMapper;
import com.disaster.emergency.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    @Override
    public Long createNode(String nodeType, Long businessId, String nodeName, Map<String, Object> properties) {
        KnowledgeNode node = new KnowledgeNode();
        node.setNodeType(nodeType);
        node.setBusinessId(businessId);
        node.setNodeName(nodeName);
        node.setProperties(mapToJson(properties));
        node.setStatus("active");
        node.setCreateTime(LocalDateTime.now());
        node.setUpdateTime(LocalDateTime.now());
        
        knowledgeNodeMapper.insert(node);
        return node.getId();
    }
    
    @Override
    public Long createRelation(Long sourceNodeId, Long targetNodeId, String relationType, 
                              Double weight, Map<String, Object> properties) {
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setSourceNodeId(sourceNodeId);
        relation.setTargetNodeId(targetNodeId);
        relation.setRelationType(relationType);
        relation.setWeight(new BigDecimal(weight != null ? weight : 1.0));
        relation.setProperties(mapToJson(properties));
        relation.setStatus("active");
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        
        knowledgeRelationMapper.insert(relation);
        return relation.getId();
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
    public boolean updateNodeProperties(Long nodeId, Map<String, Object> properties) {
        KnowledgeNode node = getNode(nodeId);
        if (node == null) {
            return false;
        }
        
        node.setProperties(mapToJson(properties));
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
    
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue()).append("\",");
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
