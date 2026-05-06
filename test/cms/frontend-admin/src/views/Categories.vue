<template>
  <div class="categories-container">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon><Folder /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalCategories || 0 }}</div>
          <div class="stat-label">总分类数</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.level1Categories || 0 }}</div>
          <div class="stat-label">一级分类</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon><Menu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.level2Categories || 0 }}</div>
          <div class="stat-label">二级分类</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.activeCategories || 0 }}</div>
          <div class="stat-label">启用分类</div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" class="gradient-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
      <el-button @click="handleExport" class="export-btn">
        <el-icon><Download /></el-icon>
        导出分类
      </el-button>
      <el-button @click="loadCategoryTree" class="refresh-btn">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 分类树卡片 -->
    <div class="tree-card">
      <div class="card-header">
        <h3>
          <el-icon><Collection /></el-icon>
          分类结构
        </h3>
        <span class="category-tip">拖拽节点可调整顺序</span>
      </div>
      <el-tree
        :data="categoryTree"
        :props="treeProps"
        node-key="id"
        default-expand-all
        draggable
        @node-drop="handleDrop"
        class="modern-tree"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-info">
              <div class="node-icon" :style="{ background: getNodeColor(node.level) }">
                <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
                <span v-else>{{ getNodeEmoji(node.level) }}</span>
              </div>
              <div class="node-details">
                <span class="node-label">{{ data.name }}</span>
                <span class="node-meta">
                  <el-tag size="small" :type="data.status === 1 ? 'success' : 'info'" effect="dark">
                    {{ data.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                  <span class="resource-count">
                    <el-icon><Document /></el-icon>
                    {{ data.resourceCount || 0 }} 个资源
                  </span>
                </span>
              </div>
            </div>
            <div class="node-actions">
              <el-button type="primary" link size="small" @click.stop="handleEdit(data)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="success" link size="small" @click.stop="handleAddChild(data)">
                <el-icon><Plus /></el-icon>
                添加子分类
              </el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(data)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
      class="category-dialog"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="category-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="treeProps"
            placeholder="请选择父分类（不选则为一级分类）"
            clearable
            check-strictly
            :render-after-expand="false"
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称">
            <template #append>
              <el-popover placement="top" :width="400" trigger="click">
                <template #reference>
                  <el-button icon="InfoFilled">选择图标</el-button>
                </template>
                <div class="icon-picker">
                  <el-icon v-for="icon in commonIcons" :key="icon" :class="{ active: form.icon === icon }" @click="form.icon = icon">
                    <component :is="icon" />
                  </el-icon>
                </div>
              </el-popover>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" class="submit-btn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, markRaw } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Folder, Grid, Menu, CircleCheck, Plus, Download, Refresh,
  Collection, Document, Edit, Delete, InfoFilled,
  Files, Picture, Video, Music, FolderOpened, DocumentCopy,
  Setting, Tool, Brush, Camera, Film, Notebook, Star
} from '@element-plus/icons-vue'
import {
  getCategoryStatistics,
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategorySortOrder,
  exportCategories
} from '@/api/modules/categories'

const statistics = ref({})
const categoryTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const treeProps = {
  label: 'name',
  children: 'children',
  value: 'id'
}

const form = reactive({
  id: null,
  name: '',
  parentId: null,
  icon: '',
  description: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const commonIcons = [
  'Files', 'Picture', 'Video', 'Music', 'FolderOpened', 'DocumentCopy',
  'Setting', 'Tool', 'Brush', 'Camera', 'Film', 'Notebook', 'Star'
]

const getStatistics = async () => {
  try {
    const { data } = await getCategoryStatistics()
    statistics.value = data
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const loadCategoryTree = async () => {
  try {
    const { data } = await getCategoryTree()
    categoryTree.value = data
  } catch (error) {
    ElMessage.error('加载分类树失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  resetForm()
  dialogVisible.value = true
}

const handleAddChild = (data) => {
  dialogTitle.value = '新增子分类'
  resetForm()
  form.parentId = data.id
  dialogVisible.value = true
}

const handleEdit = (data) => {
  dialogTitle.value = '编辑分类'
  Object.assign(form, {
    id: data.id,
    name: data.name,
    parentId: data.parentId,
    icon: data.icon || '',
    description: data.description || '',
    sortOrder: data.sortOrder || 0,
    status: data.status
  })
  dialogVisible.value = true
}

const handleDelete = (data) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCategory(data.id)
      ElMessage.success('删除成功')
      loadCategoryTree()
      getStatistics()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (form.id) {
      await updateCategory(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createCategory(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCategoryTree()
    getStatistics()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const handleDrop = async (draggingNode, dropNode, dropType) => {
  try {
    const newSortOrder = dropNode.data.sortOrder
    await updateCategorySortOrder(draggingNode.data.id, newSortOrder)
    ElMessage.success('排序更新成功')
  } catch (error) {
    ElMessage.error('排序更新失败')
    loadCategoryTree()
  }
}

const handleExport = async () => {
  try {
    const { data } = await exportCategories()
    ElMessage.success('导出成功: ' + data)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    parentId: null,
    icon: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
  formRef.value?.clearValidate()
}

const handleDialogClose = () => {
  resetForm()
}

const getNodeColor = (level) => {
  const colors = {
    1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    2: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    3: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
  return colors[level] || colors[1]
}

const getNodeEmoji = (level) => {
  const emojis = { 1: '📁', 2: '📂', 3: '📄' }
  return emojis[level] || '📁'
}

onMounted(() => {
  getStatistics()
  loadCategoryTree()
})
</script>

<style scoped>
.categories-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 统计卡片 */
.statistics-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.stat-primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-warning .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 操作栏 */
.action-bar {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.gradient-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.export-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  color: white;
}

.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(56, 239, 125, 0.4);
}

.refresh-btn {
  background: #f5f7fa;
  border: none;
}

.refresh-btn:hover {
  background: #e4e7ed;
}

/* 分类树卡片 */
.tree-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-tip {
  font-size: 13px;
  color: #909399;
}

/* 树形节点 */
.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  transition: all 0.3s;
}

.tree-node:hover {
  background: #f5f7fa;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.node-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.node-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.node-label {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.node-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-count {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.node-actions {
  display: flex;
  gap: 8px;
}

/* 图标选择器 */
.icon-picker {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.icon-picker .el-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 20px;
  transition: all 0.3s;
}

.icon-picker .el-icon:hover {
  background: #f5f7fa;
  transform: scale(1.1);
}

.icon-picker .el-icon.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 表单提交按钮 */
.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 响应式 */
@media (max-width: 1200px) {
  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: 1fr;
  }
  
  .action-bar {
    flex-wrap: wrap;
  }
  
  .tree-node {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .node-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
