package com.resource.platform.common;

public class ErrorCode {
    public static final Integer SUCCESS = 200;
    public static final Integer PARAM_ERROR = 400;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer FORBIDDEN = 403;
    public static final Integer NOT_FOUND = 404;
    public static final Integer SERVER_ERROR = 500;
    
    // 业务错误码
    public static final Integer RESOURCE_NOT_FOUND = 1001;
    public static final Integer CATEGORY_NOT_FOUND = 1002;
    public static final Integer CATEGORY_HAS_RESOURCES = 1003;
    public static final Integer CATEGORY_NAME_EXISTS = 1004;
    public static final Integer USER_NOT_FOUND = 1005;
    public static final Integer PASSWORD_ERROR = 1006;
    public static final Integer TOKEN_EXPIRED = 1007;
    public static final Integer TOKEN_INVALID = 1008;
}
