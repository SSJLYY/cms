---
name: frontend-admin-dev
description: 处理本项目 frontend-admin 目录下的后台页面开发、接口对接、筛选分页、弹窗表单、登录态处理和前端代码审查时使用。聚焦 Vue 3、Vite、Element Plus、axios 封装与后台管理页面模式，帮助快速定位 view、api、router 并按项目现有风格稳定改动。
---

# frontend-admin-dev

## 何时使用
当任务明确落在 `D:\个人\充电\练手项目\cms\frontend-admin\`，并且涉及以下场景时使用：

- 修改后台管理页面
- 新增加筛选条件、表格列、表单字段、操作按钮
- 对接或调整后台管理接口
- 增加或修改弹窗、分页、状态切换、批量操作入口
- 排查登录态、路由跳转、401/接口报错
- Review 后台前端改动风险

## 触发示例
以下请求适合优先加载本技能：

- “给后台资源列表加一个筛选条件”
- “帮我在反馈页面加个处理人字段”
- “排查管理后台为什么 401 自动跳登录”
- “把这个后台弹窗表单接到新接口上”
- “看看前端分页参数为什么没生效”
- “Review 一下 frontend-admin 这次改动有没有坑”

以下情况通常不需要单独加载本技能：
- 纯后端接口实现
- 项目整体启动、部署、截图总说明（优先走项目总技能）
- 客户前台 `frontend-client` 的页面改动

## 先看哪些文件
优先级建议如下：

1. 目标页面 `src/views/*.vue`
2. 对应 `src/api/` 或 `src/api/modules/` 接口文件
3. `src/router/index.js`
4. `src/main.js`
5. 如涉及登录态或请求失败，再看 `src/api/index.js` 与 `src/api/request.js`

关键入口文件：
- `frontend-admin/src/main.js`
- `frontend-admin/src/router/index.js`
- `frontend-admin/src/api/index.js`
- `frontend-admin/src/api/request.js`
- `frontend-admin/src/views/Feedback.vue`

## 目录与页面组织认知
当前后台前端主要结构：
- `src/main.js`：注册 Vue、Pinia、Element Plus、router
- `src/router/index.js`：后台路由与登录守卫
- `src/api/index.js`：axios 封装之一
- `src/api/request.js`：axios 封装之二
- `src/api/modules/`：按业务拆分接口
- `src/views/`：后台页面主体

路由组织特点：
- `/login` 单独页面
- `/` 下挂 `Layout`，子路由平铺各业务页面
- 未登录时通过 `localStorage.getItem('token')` 在前置守卫中跳 `/login`

状态管理特点：
- 虽然注册了 Pinia，但很多页面状态仍然直接写在页面内的 `ref` / `reactive`
- 做改动时先看当前页面是不是页面内自管状态，不要强行抽 Pinia

## 本项目后台前端开发约定

### 接口封装与返回结构
- 请求成功通常依赖 `res.code === 200`
- 业务数据通常从 `res.data` 取
- 401 常见处理是清 token 并跳转 `/login`
- 项目里存在两个 axios 封装：`src/api/index.js` 和 `src/api/request.js`
- 改接口前先看当前页面实际依赖的是哪一个封装，不要混着改

### 页面常见写法
常见页面模式：
- 查询区：`queryForm` / `searchForm`
- 列表区：`tableData` / `list` + `v-loading`
- 分页区：`page` / `pageSize` / `total`
- 弹窗区：`dialogVisible` / `detailVisible` / `editVisible`
- 表单区：`formData` + `rules`
- 反馈区：`ElMessage`、`ElMessageBox.confirm`

优先复用相邻页面的现有模式，不要强行引入完全不同的组织方式。

### 组件与交互约束
- 异步处理优先 `try/catch/finally`
- `finally` 中释放 loading
- 列表渲染必须有 `:key`
- 弹窗优先 `destroy-on-close`
- 错误提示不要用 `alert`，优先 `ElMessage`
- 状态值展示优先走标签映射函数，不要把魔法字符串散落在模板里
- 页面改动尽量和现有 Element Plus 风格保持一致

## 常见工作流

### 加列表筛选
1. 先看页面现有 `queryForm` 和 `handleQuery` / `loadData`
2. 确认后端接口是否已支持该筛选字段
3. 在筛选区域补控件
4. 在查询参数中补字段
5. 检查分页切换和重置逻辑是否需要同步
6. 说明涉及的 view / api 文件

### 加表单字段
1. 找到表单模型 `formData`
2. 补默认值、校验规则、表单项 UI
3. 检查详情弹窗、编辑弹窗、提交方法是否都要联动
4. 看接口 DTO/后端字段是否一致
5. 如未联调，明确标注未联调验证

### 接管理接口
1. 先看该页面当前用的是哪个 api 模块和 axios 封装
2. 补接口函数
3. 在页面方法里调用并处理 loading / success / error
4. 检查 `res.data` 解构是否符合现有返回结构
5. 如是分页接口，确认 `total/list` 字段映射

### 加或改弹窗
1. 确认当前页面使用的是详情、编辑还是状态类弹窗
2. 补 `visible` 状态和当前行数据
3. 检查关闭重置逻辑
4. 表单类弹窗要补校验与提交后刷新列表
5. 需要二次确认的操作优先 `ElMessageBox.confirm`

### 处理 loading / 空态 / 错误态
1. 请求前置 `loading.value = true`
2. 成功后更新列表或详情数据
3. 失败时给用户友好提示
4. `finally` 中关 loading
5. 未返回数据时要考虑空表格或空内容展示，不要只处理成功路径

### 排查登录态 / 401
1. 先看 `router/index.js` 的守卫
2. 再看 `api/index.js` / `api/request.js` 的 401 处理
3. 查 token 是否写入 `localStorage`
4. 查请求头是否带 `Authorization`
5. 先解释是前端未带 token、token 失效，还是后端鉴权拒绝

## 本项目已知坑点
- 存在两个 axios 封装，容易造成超时、错误处理、拦截器行为不一致
- 分页字段可能存在 `page/pageSize` 与 `pageNum/size` 混用，改动前先核实现状
- 页面展示状态值与后端状态码不一定完全同名，先看当前映射函数和实际接口数据
- 某些页面状态直接写在单文件组件里，别在没有必要时强拆成全局状态
- 新增字段时，不要只改页面表单；常常还要同步筛选区、详情弹窗、表格列、提交参数

## 本地开发与验证
常用方式：
- `npm install`
- `npm run dev`
- `npm run build`

验证建议：
- 能点页面验证就不要只做静态改动说明
- 至少说明改了哪些 view / api / router 文件
- 若未实际启动页面或未联调接口，要明确写“未验证”

## 硬规则清单
- 先分析，再动手
- 优先最小改动
- 优先复用现有页面模式
- 异步处理要管 loading 和错误提示
- 不要混乱切换两个 axios 封装
- 改动前确认前后端字段契约
- 说明涉及的 view / api / router 文件

## 输出风格约定
- 先说页面改动点，再说接口改动点
- 先给结论，再给依据
- 改代码前先给方案
- 未联调、未点页面、未构建就明确说“未验证”

## 和项目总技能的边界
本技能不重复讲：
- 项目整体总览
- Docker / 部署总说明
- 客户前台 `frontend-client` 页面工作流
- 截图规范总说明

这些内容交给 `resource-download-platform-project`；本技能只管 `frontend-admin/` 内的页面开发与排查。
