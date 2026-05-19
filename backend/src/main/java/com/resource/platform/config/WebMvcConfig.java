package com.resource.platform.config;

import com.resource.platform.module.system.service.impl.StorageSettingsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC config.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private StorageSettingsProvider storageSettingsProvider;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localUrlPrefix = normalizePrefix(storageSettingsProvider.getLocalUrlPrefix());
        String localPath = storageSettingsProvider.getLocalPath();
        registry.addResourceHandler(localUrlPrefix + "/**")
            .addResourceLocations("file:" + localPath + "/");
        if (!"/uploads".equals(localUrlPrefix)) {
            registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + localPath + "/");
        }
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "/uploads";
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
