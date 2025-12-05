package com.resource.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resource.platform.entity.SystemConfig;
import com.resource.platform.exception.ResourceNotFoundException;
import com.resource.platform.mapper.SystemConfigMapper;
import com.resource.platform.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public Map<String, List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> allConfigs = systemConfigMapper.selectList(null);
        return allConfigs.stream()
            .collect(Collectors.groupingBy(SystemConfig::getCategory));
    }

    @Override
    public List<String> getConfigCategories() {
        List<SystemConfig> allConfigs = systemConfigMapper.selectList(null);
        return allConfigs.stream()
            .map(SystemConfig::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public List<SystemConfig> getConfigsByCategory(String category) {
        return systemConfigMapper.selectList(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getCategory, category)
                .orderByAsc(SystemConfig::getSortOrder)
        );
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey)
        );
        if (config == null) {
            throw new ResourceNotFoundException("配置项不存在: " + configKey);
        }
        return config;
    }

    @Override
    public Map<String, String> getConfigsByKeys(List<String> keys) {
        List<SystemConfig> configs = systemConfigMapper.selectList(
            new LambdaQueryWrapper<SystemConfig>()
                .in(SystemConfig::getConfigKey, keys)
        );
        return configs.stream()
            .collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                SystemConfig::getConfigValue
            ));
    }

    @Override
    @Transactional
    public void updateConfig(String configKey, String configValue) {
        SystemConfig config = getConfigByKey(configKey);
        config.setConfigValue(configValue);
        systemConfigMapper.updateById(config);
        log.info("更新配置: {} = {}", configKey, configValue);
    }

    @Override
    @Transactional
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            try {
                updateConfig(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("更新配置失败: {}", entry.getKey(), e);
            }
        }
    }

    @Override
    @Transactional
    public void resetConfig(String configKey) {
        SystemConfig config = getConfigByKey(configKey);
        if (config.getDefaultValue() != null) {
            config.setConfigValue(config.getDefaultValue());
            systemConfigMapper.updateById(config);
            log.info("重置配置: {} = {}", configKey, config.getDefaultValue());
        }
    }

    @Override
    public boolean testEmailConfig(Map<String, String> emailConfig) {
        try {
            // TODO: 实现邮件配置测试
            log.info("测试邮件配置: {}", emailConfig);
            return true;
        } catch (Exception e) {
            log.error("邮件配置测试失败", e);
            return false;
        }
    }

    @Override
    public boolean testStorageConfig(Map<String, String> storageConfig) {
        try {
            // TODO: 实现存储配置测试
            log.info("测试存储配置: {}", storageConfig);
            return true;
        } catch (Exception e) {
            log.error("存储配置测试失败", e);
            return false;
        }
    }
}
