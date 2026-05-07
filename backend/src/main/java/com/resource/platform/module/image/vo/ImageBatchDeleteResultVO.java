package com.resource.platform.module.image.vo;

import lombok.Data;

@Data
public class ImageBatchDeleteResultVO {
    private Integer requestedCount;
    private Integer deletedCount;
    private Integer skippedUsedCount;
    private Integer storageFailedCount;
}
