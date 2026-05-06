# 快速启动指南

## 🎯 三种启动方式

### 方式一：Docker一键启动（最简单）⭐

**适合：** 快速体验、生产部署

```bash
# 1. 克隆项目
git clone <repository-url>
cd resource-download-platform

# 2. 一键启动所有服务
docker-compose up -d

# 3. 查看服务状态
docker-compose ps

# 4. 访问应用
```

**访问地址：**
- 客户前台：http://localhost:8080
- 管理后台：http://localhost:8081
- API文档：http://localhost:9090/doc.html
- 默认账号：admin / admin123

**常用命令：**
```bash
# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 停止并删除数据
docker-compose down -v
```

---

### 方式二：本地开发环境（推荐开发）

**适合：** 本地开发、调试

#### 前置要求
- ✅ Java 8+
- ✅ Maven 3.6+
- ✅ Node.js 16+
- ✅ MySQL 8.0
- ✅ Redis

#### 步骤1：启动MySQL和Redis

```bash
# 启动MySQL
mysql.server start

# 启动Redis
redis-server
```

#### 步骤2：初始化数据库

```bash
# 一键导入完整数据库（包含所有表结构和测试数据）
mysql -u root -p < backend/src/main/resources/init-database.sql

# 或者分步执行
mysql -u root -p
CREATE DATABASE resource_platform;
exit
mysql -u root -p resource_platform < backend/src/main/resources/init-database.sql
```

#### 步骤3：配置后端

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resource_platform
    username: root
    password: your_password  # 修改为你的密码
  
  redis:
    host: localhost
    port: 6379
```

#### 步骤4：启动后端

```bash
cd backend
mvn spring-boot:run
```

✅ 后端启动成功：http://localhost:9090  
✅ API文档：http://localhost:9090/doc.html

#### 步骤5：启动客户前台

```bash
cd frontend-client
npm install
npm run dev
```

✅ 客户前台：http://localhost:8080

#### 步骤6：启动管理后台

```bash
cd frontend-admin
npm install
npm run dev
```

✅ 管理后台：http://localhost:8081

---

### 方式三：生产环境部署

**适合：** 服务器部署

#### 1. 后端打包

```bash
cd backend
mvn clean package -DskipTests
```

生成文件：`target/resource-platform-1.0.0.jar`

#### 2. 前端构建

```bash
# 客户前台
cd frontend-client
npm run build
# 生成 dist/ 目录

# 管理后台
cd frontend-admin
npm run build
# 生成 dist/ 目录
```

#### 3. 部署到服务器

**后端部署：**
```bash
# 上传jar包到服务器
scp target/resource-platform-1.0.0.jar user@server:/app/

# 在服务器上运行
java -jar /app/resource-platform-1.0.0.jar

# 或使用nohup后台运行
nohup java -jar /app/resource-platform-1.0.0.jar > /app/logs/app.log 2>&1 &
```

**前端部署（Nginx）：**
```bash
# 上传dist目录到服务器
scp -r dist/ user@server:/var/www/html/

# Nginx配置示例
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/html/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:9090;
    }
}
```

---

## 🔑 默认账号

**管理员账号：**
- 用户名：`admin`
- 密码：`admin123`

⚠️ **生产环境请立即修改默认密码！**

---

## 📋 功能清单

### 客户前台
- ✅ 资源浏览（列表、网格视图）
- ✅ 搜索和筛选
- ✅ 资源详情查看
- ✅ 多种下载方式
- ✅ 响应式设计

### 管理后台

**资源管理：**
- ✅ 资源CRUD
- ✅ 富文本编辑器
- ✅ 标签管理
- ✅ 资源审核
- ✅ 资源置顶
- ✅ 批量操作

**分类管理：**
- ✅ 二级分类
- ✅ 树形展示
- ✅ 拖拽排序
- ✅ 批量操作

**图片管理：**
- ✅ 图片上传（单张/批量）
- ✅ 自动生成缩略图
- ✅ 图片压缩
- ✅ 使用情况检查

**日志管理：**
- ✅ 系统日志查询
- ✅ 操作审计
- ✅ 日志导出
- ✅ 日志清理

**反馈管理：**
- ✅ 反馈查询
- ✅ 反馈回复
- ✅ 状态管理
- ✅ 批量删除

**数据统计：**
- ✅ 访问统计
- ✅ 下载分析
- ✅ 实时活动
- ✅ 趋势图表

**系统配置：**
- ✅ 分类配置
- ✅ 批量更新
- ✅ 配置重置
- ✅ 配置测试

**SEO管理：**
- ✅ 网站地图生成
- ✅ 搜索引擎提交
- ✅ 提交历史

**控制面板：**
- ✅ 核心指标
- ✅ 趋势分析
- ✅ 热门资源
- ✅ 待处理事项

---

## 🔧 配置说明

### 后端配置文件

`backend/src/main/resources/application.yml`

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/resource_platform
    username: root
    password: your_password

# Redis配置
  redis:
    host: localhost
    port: 6379

# JWT配置
jwt:
  secret: your-secret-key-change-in-production
  expiration: 86400000  # 24小时

# 服务器端口
server:
  port: 9090

# 文件上传配置
file:
  upload:
    max-size: 10485760  # 10MB
    allowed-types: jpg,jpeg,png,gif,bmp,webp

# 存储配置
storage:
  type: local  # local, oss, cos, qiniu
  local:
    path: /data/uploads
```

### 前端配置文件

**客户前台** `frontend-client/.env`
```
VITE_API_BASE_URL=http://localhost:9090
```

**管理后台** `frontend-admin/.env`
```
VITE_API_BASE_URL=http://localhost:9090
```

---

## 🐛 常见问题

### 1. 数据库连接失败

**问题：** `Communications link failure`

**解决：**
- 检查MySQL是否启动：`mysql.server status`
- 检查数据库配置是否正确
- 检查数据库是否已创建
- 检查用户名密码是否正确

### 2. Redis连接失败

**问题：** `Unable to connect to Redis`

**解决：**
- 检查Redis是否启动：`redis-cli ping`
- 检查Redis配置是否正确
- 检查端口是否被占用

### 3. 前端无法访问后端

**问题：** `Network Error` 或 `CORS Error`

**解决：**
- 检查后端是否启动
- 检查前端配置的API地址是否正确
- 检查后端CORS配置

### 4. 端口被占用

**问题：** `Port already in use`

**解决：**
```bash
# Windows
netstat -ano | findstr :9090
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :9090
kill -9 <PID>
```

### 5. Maven依赖下载慢

**解决：** 配置阿里云镜像

编辑 `~/.m2/settings.xml`：
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

### 6. npm安装依赖慢

**解决：** 使用淘宝镜像
```bash
npm config set registry https://registry.npmmirror.com
```

---

## 📊 系统要求

### 最低配置
- CPU: 2核
- 内存: 4GB
- 硬盘: 20GB
- 操作系统: Windows/Linux/Mac

### 推荐配置
- CPU: 4核
- 内存: 8GB
- 硬盘: 50GB
- 操作系统: Linux

---

## 📝 下一步

1. ✅ 登录管理后台
2. ✅ 添加分类
3. ✅ 上传图片
4. ✅ 创建资源
5. ✅ 配置系统设置
6. ✅ 查看统计数据

---

## 📚 更多文档

- **README.md** - 项目完整说明
- **API文档** - http://localhost:9090/doc.html
- **数据库设计** - 查看 init.sql

---

## 🆘 获取帮助

遇到问题？

1. 查看本文档的常见问题部分
2. 查看API文档
3. 查看日志文件 `backend/logs/`
4. 提交Issue

---

**祝你使用愉快！** 🎉
