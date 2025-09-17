package com.admin.common;

import com.disaster.emergency.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应结果处理器
 * 
 * @author admin
 * @date 2024
 */
@Slf4j
@RestControllerAdvice
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果返回类型已经是Result类型，则不需要处理
        return !returnType.getGenericParameterType().getTypeName().contains("Result");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // 如果body为null，返回成功结果
        if (body == null) {
            return Result.success("操作成功", null);
        }
        
        // 如果body已经是Result类型，直接返回
        if (body instanceof Result) {
            return body;
        }
        
        // 其他情况包装成Result
        return Result.success(body);
    }
}
