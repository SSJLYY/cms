# 资源管理批量操作功能使用指南

## 功能概述

为了提高资源管理效率，我们为资源管理页面添加了完整的批量操作功能，包括：

- ✅ **批量发布** - 一键发布多个资源
- ✅ **批量下架** - 一键下架多个资源  
- ✅ **批量删除** - 一键删除多个资源
- ✅ **批量移动分类** - 将多个资源移动到指定分类

## 使用方法

### 1. 选择资源
在资源列表页面，每行资源前面都有一个复选框，勾选需要操作的资源。

### 2. 批量操作按钮
选择资源后，顶部工具栏会显示批量操作按钮：
- **批量发布 (N)** - 绿色按钮，将选中资源状态设为"已发布"
- **批量下架 (N)** - 橙色按钮，将选中资源状态设为"已下架"  
- **批量移动 (N)** - 蓝色按钮，将选中资源移动到指定分类
- **批量删除 (N)** - 红色按钮，删除选中的资源（软删除）

*注：(N) 表示选中的资源数量*

### 3. 操作确认
- 所有批量操作都会弹出确认对话框，确保操作安全
- 批量移动需要先选择目标分类
- 批量删除会特别提醒"删除后无法恢复"

## API接口

### 后端接口

```java
// 批量发布资源
PUT /api/resources/admin/batch-publish
Body: { "ids": [1, 2, 3] }

// 批量下架资源  
PUT /api/resources/admin/batch-unpublish
Body: { "ids": [1, 2, 3] }

// 批量删除资源
DELETE /api/resources/admin/batch-delete
Body: { "ids": [1, 2, 3] }

// 批量移动分类
PUT /api/resources/admin/batch-move-category
Body: { "ids": [1, 2, 3], "categoryId": 5 }
```

### 前端API调用

```javascript
import { 
  batchPublishResources, 
  batchUnpublishResources, 
  batchDeleteResources, 
  batchMoveToCategory 
} from '../api/resource'

// 批量发布
const result = await batchPublishResources([1, 2, 3])

// 批量下架
const result = await batchUnpublishResources([1, 2, 3])

// 批量删除
const result = await batchDeleteResources([1, 2, 3])

// 批量移动分类
const result = await batchMoveToCategory([1, 2, 3], 5)
```

## 技术实现

### 后端实现
- 使用MyBatis-Plus的`LambdaUpdateWrapper`进行批量更新
- 事务保证数据一致性
- 详细的操作日志记录
- 参数验证和错误处理

### 前端实现
- Element Plus表格的多选功能
- 响应式的批量操作按钮显示
- 用户友好的确认对话框
- 操作结果反馈

## 注意事项

1. **权限控制**：所有批量操作都需要管理员权限
2. **数据安全**：批量删除是软删除，数据不会真正丢失
3. **操作反馈**：每次操作都会显示实际影响的资源数量
4. **错误处理**：单个资源操作失败不会影响其他资源
5. **性能优化**：使用批量SQL操作，避免循环单个操作

## 使用场景

- **内容审核**：批量发布审核通过的资源
- **内容管理**：批量下架过期或违规资源
- **分类整理**：批量移动资源到正确分类
- **数据清理**：批量删除测试或无效资源

这些批量操作功能大大提高了资源管理的效率，特别是在处理大量爬虫采集的资源时非常有用！