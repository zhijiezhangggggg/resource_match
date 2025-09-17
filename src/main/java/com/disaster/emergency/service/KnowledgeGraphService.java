package com.disaster.emergency.service;

import com.disaster.emergency.entity.KnowledgeNode;
import com.disaster.emergency.entity.KnowledgeRelation;

import java.util.List;
import java.util.Map;

public interface KnowledgeGraphService {
    
    /**
     * 创建知识节点
     * @param nodeType 节点类型
     * @param businessId 业务ID
     * @param nodeName 节点名称
     * @param properties 节点属性
     * @return 节点ID
     */
    Long createNode(String nodeType, Long businessId, String nodeName, Map<String, Object> properties);
    
    /**
     * 创建知识关系
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @param relationType 关系类型
     * @param weight 权重
     * @param properties 关系属性
     * @return 关系ID
     */
    Long createRelation(Long sourceNodeId, Long targetNodeId, String relationType, 
                       Double weight, Map<String, Object> properties);
    
    /**
     * 获取节点
     * @param nodeId 节点ID
     * @return 节点信息
     */
    KnowledgeNode getNode(Long nodeId);
    
    /**
     * 根据业务ID获取节点
     * @param nodeType 节点类型
     * @param businessId 业务ID
     * @return 节点信息
     */
    KnowledgeNode getNodeByBusinessId(String nodeType, Long businessId);
    
    /**
     * 获取节点的所有关系
     * @param nodeId 节点ID
     * @return 关系列表
     */
    List<KnowledgeRelation> getNodeRelations(Long nodeId);
    
    /**
     * 获取节点的邻居节点
     * @param nodeId 节点ID
     * @param relationType 关系类型（可选）
     * @return 邻居节点列表
     */
    List<KnowledgeNode> getNeighborNodes(Long nodeId, String relationType);
    
    /**
     * 图谱遍历查询
     * @param startNodeId 起始节点ID
     * @param maxDepth 最大深度
     * @return 遍历结果
     */
    Map<String, Object> traverseGraph(Long startNodeId, Integer maxDepth);
    
    /**
     * 查找路径
     * @param startNodeId 起始节点ID
     * @param endNodeId 结束节点ID
     * @param maxDepth 最大深度
     * @return 路径列表
     */
    List<List<Long>> findPaths(Long startNodeId, Long endNodeId, Integer maxDepth);
    
    /**
     * 更新节点属性
     * @param nodeId 节点ID
     * @param properties 新属性
     * @return 更新结果
     */
    boolean updateNodeProperties(Long nodeId, Map<String, Object> properties);
    
    /**
     * 删除节点
     * @param nodeId 节点ID
     * @return 删除结果
     */
    boolean deleteNode(Long nodeId);
    
    /**
     * 删除关系
     * @param relationId 关系ID
     * @return 删除结果
     */
    boolean deleteRelation(Long relationId);
    
    /**
     * 获取图谱统计信息
     * @return 统计信息
     */
    Map<String, Object> getGraphStatistics();
}
