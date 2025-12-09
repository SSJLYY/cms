package com.resource.platform.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量操作DTO
 */
@Data
public class BatchOperationDTO {
    
    /**
     * 资源ID列表
     */
    @NotEmpty(message = "资源ID列表不能为空")
    private List<Long> ids;
    
    /**
     * 批量移动分类请求
     */
    @Data
    public static class BatchMoveCategoryRequest {
        
        /**
         * 资源ID列表
         */
        @NotEmpty(message = "资源ID列表不能为空")
        private List<Long> ids;
        
        /**
         * 目标分类ID
         */
        @NotNull(message = "目标分类ID不能为空")
        private Long categoryId;
    }
    
    /**
     * 批量更新状态请求
     */
    @Data
    public static class BatchUpdateStatusRequest {
        
        /**
         * 资源ID列表
         */
        @NotEmpty(message = "资源ID列表不能为空")
        private List<Long> ids;
        
        /**
         * 目标状态：0-已下架，1-已发布
         */
        @NotNull(message = "状态不能为空")
        private Integer status;
    }
}