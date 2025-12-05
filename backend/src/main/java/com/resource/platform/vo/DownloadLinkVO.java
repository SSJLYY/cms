package com.resource.platform.vo;

import lombok.Data;

@Data
public class DownloadLinkVO {
    private Long id;
    private String linkName;
    private String linkType;
    private String linkUrl;
    private String password;
    private Integer isValid;
}
