# 代码审查检查清单（快速版）

> 每次PR审查前的快速检查清单

---

## 🚀 使用说明

1. **开发者提交PR前** - 完成所有✅检查项
2. **审查者审查PR时** - 按检查项逐一验证
3. **优先级** - 🔴=阻塞合并 🟡=建议修复 💭=可选优化

---

## 🔴 阻缩问题（必须修复）

### Java后端

#### 安全性
- [ ] 所有SQL使用参数化查询，禁止字符串拼接
- [ ] 敏感接口添加`@PreAuthorize`权限验证
- [ ] 用户输入必须验证（长度、格式、范围）
- [ ] 密码/密钥等敏感信息不记录日志
- [ ] 文件上传验证类型和大小
- [ ] 返回给前端的数据不包含敏感字段

#### 异常处理
- [ ] 不捕获`Exception`，捕获具体异常
- [ ] 不使用`printStackTrace()`
- [ ] 不吞掉异常（空catch块）
- [ ] 事务方法指定`rollbackFor(Exception.class)`
- [ ] 抛出业务异常使用自定义类型

#### 空指针安全
- [ ] 使用`StringUtils.hasText()`等工具类
- [ ] 可能返回null的方法调用前检查
- [ ] 集合返回空集合而不是null
- [ ] Optional处理正确使用

#### 资源管理
- [ ] IO流使用try-with-resources
- [ ] 数据库连接、文件句柄及时关闭
- [ ] 线程池、定时器正确关闭
- [ ] 缓存设置过期时间
- [ ] 大文件上传限制大小

#### 事务管理
- [ ] 多表操作必须有`@Transactional`
- [ ] 事务指定`rollbackFor(Exception.class)`
- [ ] 查询方法不需要事务
- [ ] 避免大事务（拆分或异步）
- [ ] 事务内不调用外部服务

#### 并发安全
- [ ] 共享变量使用`Atomic`类或加锁
- [ ] 使用线程安全的集合
- [ ] 定时器使用`AtomicBoolean`停止标志
- [ ] 避免在锁中执行耗时操作

#### 数据一致性
- [ ] 批量操作避免部分成功
- [ ] 状态转换使用枚举
- [ ] 数据库操作有异常处理
- [ ] 修改前校验数据存在性

### Vue前端

#### 错误处理
- [ ] 异步操作必须try-catch
- [ ] 错误信息友好展示（不用alert）
- [ ] finally释放loading状态
- [ ] 网络错误有重试或提示

#### 性能
- [ ] 列表渲染必须有`:key`
- [ ] 图片使用`loading="lazy"`
- [ ] 大列表使用虚拟滚动
- [ ] dialog使用`destroy-on-close`
- [ ] 避免不必要的响应式数据
- [ ] 频繁操作使用防抖/节流

#### 组件规范
- [ ] 组件名使用PascalCase
- [ ] 使用组合式API（setup）
- [ ] Props定义清晰（type, required）
- [ ] 事件命名使用kebab-case
- [ ] 样式使用scoped

#### 响应式
- [ ] 基本类型用ref
- [ ] 对象用reactive
- [ ] 解构props用toRefs
- [ ] 不直接修改props
- [ ] computed正确使用

---

## 🟡 建议问题（应该修复）

### Java后端

#### 代码质量
- [ ] 命名清晰有意义（避免缩写）
- [ ] 常量定义，避免魔法数字
- [ ] 重复代码提取公共方法
- [ ] 函数长度<50行
- [ ] 嵌套层级<3层
- [ ] 单一职责原则

#### 日志规范
- [ ] 使用SLF4J而不是System.out
- [ ] 异常日志包含异常对象
- [ ] 使用占位符而不是字符串拼接
- [ ] DEBUG记录详细信息
- [ ] ERROR记录错误和异常

#### 性能优化
- [ ] 避免N+1查询
- [ ] 使用批量查询代替循环
- [ ] 数据库查询有索引
- [ ] 避免全表扫描
- [ ] 缓存合理使用
- [ ] 避免循环中调用远程服务

#### 代码复杂度
- [ ] 圈复杂度<10
- [ ] 使用早期返回减少嵌套
- [ ] 复杂逻辑提取私有方法
- [ ] 使用策略模式代替多个if-else

#### 可维护性
- [ ] 关键业务逻辑有注释
- [ ] 复杂算法有说明
- [ ] Javadoc完整（公开方法）
- [ ] 更新相关文档

### Vue前端

#### 代码风格
- [ ] 变量命名有意义
- [ ] 函数命名动词开头（handle/load/update）
- [ ] 常量定义在文件顶部
- [ ] 注释解释复杂逻辑

#### 代码组织
- [ ] 组件拆分合理
- [ ] 逻辑复用使用composable
- [ ] API调用统一管理
- [ ] 工具函数提取

#### 用户体验
- [ ] 加载状态提示
- [ ] 空状态提示
- [ ] 错误状态提示
- [ ] 操作反馈及时

#### 代码质量
- [ ] Props验证完整
- [ ] 事件命名规范
- [ ] 避免组件间直接引用
- [ ] 使用provide/inject代替props drilling

---

## 💭 挑剔问题（可选优化）

### 通用

#### 代码风格
- [ ] 缩进一致性（由linter处理）
- [ ] 格式化统一（由linter处理）
- [ ] 命名风格微调

#### 文档
- [ ] Javadoc补充
- [ ] README更新
- [ ] 注释优化

#### 重构
- [ ] 使用更优雅的API
- [ ] 代码简化
- [ ] 设计模式应用

---

## 📋 PR提交前检查

### 自动化检查
- [ ] 代码格式化通过（`mvn spotless:apply`, `npm run lint`）
- [ ] 单元测试全部通过（`mvn test`, `npm run test`）
- [ ] 静态代码分析通过（`mvn checkstyle:check`, `npm run type-check`）
- [ ] 测试覆盖率达标（>80%）

### 代码检查
- [ ] 新代码符合项目规范
- [ ] 添加了必要的测试
- [ ] 更新了相关文档
- [ ] 提交信息符合规范
- [ ] 没有合并冲突
- [ ] 没有遗留的TODO/FIXME

---

## 🎯 快速审查流程

### 第一步：自动化检查（5分钟）
```
1. 检查CI/CD是否全部通过
2. 查看测试覆盖率报告
3. 检查代码重复率
```

### 第二步：关键路径审查（15分钟）
```
1. 查看主文件修改
2. 检查关键业务逻辑
3. 验证安全相关问题
4. 检查异常处理
```

### 第三步：详细审查（30分钟）
```
1. 逐文件审查
2. 检查代码质量
3. 验证性能问题
4. 检查测试覆盖
```

### 第四步：给出反馈
```
1. 🔴 阻缩问题：必须修复
2. 🟡 建议问题：讨论或修复
3. 💭 挑剔问题：可选优化
4. 肯定好的代码
```

---

## 📊 审查统计

### 个人审查记录

| 日期 | PR编号 | 类型 | 阻缩 | 建议 | 挑剔 | 通过 |
|------|--------|------|------|------|------|------|
|      |        |      |      |      |      |      |

### 团队审查统计

| 指标 | 本周 | 本月 | 目标 |
|------|------|------|------|
| PR数量 |      |      | - |
| 平均响应时间 |      |      | <24h |
| 平均审查时间 |      |      | <48h |
| 阻缩问题数 |      |      | - |
| 测试覆盖率 |      |      | >80% |

---

## 💡 审查技巧

### 1. 高效审查
```
✅ 关注核心逻辑
✅ 检查安全漏洞
✅ 验证异常处理
✅ 查看性能问题
❌ 不纠结格式问题（交给linter）
❌ 不纠结个人偏好
```

### 2. 建设性反馈
```
❌ "这里写的不好"
✅ "建议使用XX，因为XX"

❌ "要改成这样"
✅ "考虑使用XX，这样可以XX"

❌ "不对，错了"
✅ "这里可能有XX问题，建议检查一下"
```

### 3. 优先级判断
```
🔴 安全漏洞 > 数据损坏 > 功能缺陷 > 性能问题 > 代码质量
🟡 优先修复阻塞问题
💭 挑剔问题可以延后
```

---

## 🚨 常见反模式

### Java后端
```java
// ❌ 反模式1：SQL注入
String sql = "SELECT * FROM user WHERE name = '" + name + "'";

// ✅ 正确：参数化查询
@Select("SELECT * FROM user WHERE name = #{name}")
User findByName(@Param("name") String name);

// ❌ 反模式2：吞掉异常
try {
    doSomething();
} catch (Exception e) {
    // 什么都不做
}

// ✅ 正确：记录日志或抛出
try {
    doSomething();
} catch (Exception e) {
    log.error("操作失败", e);
    throw new BusinessException(BizErrorCode.OPERATION_FAILED, e);
}

// ❌ 反模式3：N+1查询
for (Resource r : resources) {
    Category c = categoryMapper.selectById(r.getCategoryId());
}

// ✅ 正确：批量查询
Map<Long, Category> map = categoryMapper.selectBatchIds(ids).stream()
    .collect(Collectors.toMap(Category::getId, Function.identity()));

// ❌ 反模式4：资源泄漏
InputStream is = file.getInputStream();
// 忘记关闭

// ✅ 正确：try-with-resources
try (InputStream is = file.getInputStream()) {
    // 使用
}
```

### Vue前端
```javascript
// ❌ 反模式1：v-for没有key
<div v-for="item in items">{{ item.name }}</div>

// ✅ 正确：添加key
<div v-for="item in items" :key="item.id">{{ item.name }}</div>

// ❌ 反模式2：直接修改props
props.data.value = newValue;

// ✅ 正确：emit事件
emit('update', newValue);

// ❌ 反模式3：没有错误处理
const loadData = async () => {
  loading.value = true
  const res = await getData()
  data.value = res.data
  loading.value = false
}

// ✅ 正确：try-catch
const loadData = async () => {
  try {
    loading.value = true
    const res = await getData()
    data.value = res.data
  } catch (error) {
    console.error('加载失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}
```

---

## 📝 审查模板

### PR描述模板
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
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试通过
- [ ] 测试覆盖率达标

## 检查清单
- [ ] 代码符合项目规范
- [ ] 添加/更新了测试
- [ ] 更新了文档
- [ ] 没有合并冲突
```

### 审查评论模板
```markdown
## 🔴 阻缩

**安全：XX风险**
第XX行：问题描述。

**为什么：** 影响说明。

**建议：**
```代码示例
```

---

## 🟡 建议

**性能：XX问题**
第XX行：问题描述。

**为什么：** 影响说明。

**建议：**
```代码示例
```

---

## 💭 挑剔

**风格：XX可以优化**
第XX行：优化建议。

**为什么：** 提升可读性。

**建议：**
```代码示例
```

---

## ✅ 优点

- XX做得很好
- XX代码清晰
- XX测试完整
```

---

**文档版本**: v1.0  
**创建日期**: 2026-03-28  
**快速使用**: 打开此文档，对照检查项逐一验证
