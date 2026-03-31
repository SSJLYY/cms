package com.resource.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 *
 * <p>生产环境通过配置 cors.allowed-origins 指定允许的域名列表，禁止使用 "*".
 * 开发环境默认允许 localhost 系列地址。
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:8080,http://localhost:8081,http://127.0.0.1:8080,http://127.0.0.1:8081}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowCredentials(true);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Trace-Id", "Accept", "Origin"));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Trace-Id"));
        config.setMaxAge(3600L); // 预检请求缓存1小时

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
