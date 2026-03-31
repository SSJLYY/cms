-- ============================================================
-- 数据库索引优化脚本
-- 版本：v2.0
-- 日期：2026-03-27
-- 说明：在生产数据库执行前，请先在测试环境验证
--       所有 ADD INDEX 操作为非阻塞（ALGORITHM=INPLACE, LOCK=NONE）
--       建议在业务低峰期执行
-- ============================================================

-- ==================== resource 表 ====================

-- 前台：按分类+状态分页查询（高频）
-- 对应查询：WHERE category_id = ? AND status = 1 ORDER BY sort_order DESC
ALTER TABLE `resource`
    ADD INDEX IF NOT EXISTS `idx_category_status_sort`
        (`category_id`, `status`, `sort_order` DESC, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 后台：按状态+创建时间分页（高频）
ALTER TABLE `resource`
    ADD INDEX IF NOT EXISTS `idx_status_created_at`
        (`status`, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 全文检索（替代 LIKE '%keyword%' 全表扫描）
-- 注意：需要 MySQL 5.7+ 且 ngram 插件（默认已启用）
ALTER TABLE `resource`
    ADD FULLTEXT INDEX IF NOT EXISTS `ft_resource_title_desc`
        (`title`, `description`) WITH PARSER ngram;

-- 按标签搜索（如有 tags 字段）
-- ALTER TABLE `resource`
--     ADD INDEX IF NOT EXISTS `idx_tags` (`tags`(100));

-- ==================== access_log 表 ====================
-- 数据量可能达到千万级，索引非常关键

-- 统计查询：按资源+时间范围统计下载量
ALTER TABLE `access_log`
    ADD INDEX IF NOT EXISTS `idx_resource_created`
        (`resource_id`, `created_at`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 统计查询：按时间范围统计总访问量
ALTER TABLE `access_log`
    ADD INDEX IF NOT EXISTS `idx_created_at`
        (`created_at`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 统计查询：按 IP 防刷检测
ALTER TABLE `access_log`
    ADD INDEX IF NOT EXISTS `idx_ip_created`
        (`ip_address`, `created_at`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== system_log 表 ====================

-- 后台：按操作人+时间查询日志
ALTER TABLE `system_log`
    ADD INDEX IF NOT EXISTS `idx_operator_created`
        (`operator_id`, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 后台：按模块+操作类型查询
ALTER TABLE `system_log`
    ADD INDEX IF NOT EXISTS `idx_module_action_created`
        (`module`, `action`, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== download_link 表 ====================

-- 获取资源的所有下载链接（极高频）
ALTER TABLE `download_link`
    ADD INDEX IF NOT EXISTS `idx_resource_sort`
        (`resource_id`, `sort_order`, `status`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== feedback 表 ====================

-- 后台：按状态+时间查询待处理反馈
ALTER TABLE `feedback`
    ADD INDEX IF NOT EXISTS `idx_status_created`
        (`status`, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== advertisement (promotion) 表 ====================

-- 前台：查询有效广告（状态+时间范围）
ALTER TABLE `advertisement`
    ADD INDEX IF NOT EXISTS `idx_status_time_range`
        (`status`, `start_time`, `end_time`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== category 表 ====================

-- 按父分类查询子分类（构建树形结构）
ALTER TABLE `category`
    ADD INDEX IF NOT EXISTS `idx_parent_sort`
        (`parent_id`, `sort_order`, `status`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== image 表 ====================

-- 按关联资源查询图片
ALTER TABLE `image`
    ADD INDEX IF NOT EXISTS `idx_resource_sort`
        (`resource_id`, `sort_order`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== revenue 表 ====================

-- 收益统计：按类型+时间范围
ALTER TABLE `revenue`
    ADD INDEX IF NOT EXISTS `idx_type_created`
        (`type`, `created_at`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- 收益统计：按状态+时间
ALTER TABLE `revenue`
    ADD INDEX IF NOT EXISTS `idx_status_created`
        (`status`, `created_at` DESC),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ==================== friend_link 表 ====================

-- 前台：查询已启用的友情链接
ALTER TABLE `friend_link`
    ADD INDEX IF NOT EXISTS `idx_status_sort`
        (`status`, `sort_order`),
    ALGORITHM=INPLACE, LOCK=NONE;

-- ============================================================
-- 执行完成后，用以下命令验证索引是否生效：
-- SHOW INDEX FROM resource;
-- EXPLAIN SELECT * FROM resource WHERE category_id = 1 AND status = 1;
-- ============================================================
