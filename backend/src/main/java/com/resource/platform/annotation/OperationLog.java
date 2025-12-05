package com.resource.platform.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String type() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录到审计日志
     */
    boolean audit() default false;
}
