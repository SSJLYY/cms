package com.resource.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdvertisementDTO {
    private Long id;
    private String name;
    private String title;
    private String position;
    private String type;
    private String imageUrl;
    private String linkUrl;
    private String content;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
