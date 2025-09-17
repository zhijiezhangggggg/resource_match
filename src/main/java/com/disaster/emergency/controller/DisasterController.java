package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.Disaster;
import com.disaster.emergency.service.DisasterService;
import com.disaster.emergency.service.TextParseService;
import com.disaster.emergency.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/disaster")
@CrossOrigin
public class DisasterController {

    @Autowired
    private DisasterService disasterService;
    
    @Autowired
    private TextParseService textParseService;
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

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

    @GetMapping("/list")
    public Result<Map<String, Object>> getDisasterList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String disasterType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String status) {
        
        // 参数验证
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", 50);
        result.put("pages", 5);
        result.put("current", page);
        result.put("size", size);
        result.put("records", disasterService.list());
        
        return Result.success("查询成功", result);
    }

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

    @PutMapping("/{id}/status")
    public Result<Map<String, Object>> updateDisasterStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        if (id == null || id <= 0) {
            return Result.error(20002, "灾情ID无效");
        }
        
        String status = request.get("status");
        String remark = request.get("remark");
        
        if (status == null || status.trim().isEmpty()) {
            return Result.error(20001, "状态不能为空");
        }
        
        Disaster disaster = disasterService.getById(id);
        if (disaster == null) {
            return Result.error(20002, "灾情ID不存在");
        }
        
        disaster.setStatus(status);
        disaster.setUpdateTime(LocalDateTime.now());
        disasterService.updateById(disaster);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", status);
        result.put("updateTime", disaster.getUpdateTime());
        
        return Result.success("状态更新成功", result);
    }
    
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
            disaster.setParsedData(parseResult.toString());
            
            // 从解析结果中提取信息
            if (parseResult.containsKey("disaster_type")) {
                disaster.setDisasterType((String) parseResult.get("disaster_type"));
            }
            if (parseResult.containsKey("severity")) {
                disaster.setSeverity((String) parseResult.get("severity"));
            }
            if (parseResult.containsKey("location")) {
                String location = (String) parseResult.get("location");
                // 简单的位置解析
                if (location.contains("省")) {
                    disaster.setProvince(location.split("省")[0] + "省");
                }
                if (location.contains("市")) {
                    disaster.setCity(location.split("市")[0] + "市");
                }
                if (location.contains("区") || location.contains("县")) {
                    disaster.setDistrict(location);
                }
            }
            
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
}