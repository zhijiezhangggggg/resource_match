package com.disaster.emergency.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger/OpenAPI 配置类
 * 
 * @author 系统管理员
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("灾害应急资源匹配平台 API 文档")
                        .description("基于知识图谱的灾害应急资源智能匹配系统接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@disaster-emergency.com")
                                .url("https://github.com/disaster-emergency"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080/api").description("开发环境"),
                        new Server().url("https://api.disaster-emergency.com").description("生产环境")
                ))
                .tags(Arrays.asList(
                        new Tag().name("用户管理").description("用户登录、注册、信息管理"),
                        new Tag().name("灾情管理").description("灾情上报、查询、状态管理"),
                        new Tag().name("需求管理").description("救援需求提交、查询、管理"),
                        new Tag().name("资源管理").description("资源信息管理、查询、更新"),
                        new Tag().name("知识图谱").description("知识节点、关系、图谱操作"),
                        new Tag().name("文本解析").description("文本解析、关键词提取"),
                        new Tag().name("相似度计算").description("需求与资源相似度计算"),
                        new Tag().name("匹配调度").description("资源匹配、调度管理"),
                        new Tag().name("统计分析").description("数据统计、分析报表"),
                        new Tag().name("系统配置").description("系统参数配置管理")
                ));
    }
}
