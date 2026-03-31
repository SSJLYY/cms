package com.resource.platform.module.system.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ConfigDTO {
    private Map<String, String> configs;
}
