package com.resource.platform.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存配置。
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats());

        manager.registerCustomCache("category:tree",
            Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build());

        manager.registerCustomCache("config:public",
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());

        manager.registerCustomCache("link-type:all",
            Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build());

        log.info("Caffeine L1 本地缓存管理器初始化完成");
        return manager;
    }

    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(jackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("resource:detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        configMap.put("resource:list", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("category:tree", defaultConfig.entryTtl(Duration.ofHours(1)));
        configMap.put("config:public", defaultConfig.entryTtl(Duration.ofHours(2)));
        configMap.put("link-type:all", defaultConfig.entryTtl(Duration.ofHours(24)));
        configMap.put("friendlink:enabled", defaultConfig.entryTtl(Duration.ofHours(1)));
        configMap.put("promotion:active", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configMap.put("statistics:overview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configMap.put("resource:hot", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        RedisCacheManager manager = RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configMap)
            .transactionAware()
            .build();

        log.info("Redis L2 分布式缓存管理器初始化完成");
        return manager;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .allowIfSubType("com.resource.platform.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.lang.")
            .allowIfSubType("java.math.")
            .allowIfSubType("java.sql.")
            .allowIfSubType("[Ljava.lang.")
            .allowIfSubType("[Lcom.resource.platform.")
            .build();

        mapper.activateDefaultTyping(
            typeValidator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        serializer.setObjectMapper(mapper);
        return serializer;
    }
}
