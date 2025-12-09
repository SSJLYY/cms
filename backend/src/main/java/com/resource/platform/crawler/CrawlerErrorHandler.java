package com.resource.platform.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 爬虫错误处理器
 * 负责错误分类和错误信息格式化
 */
@Slf4j
@Component
public class CrawlerErrorHandler {

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NETWORK_ERROR("网络错误"),
        PARSE_ERROR("解析错误"),
        VALIDATION_ERROR("验证错误"),
        UNKNOWN_ERROR("未知错误");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 分类错误
     */
    public ErrorType categorizeError(Exception e) {
        if (e instanceof java.io.IOException || 
            e instanceof java.net.SocketTimeoutException ||
            e instanceof java.net.UnknownHostException ||
            e instanceof java.net.ConnectException) {
            return ErrorType.NETWORK_ERROR;
        } else if (e instanceof org.jsoup.select.Selector.SelectorParseException ||
                   e.getMessage() != null && e.getMessage().contains("parse")) {
            return ErrorType.PARSE_ERROR;
        } else if (e instanceof IllegalArgumentException ||
                   e.getMessage() != null && e.getMessage().contains("验证") ||
                   e.getMessage() != null && e.getMessage().contains("validation")) {
            return ErrorType.VALIDATION_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }

    /**
     * 获取错误类型字符串
     */
    public String getErrorTypeString(Exception e) {
        return categorizeError(e).name();
    }

    /**
     * 格式化错误信息
     */
    public String formatErrorMessage(Exception e) {
        ErrorType errorType = categorizeError(e);
        String message = e.getMessage() != null ? e.getMessage() : "未知错误";
        
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorType.getDescription()).append("] ");
        sb.append(message);
        
        // 添加可操作的建议
        switch (errorType) {
            case NETWORK_ERROR:
                sb.append(" - 建议: 检查网络连接或目标网站是否可访问");
                break;
            case PARSE_ERROR:
                sb.append(" - 建议: 检查网站结构是否变化，或调整解析规则");
                break;
            case VALIDATION_ERROR:
                sb.append(" - 建议: 检查数据格式是否符合要求");
                break;
            case UNKNOWN_ERROR:
                sb.append(" - 建议: 查看详细日志以获取更多信息");
                break;
        }
        
        return sb.toString();
    }

    /**
     * 记录错误日志
     */
    public void logError(ErrorType errorType, Exception e, String context) {
        String errorMessage = formatErrorMessage(e);
        
        switch (errorType) {
            case NETWORK_ERROR:
                log.warn("网络错误 [{}]: {}", context, errorMessage, e);
                break;
            case PARSE_ERROR:
                log.error("解析错误 [{}]: {}", context, errorMessage, e);
                break;
            case VALIDATION_ERROR:
                log.warn("验证错误 [{}]: {}", context, errorMessage, e);
                break;
            case UNKNOWN_ERROR:
                log.error("未知错误 [{}]: {}", context, errorMessage, e);
                break;
        }
    }

    /**
     * 判断错误是否可重试
     */
    public boolean isRetryable(ErrorType errorType) {
        return errorType == ErrorType.NETWORK_ERROR;
    }

    /**
     * 判断错误是否可重试
     */
    public boolean isRetryable(Exception e) {
        return isRetryable(categorizeError(e));
    }
}
