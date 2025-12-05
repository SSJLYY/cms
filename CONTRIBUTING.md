# 🤝 贡献指南

感谢你考虑为资源下载平台做出贡献！

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发流程](#开发流程)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [Pull Request流程](#pull-request流程)
- [问题反馈](#问题反馈)

---

## 行为准则

### 我们的承诺

为了营造一个开放和友好的环境，我们承诺：

- 使用友好和包容的语言
- 尊重不同的观点和经验
- 优雅地接受建设性批评
- 关注对社区最有利的事情
- 对其他社区成员表示同理心

### 不可接受的行为

- 使用性化的语言或图像
- 人身攻击或侮辱性评论
- 公开或私下骚扰
- 未经许可发布他人的私人信息
- 其他不道德或不专业的行为

---

## 如何贡献

### 贡献类型

我们欢迎以下类型的贡献：

#### 🐛 Bug修复
- 修复已知的Bug
- 改进错误处理
- 提高系统稳定性

#### ✨ 新功能
- 添加新的功能模块
- 改进现有功能
- 提供新的API接口

#### 📝 文档改进
- 修正文档错误
- 添加使用示例
- 翻译文档
- 改进API文档

#### 🎨 UI/UX改进
- 改进界面设计
- 优化用户体验
- 添加动画效果
- 响应式优化

#### ⚡ 性能优化
- 提高代码性能
- 优化数据库查询
- 减少资源占用
- 改进加载速度

#### 🧪 测试
- 添加单元测试
- 添加集成测试
- 改进测试覆盖率

---

## 开发流程

### 1. Fork项目

点击GitHub页面右上角的"Fork"按钮，将项目复制到你的账号下。

### 2. 克隆仓库

```bash
git clone https://github.com/your-username/resource-download-platform.git
cd resource-download-platform
```

### 3. 添加上游仓库

```bash
git remote add upstream https://github.com/original-owner/resource-download-platform.git
```

### 4. 创建分支

```bash
# 从main分支创建新分支
git checkout -b feature/your-feature-name

# 或修复bug
git checkout -b fix/your-bug-fix
```

### 5. 开发和测试

- 编写代码
- 添加测试
- 运行测试确保通过
- 更新文档

### 6. 提交更改

```bash
git add .
git commit -m "feat: add amazing feature"
```

### 7. 同步上游更改

```bash
git fetch upstream
git rebase upstream/main
```

### 8. 推送到你的仓库

```bash
git push origin feature/your-feature-name
```

### 9. 创建Pull Request

在GitHub上创建Pull Request，描述你的更改。

---

## 代码规范

### Java代码规范

遵循阿里巴巴Java开发规范：

#### 命名规范
```java
// 类名：大驼峰
public class ResourceController { }

// 方法名：小驼峰
public void getResourceList() { }

// 常量：全大写，下划线分隔
public static final String API_BASE_URL = "http://api.example.com";

// 变量：小驼峰
private String userName;
```

#### 注释规范
```java
/**
 * 获取资源列表
 * 
 * @param queryDTO 查询条件
 * @return 资源列表
 */
public Result<List<ResourceVO>> getResourceList(ResourceQueryDTO queryDTO) {
    // 实现代码
}
```

#### 代码格式
- 缩进：4个空格
- 每行最大长度：120字符
- 大括号不换行

### Vue代码规范

遵循Vue 3官方风格指南：

#### 组件命名
```vue
<!-- 多词组件名 -->
<template>
  <ResourceCard />
</template>

<script setup>
// 组件文件名：PascalCase
// ResourceCard.vue
</script>
```

#### 代码格式
```vue
<template>
  <!-- 2个空格缩进 -->
  <div class="container">
    <h1>{{ title }}</h1>
  </div>
</template>

<script setup>
// 使用组合式API
import { ref, computed } from 'vue'

const title = ref('Hello')
const message = computed(() => `${title.value} World`)
</script>

<style scoped>
/* 使用scoped样式 */
.container {
  padding: 20px;
}
</style>
```

### SQL规范

```sql
-- 表名：小写，下划线分隔
CREATE TABLE resource_download (
    -- 字段名：小写，下划线分隔
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    download_time DATETIME NOT NULL,
    -- 索引命名：idx_表名_字段名
    INDEX idx_resource_download_resource_id (resource_id)
);
```

---

## 提交规范

使用约定式提交（Conventional Commits）：

### 提交格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type类型

- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式（不影响代码运行）
- `refactor`: 重构（既不是新功能也不是Bug修复）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动
- `revert`: 回滚提交

### 示例

```bash
# 新功能
git commit -m "feat(resource): add image carousel feature"

# Bug修复
git commit -m "fix(auth): resolve JWT token expiration issue"

# 文档更新
git commit -m "docs(readme): update installation guide"

# 性能优化
git commit -m "perf(api): optimize database query performance"

# 重构
git commit -m "refactor(service): simplify resource service logic"
```

### 详细提交示例

```bash
git commit -m "feat(resource): add image carousel feature

- Add left/right navigation buttons
- Add image indicators
- Support click to view full image
- Add modal for large image view

Closes #123"
```

---

## Pull Request流程

### 1. 创建PR前检查

- [ ] 代码遵循项目规范
- [ ] 所有测试通过
- [ ] 文档已更新
- [ ] 提交信息清晰
- [ ] 没有合并冲突

### 2. PR标题格式

```
<type>: <description>
```

示例：
- `feat: add user authentication module`
- `fix: resolve image upload issue`
- `docs: update API documentation`

### 3. PR描述模板

```markdown
## 变更类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 文档更新
- [ ] 性能优化
- [ ] 代码重构

## 变更说明
简要描述你的更改...

## 相关Issue
Closes #123

## 测试
描述如何测试你的更改...

## 截图（如适用）
添加截图展示UI变更...

## 检查清单
- [ ] 代码遵循项目规范
- [ ] 已添加/更新测试
- [ ] 所有测试通过
- [ ] 文档已更新
- [ ] 无合并冲突
```

### 4. Code Review

- 至少需要1个维护者的批准
- 解决所有评审意见
- 保持讨论友好和建设性

### 5. 合并

- 维护者会合并你的PR
- 合并后会自动关闭相关Issue

---

## 问题反馈

### 报告Bug

使用以下模板报告Bug：

```markdown
**Bug描述**
清晰简洁地描述Bug...

**复现步骤**
1. 访问 '...'
2. 点击 '...'
3. 滚动到 '...'
4. 看到错误

**期望行为**
描述你期望发生什么...

**实际行为**
描述实际发生了什么...

**截图**
如果适用，添加截图...

**环境信息**
- OS: [e.g. Windows 10]
- Browser: [e.g. Chrome 90]
- Version: [e.g. v1.0.0]

**附加信息**
添加其他相关信息...
```

### 功能建议

使用以下模板提出功能建议：

```markdown
**功能描述**
清晰简洁地描述你想要的功能...

**问题背景**
这个功能解决什么问题？

**建议方案**
描述你希望如何实现...

**替代方案**
描述你考虑过的其他方案...

**附加信息**
添加其他相关信息、截图或示例...
```

---

## 开发环境设置

### 后端开发

```bash
cd backend

# 安装依赖
mvn clean install

# 运行测试
mvn test

# 启动开发服务器
mvn spring-boot:run
```

### 前端开发

```bash
# 客户前台
cd frontend-client
npm install
npm run dev

# 管理后台
cd frontend-admin
npm install
npm run dev
```

### 数据库设置

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE resource_platform"

# 导入初始化脚本
mysql -u root -p resource_platform < backend/src/main/resources/init-database.sql
```

---

## 测试指南

### 运行测试

```bash
# 后端测试
cd backend
mvn test

# 前端测试（如果有）
cd frontend-client
npm run test
```

### 编写测试

```java
// 单元测试示例
@Test
public void testGetResourceList() {
    // Given
    ResourceQueryDTO queryDTO = new ResourceQueryDTO();
    
    // When
    Result<List<ResourceVO>> result = resourceService.getResourceList(queryDTO);
    
    // Then
    assertNotNull(result);
    assertTrue(result.isSuccess());
}
```

---

## 文档贡献

### 文档类型

- README.md - 项目主文档
- API文档 - 接口说明
- 用户指南 - 使用教程
- 开发文档 - 技术细节

### 文档规范

- 使用Markdown格式
- 添加目录
- 使用清晰的标题
- 添加代码示例
- 包含截图（如适用）

---

## 版本发布

### 版本号规范

遵循语义化版本（Semantic Versioning）：

- **主版本号**：不兼容的API修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

示例：`v1.2.3`

### 发布流程

1. 更新版本号
2. 更新CHANGELOG.md
3. 创建Git标签
4. 发布Release

---

## 获取帮助

如果你有任何问题：

1. 查看[文档](README.md)
2. 搜索[已有Issue](https://github.com/your-username/resource-download-platform/issues)
3. 创建新Issue
4. 参与[Discussions](https://github.com/your-username/resource-download-platform/discussions)

---

## 致谢

感谢所有为本项目做出贡献的开发者！

---

**最后更新**: 2024-12-05
