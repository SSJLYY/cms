package com.resource.platform.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(404, resourceType + " ID为 " + id + " 的资源不存在");
    }
}
