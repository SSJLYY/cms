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
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + storageSettingsProvider.getLocalPath() + "/");
    }
}
