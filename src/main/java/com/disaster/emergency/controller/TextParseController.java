package com.disaster.emergency.controller;

import com.disaster.emergency.common.Result;
import com.disaster.emergency.entity.TextParseRecord;
import com.disaster.emergency.service.TextParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/text-parse")
@CrossOrigin
public class TextParseController {
    
    @Autowired
    private TextParseService textParseService;
    
    @PostMapping("/parse")
    public Result<Map<String, Object>> parseText(@RequestBody Map<String, String> request) {
        try {
            String originalText = request.get("originalText");
            String businessType = request.get("businessType");
            
            if (originalText == null || originalText.trim().isEmpty()) {
                return Result.error(20001, "原始文本不能为空");
            }
            if (businessType == null || businessType.trim().isEmpty()) {
                businessType = "general";
            }
            
            Map<String, Object> parseResult = textParseService.parseText(originalText, businessType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalText", originalText);
            result.put("businessType", businessType);
            result.put("parsedResult", parseResult);
            result.put("parseTime", System.currentTimeMillis());
            
            return Result.success("文本解析成功", result);
        } catch (Exception e) {
            return Result.error(20001, "文本解析失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/parse-keywords")
    public Result<Map<String, Object>> parseByKeywords(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return Result.error(20001, "文本不能为空");
            }
            
            Map<String, Object> parseResult = textParseService.parseByKeywords(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("parseResult", parseResult);
            result.put("parseMethod", "keywords");
            
            return Result.success("关键词解析成功", result);
        } catch (Exception e) {
            return Result.error(20001, "关键词解析失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/parse-regex")
    public Result<Map<String, Object>> parseByRegex(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return Result.error(20001, "文本不能为空");
            }
            
            Map<String, Object> parseResult = textParseService.parseByRegex(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text", text);
            result.put("parseResult", parseResult);
            result.put("parseMethod", "regex");
            
            return Result.success("正则表达式解析成功", result);
        } catch (Exception e) {
            return Result.error(20001, "正则表达式解析失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/record/{businessId}/{businessType}")
    public Result<TextParseRecord> getParseRecord(@PathVariable Long businessId, @PathVariable String businessType) {
        try {
            TextParseRecord record = textParseService.getParseRecord(businessId, businessType);
            if (record == null) {
                return Result.error(20002, "解析记录不存在");
            }
            return Result.success("获取解析记录成功", record);
        } catch (Exception e) {
            return Result.error(20001, "获取解析记录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/test")
    public Result<Map<String, Object>> testParse(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return Result.error(20001, "测试文本不能为空");
            }
            
            // 测试关键词解析
            Map<String, Object> keywordResult = textParseService.parseByKeywords(text);
            
            // 测试正则表达式解析
            Map<String, Object> regexResult = textParseService.parseByRegex(text);
            
            // 综合解析
            Map<String, Object> combinedResult = textParseService.parseText(text, "test");
            
            Map<String, Object> result = new HashMap<>();
            result.put("originalText", text);
            result.put("keywordParse", keywordResult);
            result.put("regexParse", regexResult);
            result.put("combinedParse", combinedResult);
            
            return Result.success("文本解析测试成功", result);
        } catch (Exception e) {
            return Result.error(20001, "文本解析测试失败: " + e.getMessage());
        }
    }
}
