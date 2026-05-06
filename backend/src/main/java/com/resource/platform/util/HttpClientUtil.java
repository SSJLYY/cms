package com.resource.platform.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class HttpClientUtil {

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL_MS = 1000;
    private static final RestTemplate REST_TEMPLATE = createRestTemplate();

    private HttpClientUtil() {
    }

    private static RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        log.info("HttpClientUtil 初始化完成");
        return new RestTemplate(factory);
    }

    public static String get(String url) {
        return get(url, null, DEFAULT_MAX_RETRIES);
    }

    public static String get(String url, Map<String, String> headers) {
        return get(url, headers, DEFAULT_MAX_RETRIES);
    }

    public static String get(String url, Map<String, String> headers, int maxRetries) {
        return exchange(url, HttpMethod.GET, null, headers, maxRetries);
    }

    public static String post(String url, Object body) {
        return post(url, body, null, DEFAULT_MAX_RETRIES);
    }

    public static String post(String url, Object body, Map<String, String> headers) {
        return post(url, body, headers, DEFAULT_MAX_RETRIES);
    }

    public static String post(String url, Object body, Map<String, String> headers, int maxRetries) {
        return exchange(url, HttpMethod.POST, body, headers, maxRetries);
    }

    public static String put(String url, Object body) {
        return put(url, body, null, DEFAULT_MAX_RETRIES);
    }

    public static String put(String url, Object body, Map<String, String> headers, int maxRetries) {
        return exchange(url, HttpMethod.PUT, body, headers, maxRetries);
    }

    public static String delete(String url) {
        return delete(url, null, DEFAULT_MAX_RETRIES);
    }

    public static String delete(String url, Map<String, String> headers, int maxRetries) {
        return exchange(url, HttpMethod.DELETE, null, headers, maxRetries);
    }

    private static String exchange(String url, HttpMethod method, Object body, Map<String, String> headers, int maxRetries) {
        int retries = 0;
        Exception lastException = null;

        while (retries <= maxRetries) {
            try {
                HttpHeaders httpHeaders = new HttpHeaders();
                if (body != null) {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                }
                if (headers != null) {
                    headers.forEach(httpHeaders::set);
                }

                HttpEntity<?> entity = new HttpEntity<>(body, httpHeaders);
                ResponseEntity<String> response = REST_TEMPLATE.exchange(url, method, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                }
                throw new RuntimeException("HTTP请求失败，状态码: " + response.getStatusCode());
            } catch (Exception e) {
                lastException = e;
                retries++;
                if (retries > maxRetries) {
                    break;
                }
                try {
                    Thread.sleep((long) RETRY_INTERVAL_MS * retries);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new RuntimeException("HTTP请求失败，已重试 " + maxRetries + " 次", lastException);
    }
}
