# 简化部署流程

## 目录结构

```
/data/xiazai/
├── backend/                    # 后端代码
├── frontend-admin/             # 管理端代码
│   └── dist/                   # 构建后的文件（nginx直接指向这里）
└── frontend-client/            # 客户端代码
    └── dist/                   # 构建后的文件（nginx直接指向这里）
```

---

## Nginx配置

Nginx已配置为直接指向项目目录下的dist文件夹：

- **管理端：** `/data/xiazai/frontend-admin/dist` → http://47.103.87.55:8081
- **客户端：** `/data/xiazai/frontend-client/dist` → http://47.103.87.55:8080

---

## 部署步骤

### 1. 首次部署Nginx配置

```bash
# 复制nginx配置文件
sudo cp /data/xiazai/nginx-production.conf /etc/nginx/conf.d/resource-platform.conf

# 测试nginx配置
sudo nginx -t

# 重启nginx
sudo systemctl restart nginx
```

---

### 2. 前端更新流程（非常简单！）

#### 管理端更新

```bash
# 进入管理端目录
cd /data/xiazai/frontend-admin

npm install
# 构建（会自动生成dist目录）
npm run build

# 完成！nginx会自动读取新的dist文件
# 清除浏览器缓存或 Ctrl+F5 刷新即可
```

#### 客户端更新

```bash
# 进入客户端目录
cd /data/xiazai/frontend-client

npm install
# 构建
npm run build

# 完成！
```

**优势：**
- ✅ 不需要复制文件
- ✅ 不需要设置权限
- ✅ 直接在项目目录构建即可
- ✅ 更新速度快

---

### 3. 后端更新流程

```bash
# 进入后端目录
cd /data/xiazai/backend

# 构建
mvn clean package -DskipTests

# 停止服务
cd /opt/resource-platform
./stop.sh

# 复制新的jar包
cp /data/xiazai/backend/target/resource-platform-1.0.0.jar /opt/resource-platform/

# 启动服务
./start.sh
```

---

## 常用命令

### 查看Nginx状态
```bash
sudo systemctl status nginx
```

### 重启Nginx
```bash
sudo systemctl restart nginx
```

### 查看Nginx错误日志
```bash
sudo tail -f /var/log/nginx/error.log
```

### 查看后端日志
```bash
tail -f /opt/resource-platform/logs/console.log
```

---

## 目录权限设置

确保nginx用户可以读取dist目录：

```bash
# 设置目录权限（只需要执行一次）
sudo chmod -R 755 /data/xiazai/frontend-admin/dist
sudo chmod -R 755 /data/xiazai/frontend-client/dist

# 如果nginx用户无法访问，可以设置所有者
sudo chown -R nginx:nginx /data/xiazai/frontend-admin/dist
sudo chown -R nginx:nginx /data/xiazai/frontend-client/dist
```

---

## 开发流程

### 本地开发
```bash
# 管理端
cd frontend-admin
npm run dev

# 客户端
cd frontend-client
npm run dev
```

### 部署到生产
```bash
# 管理端
cd /data/xiazai/frontend-admin
npm run build
# 完成！刷新浏览器即可

# 客户端
cd /data/xiazai/frontend-client
npm run build
# 完成！
```

---

## 故障排查

### 问题1：页面显示404
**检查：**
```bash
# 确认dist目录存在
ls -la /data/xiazai/frontend-admin/dist
ls -la /data/xiazai/frontend-client/dist

# 确认nginx配置正确
sudo nginx -t

# 查看nginx错误日志
sudo tail -f /var/log/nginx/error.log
```

### 问题2：权限被拒绝
**解决：**
```bash
# 设置正确的权限
sudo chmod -R 755 /data/xiazai/frontend-admin/dist
sudo chmod -R 755 /data/xiazai/frontend-client/dist
```

### 问题3：修改后没有生效
**解决：**
```bash
# 清除浏览器缓存
# 或使用 Ctrl+F5 强制刷新

# 确认dist目录已更新
ls -lt /data/xiazai/frontend-admin/dist/assets/
```

---

## 完整的更新流程示例

```bash
# 1. 更新代码（如果使用git）
cd /data/xiazai
git pull

# 2. 更新管理端
cd /data/xiazai/frontend-admin
npm install  # 如果package.json有变化
npm run build

# 3. 更新客户端
cd /data/xiazai/frontend-client
npm install  # 如果package.json有变化
npm run build

# 4. 更新后端
cd /data/xiazai/backend
mvn clean package -DskipTests
cd /opt/resource-platform
./stop.sh
cp /data/xiazai/backend/target/resource-platform-1.0.0.jar .
./start.sh

# 5. 完成！清除浏览器缓存即可
```

---

## 备份建议

虽然不需要复制文件了，但建议定期备份：

```bash
# 备份当前的dist（可选）
cp -r /data/xiazai/frontend-admin/dist /data/xiazai/frontend-admin/dist.backup.$(date +%Y%m%d)
cp -r /data/xiazai/frontend-client/dist /data/xiazai/frontend-client/dist.backup.$(date +%Y%m%d)
```

---

## 总结

新的部署方式的优势：

1. ✅ **简化流程** - 不需要复制文件到 `/var/www`
2. ✅ **节省时间** - 直接在项目目录构建
3. ✅ **减少错误** - 不需要手动设置权限
4. ✅ **便于管理** - 所有代码在一个目录
5. ✅ **快速更新** - 构建完成即生效

只需要记住：
- 前端更新：`cd /data/xiazai/frontend-admin && npm run build`
- 刷新浏览器即可！
