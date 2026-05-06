---
name: 资源下载平台项目助手
description: 处理本项目的开发、排查、代码审查、文档更新时使用。提供项目架构、目录入口、启动方式、修改前分析要求、后端与前端审查规范、批量操作注意事项与截图约定，帮助代理快速进入正确上下文。
---

# 资源下载平台项目助手

## 何时使用
当任务与当前工作区 `D:\个人\充电\练手项目\cms` 有关，并且涉及以下任一场景时使用：

- 阅读或修改 `backend`、`frontend-admin`、`frontend-client` 代码
- 排查启动、构建、接口、页面、部署问题
- 进行代码审查、规范检查、批量操作相关修改
- 更新项目文档、截图说明、部署说明

## 触发示例
以下请求应优先加载这个技能：

- “帮我看下这个项目怎么启动”
- “分析一下后台资源管理为什么保存失败”
- “给 frontend-admin 的某个页面加个筛选条件”
- “Review 一下 backend 这次改动有没有风险”
- “补充这个项目的部署说明或截图文档”
- “排查 Docker 启动后为什么前端打不开”

以下情况通常不需要单独加载本技能：
- 与当前项目无关的纯通用知识问答
- 独立的小片段代码题，且不依赖本项目上下文
- 单纯做文件格式转换，且不涉及项目内容理解

## 项目概览
这是一个基于 Spring Boot + Vue 3 + MySQL + Redis 的前后端分离资源下载平台。

核心子系统：
- `backend/`：Spring Boot 2.7.18 后端，Maven 构建，默认端口 `9090`
- `frontend-client/`：Vue 3 + Vite 客户前台，面向资源站浏览与下载
- `frontend-admin/`：Vue 3 + Vite 管理后台，面向资源、分类、图片、日志、反馈、统计等管理
- `docker-compose.yml`：一键拉起 MySQL、Redis、后端、客户前台、管理后台

配套文档：
- `README.md`：完整项目说明、功能、启动方式、默认账号
- `PROJECT_SUMMARY.md`：架构、模块、接口与规模概览
- `QUICKSTART.md`：快速启动指南
- `FILE_LIST.md`：关键文件导航
- `CODE_REVIEW_STANDARDS.md` / `CODE_REVIEW_CHECKLIST.md`：审查规范
- `BATCH_OPERATIONS_GUIDE.md`：批量操作设计与注意事项
- `SCREENSHOTS.md`：截图说明与界面清单

## 关键目录与入口

### 后端
- `backend/pom.xml`：依赖与构建配置
- `backend/src/main/resources/application.yml`：主配置
- `backend/src/main/resources/init-database.sql`：数据库初始化脚本
- `backend/src/main/java/com/resource/platform/controller/`：控制器层
- `backend/src/main/java/com/resource/platform/service/`：服务层
- `backend/src/main/java/com/resource/platform/mapper/`：数据访问层

### 客户前台
- `frontend-client/package.json`
- `frontend-client/vite.config.js`
- `frontend-client/src/views/Home.vue`
- `frontend-client/src/views/ResourceDetail.vue`

### 管理后台
- `frontend-admin/package.json`
- `frontend-admin/vite.config.js`
- `frontend-admin/src/views/`
- `frontend-admin/src/api/`

### 其他关键入口
- `start.sh` / `start.bat`：一键启动
- `stop.sh` / `stop.bat`：一键停止
- `screenshots/`：界面截图目录

## 工作方式

### 第一原则
先分析，再动手。

处理任何改动前，优先按下面顺序推进：
1. 先读 `README.md`、`PROJECT_SUMMARY.md`、相关模块代码
2. 明确修改点、影响范围、验证方式
3. 先给方案，再开始改代码
4. 改动后做最小必要验证，并明确说明验证结果与未验证部分

### 启动与运行
优先级：**Docker > 本地开发**

#### Docker 方式
常用命令：
- `docker-compose up -d`
- `docker-compose logs -f`
- `docker-compose down`

默认访问：
- 客户前台：`http://localhost:8080`
- 管理后台：`http://localhost:8081`
- API 文档：`http://localhost:9090/doc.html`

#### 本地开发方式
后端常用：
- `mvn spring-boot:run`
- `mvn clean package -DskipTests`

前端常用：
- `npm install`
- `npm run dev`
- `npm run build`

本地依赖：
- MySQL 8.0
- Redis 6.0+
- Java 8+
- Maven 3.6+
- Node.js 16+

默认账号：
- 用户名：`admin`
- 密码：`admin123`

## 修改代码时的项目约束

### 通用约束
- 先分析方案，再实施
- 优先最小改动，避免无关重构
- 修改后说明影响范围
- 如果涉及 UI 改动，参考 `SCREENSHOTS.md`，必要时补截图
- 测试体系较弱时，不要假装“已充分验证”；应明确写出实际验证范围

### Java / 后端约束
- 禁止 `System.out.println`
- 禁止 `printStackTrace()`
- 不要吞异常
- 写操作、多表操作重点检查事务边界
- 涉及事务时优先关注 `@Transactional(rollbackFor = Exception.class)`
- 不要拼接 SQL，避免注入风险
- 敏感接口要关注权限校验
- 用户输入要做长度、格式、范围校验
- 注意 N+1 查询、全表扫描、循环内远程调用

### Vue / 前端约束
- 异步操作必须有错误处理
- 不使用 `alert` 做错误提示
- `finally` 中释放 loading 状态
- 列表渲染必须有 `:key`
- 组件名使用 PascalCase
- 样式优先 `scoped`
- 高频操作考虑防抖/节流
- 定时器、监听器、异步副作用要注意释放

## 批量操作相关注意事项
参考 `BATCH_OPERATIONS_GUIDE.md`，处理批量操作时重点检查：

- 必须有确认弹窗
- 必须有权限控制
- 要反馈实际影响数量
- 尽量避免“部分成功、部分失败”带来的状态不一致
- 涉及删除时优先确认是否为软删除
- 优先批量 SQL / 批量更新，不要循环逐条低效操作

## 代码审查时的关注点
优先级按以下方式表达：
- `阻塞`：必须修复，否则不应合并
- `建议`：应该修复，但不一定阻塞
- `可选`：锦上添花

审查时重点看：
- 安全问题：SQL 注入、权限缺失、敏感信息泄露
- 数据一致性：事务、状态流转、并发风险
- 异常处理：空 catch、宽泛捕获、日志缺失
- 性能问题：N+1、全表扫描、不必要循环
- 前端稳定性：加载态、空态、错误态、销毁清理

## 建议的工作流

### 需求实现
1. 读文档和目标模块
2. 梳理当前实现与改动点
3. 输出方案
4. 实施最小改动
5. 运行最小必要验证
6. 汇报改动文件、风险点、验证结果

### Bug 排查
1. 先定位入口文件和调用链
2. 明确是前端、后端、配置还是数据问题
3. 先解释根因，再给修复方案
4. 修复后说明如何复现与验证

### 新功能开发
1. 先确认需求落点属于 `backend`、`frontend-admin` 还是 `frontend-client`
2. 读取相邻模块实现，优先复用现有模式
3. 先给实现方案：涉及文件、接口、数据结构、验证方式
4. 实施时优先最小闭环，避免顺手大改
5. 如涉及接口联调，明确前后端字段和返回结构
6. 完成后说明新增能力、影响范围、验证结果

### 文档与截图更新
1. 先确认是补 README、部署文档还是截图文档
2. 文档内容要与实际实现一致，避免写成“想当然”
3. UI 变化优先同步 `SCREENSHOTS.md` 或 `screenshots/` 相关说明
4. 如果截图未补齐，要明确指出缺口，不要假装已完成

### 代码审查
1. 先看风险最高的文件
2. 先提阻塞问题
3. 每条意见尽量给出原因和建议
4. 不做纯情绪化评价

## 输出风格约定
- 先给结论，再给依据
- 能列点就别写大段废话
- 改代码前先给方案
- 有风险就直接指出，并给替代路径
- 验证不到的地方要明确说“未验证”

## 参考文件
- `D:\个人\充电\练手项目\cms\README.md`
- `D:\个人\充电\练手项目\cms\PROJECT_SUMMARY.md`
- `D:\个人\充电\练手项目\cms\QUICKSTART.md`
- `D:\个人\充电\练手项目\cms\FILE_LIST.md`
- `D:\个人\充电\练手项目\cms\CONTRIBUTING.md`
- `D:\个人\充电\练手项目\cms\CODE_REVIEW_STANDARDS.md`
- `D:\个人\充电\练手项目\cms\CODE_REVIEW_CHECKLIST.md`
- `D:\个人\充电\练手项目\cms\BATCH_OPERATIONS_GUIDE.md`
- `D:\个人\充电\练手项目\cms\SCREENSHOTS.md`

## 使用提醒
这个技能的目标不是替代具体阅读，而是提供本项目的稳定工作上下文。遇到具体任务时，仍应继续读取目标模块源码与配置，避免只靠文档猜实现。
