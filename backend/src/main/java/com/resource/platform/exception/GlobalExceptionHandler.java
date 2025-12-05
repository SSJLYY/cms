package com.resource.platform.exception;

import com.resource.platform.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<?> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        log.error("资源未找到 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(FileUploadException.class)
    public Result<?> handleFileUploadException(FileUploadException e, HttpServletRequest request) {
        log.error("文件上传异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public Result<?> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.error("数据验证异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Result<?> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.error("未授权访问 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数验证失败";
        log.error("参数验证异常 [{}]: {}", request.getRequestURI(), message);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("field", fieldError != null ? fieldError.getField() : "unknown");
        errorDetails.put("message", message);
        errorDetails.put("timestamp", LocalDateTime.now());
        
        return Result.error(400, message);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "缺少必需参数: " + e.getParameterName();
        log.error("缺少请求参数 [{}]: {}", request.getRequestURI(), message);
        return Result.error(400, message);
    }
    
    /**
     * 处理请求体不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求体解析失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "请求体格式错误");
    }
    
    /**
     * 处理JWT认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("认证异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(401, "认证失败：" + e.getMessage());
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.error("接口不存在 [{}]", request.getRequestURI());
        return Result.error(404, "接口不存在");
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.error("请求方法不支持 [{}]: {}", request.getRequestURI(), e.getMethod());
        return Result.error(405, "不支持的请求方法: " + e.getMethod());
    }
    
    /**
     * 处理文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.error("文件大小超限 [{}]", request.getRequestURI());
        return Result.error(400, "上传文件大小超过限制");
    }
    
    /**
     * 处理数据库异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<?> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        log.error("数据库操作异常 [{}]", request.getRequestURI(), e);
        return Result.error(500, "数据库操作失败");
    }
    
    /**
     * 处理重复键异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.error("数据重复 [{}]", request.getRequestURI(), e);
        return Result.error(400, "数据已存在，请勿重复提交");
    }
    
    /**
     * 处理SQL异常
     */
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSQLException(SQLException e, HttpServletRequest request) {
        log.error("SQL异常 [{}]", request.getRequestURI(), e);
        return Result.error(500, "数据库操作失败");
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 [{}]", request.getRequestURI(), e);
        return Result.error(500, "系统内部错误");
    }
    
    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]", request.getRequestURI(), e);
        return Result.error(500, "系统异常，请联系管理员");
    }
}
