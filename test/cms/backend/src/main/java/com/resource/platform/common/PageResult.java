package com.resource.platform.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页响应体
 *
 * <p>使用示例：
 * <pre>{@code
 * // 方式1：直接从 IPage 转换（VO 类型与 Entity 相同时）
 * PageResult<ResourceVO> result = PageResult.of(mpPage, converter);
 *
 * // 方式2：手动构建
 * PageResult<ResourceVO> result = new PageResult<>(total, voList);
 * }</pre>
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页数据列表 */
    private List<T> records;

    /** 总页数 */
    private Long pages;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    /**
     * 从 MyBatis Plus IPage 直接转换（含 VO 转换）
     *
     * @param page      MybatisPlus 分页结果
     * @param converter Entity → VO 转换函数
     * @param <E>       Entity 类型
     * @param <V>       VO 类型
     */
    public static <E, V> PageResult<V> of(IPage<E> page, Function<E, V> converter) {
        PageResult<V> result = new PageResult<>();
        result.total = page.getTotal();
        result.pages = page.getPages();
        result.current = page.getCurrent();
        result.size = page.getSize();
        result.records = page.getRecords().stream()
            .map(converter)
            .collect(Collectors.toList());
        return result;
    }

    /**
     * 从 MyBatis Plus IPage 直接转换（不做类型转换，VO 与 Entity 同类型时）
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return of(page, item -> item);
    }
}
