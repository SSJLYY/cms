package com.resource.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * HTTP客户端工具类
 * 提供HTTP请求功能，支持重试机制和超时处理
 */
@Slf4j
public class HttpClientUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    
    // 默认重试次数
    private static final int DEFAULT_MAX_RETRIES = 3;
    
    // 默认超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 10000;
    
    // 重试间隔（毫秒）
    private static final int RETRY_INTERVAL = 1000;

    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, null, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送GET请求（带请求头）
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers) {
        return get(url, headers, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送GET请求（带重试）
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param maxRetries 最大重试次数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers, int maxRetries) {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                if (headers != null) {
                    headers.forEach(httpHeaders::set);
                }
                
                HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("GET请求成功: {}", url);
                    return response.getBody();
                } else {
                    log.warn("GET请求失败，状态码: {}", response.getStatusCode());
                    throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                
                if (retries <= maxRetries) {
                    log.warn("GET请求失败，第{}次重试: {}", retries, url, e);
                    try {
                        Thread.sleep(RETRY_INTERVAL * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("GET请求失败，已达最大重试次数: {}", url, e);
                }
            }
        }
        
        throw new RuntimeException("HTTP请求失败，已重试" + maxRetries + "次", lastException);
    }

    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     */
    public static String post(String url, Object body) {
        return post(url, body, null, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送POST请求（带请求头）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, Object body, Map<String, String> headers) {
        return post(url, body, headers, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送POST请求（带重试）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param maxRetries 最大重试次数
     * @return 响应内容
     */
    public static String post(String url, Object body, Map<String, String> headers, int maxRetries) {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                if (headers != null) {
                    headers.forEach(httpHeaders::set);
                }
                
                HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("POST请求成功: {}", url);
                    return response.getBody();
                } else {
                    log.warn("POST请求失败，状态码: {}", response.getStatusCode());
                    throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                
                if (retries <= maxRetries) {
                    log.warn("POST请求失败，第{}次重试: {}", retries, url, e);
                    try {
                        Thread.sleep(RETRY_INTERVAL * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("POST请求失败，已达最大重试次数: {}", url, e);
                }
            }
        }
        
        throw new RuntimeException("HTTP请求失败，已重试" + maxRetries + "次", lastException);
    }

    /**
     * 发送PUT请求
     * 
     * @param url 请求URL
     * @param body 请求体
     * @return 响应内容
     */
    public static String put(String url, Object body) {
        return put(url, body, null, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送PUT请求（带重试）
     * 
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param maxRetries 最大重试次数
     * @return 响应内容
     */
    public static String put(String url, Object body, Map<String, String> headers, int maxRetries) {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                if (headers != null) {
                    headers.forEach(httpHeaders::set);
                }
                
                HttpEntity<Object> entity = new HttpEntity<>(body, httpHeaders);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("PUT请求成功: {}", url);
                    return response.getBody();
                } else {
                    log.warn("PUT请求失败，状态码: {}", response.getStatusCode());
                    throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                
                if (retries <= maxRetries) {
                    log.warn("PUT请求失败，第{}次重试: {}", retries, url, e);
                    try {
                        Thread.sleep(RETRY_INTERVAL * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("PUT请求失败，已达最大重试次数: {}", url, e);
                }
            }
        }
        
        throw new RuntimeException("HTTP请求失败，已重试" + maxRetries + "次", lastException);
    }

    /**
     * 发送DELETE请求
     * 
     * @param url 请求URL
     * @return 响应内容
     */
    public static String delete(String url) {
        return delete(url, null, DEFAULT_MAX_RETRIES);
    }

    /**
     * 发送DELETE请求（带重试）
     * 
     * @param url 请求URL
     * @param headers 请求头
     * @param maxRetries 最大重试次数
     * @return 响应内容
     */
    public static String delete(String url, Map<String, String> headers, int maxRetries) {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                if (headers != null) {
                    headers.forEach(httpHeaders::set);
                }
                
                HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("DELETE请求成功: {}", url);
                    return response.getBody();
                } else {
                    log.warn("DELETE请求失败，状态码: {}", response.getStatusCode());
                    throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
                }
                
            } catch (Exception e) {
                lastException = e;
                retries++;
                
                if (retries <= maxRetries) {
                    log.warn("DELETE请求失败，第{}次重试: {}", retries, url, e);
                    try {
                        Thread.sleep(RETRY_INTERVAL * retries);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("DELETE请求失败，已达最大重试次数: {}", url, e);
                }
            }
        }
        
        throw new RuntimeException("HTTP请求失败，已重试" + maxRetries + "次", lastException);
    }
}
