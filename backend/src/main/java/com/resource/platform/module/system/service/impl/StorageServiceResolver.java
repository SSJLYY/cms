package com.resource.platform.module.system.service.impl;

import com.resource.platform.exception.BusinessException;
import com.resource.platform.module.system.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Resolves the correct storage implementation from runtime settings or file URLs.
 */
@Component
public class StorageServiceResolver {

    @Autowired
    @Qualifier("localStorageService")
    private StorageService localStorageService;

    @Autowired
    @Qualifier("ossStorageService")
    private StorageService ossStorageService;

    @Autowired
    private StorageSettingsProvider storageSettingsProvider;

    public StorageService getCurrentStorageService() {
        return getStorageServiceByType(storageSettingsProvider.getStorageType());
    }

    public String getCurrentStorageType() {
        return storageSettingsProvider.getStorageType();
    }

    public LocalStorageServiceImpl getLocalStorageService() {
        return (LocalStorageServiceImpl) localStorageService;
    }

    public StorageService getStorageServiceByType(String storageType) {
        String normalizedType = normalizeStorageType(storageType);
        switch (normalizedType) {
            case "oss":
                return ossStorageService;
            case "local":
                return localStorageService;
            case "cos":
            case "qiniu":
                throw new BusinessException("未支持的存储类型: " + normalizedType);
            default:
                throw new BusinessException("未知的存储类型: " + normalizedType);
        }
    }

    public StorageService resolveStorageServiceByUrl(String fileUrl) {
        return getStorageServiceByType(resolveStorageTypeByUrl(fileUrl));
    }

    public String resolveStorageTypeByUrl(String fileUrl) {
        if (matchesUrlPrefix(fileUrl, storageSettingsProvider.getLocalUrlPrefix())) {
            return "local";
        }
        if (matchesUrlPrefix(fileUrl, storageSettingsProvider.getResolvedOssUrlPrefix())) {
            return "oss";
        }
        return storageSettingsProvider.getStorageType();
    }

    private boolean matchesUrlPrefix(String fileUrl, String prefix) {
        String normalizedUrl = trim(fileUrl);
        String normalizedPrefix = trim(prefix);
        if (normalizedUrl.isEmpty() || normalizedPrefix.isEmpty()) {
            return false;
        }
        String cleanUrl = stripTrailingSlash(normalizedUrl);
        String cleanPrefix = stripTrailingSlash(normalizedPrefix);
        if (cleanUrl.startsWith(cleanPrefix)) {
            return true;
        }

        if (cleanPrefix.startsWith("/")) {
            try {
                URI uri = URI.create(normalizedUrl);
                String path = stripTrailingSlash(trim(uri.getPath()));
                return !path.isEmpty() && path.startsWith(cleanPrefix);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }
        return false;
    }

    private String normalizeStorageType(String storageType) {
        String normalized = trim(storageType).toLowerCase();
        return normalized.isEmpty() ? "local" : normalized;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
