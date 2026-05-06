---
name: backend-dev
description: 处理本项目 backend 目录下的接口开发、事务处理、权限配置、SQL 排查、配置修改和后端代码审查时使用。聚焦 Spring Boot、MyBatis-Plus、JWT/Security、Redis 与统一返回结构，帮助快速定位 Controller、Service、Mapper 调用链并按项目约束落地修改。
---

# backend-dev

## 何时使用
当任务明确落在 `D:\个人\充电\练手项目\cms\backend\`，并且涉及以下场景时使用：

- 新增或修改后端接口
- 排查权限、认证、JWT、安全链问题
- 排查事务、多表写入、数据一致性问题
- 查找 SQL、Mapper、分页、筛选逻辑
- 调整 Spring Boot 配置、Redis、异常处理
- Review 后端改动风险

## 触发示例
以下请求适合优先加载本技能：

- “给资源管理加一个后端筛选接口”
- “查一下这个接口为什么 401 / 403”
- “看看这个批量操作事务有没有问题”
- “排查资源列表分页查询为什么慢”
- “帮我改 application.yml 相关配置”
- “Review 一下 backend 这次提交有没有风险”

以下情况通常不需要单独加载本技能：
- 纯前端页面改动
- 项目整体启动说明、截图说明、跨端总览问题（优先走项目总技能）
- 纯通用 Java 理论问答，且不依赖本项目实现

## 先看哪些文件
优先级建议如下：

1. 目标模块 `controller` / `service` / `service/impl` / `mapper`
2. `backend/src/main/resources/application.yml`
3. `backend/src/main/java/com/resource/platform/config/`
4. `backend/src/main/java/com/resource/platform/exception/GlobalExceptionHandler.java`
5. 相关 DTO / VO / entity

关键入口文件：
- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/com/resource/platform/config/SecurityConfig.java`
- `backend/src/main/java/com/resource/platform/exception/GlobalExceptionHandler.java`
- `backend/src/main/java/com/resource/platform/module/resource/controller/ResourceController.java`
- `backend/src/main/java/com/resource/platform/module/resource/service/impl/ResourceServiceImpl.java`

## 目录与调用链认知
后端主包为：`com.resource.platform`

常见结构：
- `config/`：安全、Swagger、Redis 等配置
- `exception/`：全局异常处理、自定义异常
- `common/`：统一返回体、通用常量/错误码
- `filter/`：JWT、限流、TraceId 等过滤器
- `module/<biz>/controller/`
- `module/<biz>/service/`
- `module/<biz>/service/impl/`
- `module/<biz>/mapper/`
- `module/<biz>/dto/`
- `module/<biz>/vo/`
- `module/<biz>/entity/`

默认调用链：
`Controller -> Service -> ServiceImpl -> Mapper -> DB`

做分析时，优先把这条链路串起来，再判断问题点在入参、权限、事务、SQL 还是数据。

## 本项目后端开发约定

### 返回结构
- 统一使用 `Result<T>`
- 分页统一关注 `PageResult<T>`
- 不要随手返回裸对象或随意改返回结构，先看现有接口模式

### 安全与权限
- 安全主入口：`SecurityConfig.java`
- 管理员接口重点关注 `@PreAuthorize` 与 `/api/*/admin/**` 路由规则
- JWT、限流、TraceId 过滤器已接入，排查 401/403/请求链问题时先看过滤链
- 不要绕过现有安全链去“临时放开”接口，除非方案已明确说明风险

### 事务与写操作
- 写操作、多表操作优先放在 `service/impl`
- 涉及事务时优先关注 `@Transactional(rollbackFor = Exception.class)`
- 批量操作、状态变更、关联表写入要重点检查是否会出现部分成功
- 事务里避免长耗时操作和不必要的外部调用

### SQL 与查询
- 使用参数化查询，不拼接 SQL
- MyBatis-Plus 常见做法：`LambdaQueryWrapper`、`LambdaUpdateWrapper`、`Page`
- 重点防 N+1、全表扫描、循环查库、循环远程调用
- 分页、筛选、排序逻辑先照已有模块模式写，不要自创风格

### 异常与日志
- 禁止 `System.out.println`
- 禁止 `printStackTrace()`
- 不要吞异常
- 优先复用 `BusinessException`、`BizErrorCode`、`GlobalExceptionHandler`
- 日志要带关键上下文，不要只打模糊报错

## 常见工作流

### 新增加接口
1. 找相邻模块的同类接口抄模式
2. 明确 URL、方法类型、权限、入参 DTO、返回 VO
3. 在 `controller` 挂接口
4. 在 `service` / `serviceImpl` 实现业务
5. 在 `mapper` / SQL 层补查询或写入
6. 检查事务、校验、错误码、日志
7. 说明影响文件和验证方式

### 修改现有接口
1. 从 `controller` 入口定位到 `serviceImpl`
2. 看 DTO / VO / entity 是否联动
3. 看是否影响前端字段契约
4. 改完说明调用链影响范围
5. 如果未做联调，要明确写“未联调验证”

### 排查权限问题
1. 看接口路径是否命中 `SecurityConfig`
2. 看是否属于 `/admin/**` 路由
3. 看是否缺 token、角色或白名单配置
4. 如与过滤器链相关，再看 `filter/` 下 JWT / 限流 / TraceId
5. 先解释为何被拦，再说修法

### 排查事务 / 数据一致性问题
1. 找写入口是否在 `serviceImpl`
2. 看是否有多表写入或状态联动
3. 看是否缺 `@Transactional`
4. 看异常是否被吞掉导致不回滚
5. 看批量操作是否可能部分成功

### 排查查询 / SQL 问题
1. 先找 `mapper` 与 wrapper 条件
2. 看筛选、分页、排序字段是否正确
3. 看是否存在循环查库
4. 看返回 VO 转换是否引入额外查询
5. 必要时先给出可能瓶颈点，再决定是否改写查询

### 改配置
1. 先确认是 `application.yml`、环境配置还是 `config/*.java`
2. 说明配置影响范围
3. 涉及端口、数据库、Redis、鉴权时要特别谨慎
4. 改完说明是否需要重启以及影响哪些服务

## 本地运行与验证
常用方式：
- `mvn spring-boot:run`
- `mvn clean package -DskipTests`

本地依赖：
- MySQL
- Redis
- 正确的 `application.yml` 数据源与 Redis 配置

验证建议：
- 能做接口级验证，就别只说“理论上可行”
- 若未实际启动或未联调，要明确标注未验证部分
- 汇报时尽量说明：改了哪些文件、影响哪个接口、验证到哪一步

## 硬规则清单
- 先分析，再动手
- 优先最小改动
- 不拼接 SQL
- 不吞异常
- 不用 `System.out` / `printStackTrace`
- 写操作优先检查事务
- 管理接口优先检查权限
- 说明影响到的 Controller / Service / Mapper / DTO / VO / Config

## 输出风格约定
- 先给结论，再给调用链和依据
- 先说根因，再说修法
- 改代码前先给方案
- 有风险直接指出
- 验证不到就明确说“未验证”

## 和项目总技能的边界
本技能不重复讲：
- 项目整体总览
- Docker / 部署总说明
- 截图规范总说明
- 客户前台与后台前端的页面工作流

这些内容交给 `resource-download-platform-project` 处理；本技能只管 `backend/` 内的实现与排查。
