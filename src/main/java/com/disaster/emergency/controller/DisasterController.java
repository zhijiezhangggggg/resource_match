package com.disaster.emergency.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.service.DisasterService;
import com.disaster.emergency.service.TextParseService;
import com.disaster.emergency.service.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/disaster")
@CrossOrigin
@Tag(name = "灾情管理", description = "灾情上报、查询、状态管理")
public class DisasterController {

    @Autowired
    private DisasterService disasterService;
    
    @Autowired
    private TextParseService textParseService;
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Operation(summary = "灾情上报", description = "上报新的灾害信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "灾情上报成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @PostMapping("/report")
    public Result<Disaster> reportDisaster(@Valid @RequestBody Disaster disaster) {
        try {
            // 参数验证
            if (disaster.getDisasterType() == null || disaster.getDisasterType().trim().isEmpty()) {
                return Result.error(20001, "灾害类型不能为空");
            }
            if (disaster.getProvince() == null || disaster.getProvince().trim().isEmpty()) {
                return Result.error(20001, "省份不能为空");
            }
            if (disaster.getCity() == null || disaster.getCity().trim().isEmpty()) {
                return Result.error(20001, "城市不能为空");
            }
            if (disaster.getDistrict() == null || disaster.getDistrict().trim().isEmpty()) {
                return Result.error(20001, "区县不能为空");
            }
            if (disaster.getSeverity() == null || disaster.getSeverity().trim().isEmpty()) {
                return Result.error(20001, "严重程度不能为空");
            }
            
            // 验证经纬度
            if (disaster.getLatitude() != null && (disaster.getLatitude() < -90.0 || disaster.getLatitude() > 90.0)) {
                return Result.error(20001, "纬度必须在-90到90度之间");
            }
            if (disaster.getLongitude() != null && (disaster.getLongitude() < -180.0 || disaster.getLongitude() > 180.0)) {
                return Result.error(20001, "经度必须在-180到180度之间");
            }
            
            // 验证手机号格式
            if (disaster.getReporterPhone() != null && !disaster.getReporterPhone().matches("^1[3-9]\\d{9}$")) {
                return Result.error(20001, "手机号格式不正确");
            }
            
            // 设置默认值
            if (disaster.getOccurTime() == null) {
                disaster.setOccurTime(LocalDateTime.now());
            }
            
            Disaster reportedDisaster = disasterService.reportDisaster(disaster);
            return Result.success("灾情上报成功", reportedDisaster);
        } catch (Exception e) {
            return Result.error(20001, "灾情上报失败: " + e.getMessage());
        }
    }

    @Operation(summary = "灾情列表查询", description = "分页查询灾情列表，支持多条件筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @GetMapping("/list")
    public Result<Map<String, Object>> getDisasterList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        
        try {
            // 参数验证
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            
            // 调用服务层进行分页查询
            IPage<Disaster> pageResult = disasterService.getDisasterList(page, size, disasterType, severity, province, city, status);
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", pageResult.getTotal());
            result.put("pages", pageResult.getPages());
            result.put("current", pageResult.getCurrent());
            result.put("size", pageResult.getSize());
            result.put("records", pageResult.getRecords());
            
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error(20004, "查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "灾情详情查询", description = "根据灾情ID获取详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "20002", description = "数据不存在")
    })
    @GetMapping("/{id}")
    public Result<Disaster> getDisasterDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(20002, "灾情ID无效");
        }
        
        Disaster disaster = disasterService.getById(id);
        if (disaster == null) {
            return Result.error(20002, "灾情ID不存在");
        }
        return Result.success("查询成功", disaster);
    }

    @Operation(summary = "灾情状态更新", description = "更新灾情的处理状态")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "状态更新成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误"),
            @ApiResponse(responseCode = "20002", description = "数据不存在")
    })
    @PutMapping("/{id}/status")
    public Result<Map<String, Object>> updateDisasterStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            if (id == null || id <= 0) {
                return Result.error(20002, "灾情ID无效");
            }
            
            String status = request.get("status");
            String remark = request.get("remark");
            
            if (status == null || status.trim().isEmpty()) {
                return Result.error(20001, "状态不能为空");
            }
            
            // 验证状态值是否有效
            if (!isValidStatus(status)) {
                return Result.error(20001, "无效的状态值，支持的状态：active, resolved, closed");
            }
            
            Disaster disaster = disasterService.getById(id);
            if (disaster == null) {
                return Result.error(20002, "灾情ID不存在");
            }
            
            // 检查状态转换是否合理
            if (!isValidStatusTransition(disaster.getStatus(), status)) {
                return Result.error(20001, "无效的状态转换：" + disaster.getStatus() + " -> " + status);
            }
            
            disaster.setStatus(status);
            disaster.setUpdateTime(LocalDateTime.now());
            disasterService.updateById(disaster);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("status", status);
            result.put("updateTime", disaster.getUpdateTime());
            if (remark != null && !remark.trim().isEmpty()) {
                result.put("remark", remark);
            }
            
            return Result.success("状态更新成功", result);
        } catch (Exception e) {
            return Result.error(20004, "状态更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证状态值是否有效
     */
    private boolean isValidStatus(String status) {
        return "active".equals(status) || "resolved".equals(status) || "closed".equals(status);
    }
    
    /**
     * 验证状态转换是否合理
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        if (fromStatus == null) return true;
        
        // 允许的状态转换
        switch (fromStatus) {
            case "active":
                return "resolved".equals(toStatus) || "closed".equals(toStatus);
            case "resolved":
                return "closed".equals(toStatus);
            case "closed":
                return false; // 已关闭状态不能转换
            default:
                return true;
        }
    }
    
    /**
     * 解析位置信息
     */
    private void parseLocation(Disaster disaster, String location) {
        if (location == null || location.trim().isEmpty()) return;
        
        // 按省市区县层级解析
        String[] parts = location.split("省|市|区|县");
        if (parts.length >= 1 && location.contains("省")) {
            disaster.setProvince(parts[0] + "省");
        }
        if (parts.length >= 2 && location.contains("市")) {
            disaster.setCity(parts[1] + "市");
        }
        if (parts.length >= 3 && (location.contains("区") || location.contains("县"))) {
            disaster.setDistrict(parts[2] + (location.contains("区") ? "区" : "县"));
        }
    }
    
    /**
     * 从原始文本中提取额外信息
     */
    private void extractAdditionalInfo(Disaster disaster, String originalText) {
        if (originalText == null || originalText.trim().isEmpty()) return;
        
        // 提取描述信息
        if (disaster.getDescription() == null || disaster.getDescription().trim().isEmpty()) {
            disaster.setDescription(originalText);
        }
        
        // 提取上报人信息（如果有的话）
        if (originalText.contains("报告") || originalText.contains("上报")) {
            // 简单的上报人信息提取逻辑
            String[] lines = originalText.split("\\n");
            for (String line : lines) {
                if (line.contains("报告人") || line.contains("联系人")) {
                    // 提取姓名和电话
                    if (line.matches(".*[\\u4e00-\\u9fa5]{2,4}.*")) {
                        String name = line.replaceAll("[^\\u4e00-\\u9fa5]", "").substring(0, Math.min(4, line.replaceAll("[^\\u4e00-\\u9fa5]", "").length()));
                        disaster.setReporterName(name);
                    }
                    if (line.matches(".*1[3-9]\\d{9}.*")) {
                        String phone = line.replaceAll("[^0-9]", "").replaceAll(".*(1[3-9]\\d{9}).*", "$1");
                        disaster.setReporterPhone(phone);
                    }
                }
            }
        }
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        // 简单的JSON转换，处理特殊字符
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                .append(escapeJson(String.valueOf(entry.getValue()))).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    @Operation(summary = "文本解析", description = "解析灾情文本，提取结构化信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "文本解析成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @PostMapping("/parse-text")
    public Result<Map<String, Object>> parseDisasterText(@RequestBody Map<String, String> request) {
        try {
            String originalText = request.get("originalText");
            if (originalText == null || originalText.trim().isEmpty()) {
                return Result.error(20001, "原始文本不能为空");
            }
            
            // 解析文本
            Map<String, Object> parseResult = textParseService.parseText(originalText, "disaster_report");
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalText", originalText);
            result.put("parsedResult", parseResult);
            result.put("parseTime", LocalDateTime.now());
            
            return Result.success("文本解析成功", result);
        } catch (Exception e) {
            return Result.error(20001, "文本解析失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "文本解析灾情上报", description = "通过文本解析自动提取灾情信息并上报")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "灾情上报和解析成功"),
            @ApiResponse(responseCode = "20001", description = "参数错误")
    })
    @PostMapping("/report-with-parse")
    public Result<Map<String, Object>> reportDisasterWithParse(@RequestBody Map<String, Object> request) {
        try {
            String originalText = (String) request.get("originalText");
            if (originalText == null || originalText.trim().isEmpty()) {
                return Result.error(20001, "原始文本不能为空");
            }
            
            // 解析文本
            Map<String, Object> parseResult = textParseService.parseText(originalText, "disaster_report");
            
            // 创建灾情对象
            Disaster disaster = new Disaster();
            disaster.setOriginalText(originalText);
            disaster.setParsedData(mapToJson(parseResult));
            
            // 从解析结果中提取信息
            if (parseResult.containsKey("disaster_type")) {
                disaster.setDisasterType((String) parseResult.get("disaster_type"));
            }
            if (parseResult.containsKey("severity")) {
                disaster.setSeverity((String) parseResult.get("severity"));
            }
            if (parseResult.containsKey("location")) {
                String location = (String) parseResult.get("location");
                parseLocation(disaster, location);
            }
            
            // 从原始文本中提取更多信息
            extractAdditionalInfo(disaster, originalText);
            
            // 设置默认值
            if (disaster.getDisasterType() == null) {
                disaster.setDisasterType("未知");
            }
            if (disaster.getSeverity() == null) {
                disaster.setSeverity("一般");
            }
            if (disaster.getProvince() == null) {
                disaster.setProvince("四川省");
            }
            if (disaster.getCity() == null) {
                disaster.setCity("成都市");
            }
            if (disaster.getDistrict() == null) {
                disaster.setDistrict("高新区");
            }
            
            disaster.setOccurTime(LocalDateTime.now());
            disaster.setStatus("active");
            
            // 保存灾情
            Disaster reportedDisaster = disasterService.reportDisaster(disaster);
            
            // 创建知识图谱节点
            Map<String, Object> nodeProperties = new HashMap<>();
            nodeProperties.put("severity", disaster.getSeverity());
            nodeProperties.put("occur_time", disaster.getOccurTime());
            nodeProperties.put("location", disaster.getProvince() + disaster.getCity() + disaster.getDistrict());
            
            Long nodeId = knowledgeGraphService.createNode(
                "disaster", 
                reportedDisaster.getId(), 
                disaster.getDisasterType() + "-" + disaster.getCity(),
                nodeProperties
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("disaster", reportedDisaster);
            result.put("parseResult", parseResult);
            result.put("knowledgeNodeId", nodeId);
            
            return Result.success("灾情上报和解析成功", result);
        } catch (Exception e) {
            return Result.error(20001, "灾情上报失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除灾情", description = "根据灾情ID删除灾情记录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "20002", description = "数据不存在")
    })
    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteDisaster(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return Result.error(20002, "灾情ID无效");
        }
        
        Disaster disaster = disasterService.getById(id);
        if (disaster == null) {
            return Result.error(20002, "灾情ID不存在");
        }
        
        boolean success = disasterService.removeById(id);
        if (success) {
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("deleteTime", LocalDateTime.now());
            return Result.success("删除成功", result);
        } else {
            return Result.error(20004, "删除失败");
        }
    }
    
    @Operation(summary = "灾情统计", description = "获取灾情统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取统计成功"),
            @ApiResponse(responseCode = "20004", description = "系统错误")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDisasterStatistics() {
        try {
            Map<String, Object> statistics = disasterService.getDisasterStatistics();
            return Result.success("获取灾情统计成功", statistics);
        } catch (Exception e) {
            return Result.error(20004, "获取统计失败: " + e.getMessage());
        }
    }
}