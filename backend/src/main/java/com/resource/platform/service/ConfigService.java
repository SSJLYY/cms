package com.resource.platform.service;

import com.resource.platform.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface ConfigService {
    
    /**
     * 获取所有配置（按类别分组）
     */
    Map<String, List<SystemConfig>> getAllConfigs();
    
    /**
     * 获取配置分类列表
     */
    List<String> getConfigCategories();
    
    /**
     * 根据类别获取配置
     */
    List<SystemConfig> getConfigsByCategory(String category);
    
    /**
     * 根据键获取配置
     */
    SystemConfig getConfigByKey(String configKey);
    
    /**
     * 批量获取配置
     */
    Map<String, String> getConfigsByKeys(List<String> keys);
    
    /**
     * 更新配置
     */
    void updateConfig(String configKey, String configValue);
    
    /**
     * 批量更新配置
     */
    void updateConfigs(Map<String, String> configs);
    
    /**
     * 重置配置为默认值
     */
    void resetConfig(String configKey);
    
    /**
     * 测试邮件配置
     */
    boolean testEmailConfig(Map<String, String> emailConfig);
    
    /**
     * 测试存储配置
     */
    boolean testStorageConfig(Map<String, String> storageConfig);
}
