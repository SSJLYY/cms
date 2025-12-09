package com.resource.platform.vo;

import lombok.Data;

@Data
public class DownloadLinkVO {
    private Long id;
    private Long resourceId;
    private String title;
    private String linkName;
    private String linkType;
    private String linkUrl;
    private String downloadUrl;
    private String password;
    private Integer isValid;
}
