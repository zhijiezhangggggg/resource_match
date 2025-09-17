package com.disaster.emergency.service;

import com.disaster.emergency.entity.TextParseRecord;

import java.util.Map;

public interface TextParseService {
    
    /**
     * 解析文本
     * @param originalText 原始文本
     * @param businessType 业务类型
     * @return 解析结果
     */
    Map<String, Object> parseText(String originalText, String businessType);
    
    /**
     * 保存解析记录
     * @param record 解析记录
     * @return 保存结果
     */
    boolean saveParseRecord(TextParseRecord record);
    
    /**
     * 根据业务ID获取解析记录
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 解析记录
     */
    TextParseRecord getParseRecord(Long businessId, String businessType);
    
    /**
     * 基于关键词的简单解析
     * @param text 文本
     * @return 解析结果
     */
    Map<String, Object> parseByKeywords(String text);
    
    /**
     * 基于正则表达式的解析
     * @param text 文本
     * @return 解析结果
     */
    Map<String, Object> parseByRegex(String text);
}
