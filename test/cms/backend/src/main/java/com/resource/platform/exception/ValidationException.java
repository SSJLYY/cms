package com.resource.platform.exception;

/**
 * 数据验证异常
 */
public class ValidationException extends BusinessException {
    
    public ValidationException(String message) {
        super(400, message);
    }
}
