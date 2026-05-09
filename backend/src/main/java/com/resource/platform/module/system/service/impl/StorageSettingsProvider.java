package com.resource.platform.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.module.system.entity.SystemConfig;
import com.resource.platform.module.system.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads storage settings from system_config with application.yml defaults as fallback.
 */
@Slf4j
@Component
public class StorageSettingsProvider {

    private static final long CACHE_TTL_MS = 60_000L;

    private static final List<String> STORAGE_KEYS = Arrays.asList(
        "storage.type",
        "storage.local.path",
        "storage.local.urlPrefix",
        "storage.local.url-prefix",
        "storage.oss.endpoint",
        "storage.oss.accessKey",
        "storage.oss.access-key-id",
        "storage.oss.secretKey",
        "storage.oss.access-key-secret",
        "storage.oss.bucket",
        "storage.oss.bucket-name",
        "storage.oss.urlPrefix",
        "storage.oss.url-prefix"
    );

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Value("${storage.type:local}")
    private String defaultStorageType;

    @Value("${storage.local.path:/data/uploads}")
    private String defaultLocalPath;

    @Value("${storage.local.url-prefix:/uploads}")
    private String defaultLocalUrlPrefix;

    @Value("${storage.oss.endpoint:}")
    private String defaultOssEndpoint;

    @Value("${storage.oss.access-key-id:}")
    private String defaultOssAccessKeyId;

    @Value("${storage.oss.access-key-secret:}")
    private String defaultOssAccessKeySecret;

    @Value("${storage.oss.bucket-name:}")
    private String defaultOssBucketName;

    @Value("${storage.oss.url-prefix:}")
    private String defaultOssUrlPrefix;

    private volatile StorageSettings cachedSettings;
    private volatile long cacheExpiresAt;

    public StorageSettings getSettings() {
        long now = System.currentTimeMillis();
        StorageSettings snapshot = cachedSettings;
        if (snapshot != null && now < cacheExpiresAt) {
            return snapshot;
        }

        synchronized (this) {
            if (cachedSettings != null && now < cacheExpiresAt) {
                return cachedSettings;
            }
            StorageSettings loaded = loadSettings();
            cachedSettings = loaded;
            cacheExpiresAt = now + CACHE_TTL_MS;
            return loaded;
        }
    }

    public void evictCache() {
        synchronized (this) {
            cachedSettings = null;
            cacheExpiresAt = 0L;
        }
    }

    public String getStorageType() {
        return getSettings().getStorageType();
    }

    public String getLocalPath() {
        return getSettings().getLocalPath();
    }

    public String getLocalUrlPrefix() {
        return getSettings().getLocalUrlPrefix();
    }

    public String getResolvedOssUrlPrefix() {
        return getResolvedOssUrlPrefix(getSettings());
    }

    public String getResolvedOssUrlPrefix(StorageSettings settings) {
        String explicitPrefix = normalizePrefix(settings.getOssUrlPrefix());
        if (!explicitPrefix.isEmpty()) {
            return explicitPrefix;
        }

        String endpoint = trim(settings.getOssEndpoint());
        String bucketName = trim(settings.getOssBucketName());
        if (endpoint.isEmpty() || bucketName.isEmpty()) {
            return "";
        }

        String normalizedEndpoint = endpoint
            .replaceFirst("^https?://", "")
            .replaceAll("/+$", "");

        if (normalizedEndpoint.isEmpty()) {
            return "";
        }

        if (normalizedEndpoint.startsWith(bucketName + ".")) {
            return "https://" + normalizedEndpoint;
        }

        return "https://" + bucketName + "." + normalizedEndpoint;
    }

    private StorageSettings loadSettings() {
        try {
            Map<String, String> configMap = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                    .in(SystemConfig::getConfigKey, STORAGE_KEYS)
            ).stream().collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                SystemConfig::getConfigValue,
                (first, second) -> first
            ));

            StorageSettings settings = new StorageSettings();
            settings.setStorageType(normalizeStorageType(
                firstNonBlank(configMap, Arrays.asList("storage.type"), defaultStorageType)
            ));
            settings.setLocalPath(firstNonBlank(
                configMap,
                Arrays.asList("storage.local.path"),
                defaultLocalPath
            ));
            settings.setLocalUrlPrefix(normalizePrefix(firstNonBlank(
                configMap,
                Arrays.asList("storage.local.urlPrefix", "storage.local.url-prefix"),
                defaultLocalUrlPrefix
            )));
            settings.setOssEndpoint(firstNonBlank(
                configMap,
                Arrays.asList("storage.oss.endpoint"),
                defaultOssEndpoint
            ));
            settings.setOssAccessKeyId(firstNonBlank(
                configMap,
                Arrays.asList("storage.oss.accessKey", "storage.oss.access-key-id"),
                defaultOssAccessKeyId
            ));
            settings.setOssAccessKeySecret(firstNonBlank(
                configMap,
                Arrays.asList("storage.oss.secretKey", "storage.oss.access-key-secret"),
                defaultOssAccessKeySecret
            ));
            settings.setOssBucketName(firstNonBlank(
                configMap,
                Arrays.asList("storage.oss.bucket", "storage.oss.bucket-name"),
                defaultOssBucketName
            ));
            settings.setOssUrlPrefix(normalizePrefix(firstNonBlank(
                configMap,
                Arrays.asList("storage.oss.urlPrefix", "storage.oss.url-prefix"),
                defaultOssUrlPrefix
            )));
            return settings;
        } catch (Exception e) {
            log.warn("Failed to load storage settings from database, falling back to defaults: {}", e.getMessage());
            return buildDefaultSettings();
        }
    }

    private StorageSettings buildDefaultSettings() {
        StorageSettings settings = new StorageSettings();
        settings.setStorageType(normalizeStorageType(defaultStorageType));
        settings.setLocalPath(trim(defaultLocalPath));
        settings.setLocalUrlPrefix(normalizePrefix(defaultLocalUrlPrefix));
        settings.setOssEndpoint(trim(defaultOssEndpoint));
        settings.setOssAccessKeyId(trim(defaultOssAccessKeyId));
        settings.setOssAccessKeySecret(trim(defaultOssAccessKeySecret));
        settings.setOssBucketName(trim(defaultOssBucketName));
        settings.setOssUrlPrefix(normalizePrefix(defaultOssUrlPrefix));
        return settings;
    }

    private String firstNonBlank(Map<String, String> configMap, List<String> keys, String fallback) {
        for (String key : keys) {
            String value = trim(configMap.get(key));
            if (!value.isEmpty()) {
                return value;
            }
        }
        return trim(fallback);
    }

    private String normalizeStorageType(String storageType) {
        String normalized = trim(storageType).toLowerCase();
        return normalized.isEmpty() ? "local" : normalized;
    }

    private String normalizePrefix(String prefix) {
        String normalized = trim(prefix);
        if (normalized.isEmpty()) {
            return "";
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static class StorageSettings {
        private String storageType;
        private String localPath;
        private String localUrlPrefix;
        private String ossEndpoint;
        private String ossAccessKeyId;
        private String ossAccessKeySecret;
        private String ossBucketName;
        private String ossUrlPrefix;

        public String getStorageType() {
            return storageType;
        }

        public void setStorageType(String storageType) {
            this.storageType = storageType;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }

        public String getLocalUrlPrefix() {
            return localUrlPrefix;
        }

        public void setLocalUrlPrefix(String localUrlPrefix) {
            this.localUrlPrefix = localUrlPrefix;
        }

        public String getOssEndpoint() {
            return ossEndpoint;
        }

        public void setOssEndpoint(String ossEndpoint) {
            this.ossEndpoint = ossEndpoint;
        }

        public String getOssAccessKeyId() {
            return ossAccessKeyId;
        }

        public void setOssAccessKeyId(String ossAccessKeyId) {
            this.ossAccessKeyId = ossAccessKeyId;
        }

        public String getOssAccessKeySecret() {
            return ossAccessKeySecret;
        }

        public void setOssAccessKeySecret(String ossAccessKeySecret) {
            this.ossAccessKeySecret = ossAccessKeySecret;
        }

        public String getOssBucketName() {
            return ossBucketName;
        }

        public void setOssBucketName(String ossBucketName) {
            this.ossBucketName = ossBucketName;
        }

        public String getOssUrlPrefix() {
            return ossUrlPrefix;
        }

        public void setOssUrlPrefix(String ossUrlPrefix) {
            this.ossUrlPrefix = ossUrlPrefix;
        }
    }
}
