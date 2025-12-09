package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DownloadLinkDTO {
    private Long resourceId;
    
    private String title;
    
    @NotBlank(message = "链接类型不能为空")
    private String linkType;
    
    @NotBlank(message = "链接URL不能为空")
    private String linkUrl;
    
    private String downloadUrl;
    
    private String password;
    
    private String linkName;
}
