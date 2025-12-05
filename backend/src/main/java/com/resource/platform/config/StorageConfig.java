package com.resource.platform.config;

import com.resource.platform.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储配置类
 * 根据配置选择存储服务实现
 */
@Slf4j
@Configuration
public class StorageConfig {

    @Value("${storage.type:local}")
    private String storageType;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public StorageService storageService() {
        StorageService storageService;
        
        switch (storageType.toLowerCase()) {
            case "oss":
                storageService = applicationContext.getBean("ossStorageService", StorageService.class);
                log.info("使用阿里云OSS存储");
                break;
            case "cos":
                // COS实现可以后续添加
                log.warn("COS存储暂未实现，使用本地存储");
                storageService = applicationContext.getBean("localStorageService", StorageService.class);
                break;
            case "qiniu":
                // 七牛云实现可以后续添加
                log.warn("七牛云存储暂未实现，使用本地存储");
                storageService = applicationContext.getBean("localStorageService", StorageService.class);
                break;
            case "local":
            default:
                storageService = applicationContext.getBean("localStorageService", StorageService.class);
                log.info("使用本地存储");
                break;
        }
        
        return storageService;
    }
}
