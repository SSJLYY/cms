<template>
  <div class="promotion-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="page-title">
          <span class="title-icon">📢</span>
          广告推广管理
          <span class="count-badge">共 {{ total }} 条</span>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button class="btn-add" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 添加广告位
        </el-button>
        <el-button class="btn-refresh" @click="handleRefresh">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 位置标签页 + 表格 -->
    <div class="table-card">
      <div class="position-tabs">
        <button
          v-for="tab in positionTabs"
          :key="tab.value"
          class="pos-tab"
          :class="{ active: activePosition === tab.value }"
          @click="handlePositionChange(tab.value)"
        >
          <span class="tab-icon">{{ tab.icon }}</span>
          {{ tab.label }}
        </button>
      </div>

      <el-table :data="advertisementList" v-loading="loading" class="modern-table">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="广告信息" min-width="200">
          <template #default="{ row }">
            <div class="ad-info">
              <div class="ad-name">{{ row.name }}</div>
              <div class="ad-meta">
                <el-tag :type="getPositionTagType(row.position)" size="small" round>
                  {{ getPositionLabel(row.position) }}
                </el-tag>
                <el-tag :type="getTypeTagType(row.type)" size="small" round>
                  {{ getTypeLabel(row.type) }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预览" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 80px; height: 50px; cursor: pointer; border-radius: 8px;"
            />
            <span v-else class="no-preview">-</span>
          </template>
        </el-table-column>
        <el-table-column label="点击数" width="100" align="center">
          <template #default="{ row }">
            <span class="click-count">{{ row.clickCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="{ row }">
            <span class="sort-badge">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="#667eea"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleCopy(row)">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAdvertisements"
          @current-change="loadAdvertisements"
        />
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="580px"
      class="modern-dialog"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="modern-form">
        <el-form-item label="广告名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入广告名称" />
        </el-form-item>

        <el-form-item label="广告位置" prop="position">
          <el-select v-model="form.position" placeholder="请选择广告位置" style="width: 100%">
            <el-option v-for="tab in positionTabs.filter(t => t.value !== 'all')" :key="tab.value"
              :label="tab.icon + ' ' + tab.label" :value="tab.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="广告类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择广告类型" style="width: 100%">
            <el-option label="🖼️ 图片广告" value="image" />
            <el-option label="📝 文字广告" value="text" />
            <el-option label="🎬 视频广告" value="video" />
          </el-select>
        </el-form-item>

        <el-form-item label="广告图片" prop="imageUrl" v-if="form.type === 'image'">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
          <div v-if="form.imageUrl" class="img-preview">
            <el-image :src="form.imageUrl" fit="contain" style="max-width:100%;max-height:200px;border-radius:8px;" />
          </div>
        </el-form-item>

        <el-form-item label="广告内容" prop="content" v-if="form.type === 'text'">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入广告内容" />
        </el-form-item>

        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="form.status"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn-cancel" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="btn-confirm" @click="handleSubmit">
            {{ form.id ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, CopyDocument } from '@element-plus/icons-vue'
import {
  getAdvertisementList,
  createAdvertisement,
  updateAdvertisement,
  deleteAdvertisement,
  updateStatus
} from '../api/modules/promotion'

const positionTabs = [
  { value: 'all', label: '全部', icon: '📋' },
  { value: 'homepage', label: '首页', icon: '🏠' },
  { value: 'download', label: '下载页', icon: '⬇️' },
  { value: 'category', label: '分类页', icon: '📁' },
  { value: 'custom', label: '自定义页', icon: '✏️' }
]

const advertisementList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const activePosition = ref('all')

const dialogVisible = ref(false)
const dialogTitle = ref('添加广告')
const formRef = ref(null)
const form = ref({
  name: '', position: 'homepage', type: 'image',
  imageUrl: '', linkUrl: '', content: '',
  status: 1, sortOrder: 0, startTime: null, endTime: null
})

const normalizeAdvertisementForm = (data = {}) => {
  const normalizedName = data.name || data.title || ''
  return {
    ...data,
    name: normalizedName,
    title: data.title || normalizedName
  }
}

const rules = {
  name: [{ required: true, message: '请输入广告名称', trigger: 'blur' }],
  position: [{ required: true, message: '请选择广告位置', trigger: 'change' }],
  type: [{ required: true, message: '请选择广告类型', trigger: 'change' }]
}

const loadAdvertisements = async () => {
  loading.value = true
  try {
    const { data } = await getAdvertisementList({
      page: page.value, pageSize: pageSize.value,
      position: activePosition.value === 'all' ? null : activePosition.value
    })
    const records = Array.isArray(data?.records) ? data.records : []
    advertisementList.value = records.map(item => normalizeAdvertisementForm(item))
    total.value = Number(data?.total || 0)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载广告列表失败')
  } finally {
    loading.value = false
  }
}

const handlePositionChange = (val) => {
  activePosition.value = val
  page.value = 1
  loadAdvertisements()
}

const handleAdd = () => {
  dialogTitle.value = '✨ 添加广告'
  form.value = normalizeAdvertisementForm({ name: '', position: 'homepage', type: 'image', imageUrl: '', linkUrl: '', content: '', status: 1, sortOrder: 0, startTime: null, endTime: null })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '✏️ 编辑广告'
  form.value = normalizeAdvertisementForm(row)
  dialogVisible.value = true
}

const handleCopy = (row) => {
  dialogTitle.value = '📋 复制广告'
  form.value = normalizeAdvertisementForm({
    ...row,
    id: null,
    name: `${row.name || row.title || ''} (副本)`,
    title: `${row.title || row.name || ''} (副本)`
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updateAdvertisement(form.value.id, normalizeAdvertisementForm(form.value), { skipBusinessErrorMessage: true })
          ElMessage.success('更新成功')
        } else {
          await createAdvertisement(normalizeAdvertisementForm(form.value), { skipBusinessErrorMessage: true })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadAdvertisements()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      }
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    await updateStatus(row.id, row.status, { skipBusinessErrorMessage: true })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个广告吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await deleteAdvertisement(row.id, { skipBusinessErrorMessage: true })
      ElMessage.success('删除成功')
      loadAdvertisements()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleRefresh = () => loadAdvertisements()
const handleDialogClose = () => formRef.value?.resetFields()

const getPositionLabel = (p) => ({ homepage: '首页', download: '下载页', category: '分类页', custom: '自定义页' }[p] || p)
const getPositionTagType = (p) => ({ homepage: 'success', download: 'primary', category: 'warning', custom: 'info' }[p] || '')
const getTypeLabel = (t) => ({ image: '图片广告', text: '文字广告', video: '视频广告' }[t] || t)
const getTypeTagType = (t) => ({ image: 'success', text: 'primary', video: 'warning' }[t] || '')

onMounted(() => loadAdvertisements())
</script>

<style scoped>
.promotion-container {
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
}

.btn-add {
  border-radius: 10px !important;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
}

.btn-refresh {
  border-radius: 10px !important;
}

/* 表格卡片 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

/* 位置标签 */
.position-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pos-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  border: 1.5px solid #dcdfe6;
  background: white;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.pos-tab:hover {
  border-color: #667eea;
  color: #667eea;
}

.pos-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
  font-weight: 600;
}

.tab-icon { font-size: 15px; }

/* 现代表格 */
.modern-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

.modern-table :deep(.el-table__row:hover td) {
  background: rgba(102,126,234,0.04);
}

.ad-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ad-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.ad-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.click-count {
  font-size: 14px;
  font-weight: 700;
  color: #667eea;
}

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

.no-preview {
  color: #c0c4cc;
  font-size: 18px;
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

.modern-form :deep(.el-input__wrapper),
.modern-form :deep(.el-textarea__inner),
.modern-form :deep(.el-select__wrapper) {
  border-radius: 10px;
}

.img-preview {
  margin-top: 8px;
  padding: 8px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  text-align: center;
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
  .promotion-container { padding: 16px; }
  .toolbar { flex-direction: column; align-items: stretch; }
}
</style>
