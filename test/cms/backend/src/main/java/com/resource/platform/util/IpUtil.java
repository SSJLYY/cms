package com.resource.platform.util;

import javax.servlet.http.HttpServletRequest;

/**
 * IP 地址工具类
 *
 * <p>统一提取客户端真实 IP 的逻辑，支持多级代理场景。
 * 所有需要获取客户端 IP 的地方统一使用此类，避免重复代码。
 *
 * <p>IP 获取优先级：
 * <ol>
 *   <li>X-Forwarded-For（代理服务器添加，取第一个 IP）</li>
 *   <li>X-Real-IP（Nginx 等反向代理添加）</li>
 *   <li>Proxy-Client-IP</li>
 *   <li>WL-Proxy-Client-IP</li>
 *   <li>HTTP_CLIENT_IP</li>
 *   <li>HTTP_X_FORWARDED_FOR</li>
 *   <li>RemoteAddr（直连情况）</li>
 * </ol>
 */
public final class IpUtil {

    private static final String[] IP_HEADERS = {
        "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
        "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
    };

    private static final String UNKNOWN = "unknown";

    private IpUtil() {
        // 工具类禁止实例化
    }

    /**
     * 获取客户端真实 IP 地址
     *
     * <p>依次从常见代理头中提取 IP，对于多级代理取第一个 IP。
     *
     * @param request HTTP 请求对象
     * @return 客户端真实 IP 地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For 可能包含多个IP，格式为：client, proxy1, proxy2
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 校验 IP 字符串是否有效（非空、非 unknown）
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
