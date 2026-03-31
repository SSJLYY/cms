package com.resource.platform.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * 请求追踪 ID 过滤器
 *
 * <p>功能：
 * <ol>
 *   <li>每个请求生成或透传唯一的 TraceId（X-Trace-Id 请求头）</li>
 *   <li>将 TraceId 放入 MDC，使所有日志自动携带追踪信息</li>
 *   <li>将 TraceId 回写到响应头，方便前端/调用方排查问题</li>
 *   <li>请求结束后清理 MDC，防止内存泄漏</li>
 * </ol>
 *
 * <p>日志配置：在 log4j2-spring.xml 的 Pattern 中添加 %X{traceId} 即可自动输出
 */
@Slf4j
@Component
@Order(1)  // 最先执行
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = generateTraceId(request);
        try {
            // 注入 MDC（自动传播到所有日志）
            MDC.put(MDC_TRACE_KEY, traceId);
            // 回写到响应头（方便前端/调用方关联日志）
            response.addHeader(TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            // 必须清理，防止线程复用时串用 TraceId
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    /**
     * 生成或透传 TraceId
     * 如果请求头中已有 X-Trace-Id（来自上游网关），则直接使用
     * 否则生成新的短 UUID（16位，节省日志空间）
     */
    private String generateTraceId(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
            .filter(id -> !id.isBlank())
            .orElseGet(() -> UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    }
}
