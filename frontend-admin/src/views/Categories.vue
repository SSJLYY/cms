<template>
  <div class="categories-container">
    <el-card class="statistics-card">
      <div class="statistics-grid">
        <div class="stat-item">
          <div class="stat-label">总分类数</div>
          <div class="stat-value">{{ statistics.totalCategories || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">一级分类</div>
          <div class="stat-value">{{ statistics.level1Categories || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">二级分类</div>
          <div class="stat-value">{{ statistics.level2Categories || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">启用分类</div>
          <div class="stat-value success">{{ statistics.activeCategories || 0 }}</div>
        </div>
      </div>
    </el-card>

    <el-card class="action-card">
      <el-button type="primary" @click="handleAdd">新增分类</el-button>
      <el-button @click="handleExport">导出分类</el-button>
      <el-button @click="loadCategoryTree">刷新</el-button>
    </el-card>

    <el-card class="tree-card">
      <el-tree
        :data="categoryTree"
        :props="treeProps"
        node-key="id"
        default-expand-all
        draggable
        @node-drop="handleDrop"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <div class="node-content">
              <el-icon v-if="data.icon"><component :is="data.icon" /></el-icon>
              <span class="node-label">{{ data.name }}</span>
              <el-tag size="small" :type="data.status === 1 ? 'success' : 'info'">
                {{ data.status === 1 ? '启用' : '禁用' }}
              </el-tag>
              <span class="node-count">{{ data.resourceCount || 0 }} 个资源</span>
            </div>
            <div class="node-actions">
              <el-button type="text" size="small" @click.stop="handleEdit(data)">编辑</el-button>
              <el-button type="text" size="small" @click.stop="handleAddChild(data)">添加子分类</el-button>
              <el-button type="text" size="small" @click.stop="handleDelete(data)" style="color: #f56c6c">
                删除
              </el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
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
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
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
          <el-input-number v-model="form.sortOrder" :min="0" />
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
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  Object.assign(form, data)
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
  // 更新排序
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

onMounted(() => {
  getStatistics()
  loadCategoryTree()
})
</script>

<style scoped>
.categories-container {
  padding: 20px;
}

.statistics-card,
.action-card,
.tree-card {
  margin-bottom: 20px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 10px;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.node-label {
  font-size: 14px;
  font-weight: 500;
}

.node-count {
  font-size: 12px;
  color: #999;
}

.node-actions {
  display: flex;
  gap: 5px;
}
</style>
