package com.resource.platform.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.slf4j.MDC;

/**
 * 统一 API 响应体
 *
 * <p>格式：
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... },
 *   "traceId": "a1b2c3d4e5f6g7h8",
 *   "timestamp": 1711548000000
 * }
 * </pre>
 *
 * <p>traceId 自动从 MDC 读取（由 TraceIdFilter 注入），null 时不序列化到 JSON。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    /** 请求追踪 ID，便于日志关联排查 */
    private String traceId;
    /** 服务器时间戳（毫秒） */
    private Long timestamp;

    // ==================== 静态工厂方法 ====================

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        result.traceId = MDC.get("traceId");
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = success(data);
        result.message = message;
        return result;
    }

    /** 使用业务错误码枚举构建错误响应 */
    public static <T> Result<T> error(BizErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.code = errorCode.getCode();
        result.message = errorCode.getMessage();
        result.traceId = MDC.get("traceId");
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    /** 使用业务错误码枚举 + 自定义消息 */
    public static <T> Result<T> error(BizErrorCode errorCode, String message) {
        Result<T> result = error(errorCode);
        result.message = message;
        return result;
    }

    /** 直接使用错误码和消息（兼容旧代码） */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.traceId = MDC.get("traceId");
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    /** 通用错误（兼容旧代码） */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    // ==================== 便捷判断方法 ====================

    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
