package com.resource.platform.exception;

import com.resource.platform.common.BizErrorCode;
import com.resource.platform.common.Result;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局统一异常处理器
 *
 * <p>处理优先级（从高到低）：
 * <ol>
 *   <li>业务异常（BusinessException 及子类）— 返回具体业务错误码</li>
 *   <li>参数验证异常 — 返回 400 + 详细字段错误</li>
 *   <li>Spring Security 异常 — 返回 401/403</li>
 *   <li>Resilience4j 熔断异常 — 返回 503</li>
 *   <li>数据库异常 — 返回 500（不暴露 SQL 细节）</li>
 *   <li>其余未知异常 — 返回 500（记录完整堆栈）</li>
 * </ol>
 *
 * <p>所有响应都自动携带 traceId（由 Result 从 MDC 读取）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] uri={}: {}", e.getCode(), request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<?> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("资源未找到 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public Result<?> handleFileUploadException(FileUploadException e, HttpServletRequest request) {
        log.warn("文件上传失败 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public Result<?> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("数据验证失败 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.warn("未授权访问 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数验证异常 ====================

    /**
     * IllegalArgumentException — 参数非法
     * 返回 400 而非 500，避免与服务器内部错误混淆
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e,
                                                      HttpServletRequest request) {
        log.warn("参数非法 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(BizErrorCode.PARAM_ERROR, e.getMessage());
    }

    /**
     * JSR-303 校验失败（@Valid 注解）
     * 收集所有字段错误，以 "field: message" 格式拼接返回
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                           HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        String errorMessage;
        if (fieldErrors.size() == 1) {
            FieldError fe = fieldErrors.get(0);
            errorMessage = fe.getField() + ": " + fe.getDefaultMessage();
        } else {
            errorMessage = fieldErrors.stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        }

        log.warn("参数验证失败 uri={}: {}", request.getRequestURI(), errorMessage);
        return Result.error(BizErrorCode.PARAM_ERROR, errorMessage);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e,
                                        HttpServletRequest request) {
        String msg = "缺少必需参数: " + e.getParameterName() + " (" + e.getParameterType() + ")";
        log.warn("缺少请求参数 uri={}: {}", request.getRequestURI(), msg);
        return Result.error(BizErrorCode.PARAM_ERROR, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
                                                  HttpServletRequest request) {
        log.warn("请求体解析失败 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(BizErrorCode.PARAM_ERROR, "请求体格式错误，请检查 JSON 格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                              HttpServletRequest request) {
        log.warn("请求方法不支持 uri={}: {}", request.getRequestURI(), e.getMethod());
        return Result.error(405, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFound(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("接口不存在: {}", request.getRequestURI());
        return Result.error(BizErrorCode.NOT_FOUND, "接口不存在: " + request.getRequestURI());
    }

    // ==================== Spring Security 异常 ====================

    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(BizErrorCode.UNAUTHORIZED, "认证失败: " + e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(BizErrorCode.FORBIDDEN);
    }

    // ==================== 熔断异常 ====================

    /**
     * Resilience4j 熔断器打开时抛出此异常
     * 向客户端返回 503，提示稍后重试
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public Result<?> handleCircuitBreakerOpen(CallNotPermittedException e, HttpServletRequest request) {
        log.warn("服务熔断 uri={}: circuitBreaker={}", request.getRequestURI(), e.getCausingCircuitBreakerName());
        return Result.error(BizErrorCode.SERVICE_CIRCUIT_OPEN);
    }

    // ==================== 文件上传异常 ====================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件大小超限 uri={}", request.getRequestURI());
        return Result.error(BizErrorCode.FILE_TOO_LARGE);
    }

    // ==================== 数据库异常（不暴露细节）====================

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest request) {
        // 只记录日志，不向前端暴露 SQL 细节
        log.warn("数据重复 uri={}: {}", request.getRequestURI(), e.getMessage());
        return Result.error(BizErrorCode.PARAM_ERROR, "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccess(DataAccessException e, HttpServletRequest request) {
        log.error("数据库操作异常 uri={}", request.getRequestURI(), e);
        return Result.error(BizErrorCode.SERVER_ERROR, "数据操作失败，请稍后重试");
    }

    // ==================== 兜底异常 ====================

    /**
     * 捕获所有未处理异常
     * 记录完整堆栈（用于排查），但只向前端返回通用错误信息（防止信息泄露）
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 uri={}", request.getRequestURI(), e);
        return Result.error(BizErrorCode.SERVER_ERROR, "系统繁忙，请稍后重试");
    }
}
