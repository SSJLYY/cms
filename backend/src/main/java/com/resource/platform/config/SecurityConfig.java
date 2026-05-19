package com.resource.platform.config;

import com.resource.platform.filter.JwtAuthenticationFilter;
import com.resource.platform.filter.RateLimitFilter;
import com.resource.platform.filter.TraceIdFilter;
import com.resource.platform.module.system.service.impl.StorageSettingsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security 安全配置
 *
 * <p>安全特性：
 * <ul>
 *   <li>JWT 无状态认证（Session-less）</li>
 *   <li>BCrypt 密码加密</li>
 *   <li>HTTP 安全响应头防御（XSS / 点击劫持 / MIME 嗅探）</li>
 *   <li>Actuator 端点需要 ADMIN 权限</li>
 *   <li>Debug 接口通过 @Profile("dev") 在生产环境不注册，此处也不开放白名单</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private TraceIdFilter traceIdFilter;

    @Autowired
    private StorageSettingsProvider storageSettingsProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 强度12，兼顾安全和性能
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String localUploadPattern = normalizePrefix(storageSettingsProvider.getLocalUrlPrefix()) + "/**";
        http
            // ========== 基础安全策略 ==========
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()

            // ========== HTTP 安全响应头 ==========
            .headers()
                // 防止点击劫持（拒绝 iframe 嵌入）
                .frameOptions().deny()
                // 禁止 MIME 类型嗅探
                .contentTypeOptions().and()
                // 强制 HTTPS（如果后端直接对外则启用；若通过Nginx反代，Nginx层加即可）
                // .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000).and()
                // XSS 保护
                .xssProtection().and()
                // Referrer 策略
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN).and()
                // 权限策略
                .permissionsPolicy(policy -> policy.policy("geolocation=(), microphone=(), camera=()")).and()
            .and()

            // ========== 接口访问控制 ==========
            .authorizeRequests()
                // --- 认证接口 ---
                .antMatchers(HttpMethod.POST, "/api/users/login").permitAll()

                // ✅ 移除：/api/debug/** 不再在此白名单（@Profile("dev") 已限制）
                // --- 客户端公开接口 - 资源 ---
                .antMatchers(HttpMethod.GET, "/api/resources/public/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/resources/public/**").permitAll()
                // --- 客户端公开接口 - 分类 ---
                .antMatchers(HttpMethod.GET, "/api/categories/list").permitAll()
                .antMatchers(HttpMethod.GET, "/api/categories/tree").permitAll()
                .antMatchers(HttpMethod.GET, "/api/categories/public/**").permitAll()
                // --- 客户端公开接口 - 配置 ---
                .antMatchers(HttpMethod.GET, "/api/config/public").permitAll()
                .antMatchers(HttpMethod.GET, "/api/config/public/**").permitAll()
                // --- 客户端公开接口 - 下载链接 ---
                .antMatchers(HttpMethod.GET, "/api/download-links/resource/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/download-links/public/**").permitAll()
                // --- 客户端公开接口 - 网盘类型 ---
                .antMatchers(HttpMethod.GET, "/api/link-types/public/**").permitAll()
                // --- 客户端公开接口 - 反馈提交 ---
                .antMatchers(HttpMethod.POST, "/api/feedback/public/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/feedback/public/**").permitAll()
                // --- 客户端公开接口 - 友情链接 ---
                .antMatchers(HttpMethod.GET, "/api/friendlinks/enabled").permitAll()
                // --- 客户端公开接口 - 广告推广 ---
                .antMatchers(HttpMethod.GET, "/api/promotion/active").permitAll()
                .antMatchers(HttpMethod.POST, "/api/promotion/*/click").permitAll()
                // --- 静态资源 ---
                .antMatchers(localUploadPattern, "/uploads/**", "/images/**", "/thumbnails/**", "/static/**").permitAll()
                .antMatchers("/*.html", "/*.js", "/*.css", "/*.png", "/*.jpg", "/*.ico").permitAll()
                // --- API 文档（生产环境通过 application-prod.yml 关闭 knife4j）---
                .antMatchers(
                    "/doc.html", "/webjars/**", "/swagger-resources/**",
                    "/v2/api-docs", "/v3/api-docs/**", "/swagger-ui/**",
                    "/swagger-ui.html", "/favicon.ico"
                ).permitAll()
                // --- Actuator：仅 ADMIN 可访问（修复安全漏洞）---
                .antMatchers("/actuator/**").hasRole("ADMIN")
                // --- 管理员接口 ---
                .antMatchers("/api/*/admin/**").hasRole("ADMIN")
                // --- 其余接口需认证 ---
                .anyRequest().authenticated()
            .and()

            // ========== 过滤器链 ==========
            // 顺序：TraceId → RateLimit → JWT 认证
            .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "/uploads";
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
