package com.resource.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("revenue")
public class Revenue {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 收益来源
     */
    private String source;
    
    /**
     * 收益金额
     */
    private BigDecimal amount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 收益类型
     */
    private String revenueType;
    
    /**
     * 状态：pending-待结算, settled-已结算, cancelled-已取消
     */
    private String status;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
