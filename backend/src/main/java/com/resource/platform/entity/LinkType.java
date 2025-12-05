package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网盘类型配置实体类
 */
@Data
@TableName("link_type")
public class LinkType {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 类型代码（英文标识）
     */
    private String typeCode;
    
    /**
     * 类型名称（中文显示）
     */
    private String typeName;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 颜色代码
     */
    private String color;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
