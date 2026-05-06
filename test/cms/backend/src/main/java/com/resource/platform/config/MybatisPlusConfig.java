package com.resource.platform.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
    "com.resource.platform.module.resource.mapper",
    "com.resource.platform.module.category.mapper",
    "com.resource.platform.module.user.mapper",
    "com.resource.platform.module.image.mapper",
    "com.resource.platform.module.crawler.mapper",
    "com.resource.platform.module.promotion.mapper",
    "com.resource.platform.module.feedback.mapper",
    "com.resource.platform.module.system.mapper",
    "com.resource.platform.module.revenue.mapper"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
