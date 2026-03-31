package com.resource.platform.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * 统一分页查询参数
 *
 * <p>替代各 Controller 中散乱的 pageNum/pageSize/current/size 参数，
 * 统一用法，减少重复代码。
 *
 * <p>使用示例：
 * <pre>{@code
 * @GetMapping("/list")
 * public Result<PageResult<ResourceVO>> list(@Valid PageQuery query) {
 *     IPage<Resource> page = resourceService.page(query.toMpPage());
 *     return Result.success(PageResult.of(page, ...));
 * }
 * }</pre>
 */
@Data
public class PageQuery {

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /** 每页条数，最大 100 */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Integer size = 20;

    /** 排序字段（可选） */
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "排序字段不合法")
    private String sortField;

    /** 排序字段安全校验正则：仅允许字母、数字、下划线、点号（支持 table.column 格式） */
    private static final java.util.regex.Pattern SORT_FIELD_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_.]+$");

    /** 排序方向：asc / desc，默认 desc */
    @Pattern(regexp = "^(asc|desc)$", message = "排序方向只能为 asc 或 desc")
    private String sortOrder = "desc";

    /** 转换为 MyBatis Plus Page 对象 */
    public <T> Page<T> toMpPage() {
        return new Page<>(page, size);
    }

    /** 转换为 MyBatis Plus Page 并设置排序（需调用方传入实际列名，防 SQL 注入）*/
    public <T> Page<T> toMpPage(String defaultSortField) {
        Page<T> mpPage = new Page<>(page, size);
        String field = (sortField != null && !sortField.isBlank()) ? sortField : defaultSortField;
        if (field != null && !field.isBlank()) {
            // 安全校验：排序字段只允许字母、数字、下划线和点号，防止SQL注入
            if (!SORT_FIELD_PATTERN.matcher(field).matches()) {
                field = defaultSortField;
                if (field == null || !field.isBlank()) {
                    // defaultSortField 也不合法则跳过排序
                    return mpPage;
                }
            }
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            if (isAsc) {
                mpPage.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.asc(field));
            } else {
                mpPage.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.desc(field));
            }
        }
        return mpPage;
    }
}
