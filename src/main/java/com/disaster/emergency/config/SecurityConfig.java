package com.disaster.emergency.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 允许访问的路径
            .antMatchers("/user/login", "/user/register").permitAll()
            .antMatchers("/system/**").permitAll()  // 系统接口
            .antMatchers("/disaster/**").permitAll()  // 灾情相关接口
            .antMatchers("/demand/**").permitAll()  // 需求相关接口
            .antMatchers("/resource/**").permitAll()  // 资源相关接口
            .antMatchers("/organization/**").permitAll()  // 机构相关接口
            .antMatchers("/matching/**").permitAll()  // 匹配相关接口
            .antMatchers("/scheduling/**").permitAll()  // 调度相关接口
            .antMatchers("/statistics/**").permitAll()  // 统计相关接口
            .antMatchers("/knowledge-graph/**").permitAll()  // 知识图谱相关接口
            .antMatchers("/similarity/**").permitAll()  // 相似度相关接口
            .antMatchers("/text-parse/**").permitAll()  // 文本解析相关接口
            .antMatchers("/system-config/**").permitAll()  // 系统配置相关接口
            .antMatchers("/map/**").permitAll()  // 地图相关接口
            .antMatchers("/druid/**").permitAll()  // Druid监控
            .antMatchers("/actuator/**").permitAll()  // Spring Boot Actuator
            .antMatchers("/error").permitAll()  // 错误页面
            .anyRequest().authenticated()
            .and()
            .httpBasic().disable()
            .formLogin().disable();
    }
}
