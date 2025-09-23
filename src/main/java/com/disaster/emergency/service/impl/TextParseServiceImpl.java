package com.disaster.emergency.service.impl;

import com.disaster.emergency.entity.TextParseRecord;
import com.disaster.emergency.mapper.TextParseRecordMapper;
import com.disaster.emergency.service.TextParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextParseServiceImpl implements TextParseService {
    
    @Autowired
    private TextParseRecordMapper textParseRecordMapper;
    
    @Override
    public Map<String, Object> parseText(String originalText, String businessType) {
        Map<String, Object> result = new HashMap<>();
        
        // 先尝试关键词解析
        Map<String, Object> keywordResult = parseByKeywords(originalText);
        if (!keywordResult.isEmpty()) {
            result.putAll(keywordResult);
        }
        
        // 再尝试正则表达式解析
        Map<String, Object> regexResult = parseByRegex(originalText);
        if (!regexResult.isEmpty()) {
            result.putAll(regexResult);
        }
        
        // 保存解析记录
        TextParseRecord record = new TextParseRecord();
        record.setOriginalText(originalText);
        record.setParsedResult(mapToJson(result));
        record.setParseStatus(result.isEmpty() ? "failed" : "success");
        record.setConfidenceScore(calculateConfidence(result));
        // record.setParseAlgorithm("keyword+regex"); // 暂时注释掉
        record.setBusinessType(businessType);
        record.setCreateTime(LocalDateTime.now());
        
        saveParseRecord(record);
        
        return result;
    }
    
    @Override
    public boolean saveParseRecord(TextParseRecord record) {
        return textParseRecordMapper.insert(record) > 0;
    }
    
    @Override
    public TextParseRecord getParseRecord(Long businessId, String businessType) {
        return textParseRecordMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TextParseRecord>()
                .eq("business_id", businessId)
                .eq("business_type", businessType)
        );
    }
    
    @Override
    public Map<String, Object> parseByKeywords(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // 灾害类型关键词
        if (text.contains("地震") || text.contains("震级") || text.contains("震中")) {
            result.put("disaster_type", "地震");
        } else if (text.contains("洪水") || text.contains("洪涝") || text.contains("暴雨")) {
            result.put("disaster_type", "洪水");
        } else if (text.contains("火灾") || text.contains("失火") || text.contains("燃烧")) {
            result.put("disaster_type", "火灾");
        } else if (text.contains("台风") || text.contains("飓风")) {
            result.put("disaster_type", "台风");
        }
        
        // 需求类型关键词
        if (text.contains("帐篷") || text.contains("临时住所")) {
            result.put("demand_type", "帐篷");
        } else if (text.contains("食品") || text.contains("食物") || text.contains("粮食")) {
            result.put("demand_type", "食品");
        } else if (text.contains("药品") || text.contains("药物") || text.contains("医疗")) {
            result.put("demand_type", "药品");
        } else if (text.contains("饮用水") || text.contains("水")) {
            result.put("demand_type", "饮用水");
        }
        
        // 严重程度关键词
        if (text.contains("严重") || text.contains("重大")) {
            result.put("severity", "严重");
        } else if (text.contains("一般") || text.contains("中等")) {
            result.put("severity", "一般");
        } else if (text.contains("轻微") || text.contains("轻微")) {
            result.put("severity", "轻微");
        }
        
        // 紧急程度关键词
        if (text.contains("紧急") || text.contains("急")) {
            result.put("urgency", "紧急");
        } else if (text.contains("高") || text.contains("重要")) {
            result.put("urgency", "高");
        } else if (text.contains("中") || text.contains("一般")) {
            result.put("urgency", "中");
        } else if (text.contains("低")) {
            result.put("urgency", "低");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> parseByRegex(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // 提取数量
        Pattern quantityPattern = Pattern.compile("(\\d+)\\s*(顶|箱|瓶|台|个|件)");
        Matcher quantityMatcher = quantityPattern.matcher(text);
        if (quantityMatcher.find()) {
            result.put("quantity", Integer.parseInt(quantityMatcher.group(1)));
            result.put("unit", quantityMatcher.group(2));
        }
        
        // 提取地点
        Pattern locationPattern = Pattern.compile("([^，,。.]+?[市县区])");
        Matcher locationMatcher = locationPattern.matcher(text);
        if (locationMatcher.find()) {
            result.put("location", locationMatcher.group(1));
        }
        
        // 提取时间
        Pattern timePattern = Pattern.compile("(\\d{4}[-年]\\d{1,2}[-月]\\d{1,2}[日]?\\s*\\d{1,2}:\\d{1,2})");
        Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            result.put("time", timeMatcher.group(1));
        }
        
        return result;
    }
    
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
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    private BigDecimal calculateConfidence(Map<String, Object> result) {
        // 简单的置信度计算
        int fieldCount = result.size();
        if (fieldCount == 0) return BigDecimal.ZERO;
        if (fieldCount >= 3) return new BigDecimal("90");
        if (fieldCount >= 2) return new BigDecimal("70");
        return new BigDecimal("50");
    }
}
