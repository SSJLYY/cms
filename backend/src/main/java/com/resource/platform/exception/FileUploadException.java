package com.resource.platform.exception;

/**
 * 文件上传异常
 */
public class FileUploadException extends BusinessException {
    
    public FileUploadException(String message) {
        super(400, message);
    }
}
