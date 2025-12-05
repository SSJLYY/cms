package com.resource.platform.vo;

import lombok.Data;

import java.util.List;

/**
 * 趋势数据VO
 */
@Data
public class TrendDataVO {
    
    /**
     * 日期列表
     */
    private List<String> dates;
    
    /**
     * 资源数据
     */
    private List<Long> resourceData;
    
    /**
     * 下载数据
     */
    private List<Long> downloadData;
    
    /**
     * 用户数据
     */
    private List<Long> userData;
}
