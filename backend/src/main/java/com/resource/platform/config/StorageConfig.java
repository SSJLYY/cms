package com.resource.platform.config;

import com.resource.platform.module.system.service.StorageService;
import com.resource.platform.module.system.service.impl.DynamicStorageService;
import com.resource.platform.module.system.service.impl.StorageServiceResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Storage service wiring.
 */
@Slf4j
@Configuration
public class StorageConfig {

    @Autowired
    private StorageServiceResolver storageServiceResolver;

    @Autowired
    private DynamicStorageService dynamicStorageService;

    @Bean
    public StorageService storageService() {
        StorageService activeStorageService = storageServiceResolver.getCurrentStorageService();
        log.info("Loaded dynamic storage service, current backend: {}", activeStorageService.getClass().getSimpleName());
        return dynamicStorageService;
    }
}
