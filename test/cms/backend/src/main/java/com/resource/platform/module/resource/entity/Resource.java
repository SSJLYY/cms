package com.resource.platform.module.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源实体
 *
 * <p>对应数据库 resource 表，表示平台上的一个下载资源。
 */
@Data
@TableName("resource")
public class Resource {

    /** 资源ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资源标题 */
    private String title;

    /** 资源描述 */
    private String description;

    /** 封面图片ID */
    private Long coverImageId;

    /** 标签（逗号分隔） */
    private String tags;

    /** 所属分类ID */
    private Long categoryId;

    /** 资源状态：0-已下架，1-已发布 */
    private Integer status;

    /** 下载次数 */
    private Integer downloadCount;

    /** 浏览次数 */
    private Integer viewCount;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    /** 是否置顶：0-否，1-是 */
    private Integer isPinned;

    /** 审核状态 */
    private String auditStatus;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditorId;

    /** 爬虫任务ID（爬取来源标识） */
    private Long crawlerTaskId;

    /** 原始来源URL */
    private String sourceUrl;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
