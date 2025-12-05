package com.resource.platform.config;

import com.resource.platform.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 密码加密
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 登录接口
                .antMatchers("/api/users/login").permitAll()
                // 调试接口
                .antMatchers("/api/debug/**").permitAll()
                // 客户端公开接口 - 资源相关
                .antMatchers("/api/resources/public/**").permitAll()
                // 客户端公开接口 - 分类相关
                .antMatchers("/api/categories/list").permitAll()
                .antMatchers("/api/categories/tree").permitAll()
                .antMatchers("/api/categories/public/**").permitAll()
                // 客户端公开接口 - 配置相关
                .antMatchers("/api/config/public").permitAll()
                .antMatchers("/api/config/public/**").permitAll()
                // 客户端公开接口 - 下载链接相关
                .antMatchers("/api/download-links/resource/**").permitAll()
                .antMatchers("/api/download-links/public/**").permitAll()
                // 客户端公开接口 - 网盘类型相关
                .antMatchers("/api/link-types/public/**").permitAll()
                // 客户端公开接口 - 反馈相关
                .antMatchers("/api/feedback/public/**").permitAll()
                // 静态资源和文件访问
                .antMatchers("/uploads/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/thumbnails/**").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/*.html", "/*.js", "/*.css", "/*.png", "/*.jpg", "/*.ico").permitAll()
                // Swagger文档
                .antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/favicon.ico").permitAll()
                // 管理员接口需要ADMIN角色
                .antMatchers("/api/*/admin/**").hasRole("ADMIN")
                // 其他所有接口需要认证
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
