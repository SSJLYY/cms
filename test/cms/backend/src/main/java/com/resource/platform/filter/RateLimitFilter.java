package com.resource.platform.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 全局 API 限流过滤器（基于 Redis + Lua 滑动窗口计数器）
 *
 * <p>限流策略（按请求路径分级）：
 * <ul>
 *   <li>登录接口 /api/users/login — 每IP每分钟最多 10 次（防暴力破解）</li>
 *   <li>公开接口 /api/**\/public/** — 每IP每分钟最多 100 次</li>
 *   <li>其他接口 — 每IP每分钟最多 200 次</li>
 * </ul>
 *
 * <p>Lua 脚本原子性保证计数器操作线程安全，无并发竞争问题。
 * 超出限制返回 HTTP 429，不影响正常用户。
 */
@Slf4j
@Component
@Order(2)  // 在 TraceIdFilter 之后执行
public class RateLimitFilter extends OncePerRequestFilter {

    /**
     * Redis Lua 滑动窗口限流脚本
     * KEYS[1] = 限流 key
     * ARGV[1] = 最大请求数
     * ARGV[2] = 窗口时间（秒）
     * 返回值：1=允许，0=拒绝
     */
    private static final String RATE_LIMIT_SCRIPT =
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local current = redis.call('INCR', key)\n" +
        "if current == 1 then\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "end\n" +
        "if current > limit then\n" +
        "    return 0\n" +
        "end\n" +
        "return 1";

    private static final DefaultRedisScript<Long> REDIS_SCRIPT;

    static {
        REDIS_SCRIPT = new DefaultRedisScript<>();
        REDIS_SCRIPT.setScriptText(RATE_LIMIT_SCRIPT);
        REDIS_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.global.limit:200}")
    private int globalLimit;

    @Value("${rate-limit.global.window-seconds:60}")
    private int globalWindowSeconds;

    @Value("${rate-limit.public.limit:100}")
    private int publicLimit;

    @Value("${rate-limit.login.limit:10}")
    private int loginLimit;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // 根据路径确定限流策略
        RateLimitPolicy policy = resolvePolicy(requestUri);

        String redisKey = "ratelimit:" + policy.name + ":" + clientIp;

        try {
            Long result = redisTemplate.execute(
                REDIS_SCRIPT,
                Collections.singletonList(redisKey),
                String.valueOf(policy.limit),
                String.valueOf(policy.windowSeconds)
            );

            if (result == null || result == 0L) {
                log.warn("请求被限流: ip={}, uri={}, policy={}, limit={}/{}s",
                    clientIp, requestUri, policy.name, policy.limit, policy.windowSeconds);
                writeRateLimitResponse(response);
                return;
            }
        } catch (Exception e) {
            // Redis 不可用时降级放行（宁可放过，不可误杀）
            log.error("限流 Redis 操作失败，降级放行: ip={}, uri={}, error={}", clientIp, requestUri, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 根据请求路径决定使用哪套限流策略
     */
    private RateLimitPolicy resolvePolicy(String uri) {
        if (uri.equals("/api/users/login")) {
            return new RateLimitPolicy("login", loginLimit, 60);
        }
        if (uri.contains("/public/") || uri.endsWith("/active") || uri.endsWith("/enabled")) {
            return new RateLimitPolicy("public", publicLimit, 60);
        }
        return new RateLimitPolicy("global", globalLimit, globalWindowSeconds);
    }

    /**
     * 获取客户端真实 IP（支持 Nginx 反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        return com.resource.platform.util.IpUtil.getClientIp(request);
    }

    /**
     * 写入 429 限流响应
     */
    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}";
        response.getWriter().write(body);
    }

    /**
     * 限流策略内部类
     */
    private static class RateLimitPolicy {
        final String name;
        final int limit;
        final int windowSeconds;

        RateLimitPolicy(String name, int limit, int windowSeconds) {
            this.name = name;
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }
}
