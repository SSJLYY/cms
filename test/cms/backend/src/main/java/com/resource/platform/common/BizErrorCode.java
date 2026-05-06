package com.resource.platform.common;

/**
 * 统一错误码枚举
 *
 * <p>命名规范：{业务模块}_{错误类型}
 * <p>错误码段：
 * <ul>
 *   <li>2xx — 成功</li>
 *   <li>4xx — 客户端错误（参数、权限等）</li>
 *   <li>5xx — 服务端错误</li>
 *   <li>1xxx — 资源/分类相关</li>
 *   <li>2xxx — 用户/认证相关</li>
 *   <li>3xxx — 文件/存储相关</li>
 *   <li>4xxx — 爬虫相关</li>
 *   <li>9xxx — 系统/基础设施相关</li>
 * </ul>
 */
public enum BizErrorCode {

    // ==================== 通用 ====================
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    SERVER_ERROR(500, "服务器内部错误"),

    // ==================== 资源/分类（1xxx）====================
    RESOURCE_NOT_FOUND(1001, "资源不存在"),
    CATEGORY_NOT_FOUND(1002, "分类不存在"),
    CATEGORY_HAS_RESOURCES(1003, "该分类下存在资源，无法删除"),
    CATEGORY_NAME_EXISTS(1004, "分类名称已存在"),
    RESOURCE_STATUS_ERROR(1005, "资源状态不允许该操作"),
    RESOURCE_BATCH_DELETE_FAILED(1006, "资源批量删除失败"),

    // ==================== 用户/认证（2xxx）====================
    USER_NOT_FOUND(2001, "用户不存在"),
    PASSWORD_ERROR(2002, "密码错误"),
    TOKEN_EXPIRED(2003, "登录已过期，请重新登录"),
    TOKEN_INVALID(2004, "无效的 Token"),
    USER_DISABLED(2005, "账号已被禁用"),
    USER_ALREADY_EXISTS(2006, "用户名已存在"),

    // ==================== 文件/存储（3xxx）====================
    FILE_TOO_LARGE(3001, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(3002, "不支持的文件类型"),
    FILE_UPLOAD_FAILED(3003, "文件上传失败"),
    FILE_NOT_FOUND(3004, "文件不存在"),
    STORAGE_SERVICE_UNAVAILABLE(3005, "存储服务暂时不可用"),

    // ==================== 爬虫（4xxx）====================
    CRAWLER_TASK_RUNNING(4001, "爬虫任务正在执行中"),
    CRAWLER_TASK_NOT_FOUND(4002, "爬虫任务不存在"),
    CRAWLER_URL_INVALID(4003, "目标 URL 无效"),
    CRAWLER_ROBOTS_DENIED(4004, "目标网站 robots.txt 禁止爬取"),

    // ==================== 系统（9xxx）====================
    SERVICE_CIRCUIT_OPEN(9001, "服务熔断中，请稍后重试"),
    RATE_LIMIT_EXCEEDED(9002, "请求频率超限"),
    CONFIG_NOT_FOUND(9003, "配置项不存在"),
    CONFIG_UPDATE_FAILED(9004, "配置更新失败"),
    CONFIG_BATCH_UPDATE_FAILED(9005, "批量更新配置全部失败"),
    CONFIG_RESET_FAILED(9006, "配置重置失败"),
    EMAIL_SERVICE_DISABLED(9007, "邮件服务未启用"),
    EMAIL_SENDER_NOT_CONFIGURED(9008, "邮件发送器未配置"),
    EMAIL_SEND_FAILED(9009, "邮件发送失败，请检查邮件配置"),
    LOG_EXPORT_FAILED(9010, "导出日志失败"),
    DOWNLOAD_LINK_SAVE_FAILED(1006, "下载链接保存失败"),
    DOWNLOAD_LINK_DELETE_FAILED(1007, "下载链接删除失败"),
    CRAWLER_CATEGORY_MAPPING_ERROR(4005, "分类映射格式错误"),
    CRAWLER_CUSTOM_RULES_ERROR(4006, "自定义规则格式错误"),
    BATCH_OPERATION_FAILED(9100, "批量操作失败");

    private final int code;
    private final String message;

    BizErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
