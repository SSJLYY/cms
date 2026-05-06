package com.resource.platform.module.system.vo;

import lombok.Data;
import java.util.Map;

@Data
public class ConfigVO {
    private Map<String, String> configs;
    
    public ConfigVO(Map<String, String> configs) {
        this.configs = configs;
    }
}
