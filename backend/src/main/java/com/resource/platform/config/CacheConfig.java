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
 * 多级缓存配置
 *
 * <p>缓存策略（Cache-Aside 模式）：
 * <pre>
 * 请求 → L1 Caffeine (JVM本地缓存, TTL=5min, 最热数据)
 *           ↓ Miss
 *       L2 Redis (分布式缓存, TTL=30min)
 *           ↓ Miss
 *       L3 MySQL 数据库
 * </pre>
 *
 * <p>缓存命名规范：{业务域}:{实体类型} 例如 "category:tree"、"resource:detail"
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    // ==================== L1：Caffeine 本地缓存 ====================

    /**
     * Caffeine 缓存管理器（L1 本地缓存）
     *
     * <p>适用于：读多写少、更新频率低的热点数据
     * <ul>
     *   <li>category:tree — 分类树（全站共享）</li>
     *   <li>config:public — 公开系统配置</li>
     *   <li>link-type:all — 网盘类型列表</li>
     * </ul>
     */
    @Bean("caffeineCacheManager")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // 默认配置
        manager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()          // 开启统计（用于 Prometheus 监控命中率）
        );

        // 各缓存区域独立配置
        Map<String, com.github.benmanes.caffeine.cache.Cache<Object, Object>> caches = new HashMap<>();

        // 分类树：低频更新，长缓存
        manager.registerCustomCache("category:tree",
            Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // 系统配置：极低频更新
        manager.registerCustomCache("config:public",
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());

        // 网盘类型：几乎不变
        manager.registerCustomCache("link-type:all",
            Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build());

        log.info("Caffeine L1 本地缓存管理器初始化完成");
        return manager;
    }

    // ==================== L2：Redis 分布式缓存 ====================

    /**
     * Redis 缓存管理器（L2 分布式缓存，默认缓存管理器）
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        // 默认缓存配置：TTL 30分钟
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(jackson2JsonRedisSerializer()))
            .disableCachingNullValues();    // null值不缓存

        // 各缓存区域独立 TTL 配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();

        // 资源详情：30分钟
        configMap.put("resource:detail", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // 资源列表：10分钟（变更较频繁）
        configMap.put("resource:list", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // 分类树：60分钟
        configMap.put("category:tree", defaultConfig.entryTtl(Duration.ofHours(1)));
        // 系统配置：2小时
        configMap.put("config:public", defaultConfig.entryTtl(Duration.ofHours(2)));
        // 网盘类型：24小时
        configMap.put("link-type:all", defaultConfig.entryTtl(Duration.ofHours(24)));
        // 友情链接：1小时
        configMap.put("friendlink:enabled", defaultConfig.entryTtl(Duration.ofHours(1)));
        // 广告：15分钟（时效性强）
        configMap.put("promotion:active", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        // 统计概览：5分钟（数据实时性要求较高）
        configMap.put("statistics:overview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        // 热门资源：10分钟
        configMap.put("resource:hot", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        RedisCacheManager manager = RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configMap)
            .transactionAware()
            .build();

        log.info("Redis L2 分布式缓存管理器初始化完成");
        return manager;
    }

    /**
     * Redis Template（通用，支持任意类型值）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer();

        // Key 使用字符串序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // Value 使用 JSON 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Jackson2 JSON Redis 序列化器
     * 支持 LocalDateTime 等 Java 8 时间类型
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 安全的类型验证器：白名单模式，防止反序列化漏洞（CVE-2019-12384 类攻击）
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)  // 允许基础 Object 类型
            .allowIfSubType("com.resource.platform.")  // 只允许项目自身的包
            .allowIfSubType("java.util.")               // Java 集合类
            .allowIfSubType("java.time.")               // Java 时间类
            .allowIfSubType("java.lang.")               // Java 基础类
            .allowIfSubType("java.math.")               // 数值类
            .allowIfSubType("java.sql.")                // SQL 类型
            .allowIfSubType("[Ljava.lang.")             // 数组类型
            .allowIfSubType("[Lcom.resource.platform.") // 项目数组类型
            .build();

        // 开启类型信息（反序列化时能还原具体类型）
        mapper.activateDefaultTyping(
            typeValidator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        // 支持 Java 8 时间类型
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        serializer.setObjectMapper(mapper);
        return serializer;
    }
}
