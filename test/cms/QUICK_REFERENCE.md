# 🚀 快速参考卡片

一页纸快速了解和使用本项目。

---

## 📦 项目信息

**名称**: 资源下载平台 (Resource Download Platform)  
**版本**: v0.0.1  
**技术栈**: Spring Boot 2.7.18 + Vue 3.3.4 + MySQL 8.0  
**部署方式**: Docker Compose

---

## ⚡ 快速启动（3步）

```bash
# 1. 克隆项目
git clone <repository-url>
cd resource-download-platform

# 2. 启动服务（Linux/Mac）
chmod +x start.sh
./start.sh

# 或 Windows
start.bat

# 3. 访问应用
# 客户前台: http://localhost:8080
# 管理后台: http://localhost:8081
# API文档: http://localhost:9090/doc.html
```

---

## 🔑 默认账号

```
用户名: admin
密码: admin123
```

---

## 🌐 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 客户前台 | http://localhost:8080 | 资源浏览和下载 |
| 管理后台 | http://localhost:8081 | 后台管理系统 |
| API文档 | http://localhost:9090/doc.html | Knife4j接口文档 |
| MySQL | localhost:3306 | 数据库 |
| Redis | localhost:6379 | 缓存 |

---

## 📝 常用命令

### Docker命令
```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart

# 停止并删除数据
docker-compose down -v
```

### 脚本命令
```bash
# Linux/Mac
./start.sh              # 启动
./stop.sh               # 停止
./stop.sh -v            # 停止并删除数据

# Windows
start.bat               # 启动
stop.bat                # 停止
stop.bat /v             # 停止并删除数据
```

---

## 📚 核心文档

| 文档 | 说明 | 大小 |
|------|------|------|
| [README.md](README.md) | 完整项目说明 | 21.7 KB |
| [SCREENSHOTS.md](SCREENSHOTS.md) | 功能截图展示 | 14.9 KB |
| [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) | 项目总结 | 14.1 KB |
| [CHECKLIST.md](CHECKLIST.md) | 检查清单 | 10.5 KB |
| [FILE_LIST.md](FILE_LIST.md) | 文件清单 | - |

---

## 🎯 12个核心模块

### 核心业务（3个）
1. **资源管理** - CRUD、审核、置顶
2. **分类管理** - 二级分类、树形展示
3. **图片管理** - 上传、压缩、缩略图

### 运营管理（3个）
4. **日志管理** - 系统日志、访问日志
5. **反馈管理** - 用户反馈、回复
6. **统计管理** - 访问统计、下载分析

### 营销推广（3个）
7. **SEO管理** - 网站地图、搜索引擎提交
8. **推广管理** - 广告位管理
9. **友情链接** - 链接管理

### 系统管理（3个）
10. **收益管理** - 收益统计、类型分析
11. **系统配置** - 参数配置
12. **控制面板** - 核心指标、趋势分析

---

## 🗄️ 数据库信息

**数据库名**: resource_platform  
**字符集**: utf8mb4  
**表数量**: 14个  
**初始化脚本**: backend/src/main/resources/init-database.sql

### 核心表
- user - 用户表
- category - 分类表
- resource - 资源表
- download_link - 下载链接表
- image - 图片表
- resource_image - 资源图片关联表

---

## 🔧 配置文件位置

### 后端配置
```
backend/src/main/resources/application.yml
```

### 前端配置
```
frontend-client/.env.production
frontend-admin/.env.production
```

### Docker配置
```
docker-compose.yml
```

---

## 🐛 常见问题

### 1. 端口被占用
```bash
# 检查端口占用
netstat -ano | findstr "8080"  # Windows
lsof -i :8080                  # Linux/Mac

# 修改端口（docker-compose.yml）
ports:
  - "8888:80"  # 改为8888
```

### 2. 数据库连接失败
```bash
# 检查MySQL是否启动
docker-compose ps

# 查看MySQL日志
docker-compose logs mysql

# 等待MySQL初始化完成（首次启动需要1-2分钟）
```

### 3. 前端无法访问后端
```bash
# 检查后端是否启动
curl http://localhost:9090/actuator/health

# 检查网络连接
docker network ls
```

### 4. 图片上传失败
```bash
# 检查存储路径权限
# 修改 application.yml 中的 storage.local.path
```

---

## 📊 项目规模

- **代码行数**: ~13,000行
- **Java文件**: ~100个
- **Vue组件**: ~20个
- **API接口**: 60+个
- **数据表**: 14个
- **文档**: 8个

---

## 🔒 安全提示

1. **修改默认密码**
   - 登录后立即修改admin密码

2. **修改JWT密钥**
   ```yaml
   # application.yml
   jwt:
     secret: your-new-secret-key
   ```

3. **配置HTTPS**
   - 生产环境使用HTTPS
   - 配置SSL证书

4. **数据库安全**
   - 修改root密码
   - 限制远程访问

---

## 📱 浏览器支持

- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+
- ✅ 移动端浏览器

---

## 🎨 技术栈速查

### 后端
- Spring Boot 2.7.18
- Spring Security + JWT
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis 6.0+
- Knife4j 4.0.0

### 前端
- Vue 3.3.4
- Vite 5.0.0
- Element Plus 2.4.4
- ECharts 5.4.3
- Axios 1.6.0

### 部署
- Docker
- Docker Compose
- Nginx

---

## 📞 获取帮助

1. **查看文档**
   - README.md - 完整说明
   - CHECKLIST.md - 问题排查

2. **查看日志**
   ```bash
   # Docker日志
   docker-compose logs -f
   
   # 后端日志
   tail -f backend/logs/application.log
   ```

3. **提交Issue**
   - 描述问题
   - 附上错误日志
   - 说明环境信息

---

## ✅ 快速检查清单

启动后检查：
- [ ] 所有容器运行正常
- [ ] 客户前台可访问
- [ ] 管理后台可访问
- [ ] API文档可访问
- [ ] 可以登录管理后台
- [ ] 资源列表正常显示

---

## 🎯 下一步

1. **截图**
   - 按照 SCREENSHOTS.md 进行截图
   - 保存到 screenshots/ 文件夹

2. **自定义配置**
   - 修改网站名称
   - 配置存储方式
   - 设置邮件服务

3. **添加数据**
   - 添加分类
   - 上传资源
   - 配置下载链接

---

**提示**: 将本文件打印或保存为PDF，作为快速参考手册。

---

**最后更新**: 2024-12-05
