package com.resource.platform.exception;

/**
 * 未授权异常
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(403, message);
    }
    
    public UnauthorizedException() {
        super(403, "无权限访问该资源");
    }
}
