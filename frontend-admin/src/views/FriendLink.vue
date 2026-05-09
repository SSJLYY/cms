<template>
  <div class="friendlink-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="page-title">
          <span class="title-icon">🔗</span>
          友情链接管理
          <span class="count-badge">共 {{ pagination.total }} 条</span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchForm.name"
          placeholder="🔍 搜索网站名称..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchForm.status" placeholder="状态筛选" clearable class="status-select">
          <el-option label="✅ 启用" :value="1" />
          <el-option label="🚫 禁用" :value="0" />
        </el-select>
        <el-button class="btn-search" @click="handleSearch">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button class="btn-reset" @click="handleReset">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
        <el-button class="btn-add" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 添加友联
        </el-button>
        <el-button
          class="btn-batch-delete"
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon> 批量删除 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        class="modern-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="网站信息" min-width="280">
          <template #default="{ row }">
            <div class="site-info">
              <div class="site-logo-wrap">
                <el-image
                  v-if="row.logo"
                  :src="row.logo"
                  fit="cover"
                  class="site-logo"
                >
                  <template #error>
                    <div class="site-logo-fallback">{{ row.name?.charAt(0) }}</div>
                  </template>
                </el-image>
                <div v-else class="site-logo-fallback">{{ row.name?.charAt(0) }}</div>
              </div>
              <div class="site-detail">
                <div class="site-name">{{ row.name }}</div>
                <div class="site-desc" v-if="row.description">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="网址" min-width="220">
          <template #default="{ row }">
            <el-link :href="row.url" target="_blank" type="primary" class="site-url">
              <el-icon class="link-icon"><Link /></el-icon>
              {{ row.url }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="{ row }">
            <span class="sort-badge">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              class="status-tag"
              round
            >
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      class="modern-dialog"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
        class="modern-form"
      >
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入网站名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="网站URL" prop="url">
          <el-input v-model="formData.url" placeholder="以 http:// 或 https:// 开头" maxlength="500" />
        </el-form-item>
        <el-form-item label="Logo URL" prop="logo">
          <el-input v-model="formData.logo" placeholder="Logo图片地址（可选）" maxlength="500" />
          <div v-if="formData.logo" class="logo-preview">
            <el-image :src="formData.logo" fit="contain" style="max-width:120px;max-height:60px;border-radius:8px;" />
          </div>
        </el-form-item>
        <el-form-item label="网站描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="简短描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">✅ 启用</el-radio>
            <el-radio :label="0">🚫 禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" placeholder="数字越小越靠前" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="btn-confirm" @click="handleSubmit" :loading="submitLoading">
            {{ formData.id ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Link } from '@element-plus/icons-vue'
import {
  getFriendLinkPage,
  createFriendLink,
  updateFriendLink,
  deleteFriendLink,
  batchDeleteFriendLinks
} from '../api/modules/friendlink'

const searchForm = reactive({ name: '', status: null })

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const tableData = ref([])
const loading = ref(false)
const selectedIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  id: null,
  name: '',
  url: '',
  logo: '',
  description: '',
  status: 1,
  sortOrder: 0
})

const formRules = {
  name: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入网站URL', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/, message: 'URL必须以http://或https://开头', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...searchForm, page: pagination.page, pageSize: pagination.pageSize }
    const res = await getFriendLinkPage(params)
    const records = Array.isArray(res?.data?.records) ? res.data.records : []
    const totalCount = Number(res?.data?.total || 0)
    if (records.length === 0 && totalCount > 0 && pagination.page > 1) {
      pagination.page -= 1
      return await loadData()
    }
    tableData.value = records
    pagination.total = totalCount
    selectedIds.value = selectedIds.value.filter((id) => tableData.value.some((item) => item.id === id))
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  selectedIds.value = []
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.status = null
  pagination.page = 1
  selectedIds.value = []
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '✨ 添加友联'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '✏️ 编辑友联'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除友情链接“${row.name}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteFriendLink(row.id, { skipBusinessErrorMessage: true })
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await batchDeleteFriendLinks(selectedIds.value, { skipBusinessErrorMessage: true })
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (formData.id) {
      await updateFriendLink(formData, { skipBusinessErrorMessage: true })
      ElMessage.success('更新成功')
    } else {
      await createFriendLink(formData, { skipBusinessErrorMessage: true })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.response?.data?.message || (formData.id ? '更新失败' : '创建失败'))
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, { id: null, name: '', url: '', logo: '', description: '', status: 1, sortOrder: 0 })
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleSizeChange = () => loadData()
const handleCurrentChange = () => loadData()

onMounted(() => loadData())
</script>

<style scoped>
.friendlink-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100%;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: 16px;
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon { font-size: 20px; }

.count-badge {
  font-size: 12px;
  background: linear-gradient(135deg, rgba(102,126,234,0.15), rgba(118,75,162,0.15));
  color: #667eea;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.status-select {
  width: 130px;
}

.status-select :deep(.el-select__wrapper) {
  border-radius: 10px;
}

.btn-search {
  border-radius: 10px !important;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  color: white !important;
}

.btn-reset {
  border-radius: 10px !important;
}

.btn-add {
  border-radius: 10px !important;
  background: linear-gradient(135deg, #43e97b, #38f9d7) !important;
  border: none !important;
}

.btn-batch-delete {
  border-radius: 10px !important;
}

/* 表格 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.modern-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

.modern-table :deep(.el-table__row:hover td) {
  background: rgba(102,126,234,0.04);
}

/* 网站信息 */
.site-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.site-logo-wrap {
  flex-shrink: 0;
}

.site-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
}

.site-logo-fallback {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
}

.site-detail {
  flex: 1;
  min-width: 0;
}

.site-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.site-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.site-url {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-icon { font-size: 12px; }

.sort-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f5f7fa;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.status-tag {
  font-size: 12px;
  font-weight: 500;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 对话框 */
.modern-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px 16px 0 0;
  padding: 20px 24px;
}

.modern-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.modern-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.modern-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.modern-form {
  padding: 8px 0;
}

.modern-form :deep(.el-input__wrapper),
.modern-form :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.logo-preview {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  display: inline-block;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-cancel {
  border-radius: 10px !important;
  padding: 8px 20px !important;
}

.btn-confirm {
  border-radius: 10px !important;
  padding: 8px 24px !important;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
}

@media (max-width: 768px) {
  .friendlink-container { padding: 16px; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .toolbar-right { flex-wrap: wrap; }
  .search-input, .status-select { width: 100%; }
}
</style>
