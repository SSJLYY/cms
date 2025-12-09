<div align="center">

# 📦 资源下载平台

### Resource Download Platform

*一个功能完整、开箱即用的资源下载站系统*

基于 Spring Boot + Vue 3 + MySQL 构建

[快速开始](QUICK_REFERENCE.md) · [功能截图](SCREENSHOTS.md) · [在线演示](#) · [问题反馈](https://github.com/your-username/resource-download-platform/issues)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.3.4-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/your-username/resource-download-platform)](https://github.com/your-username/resource-download-platform/releases)
[![GitHub stars](https://img.shields.io/github/stars/your-username/resource-download-platform)](https://github.com/your-username/resource-download-platform/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/your-username/resource-download-platform)](https://github.com/your-username/resource-download-platform/network)
[![GitHub issues](https://img.shields.io/github/issues/your-username/resource-download-platform)](https://github.com/your-username/resource-download-platform/issues)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

---

</div>

## 📑 快速导航

- [功能特性](#-功能特性)
- [快速启动](#-快速启动)
- [默认账号](#-默认账号)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [配置说明](#-配置说明)
- [数据库设计](#-数据库设计)
- [12个功能模块](#-12个功能模块详细说明)
- [部署指南](#-部署)
- [API文档](#-api文档)
- [常见问题](#-常见问题)
- [相关文档](#-相关文档)

## ✨ 功能特性

### 客户前台
- 🔍 资源浏览和搜索
- 📦 多种下载方式（夸克、迅雷、百度网盘等）
- 📱 响应式设计，支持移动端
- 📊 下载统计和热门推荐
- 🖼️ 资源详情页面

### 管理后台（13个核心模块）

#### 核心业务模块
1. **资源管理** - CRUD、审核、置顶、批量操作
2. **分类管理** - 二级分类、树形展示、拖拽排序
3. **图片管理** - 上传、压缩、缩略图、使用检查

#### 运营管理模块
4. **日志管理** - 系统日志、访问日志、操作审计
5. **反馈管理** - 用户反馈、回复、状态管理
6. **统计管理** - 访问统计、下载分析、实时监控

#### 营销推广模块
7. **SEO管理** - 网站地图、搜索引擎提交
8. **推广管理** - 广告位管理、点击统计
9. **友情链接** - 链接管理、Logo展示

#### 插件管理模块
10. **爬虫管理** - 自动化采集、智能解析、定时任务

#### 系统管理模块
11. **收益管理** - 收益统计、类型分析
12. **系统配置** - 参数配置、批量更新
13. **控制面板** - 核心指标、趋势分析

## 🚀 快速启动

### 方式一：一键启动脚本（推荐）

**Linux/Mac:**
```bash
# 1. 克隆项目
git clone <repository-url>
cd resource-download-platform

# 2. 赋予执行权限
chmod +x start.sh stop.sh

# 3. 一键启动
./start.sh

# 4. 停止服务
./stop.sh
```

**Windows:**
```cmd
# 1. 克隆项目
git clone <repository-url>
cd resource-download-platform

# 2. 一键启动
start.bat

# 3. 停止服务
stop.bat
```

启动脚本会自动：
- ✅ 检查 Docker 环境
- ✅ 检查端口占用
- ✅ 启动所有服务
- ✅ 显示访问地址

### 方式二：Docker Compose 手动启动

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

### 访问地址

启动成功后，访问以下地址：

- 🌐 **客户前台**: http://localhost:8080
- 🔧 **管理后台**: http://localhost:8081  
- 📚 **API文档**: http://localhost:9090/doc.html
- 🗄️ **MySQL**: localhost:3306
- 🔴 **Redis**: localhost:6379

### 方式三：本地开发环境

#### 前置要求
- ☕ Java 8+
- 📦 Maven 3.6+
- 🟢 Node.js 16+
- 🗄️ MySQL 8.0
- 🔴 Redis 6.0+

#### 1. 数据库初始化
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE resource_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本（包含所有表结构和测试数据）
mysql -u root -p resource_platform < backend/src/main/resources/init-database.sql
```

#### 2. 启动后端
```bash
cd backend

# 安装 Maven（如果未安装）
# CentOS/RHEL: sudo dnf install maven -y
# Ubuntu/Debian: sudo apt install maven -y
# Mac: brew install maven

# 验证安装
mvn -v

# 修改配置文件 src/main/resources/application.yml
# 配置数据库连接信息和Redis连接信息

# 编译打包
mvn clean package -DskipTests

# 启动方式1：使用Maven
mvn spring-boot:run

# 启动方式2：使用jar包
java -jar target/resource-platform-1.0.0.jar
```

后端地址：http://localhost:9090  
API文档：http://localhost:9090/doc.html

#### 3. 启动前端

**客户前台：**
```bash
cd frontend-client

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```
访问：http://localhost:5173（开发模式）

**管理后台：**
```bash
cd frontend-admin

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```
访问：http://localhost:5174（开发模式）

## 🔑 默认账号

- 用户名：`admin`
- 密码：`admin123`

若是密码不正确,自己去
https://bcrypt-generator.com/
这个网站加密后,填写到 user表的password字段中;

## 📦 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.1
- Spring Security + JWT
- MySQL 8.0
- Redis
- Knife4j (Swagger)
- Log4j2

### 前端
- Vue 3
- Vite
- Element Plus
- ECharts
- Axios
- Pinia

## 📁 项目结构

```
resource-download-platform/
├── backend/                          # Spring Boot 后端服务
│   ├── src/main/java/com/resource/platform/
│   │   ├── controller/              # 控制器层（15个Controller）
│   │   │   ├── CategoryController.java      # 分类管理
│   │   │   ├── ResourceController.java      # 资源管理
│   │   │   ├── ImageController.java         # 图片管理
│   │   │   ├── LogController.java           # 日志管理
│   │   │   ├── FeedbackController.java      # 反馈管理
│   │   │   ├── StatisticsController.java    # 统计管理
│   │   │   ├── SEOController.java           # SEO管理
│   │   │   ├── PromotionController.java     # 推广管理
│   │   │   ├── FriendLinkController.java    # 友情链接
│   │   │   ├── RevenueController.java       # 收益管理
│   │   │   ├── ConfigController.java        # 系统配置
│   │   │   ├── DashboardController.java     # 控制面板
│   │   │   ├── UserController.java          # 用户管理
│   │   │   ├── DownloadLinkController.java  # 下载链接
│   │   │   └── LinkTypeController.java      # 网盘类型
│   │   ├── service/                 # 服务层（业务逻辑）
│   │   ├── mapper/                  # 数据访问层（MyBatis）
│   │   ├── entity/                  # 实体类（14个核心表）
│   │   ├── dto/                     # 数据传输对象
│   │   ├── vo/                      # 视图对象
│   │   ├── config/                  # 配置类（Security、Swagger等）
│   │   ├── common/                  # 通用类（Result、Page等）
│   │   ├── exception/               # 异常处理
│   │   ├── aspect/                  # AOP切面（日志、权限）
│   │   └── util/                    # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml          # 主配置文件
│   │   ├── init-database.sql        # 数据库初始化脚本
│   │   └── log4j2.xml              # 日志配置
│   ├── Dockerfile                   # Docker镜像构建
│   └── pom.xml                      # Maven依赖配置
│
├── frontend-client/                 # Vue 3 客户前台
│   ├── src/
│   │   ├── api/                    # API接口封装
│   │   │   └── resource.js         # 资源相关API
│   │   ├── components/             # 公共组件
│   │   │   ├── ResourceCard.vue    # 资源卡片
│   │   │   └── CategoryNav.vue     # 分类导航
│   │   ├── views/                  # 页面组件
│   │   │   ├── Home.vue            # 首页（资源列表）
│   │   │   └── ResourceDetail.vue  # 资源详情页（图片轮播）
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia状态管理
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 入口文件
│   ├── public/                     # 静态资源
│   ├── Dockerfile                  # Docker镜像构建
│   ├── nginx.conf                  # Nginx配置
│   ├── package.json                # NPM依赖
│   └── vite.config.js              # Vite配置
│
├── frontend-admin/                  # Vue 3 管理后台
│   ├── src/
│   │   ├── api/                    # API接口封装
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面组件（12个核心模块）
│   │   │   ├── Dashboard.vue       # 控制面板
│   │   │   ├── ResourceManage.vue  # 资源管理
│   │   │   ├── CategoryManage.vue  # 分类管理
│   │   │   ├── ImageManage.vue     # 图片管理
│   │   │   ├── LogManage.vue       # 日志管理
│   │   │   ├── FeedbackManage.vue  # 反馈管理
│   │   │   ├── Statistics.vue      # 统计管理
│   │   │   ├── SEOManage.vue       # SEO管理
│   │   │   ├── Promotion.vue       # 推广管理
│   │   │   ├── FriendLink.vue      # 友情链接
│   │   │   ├── Revenue.vue         # 收益管理
│   │   │   └── SystemConfig.vue    # 系统配置
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia状态管理
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 入口文件
│   ├── Dockerfile                  # Docker镜像构建
│   ├── nginx.conf                  # Nginx配置
│   ├── package.json                # NPM依赖
│   └── vite.config.js              # Vite配置
│
├── docker-compose.yml               # Docker Compose编排文件
├── start.sh / start.bat            # 一键启动脚本
├── stop.sh / stop.bat              # 一键停止脚本
├── README.md                        # 项目说明文档
├── SCREENSHOTS.md                   # 项目截图展示
└── .gitignore                       # Git忽略配置
```

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resource_platform
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: your-secret-key  # 生产环境请修改
  expiration: 86400000     # 24小时

storage:
  type: local              # 存储类型：local、oss、cos、qiniu
  local:
    path: /data/uploads
```

### 前端配置

修改 `.env` 文件中的API地址：
```
VITE_API_BASE_URL=http://localhost:9090
```

## 📊 数据库设计

系统包含14个核心表：

**核心业务表（4个）：**
- `user` - 用户表
- `category` - 分类表（支持二级分类）
- `resource` - 资源表
- `download_link` - 下载链接表

**管理功能表（6个）：**
- `image` - 图片表
- `image_size` - 图片尺寸表
- `feedback` - 用户反馈表
- `system_log` - 系统日志表
- `access_log` - 访问日志表
- `system_config` - 系统配置表

**扩展功能表（4个）：**
- `seo_submission` - SEO提交记录表
- `advertisement` - 广告表
- `friend_link` - 友情链接表
- `revenue` - 收益记录表

详细的表结构请查看：`backend/src/main/resources/init-database.sql`

## 🔐 安全特性

- ✅ JWT身份认证
- ✅ 密码加密存储（BCrypt）
- ✅ XSS防护
- ✅ SQL注入防护
- ✅ 文件上传验证
- ✅ 操作日志审计
- ✅ 敏感配置加密

## 📝 日志管理

日志文件位于 `backend/logs/` 目录：
- `application.log` - 所有日志
- `application-info.log` - INFO级别
- `application-warn.log` - WARN级别
- `application-error.log` - ERROR级别

特性：
- 每天自动切割
- 单文件超过100MB自动切割
- 保留最近30天日志

## 🚢 部署

### Docker部署（推荐）

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 手动部署

1. **后端打包**
```bash
cd backend
mvn clean package -DskipTests
```
生成的jar包位于 `target/resource-platform-1.0.0.jar`

2. **前端构建**
```bash
# 客户前台
cd frontend-client
npm run build

# 管理后台
cd frontend-admin
npm run build
```

3. **部署到服务器**
- 后端：使用 `java -jar` 运行jar包
- 前端：将dist目录部署到Nginx

## 📖 API文档

启动后端后访问：http://localhost:9090/doc.html

提供完整的API文档，包括：
- 认证接口
- 资源管理接口
- 分类管理接口
- 日志管理接口
- 图片管理接口
- 反馈管理接口
- 统计分析接口
- 系统配置接口

## 🛠️ 开发指南

### 添加新功能

1. 创建实体类（Entity）
2. 创建Mapper接口
3. 创建Service接口和实现
4. 创建Controller
5. 添加前端API调用
6. 创建前端页面

### 代码规范

- 后端遵循阿里巴巴Java开发规范
- 前端遵循Vue 3官方风格指南
- 使用统一的命名规范
- 添加必要的注释

## 🐛 常见问题

### 1. 数据库连接失败
检查MySQL是否启动，配置文件中的数据库地址、用户名、密码是否正确。

### 2. Redis连接失败
检查Redis是否启动，配置文件中的Redis地址和端口是否正确。

### 3. 前端无法访问后端API
检查后端是否启动，前端配置的API地址是否正确，是否存在跨域问题。

### 4. 文件上传失败
检查存储路径是否有写入权限，文件大小是否超过限制。

### 5. JWT认证失败
检查JWT密钥配置是否正确，Token是否过期。

## 👥 作者

本项目由以下开发者共同完成：

- **shaun.sheng** - 项目负责人、核心开发
  - 负责项目架构设计
  - 后端开发（Spring Boot）
  - 前端开发（Vue 3）
  - 数据库设计
  - 业务逻辑实现

- **Kiro AI** - AI助手、技术顾问
  - 代码优化和重构
  - 文档编写和完善
  - 功能增强（图片轮播等）
  - 部署脚本优化
  - 技术咨询和建议

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

```
MIT License

Copyright (c) 2024 shaun.sheng & Kiro AI

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork 本项目**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **提交 Pull Request**

### 贡献类型

- 🐛 报告Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- ✨ 添加新功能
- 🎨 改进UI/UX
- ⚡ 性能优化

### 代码规范

- 后端遵循阿里巴巴Java开发规范
- 前端遵循Vue 3官方风格指南
- 提交信息使用清晰的描述
- 添加必要的注释和文档

## 📮 联系方式

- **提交Issue**: [GitHub Issues](https://github.com/your-username/resource-download-platform/issues)
- **讨论交流**: [GitHub Discussions](https://github.com/your-username/resource-download-platform/discussions)
- **邮件联系**: 如有商业合作或其他问题，请通过Issue联系

## ⭐ Star History

如果这个项目对你有帮助，请给我们一个 Star ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=your-username/resource-download-platform&type=Date)](https://star-history.com/#your-username/resource-download-platform&Date)

---

## 📚 13个功能模块详细说明

### 1. 资源管理模块
**核心功能**：管理所有可下载的资源内容
- 资源CRUD操作、富文本编辑器、多图片上传
- 多下载链接管理、资源审核、资源置顶
- 批量操作、高级搜索和筛选

**API接口**：
- `GET /api/resources/public/list` - 获取公开资源列表
- `POST /api/resources/admin/create` - 创建资源
- `PUT /api/resources/admin/update/{id}` - 更新资源
- `DELETE /api/resources/admin/delete/{id}` - 删除资源

### 2. 分类管理模块
**核心功能**：管理资源的分类体系，支持二级分类
- 二级分类支持、树形结构展示
- 拖拽排序、批量操作
- 分类图标设置、状态管理

**API接口**：
- `GET /api/categories/tree` - 获取分类树
- `POST /api/categories` - 创建分类
- `PUT /api/categories/{id}` - 更新分类

### 3. 图片管理模块
**核心功能**：统一管理平台所有图片资源
- 图片上传(单张/批量)、自动生成缩略图
- 图片压缩、使用情况检查
- 多种存储方式(本地/OSS/COS/七牛)

**API接口**：
- `POST /api/images/upload` - 上传图片
- `GET /api/images/list` - 获取图片列表
- `DELETE /api/images/{id}` - 删除图片

### 4. 日志管理模块
**核心功能**：记录和查询系统操作日志
- 系统日志查询、操作审计
- 日志导出、日志清理
- 多维度筛选、日志统计

**API接口**：
- `GET /api/logs/system` - 获取系统日志
- `GET /api/logs/access` - 获取访问日志
- `DELETE /api/logs/clean` - 清理日志

### 5. 反馈管理模块
**核心功能**：管理用户提交的反馈信息
- 反馈查询、反馈回复
- 状态管理(待处理/处理中/已完成/已关闭)
- 类型分类(建议/问题/投诉/其他)

**API接口**：
- `GET /api/feedback/list` - 获取反馈列表
- `POST /api/feedback/reply` - 回复反馈
- `PUT /api/feedback/{id}/status` - 更新状态

### 6. 统计管理模块
**核心功能**：展示网站的访问统计和下载分布
- 统计概览(总下载量、总访问量、新增访问)
- 下载分布饼图、下载排行榜(前5名)
- 访问统计详情表格、实时访问监控
- 统计周期切换(今天/昨天/近7天/近30天)

**API接口**：
- `GET /api/statistics/overview` - 获取统计概览
- `GET /api/statistics/download-distribution` - 获取下载分布
- `GET /api/statistics/visit-details` - 获取访问详情

### 7. SEO管理模块
**核心功能**：管理网站的搜索引擎优化
- SEO统计数据展示
- 自动生成网站地图(sitemap.xml)
- 批量提交URL到搜索引擎
- 提交历史记录管理
- 支持百度和必应搜索引擎

**API接口**：
- `GET /api/seo/statistics` - 获取SEO统计
- `POST /api/seo/sitemap/generate` - 生成网站地图
- `POST /api/seo/submit/baidu` - 提交到百度
- `POST /api/seo/submit/bing` - 提交到必应

### 8. 推广管理模块
**核心功能**：管理网站的广告位和推广内容
- 广告位管理、多种广告类型(图片/文字/视频)
- 广告位置管理(首页/下载页/分类页/自定义)
- 时间段控制、点击统计、状态管理

**API接口**：
- `GET /api/promotion/list` - 获取广告列表
- `POST /api/promotion` - 创建广告
- `PUT /api/promotion/{id}` - 更新广告
- `DELETE /api/promotion/{id}` - 删除广告

### 9. 友情链接模块
**核心功能**：管理网站的友情链接
- 友情链接CRUD、Logo管理
- 排序功能、状态管理、批量操作

**API接口**：
- `GET /api/friendlinks/page` - 分页查询
- `POST /api/friendlinks` - 创建友情链接
- `PUT /api/friendlinks` - 更新友情链接
- `DELETE /api/friendlinks/{id}` - 删除友情链接

### 10. 爬虫管理模块 🆕
**核心功能**：自动化采集网站资源，智能解析内容
- **任务管理** - 创建、编辑、删除爬虫任务
- **智能解析** - 自动识别网站结构，提取资源信息
- **定时执行** - 支持定时自动爬取，可配置爬取间隔
- **手动触发** - 支持立即执行任务
- **执行日志** - 详细记录每次爬取的结果和统计
- **分类映射** - 自动映射源网站分类到平台分类
- **自定义规则** - 支持配置CSS选择器自定义解析规则
- **Robots.txt遵守** - 自动遵守目标网站的爬虫协议
- **深度控制** - 可配置爬取深度，防止过度爬取
- **去重机制** - 自动检测并跳过重复资源
- **图片下载** - 自动下载并上传资源图片
- **状态管理** - 爬取的资源默认为下架状态，需人工审核

**使用场景**：
- 批量采集其他资源网站的内容
- 定期更新资源库
- 快速扩充网站内容
- 自动化内容运营

**配置说明**：
```yaml
crawler:
  thread-pool:
    core-size: 5              # 核心线程数
    max-size: 10              # 最大线程数
  request:
    connect-timeout: 30000    # 连接超时(毫秒)
    read-timeout: 60000       # 读取超时(毫秒)
    max-retries: 3            # 最大重试次数
  limits:
    max-pages: 1000           # 单次爬取最大页面数
    max-page-size: 10485760   # 单个页面最大大小(10MB)
    max-image-size: 5242880   # 单张图片最大大小(5MB)
  rate-limit:
    requests-per-second: 2    # 每秒最大请求数
    crawl-delay: 1            # 默认爬取延迟(秒)
```

**API接口**：
- `POST /api/crawler/tasks` - 创建爬虫任务
- `PUT /api/crawler/tasks/{id}` - 更新爬虫任务
- `DELETE /api/crawler/tasks/{id}` - 删除爬虫任务
- `PUT /api/crawler/tasks/{id}/toggle` - 切换任务状态
- `GET /api/crawler/tasks` - 查询任务列表
- `GET /api/crawler/tasks/{id}` - 获取任务详情
- `POST /api/crawler/tasks/{id}/trigger` - 立即触发任务
- `POST /api/crawler/tasks/{id}/stop` - 停止任务执行
- `POST /api/crawler/validate-url` - 验证URL
- `GET /api/crawler/logs` - 查询执行日志
- `GET /api/crawler/logs/{id}` - 获取日志详情
- `GET /api/crawler/tasks/{id}/running` - 检查任务执行状态

**注意事项**：
- ⚠️ 请遵守目标网站的robots.txt协议
- ⚠️ 合理设置爬取间隔，避免对目标网站造成压力
- ⚠️ 爬取的内容需要人工审核后才能发布
- ⚠️ 注意版权问题，不要爬取受版权保护的内容

### 11. 收益管理模块
**核心功能**：管理和统计平台的各类收益数据
- 收益概览、收益类型统计(8种类型)
- 收益明细管理、统计周期选择
- 批量删除、状态管理(待结算/已结算/已取消)

**API接口**：
- `GET /api/revenue/overview` - 获取收益概览
- `GET /api/revenue/by-type` - 按类型获取收益统计
- `GET /api/revenue/list` - 获取收益明细列表
- `DELETE /api/revenue/{id}` - 删除收益记录

### 12. 系统配置模块
**核心功能**：管理系统的各项配置参数
- 基本设置(网站名称、标题、描述等)
- SEO设置、存储设置、邮件设置、安全设置
- 配置分类管理、批量更新

**API接口**：
- `GET /api/config/list` - 获取配置列表
- `PUT /api/config` - 更新配置
- `POST /api/config/batch` - 批量更新配置

### 13. 控制面板模块
**核心功能**：展示系统的核心指标和待处理事项
- 核心指标展示(资源数、用户数、下载量等)
- 趋势分析图表、热门资源排行
- 待处理事项提醒、快速操作入口、系统状态监控

**API接口**：
- `GET /api/dashboard/overview` - 获取概览数据
- `GET /api/dashboard/trends` - 获取趋势数据
- `GET /api/dashboard/hot-resources` - 获取热门资源

---

## 📚 相关文档

### 核心文档
- 🚀 **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - 快速参考卡片（一页纸了解项目）⭐
- 📖 **[README.md](README.md)** - 完整项目说明（本文档）
- 📊 **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - 项目总结（技术架构、规模统计）

### 功能文档
- 📸 **[SCREENSHOTS.md](SCREENSHOTS.md)** - 功能截图展示（含45张截图模板）
- 🖼️ **[IMAGE_CAROUSEL_FEATURE.md](IMAGE_CAROUSEL_FEATURE.md)** - 图片轮播功能说明

### 工具文档
- ✅ **[CHECKLIST.md](CHECKLIST.md)** - 项目检查清单（功能测试、部署验证）
- 📁 **[FILE_LIST.md](FILE_LIST.md)** - 项目文件清单（所有文件说明）
- 🚀 **[QUICKSTART.md](QUICKSTART.md)** - 快速启动指南（3种启动方式）

### 在线文档
- 📚 **[API文档](http://localhost:9090/doc.html)** - Knife4j接口文档（启动后访问）

## 🎯 核心特性

### 技术亮点
- ✅ **前后端分离架构** - Spring Boot + Vue 3
- ✅ **RESTful API设计** - 统一的接口规范
- ✅ **JWT身份认证** - 无状态的安全认证
- ✅ **MyBatis Plus** - 高效的ORM框架
- ✅ **Redis缓存** - 提升系统性能
- ✅ **Docker部署** - 一键启动，开箱即用
- ✅ **完整的日志系统** - Log4j2日志管理
- ✅ **API文档自动生成** - Knife4j/Swagger
- ✅ **响应式设计** - 支持PC和移动端
- ✅ **图片轮播功能** - 支持多图展示

### 业务特性
- 📦 **多网盘支持** - 夸克、迅雷、百度网盘等
- 🔍 **高级搜索** - 关键词、分类、标签筛选
- 📊 **数据统计** - 下载量、访问量统计分析
- 🖼️ **图片管理** - 自动压缩、缩略图生成
- 🔐 **权限管理** - 基于角色的访问控制
- 📝 **操作日志** - 完整的审计追踪
- 💰 **收益统计** - 多维度收益分析
- 🔗 **SEO优化** - 网站地图、搜索引擎提交

## 🔄 更新日志

### v0.0.1 (2025-12-05)
- ✨ 初始版本发布
- ✨ 完成12个核心功能模块
- ✨ 实现前后端分离架构
- ✨ 添加Docker一键部署
- ✨ 完善API文档
- ✨ 新增图片轮播功能


# 🎯 请我喝咖啡 (听我说谢谢你,因为有你,温暖了四季)

![微信请我](wx.png)

![支付宝请我](zfb.png)


