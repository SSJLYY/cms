package com.resource.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("资源下载平台 API 文档")
                        .description("资源下载平台后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Resource Platform")));
    }
}
