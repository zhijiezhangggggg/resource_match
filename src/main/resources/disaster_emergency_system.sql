-- 灾害应急资源匹配平台数据库脚本
-- 创建时间: 2024-01-01
-- 版本: 1.0

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
DROP DATABASE IF EXISTS disaster_emergency;
CREATE DATABASE disaster_emergency DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE disaster_emergency;

-- =============================================
-- 1. 用户管理相关表
-- =============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) NOT NULL COMMENT '角色：civilian-灾民，rescue_team-救援队，command_center-指挥中心，data_entry-数据录入员',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-激活，inactive-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 核心业务表
-- =============================================

-- 灾情表
DROP TABLE IF EXISTS `disaster`;
CREATE TABLE `disaster` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '灾情ID',
  `disaster_type` varchar(50) NOT NULL COMMENT '灾害类型',
  `occur_time` datetime NOT NULL COMMENT '发生时间',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `severity` varchar(20) NOT NULL COMMENT '严重程度：轻微，一般，严重，特别严重',
  `description` text COMMENT '灾情描述',
  `reporter_name` varchar(100) COMMENT '上报人姓名',
  `reporter_phone` varchar(20) COMMENT '上报人电话',
  `reporter_id` bigint COMMENT '上报人ID',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-活跃，resolved-已解决，closed-已关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_disaster_type` (`disaster_type`),
  KEY `idx_location` (`province`, `city`, `district`),
  KEY `idx_occur_time` (`occur_time`),
  KEY `idx_status` (`status`),
  KEY `idx_reporter_id` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='灾情表';

-- 需求表
DROP TABLE IF EXISTS `demand`;
CREATE TABLE `demand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `disaster_id` bigint NOT NULL COMMENT '灾情ID',
  `demand_type` varchar(50) NOT NULL COMMENT '需求类型',
  `quantity` int NOT NULL COMMENT '需求数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `urgency` varchar(20) NOT NULL COMMENT '紧急程度：低，中，高，紧急',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `description` text COMMENT '需求描述',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending-待处理，matched-已匹配，allocated-已分配，completed-已完成',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_disaster_id` (`disaster_id`),
  KEY `idx_demand_type` (`demand_type`),
  KEY `idx_location` (`province`, `city`, `district`),
  KEY `idx_urgency` (`urgency`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_demand_disaster` FOREIGN KEY (`disaster_id`) REFERENCES `disaster` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- 资源表
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `resource_type` varchar(50) NOT NULL COMMENT '资源类型',
  `resource_name` varchar(100) NOT NULL COMMENT '资源名称',
  `total_quantity` int NOT NULL COMMENT '总数量',
  `available_quantity` int NOT NULL COMMENT '可用数量',
  `unit` varchar(20) NOT NULL COMMENT '单位',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `warehouse_name` varchar(100) COMMENT '仓库名称',
  `contact_person` varchar(50) COMMENT '联系人',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `organization_id` bigint COMMENT '所属机构ID',
  `status` varchar(20) DEFAULT 'available' COMMENT '状态：available-可用，allocated-已分配，maintenance-维护中，depleted-已耗尽',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_location` (`province`, `city`, `district`),
  KEY `idx_status` (`status`),
  KEY `idx_organization_id` (`organization_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源表';

-- 机构表
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机构ID',
  `org_name` varchar(100) NOT NULL COMMENT '机构名称',
  `org_type` varchar(50) NOT NULL COMMENT '机构类型：government-政府部门，rescue_team-救援队，ngo-社会组织，enterprise-企业',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `address` varchar(255) COMMENT '详细地址',
  `contact_person` varchar(50) COMMENT '联系人',
  `contact_phone` varchar(20) COMMENT '联系电话',
  `email` varchar(100) COMMENT '邮箱',
  `description` text COMMENT '机构描述',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active-活跃，inactive-非活跃',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_org_type` (`org_type`),
  KEY `idx_location` (`province`, `city`, `district`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机构表';

-- =============================================
-- 3. 匹配和调度相关表
-- =============================================

-- 匹配记录表
DROP TABLE IF EXISTS `matching_record`;
CREATE TABLE `matching_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '匹配记录ID',
  `demand_id` bigint NOT NULL COMMENT '需求ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `match_score` decimal(5,2) NOT NULL COMMENT '匹配度评分(0-100)',
  `match_reason` text COMMENT '匹配原因',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending-待确认，approved-已批准，rejected-已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_match_score` (`match_score`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_matching_demand` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_matching_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='匹配记录表';

-- 调度记录表
DROP TABLE IF EXISTS `scheduling_record`;
CREATE TABLE `scheduling_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调度记录ID',
  `demand_id` bigint NOT NULL COMMENT '需求ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `allocated_quantity` int NOT NULL COMMENT '分配数量',
  `scheduler_id` bigint NOT NULL COMMENT '调度员ID',
  `scheduler_name` varchar(100) NOT NULL COMMENT '调度员姓名',
  `scheduling_time` datetime NOT NULL COMMENT '调度时间',
  `expected_delivery_time` datetime COMMENT '预期送达时间',
  `actual_delivery_time` datetime COMMENT '实际送达时间',
  `status` varchar(20) DEFAULT 'allocated' COMMENT '状态：allocated-已分配，in_transit-运输中，delivered-已送达，completed-已完成',
  `remark` text COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_demand_id` (`demand_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_scheduler_id` (`scheduler_id`),
  KEY `idx_status` (`status`),
  KEY `idx_scheduling_time` (`scheduling_time`),
  CONSTRAINT `fk_scheduling_demand` FOREIGN KEY (`demand_id`) REFERENCES `demand` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_scheduling_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调度记录表';

-- =============================================
-- 4. 系统配置表
-- =============================================

-- 系统配置表
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_desc` varchar(255) COMMENT '配置描述',
  `config_type` varchar(20) DEFAULT 'string' COMMENT '配置类型：string-字符串，number-数字，boolean-布尔值，json-JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint COMMENT '操作用户ID',
  `username` varchar(50) COMMENT '操作用户名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(255) COMMENT '操作描述',
  `request_method` varchar(10) COMMENT '请求方法',
  `request_url` varchar(255) COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `ip_address` varchar(50) COMMENT 'IP地址',
  `user_agent` varchar(500) COMMENT '用户代理',
  `operation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 5. 插入基础数据
-- =============================================

-- 插入系统管理员用户
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSOfvVWz8z8Ue8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8K', '系统管理员', '13800138000', 'admin@example.com', 'command_center', 'active'),
('data_entry', '$2a$10$7JB720yubVSOfvVWz8z8Ue8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8Kz8K', '数据录入员', '13800138001', 'data@example.com', 'data_entry', 'active');

-- 插入基础机构数据
INSERT INTO `organization` (`org_name`, `org_type`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `email`, `description`, `status`) VALUES
('四川省应急管理厅', 'government', '四川省', '成都市', '武侯区', '成都市武侯区人民南路四段', '张主任', '028-12345678', 'emergency@sc.gov.cn', '四川省应急管理部门', 'active'),
('成都市消防救援支队', 'rescue_team', '四川省', '成都市', '锦江区', '成都市锦江区红星路', '李队长', '028-87654321', 'fire@cd.gov.cn', '成都市消防救援队伍', 'active'),
('四川省红十字会', 'ngo', '四川省', '成都市', '青羊区', '成都市青羊区人民中路', '王秘书长', '028-11111111', 'redcross@sc.gov.cn', '四川省红十字会', 'active'),
('成都物资储备中心', 'enterprise', '四川省', '成都市', '双流区', '成都市双流区机场路', '赵经理', '028-22222222', 'storage@cd.gov.cn', '成都物资储备中心', 'active');

-- 插入基础资源数据
INSERT INTO `resource` (`resource_type`, `resource_name`, `total_quantity`, `available_quantity`, `unit`, `province`, `city`, `district`, `warehouse_name`, `contact_person`, `contact_phone`, `organization_id`, `status`) VALUES
('帐篷', '救灾帐篷', 500, 500, '顶', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available'),
('食品', '方便面', 1000, 1000, '箱', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available'),
('药品', '急救包', 200, 200, '套', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available'),
('饮用水', '瓶装水', 2000, 2000, '瓶', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available'),
('发电机', '柴油发电机', 50, 50, '台', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available'),
('通讯设备', '对讲机', 100, 100, '台', '四川省', '成都市', '双流区', '成都物资储备中心', '赵经理', '028-22222222', 4, 'available');

-- 插入系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `config_type`) VALUES
('system.name', '灾害应急资源匹配平台', '系统名称', 'string'),
('system.version', '1.0.0', '系统版本', 'string'),
('matching.distance_weight', '0.3', '距离权重', 'number'),
('matching.urgency_weight', '0.4', '紧急程度权重', 'number'),
('matching.type_weight', '0.3', '类型匹配权重', 'number'),
('notification.enabled', 'true', '是否启用通知', 'boolean'),
('map.api_key', 'your_map_api_key', '地图API密钥', 'string');

-- =============================================
-- 6. 创建视图
-- =============================================

-- 灾情统计视图
CREATE VIEW `v_disaster_statistics` AS
SELECT 
    d.disaster_type,
    d.severity,
    COUNT(*) as disaster_count,
    COUNT(CASE WHEN d.status = 'active' THEN 1 END) as active_count,
    COUNT(CASE WHEN d.status = 'resolved' THEN 1 END) as resolved_count,
    COUNT(CASE WHEN d.status = 'closed' THEN 1 END) as closed_count
FROM disaster d
GROUP BY d.disaster_type, d.severity;

-- 资源统计视图
CREATE VIEW `v_resource_statistics` AS
SELECT 
    r.resource_type,
    SUM(r.total_quantity) as total_quantity,
    SUM(r.available_quantity) as available_quantity,
    SUM(r.total_quantity - r.available_quantity) as allocated_quantity,
    COUNT(*) as resource_count
FROM resource r
WHERE r.status = 'available'
GROUP BY r.resource_type;

-- 需求统计视图
CREATE VIEW `v_demand_statistics` AS
SELECT 
    dm.demand_type,
    dm.urgency,
    COUNT(*) as demand_count,
    COUNT(CASE WHEN dm.status = 'pending' THEN 1 END) as pending_count,
    COUNT(CASE WHEN dm.status = 'matched' THEN 1 END) as matched_count,
    COUNT(CASE WHEN dm.status = 'allocated' THEN 1 END) as allocated_count,
    COUNT(CASE WHEN dm.status = 'completed' THEN 1 END) as completed_count
FROM demand dm
GROUP BY dm.demand_type, dm.urgency;

-- =============================================
-- 7. 创建索引优化
-- =============================================

-- =============================================
-- 8. 数据完整性检查
-- =============================================

-- 为常用查询字段创建复合索引
CREATE INDEX `idx_disaster_type_time` ON `disaster` (`disaster_type`, `occur_time`);
CREATE INDEX `idx_demand_type_urgency` ON `demand` (`demand_type`, `urgency`);
CREATE INDEX `idx_resource_type_status` ON `resource` (`resource_type`, `status`);
CREATE INDEX `idx_matching_demand_status` ON `matching_record` (`demand_id`, `status`);
CREATE INDEX `idx_scheduling_status_time` ON `scheduling_record` (`status`, `scheduling_time`);

-- =============================================
-- 9. 数据完整性检查
-- =============================================

-- 添加外键约束
ALTER TABLE `resource` ADD CONSTRAINT `fk_resource_organization` 
FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`) ON DELETE SET NULL;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示创建完成信息
SELECT '数据库创建完成！' as message;
SELECT '包含以下表：' as info;
SHOW TABLES;
