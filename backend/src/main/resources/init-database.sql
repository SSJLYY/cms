-- ============================================
-- 资源下载平台 - 完整数据库初始化脚本
-- 版本: 2.0
-- 描述: 包含所有表结构、初始数据和测试数据
-- 最后更新: 2024-12-03
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS resource_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE resource_platform;

-- ============================================
-- 第一部分：核心业务表
-- ============================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `level` TINYINT NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级，2-二级',
  `icon` VARCHAR(100) COMMENT '分类图标',
  `description` VARCHAR(500) COMMENT '分类描述',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_parent` (`name`, `parent_id`, `deleted`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 3. 资源表
CREATE TABLE IF NOT EXISTS `resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '资源标题',
  `description` TEXT COMMENT '资源描述',
  `cover_image_id` BIGINT COMMENT '封面图片ID',
  `tags` VARCHAR(500) COMMENT '标签，逗号分隔',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已下架，1-已发布',
  `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `audit_status` VARCHAR(20) DEFAULT 'approved' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_cover_image_id` (`cover_image_id`),
  KEY `idx_is_pinned` (`is_pinned`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 4. 下载链接表
CREATE TABLE IF NOT EXISTS `download_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `link_name` VARCHAR(100) NOT NULL COMMENT '链接名称',
  `link_url` VARCHAR(500) NOT NULL COMMENT '下载链接',
  `link_type` VARCHAR(20) NOT NULL COMMENT '链接类型：baidu、aliyun、lanzou、direct、quark、xunlei、mobile、uc',
  `password` VARCHAR(50) COMMENT '提取码',
  `is_valid` TINYINT NOT NULL DEFAULT 1 COMMENT '是否有效：0-失效，1-有效',
  `check_time` DATETIME COMMENT '最后检测时间',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_link_type` (`link_type`),
  KEY `idx_is_valid` (`is_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载链接表';

-- ============================================
-- 第二部分：管理功能表
-- ============================================

-- 5. 图片表
CREATE TABLE IF NOT EXISTS `image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(200) NOT NULL COMMENT '文件名',
  `original_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型：jpg、png、gif、webp',
  `width` INT COMMENT '图片宽度',
  `height` INT COMMENT '图片高度',
  `thumbnail_url` VARCHAR(500) COMMENT '缩略图URL',
  `storage_type` VARCHAR(20) NOT NULL DEFAULT 'local' COMMENT '存储类型：local、oss、cos、qiniu',
  `is_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被使用：0-未使用，1-已使用',
  `uploader_id` BIGINT COMMENT '上传者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_is_used` (`is_used`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片表';

-- 6. 图片尺寸表
CREATE TABLE IF NOT EXISTS `image_size` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `image_id` BIGINT NOT NULL COMMENT '图片ID',
  `size_type` VARCHAR(20) NOT NULL COMMENT '尺寸类型：thumbnail、small、medium、large',
  `width` INT NOT NULL COMMENT '宽度',
  `height` INT NOT NULL COMMENT '高度',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_image_id` (`image_id`),
  KEY `idx_size_type` (`size_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片尺寸表';

-- 6.1. 资源图片关联表
CREATE TABLE IF NOT EXISTS `resource_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `image_id` BIGINT NOT NULL COMMENT '图片ID',
  `is_cover` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为封面：0-否，1-是',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_image` (`resource_id`, `image_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_image_id` (`image_id`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_cover` (`is_cover`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源图片关联表';

-- 7. 用户反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：SUGGESTION-建议，ISSUE-问题，COMPLAINT-投诉，OTHER-其他',
  `title` VARCHAR(200) COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `contact_name` VARCHAR(100) COMMENT '联系人姓名',
  `contact_email` VARCHAR(100) COMMENT '联系邮箱',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，CLOSED-已关闭',
  `reply` TEXT COMMENT '回复内容',
  `reply_time` DATETIME COMMENT '回复时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- 8. 系统日志表
CREATE TABLE IF NOT EXISTS `system_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` VARCHAR(50) COMMENT '模块名称',
  `type` VARCHAR(50) COMMENT '操作类型',
  `description` VARCHAR(500) COMMENT '操作描述',
  `method` VARCHAR(200) COMMENT '方法名',
  `request_url` VARCHAR(500) COMMENT '请求URL',
  `request_method` VARCHAR(10) COMMENT '请求方法：GET、POST、PUT、DELETE',
  `request_params` TEXT COMMENT '请求参数',
  `response_data` TEXT COMMENT '响应数据',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `status` VARCHAR(20) COMMENT '状态：SUCCESS、ERROR',
  `error_message` TEXT COMMENT '错误信息',
  `duration` BIGINT COMMENT '执行时长（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 9. 访问日志表
CREATE TABLE IF NOT EXISTS `access_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `resource_id` BIGINT COMMENT '资源ID',
  `action_type` VARCHAR(20) NOT NULL COMMENT '操作类型：visit-访问，download-下载，search-搜索',
  `referer` VARCHAR(500) COMMENT '来源地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `browser` VARCHAR(50) COMMENT '浏览器',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- 10. 审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(100) COMMENT '操作人姓名',
  `operation_type` VARCHAR(50) COMMENT '操作类型',
  `operation_module` VARCHAR(50) COMMENT '操作模块',
  `operation_object` VARCHAR(200) COMMENT '操作对象',
  `operation_detail` TEXT COMMENT '操作详情',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `user_id` BIGINT COMMENT '用户ID',
  `module` VARCHAR(50) COMMENT '模块',
  `action` VARCHAR(50) COMMENT '动作',
  `description` VARCHAR(500) COMMENT '描述',
  `status` VARCHAR(20) COMMENT '状态',
  `error_message` TEXT COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_action` (`action`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 11. 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `category` VARCHAR(50) NOT NULL COMMENT '配置分类：basic、seo、storage、email、security',
  `description` VARCHAR(500) COMMENT '配置描述',
  `default_value` TEXT COMMENT '默认值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- 第三部分：扩展功能表
-- ============================================

-- 12. SEO提交记录表
CREATE TABLE IF NOT EXISTS `seo_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `engine` VARCHAR(20) NOT NULL COMMENT '搜索引擎：baidu-百度，bing-必应，google-谷歌',
  `url` VARCHAR(500) NOT NULL COMMENT '提交的URL',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：success-成功，failed-失败，pending-待处理',
  `response_message` TEXT COMMENT '响应消息',
  `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  KEY `idx_engine` (`engine`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SEO提交记录表';

-- 13. 广告表
CREATE TABLE IF NOT EXISTS `advertisement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '广告名称',
  `position` VARCHAR(50) NOT NULL COMMENT '广告位置：homepage-首页，download-下载页，category-分类页，custom-自定义',
  `type` VARCHAR(20) NOT NULL COMMENT '广告类型：image-图片，text-文字，video-视频',
  `image_url` VARCHAR(500) COMMENT '广告图片URL',
  `link_url` VARCHAR(500) COMMENT '跳转链接',
  `content` TEXT COMMENT '广告内容（文字广告使用）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `click_count` INT NOT NULL DEFAULT 0 COMMENT '点击次数',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告表';

-- 14. 友情链接表
CREATE TABLE IF NOT EXISTS `friend_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '网站名称',
  `url` VARCHAR(500) NOT NULL COMMENT '网站URL',
  `logo` VARCHAR(500) COMMENT '网站Logo URL',
  `description` VARCHAR(500) COMMENT '网站描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友情链接表';

-- 15. 收益记录表
CREATE TABLE IF NOT EXISTS `revenue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source` VARCHAR(100) NOT NULL COMMENT '收益来源',
  `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '收益金额',
  `download_count` INT NOT NULL DEFAULT 0 COMMENT '下载次数',
  `revenue_type` VARCHAR(50) NOT NULL COMMENT '收益类型',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待结算, settled-已结算, cancelled-已取消',
  `description` TEXT COMMENT '描述信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_revenue_type` (`revenue_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收益记录表';

-- 16. 网盘类型配置表
CREATE TABLE IF NOT EXISTS `link_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_code` VARCHAR(50) NOT NULL COMMENT '类型代码（英文标识）',
  `type_name` VARCHAR(100) NOT NULL COMMENT '类型名称（中文显示）',
  `icon` VARCHAR(100) COMMENT '图标',
  `color` VARCHAR(20) COMMENT '颜色代码',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `description` VARCHAR(500) COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`, `deleted`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网盘类型配置表';

-- ============================================
-- 第四部分：初始数据
-- ============================================

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$12$w5iVBBt6zEkjppG0Xsnay.GTTaY/WBl96Rg6bFnL.WTXWPaiK6J/m', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username=username;

-- 插入默认网盘类型配置
INSERT INTO `link_type` (`type_code`, `type_name`, `icon`, `color`, `sort_order`, `status`, `description`) VALUES
('quark', '夸克网盘', 'cloud', '#f5576c', 1, 1, '夸克网盘下载链接'),
('xunlei', '迅雷下载', 'download', '#ff8c00', 2, 1, '迅雷下载链接'),
('baidu', '百度网盘', 'cloud-download', '#00f2fe', 3, 1, '百度网盘下载链接'),
('aliyun', '阿里云盘', 'cloud-upload', '#38f9d7', 4, 1, '阿里云盘下载链接'),
('lanzou', '蓝奏云', 'folder', '#764ba2', 5, 1, '蓝奏云下载链接'),
('guanfang', '官方', 'link', '#ef2715ff', 5, 1, '官方链接'),
('direct', '直链下载', 'link', '#667eea', 6, 1, '直接下载链接')
ON DUPLICATE KEY UPDATE 
  type_name = VALUES(type_name),
  icon = VALUES(icon),
  color = VALUES(color),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  description = VALUES(description);

-- 插入初始分类数据
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `icon`, `description`, `sort_order`, `status`) VALUES
(1, '电脑软件', 0, 1, 'Monitor', '各类电脑软件资源', 1, 1),
(2, '设计软件', 1, 2, 'Edit', '设计类软件工具', 1, 1),
(3, '游戏娱乐', 0, 1, 'VideoPlay', '游戏和娱乐资源', 2, 1),
(4, 'TV软件', 1, 2, 'Monitor', '电视相关软件', 4, 1),
(5, '实用软件', 1, 2, 'Files', '实用工具软件', 5, 1),
(6, '学习课程', 0, 1, 'Reading', '学习教程和课程', 6, 1),
(7, '素材工具', 0, 1, 'PictureFilled', '设计素材和工具', 14, 1),
(8, '系统工具', 1, 2, 'Setting', '系统优化和工具', 17, 1),
(9, '网站综合', 0, 1, 'Connection', '网站相关资源', 18, 1)
ON DUPLICATE KEY UPDATE 
  name = VALUES(name),
  parent_id = VALUES(parent_id),
  level = VALUES(level),
  icon = VALUES(icon),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status);

-- 插入示例资源数据
INSERT INTO `resource` (`id`, `title`, `description`, `category_id`, `status`, `download_count`, `view_count`, `sort_order`, `is_pinned`, `audit_status`) VALUES
(1, 'WinRar解压缩工具', 'WinRar是一款功能强大的压缩文件管理器，支持RAR、ZIP等多种压缩格式，提供高压缩比和快速解压速度', 5, 1, 0, 3, 0, 0, 'approved'),
(2, 'Photoshop 2024', 'Adobe Photoshop 2024最新版本，专业图像处理软件，提供强大的图像编辑和设计功能', 2, 1, 0, 2, 0, 0, 'approved'),
(3, '网易云音乐', '网易云音乐PC客户端，海量音乐在线收听，支持歌词显示和音乐下载', 3, 1, 0, 2, 0, 0, 'approved'),
(4, 'Visual Studio Code', '微软开发的免费开源代码编辑器，支持多种编程语言和丰富的插件生态', 8, 1, 10, 147, 0, 0, 'approved'),
(5, 'Chrome浏览器', 'Google Chrome浏览器最新版，快速、安全、稳定的网页浏览体验', 8, 1, 160, 3, 0, 0, 'approved')
ON DUPLICATE KEY UPDATE 
  title = VALUES(title),
  description = VALUES(description),
  category_id = VALUES(category_id),
  status = VALUES(status);

-- 插入示例下载链接数据
INSERT INTO `download_link` (`resource_id`, `link_name`, `link_type`, `link_url`, `password`, `is_valid`, `sort_order`) VALUES
(1, '官方下载', 'direct', 'https://www.winrar.com/download.html', NULL, 1, 0),
(1, '百度网盘', 'baidu', 'https://pan.baidu.com/s/1xxxxx', 'abc123', 1, 1),
(2, '夸克网盘', 'quark', 'https://pan.quark.cn/s/xxxxx', 'ps2024', 1, 0),
(2, '阿里云盘', 'aliyun', 'https://www.aliyundrive.com/s/xxxxx', 'ali123', 1, 1),
(3, '官方下载', 'direct', 'https://music.163.com/download', NULL, 1, 0),
(4, '官方下载', 'direct', 'https://code.visualstudio.com/download', NULL, 1, 0),
(4, '百度网盘', 'baidu', 'https://pan.baidu.com/s/2xxxxx', 'vscode', 1, 1),
(5, '官方下载', 'direct', 'https://www.google.com/chrome/', NULL, 1, 0)
ON DUPLICATE KEY UPDATE 
  link_name = VALUES(link_name),
  link_type = VALUES(link_type),
  link_url = VALUES(link_url),
  password = VALUES(password),
  is_valid = VALUES(is_valid),
  sort_order = VALUES(sort_order);

-- 插入系统配置初始数据
INSERT INTO `system_config` (`config_key`, `config_value`, `category`, `description`, `default_value`, `sort_order`) VALUES
-- 基本设置
('site.name', '资源下载平台', 'basic', '网站名称', '资源下载平台', 1),
('site.title', '资源下载平台 - 管理后台', 'basic', '网站标题', '资源下载平台 - 管理后台', 2),
('site.description', '提供优质资源下载服务', 'basic', '网站描述', '提供优质资源下载服务', 3),
('site.keywords', '资源,下载,分享', 'basic', '网站关键词', '资源,下载,分享', 4),
('site.icp', '', 'basic', 'ICP备案号', '', 5),

-- SEO设置
('seo.title', '资源下载平台', 'seo', 'SEO标题', '资源下载平台', 1),
('seo.description', '提供优质资源下载服务', 'seo', 'SEO描述', '提供优质资源下载服务', 2),
('seo.keywords', '资源,下载,分享', 'seo', 'SEO关键词', '资源,下载,分享', 3),
('seo.baidu.token', '', 'seo', '百度站长Token', '', 4),
('seo.bing.key', '', 'seo', '必应站长Key', '', 5),

-- 存储设置
('storage.type', 'local', 'storage', '存储类型', 'local', 1),
('storage.local.path', '/data/uploads', 'storage', '本地存储路径', '/data/uploads', 2),
('storage.oss.endpoint', '', 'storage', 'OSS Endpoint', '', 3),
('storage.oss.accessKey', '', 'storage', 'OSS AccessKey', '', 4),
('storage.oss.secretKey', '', 'storage', 'OSS SecretKey', '', 5),
('storage.oss.bucket', '', 'storage', 'OSS Bucket', '', 6),

-- 邮件设置
('email.smtp.host', '', 'email', 'SMTP服务器', '', 1),
('email.smtp.port', '587', 'email', 'SMTP端口', '587', 2),
('email.from', '', 'email', '发件人邮箱', '', 3),
('email.from.name', '资源下载平台', 'email', '发件人名称', '资源下载平台', 4),
('email.username', '', 'email', '邮箱用户名', '', 5),
('email.password', '', 'email', '邮箱密码', '', 6),
('email.ssl.enable', 'true', 'email', '启用SSL', 'true', 7),

-- 安全设置
('jwt.secret', 'resource-platform-secret-key', 'security', 'JWT密钥', 'resource-platform-secret-key', 1),
('jwt.expiration', '24', 'security', 'JWT过期时间(小时)', '24', 2),
('password.min.length', '6', 'security', '密码最小长度', '6', 3),
('login.max.attempts', '5', 'security', '登录失败锁定次数', '5', 4),
('captcha.enable', 'false', 'security', '启用验证码', 'false', 5)
ON DUPLICATE KEY UPDATE 
  config_value = VALUES(config_value),
  description = VALUES(description);

-- ============================================
-- 初始化完成
-- ============================================

SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('共创建 ', COUNT(*), ' 个表') AS table_count FROM information_schema.tables WHERE table_schema = 'resource_platform';
