# 📁 项目文件清单

本文档列出项目的所有重要文件及其说明。

---

## 📄 根目录文档文件

| 文件名 | 大小 | 说明 |
|--------|------|------|
| README.md | 21.7 KB | 项目主文档，包含完整的项目介绍、快速启动、技术栈、12个模块说明 |
| SCREENSHOTS.md | 14.9 KB | 功能截图展示文档，包含45张截图的模板和说明 |
| PROJECT_SUMMARY.md | 14.1 KB | 项目总结文档，包含技术架构、代码统计、核心功能、项目亮点 |
| CHECKLIST.md | 10.5 KB | 项目检查清单，用于功能测试和部署验证 |
| QUICKSTART.md | 7.7 KB | 快速启动指南，3种启动方式详细说明 |
| DEPLOYMENT_SIMPLIFIED.md | 5.1 KB | 简化部署指南 |
| IMAGE_CAROUSEL_FEATURE.md | ~3 KB | 图片轮播功能详细说明 |
| FILE_LIST.md | - | 本文件，项目文件清单 |

---

## 🚀 启动脚本

| 文件名 | 平台 | 大小 | 说明 |
|--------|------|------|------|
| start.sh | Linux/Mac | 4.7 KB | 一键启动脚本，包含环境检查、端口检查、健康检查 |
| stop.sh | Linux/Mac | 3.1 KB | 停止脚本，支持保留或删除数据 |
| start.bat | Windows | 4.8 KB | Windows启动脚本，功能同start.sh |
| stop.bat | Windows | 2.5 KB | Windows停止脚本，功能同stop.sh |

**使用方法:**
```bash
# Linux/Mac
chmod +x start.sh stop.sh
./start.sh
./stop.sh

# Windows
start.bat
stop.bat
```

---

## 🐳 Docker配置

| 文件名 | 说明 |
|--------|------|
| docker-compose.yml | Docker Compose编排文件，定义5个服务（MySQL、Redis、后端、客户前台、管理后台） |
| backend/Dockerfile | 后端Docker镜像构建文件 |
| frontend-client/Dockerfile | 客户前台Docker镜像构建文件 |
| frontend-admin/Dockerfile | 管理后台Docker镜像构建文件 |

---

## 🔧 后端文件

### 配置文件
| 文件路径 | 说明 |
|----------|------|
| backend/pom.xml | Maven项目配置，定义所有依赖 |
| backend/src/main/resources/application.yml | Spring Boot主配置文件 |
| backend/src/main/resources/log4j2.xml | Log4j2日志配置 |
| backend/src/main/resources/init-database.sql | 数据库初始化脚本（完整） |

### 源代码结构
```
backend/src/main/java/com/resource/platform/
├── controller/          # 15个Controller
│   ├── CategoryController.java
│   ├── ResourceController.java
│   ├── ImageController.java
│   ├── LogController.java
│   ├── FeedbackController.java
│   ├── StatisticsController.java
│   ├── SEOController.java
│   ├── PromotionController.java
│   ├── FriendLinkController.java
│   ├── RevenueController.java
│   ├── ConfigController.java
│   ├── DashboardController.java
│   ├── UserController.java
│   ├── DownloadLinkController.java
│   └── LinkTypeController.java
├── service/            # 服务层
│   ├── impl/          # 服务实现
│   └── ...
├── mapper/            # MyBatis Mapper
├── entity/            # 14个实体类
│   ├── User.java
│   ├── Category.java
│   ├── Resource.java
│   ├── DownloadLink.java
│   ├── Image.java
│   ├── ResourceImage.java
│   ├── Feedback.java
│   ├── SystemLog.java
│   ├── AccessLog.java
│   ├── SystemConfig.java
│   ├── SEOSubmission.java
│   ├── Advertisement.java
│   ├── FriendLink.java
│   └── Revenue.java
├── dto/               # 数据传输对象
├── vo/                # 视图对象
├── config/            # 配置类
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── RedisConfig.java
│   └── ...
├── common/            # 通用类
│   ├── Result.java
│   ├── PageResult.java
│   └── ...
├── exception/         # 异常处理
│   ├── GlobalExceptionHandler.java
│   └── ...
├── aspect/            # AOP切面
│   ├── LogAspect.java
│   └── ...
└── util/              # 工具类
```

---

## 🎨 前端文件

### 客户前台 (frontend-client)

**配置文件:**
| 文件路径 | 说明 |
|----------|------|
| package.json | NPM依赖配置 |
| vite.config.js | Vite构建配置 |
| nginx.conf | Nginx配置 |
| .env.production | 生产环境配置 |

**源代码结构:**
```
frontend-client/src/
├── api/
│   └── resource.js          # 资源相关API
├── components/
│   ├── ResourceCard.vue     # 资源卡片组件
│   └── CategoryNav.vue      # 分类导航组件
├── views/
│   ├── Home.vue             # 首页
│   └── ResourceDetail.vue   # 资源详情页（含图片轮播）
├── router/
│   └── index.js             # 路由配置
├── stores/
│   └── index.js             # Pinia状态管理
├── App.vue                  # 根组件
└── main.js                  # 入口文件
```

### 管理后台 (frontend-admin)

**配置文件:**
| 文件路径 | 说明 |
|----------|------|
| package.json | NPM依赖配置（含Element Plus、ECharts） |
| vite.config.js | Vite构建配置 |
| nginx.conf | Nginx配置 |
| .env.production | 生产环境配置 |

**源代码结构:**
```
frontend-admin/src/
├── api/                     # API接口封装
├── components/              # 公共组件
├── views/                   # 12个核心模块页面
│   ├── Dashboard.vue        # 控制面板
│   ├── ResourceManage.vue   # 资源管理
│   ├── CategoryManage.vue   # 分类管理
│   ├── ImageManage.vue      # 图片管理
│   ├── LogManage.vue        # 日志管理
│   ├── FeedbackManage.vue   # 反馈管理
│   ├── Statistics.vue       # 统计管理
│   ├── SEOManage.vue        # SEO管理
│   ├── Promotion.vue        # 推广管理
│   ├── FriendLink.vue       # 友情链接
│   ├── Revenue.vue          # 收益管理
│   └── SystemConfig.vue     # 系统配置
├── router/
│   └── index.js             # 路由配置
├── stores/
│   └── index.js             # Pinia状态管理
├── App.vue                  # 根组件
└── main.js                  # 入口文件
```

---

## 📸 截图文件夹

| 文件路径 | 说明 |
|----------|------|
| screenshots/README.md | 截图说明文档，包含45张截图的清单和规范 |
| screenshots/*.png | 项目功能截图（需要自行添加） |

**截图清单:**
- 客户前台: 5张
- 管理后台: 33张
- API文档: 4张
- 移动端: 3张

---

## 🗄️ 数据库文件

| 文件路径 | 说明 |
|----------|------|
| backend/src/main/resources/init-database.sql | 完整的数据库初始化脚本 |

**包含内容:**
- 14个数据表的CREATE语句
- 索引和外键约束
- 测试数据INSERT语句
- 默认管理员账号

---

## 🔒 配置文件

| 文件名 | 说明 |
|--------|------|
| .gitignore | Git忽略配置，排除node_modules、target、logs等 |

---

## 📊 文件统计

### 文档文件
- Markdown文档: 8个
- 总大小: ~92 KB
- 总字数: ~50,000字

### 代码文件
- Java源文件: ~100个
- Vue组件: ~20个
- 配置文件: ~15个

### 脚本文件
- Shell脚本: 2个
- Batch脚本: 2个

### 配置文件
- Docker配置: 4个
- 构建配置: 5个

---

## 📦 依赖文件

### 后端依赖 (pom.xml)
- Spring Boot: 2.7.18
- MyBatis Plus: 3.5.3.1
- JWT: 0.11.5
- Knife4j: 4.0.0
- 其他依赖: 20+个

### 前端依赖 (package.json)
**客户前台:**
- Vue: 3.3.4
- Vite: 5.0.0
- Axios: 1.6.0
- Vue Router: 4.2.5
- Pinia: 2.1.7

**管理后台:**
- Vue: 3.3.4
- Element Plus: 2.4.4
- ECharts: 5.4.3
- 其他同客户前台

---

## 🎯 核心文件说明

### 必读文档（按优先级）
1. **README.md** - 首先阅读，了解项目全貌
2. **SCREENSHOTS.md** - 查看功能截图，直观了解系统
3. **PROJECT_SUMMARY.md** - 深入了解技术架构和项目规模
4. **CHECKLIST.md** - 部署和测试时使用

### 快速启动文件
1. **start.sh / start.bat** - 一键启动
2. **docker-compose.yml** - Docker配置
3. **stop.sh / stop.bat** - 停止服务

### 开发必看文件
1. **backend/pom.xml** - 后端依赖
2. **backend/src/main/resources/application.yml** - 后端配置
3. **frontend-*/package.json** - 前端依赖
4. **backend/src/main/resources/init-database.sql** - 数据库结构

---

## 🔄 文件更新记录

### 2024-12-05
- ✅ 完善 README.md（添加快速导航、核心特性、更新日志）
- ✅ 增强 start.sh/stop.sh（添加颜色输出、健康检查、详细提示）
- ✅ 增强 start.bat/stop.bat（添加端口检查、健康检查）
- ✅ 创建 SCREENSHOTS.md（45张截图模板）
- ✅ 创建 PROJECT_SUMMARY.md（项目总结）
- ✅ 创建 CHECKLIST.md（检查清单）
- ✅ 创建 FILE_LIST.md（本文件）
- ✅ 创建 screenshots/README.md（截图说明）

---

## 📝 使用建议

### 新用户
1. 阅读 README.md 了解项目
2. 使用 start.sh/start.bat 启动项目
3. 查看 SCREENSHOTS.md 了解功能
4. 访问 http://localhost:8080 和 http://localhost:8081

### 开发者
1. 阅读 PROJECT_SUMMARY.md 了解架构
2. 查看源代码结构
3. 阅读 API文档
4. 使用 CHECKLIST.md 进行测试

### 部署人员
1. 检查 CHECKLIST.md 中的环境要求
2. 使用 Docker 部署
3. 配置 application.yml
4. 执行健康检查

---

## 🔗 相关链接

- [README.md](README.md) - 项目主文档
- [SCREENSHOTS.md](SCREENSHOTS.md) - 功能截图
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - 项目总结
- [CHECKLIST.md](CHECKLIST.md) - 检查清单

---

**最后更新**: 2024-12-05
