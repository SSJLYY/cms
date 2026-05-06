package com.resource.platform.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.module.user.entity.User;
import com.resource.platform.module.user.mapper.UserMapper;
import com.resource.platform.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 从数据库查询用户实际角色
            User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username)
                    .eq(User::getStatus, 1)  // 只查询启用状态的用户
            );
            
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();
            if (user != null && StringUtils.hasText(user.getRole())) {
                // 支持多角色，逗号分隔
                authorities = List.of(user.getRole().split(",")).stream()
                    .map(role -> role.trim().toUpperCase())
                    .filter(role -> !role.isEmpty())
                    .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            }
            
            // 如果没有有效角色，赋予默认角色
            if (authorities.isEmpty()) {
                authorities = Collections.singletonList(new SimpleGrantedAuthority(ROLE_PREFIX + DEFAULT_ROLE));
            }
            
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
