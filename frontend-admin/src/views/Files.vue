<template>
  <div class="files-container">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statsConfig" :key="stat.key">
        <div class="stat-icon-wrap" :style="{ background: stat.bg }">
          <span class="stat-emoji">{{ stat.icon }}</span>
        </div>
        <div class="stat-body">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-desc">{{ stat.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 上传区域 -->
    <div class="upload-section">
      <div class="section-title">
        <span class="title-icon">📤</span>
        上传图片
      </div>
      <el-upload
        class="upload-dragger"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        multiple
        accept="image/*"
      >
        <div class="upload-inner">
          <div class="upload-icon-wrap">
            <el-icon :size="40" class="upload-icon"><upload-filled /></el-icon>
          </div>
          <div class="upload-text">
            拖拽图片到此处，或 <em class="upload-em">点击选择</em>
          </div>
          <div class="upload-tip">支持 JPG / PNG / GIF / WEBP，单文件 ≤ 10MB</div>
        </div>
      </el-upload>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <div class="filter-row">
        <el-input
          v-model="queryForm.keyword"
          placeholder="🔍 搜索文件名..."
          clearable
          class="filter-input"
          @keyup.enter="handleQuery"
        />
        <el-select v-model="queryForm.isUsed" placeholder="使用状态" clearable class="filter-select">
          <el-option label="✅ 已使用" :value="1" />
          <el-option label="⭕ 未使用" :value="0" />
        </el-select>
        <el-select v-model="queryForm.storageType" placeholder="存储类型" clearable class="filter-select">
          <el-option label="💻 本地存储" value="local" />
          <el-option label="☁️ OSS" value="oss" />
          <el-option label="🌊 COS" value="cos" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" class="btn-primary" @click="handleQuery">查询</el-button>
          <el-button class="btn-default" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 图库卡片 -->
    <div class="gallery-section">
      <div class="gallery-header">
        <div class="gallery-title">
          <span class="title-icon">🖼️</span>
          图片库
          <span class="count-badge">共 {{ total }} 张</span>
        </div>
        <div class="gallery-actions">
          <button class="view-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon> 网格
          </button>
          <button class="view-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            <el-icon><List /></el-icon> 列表
          </button>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
            class="btn-danger-sm"
          >
            <el-icon><Delete /></el-icon> 批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>
      </div>

      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" v-loading="loading" class="image-grid">
        <div
          v-for="image in imageList"
          :key="image.id"
          class="image-card"
          :class="{ selected: selectedIds.includes(image.id) }"
          @click="toggleSelection(image.id)"
        >
          <div class="image-wrapper">
            <el-image
              :src="getImageUrl(image.thumbnailUrl || image.fileUrl)"
              :preview-src-list="[getImageUrl(image.fileUrl)]"
              fit="cover"
              class="image-preview"
              @click.stop
              lazy
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="32"><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>

            <div class="image-overlay">
              <button class="overlay-btn copy" @click.stop="handleCopyUrl(image)" title="复制链接">
                <el-icon><CopyDocument /></el-icon>
              </button>
              <button class="overlay-btn delete" @click.stop="handleDelete(image)" title="删除">
                <el-icon><Delete /></el-icon>
              </button>
            </div>

            <div class="image-badge" :class="image.isUsed === 1 ? 'used' : 'unused'">
              {{ image.isUsed === 1 ? '已使用' : '未使用' }}
            </div>

            <div class="image-checkbox-wrap" @click.stop>
              <el-checkbox v-model="selectedIds" :value="image.id" class="image-checkbox" />
            </div>
          </div>

          <div class="image-info">
            <div class="image-name" :title="image.originalName">{{ image.originalName }}</div>
            <div class="image-meta">
              <span>{{ formatSize(image.fileSize) }}</span>
              <span>{{ image.width }}×{{ image.height }}</span>
            </div>
            <div class="image-time">{{ image.createTime }}</div>
          </div>
        </div>

        <div v-if="!loading && imageList.length === 0" class="empty-gallery">
          <div class="empty-icon">🖼️</div>
          <div class="empty-text">暂无图片</div>
        </div>
      </div>

      <!-- 列表视图 -->
      <el-table
        v-if="viewMode === 'list'"
        :data="imageList"
        v-loading="loading"
        class="modern-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="缩略图" width="90">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.thumbnailUrl || row.fileUrl)"
              :preview-src-list="[getImageUrl(row.fileUrl)]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 8px; cursor: pointer;"
              lazy
            >
              <template #error>
                <div style="display:flex;align-items:center;justify-content:center;width:60px;height:60px;background:#f5f7fa;border-radius:8px;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="尺寸" width="120">
          <template #default="{ row }">{{ row.width }} × {{ row.height }}</template>
        </el-table-column>
        <el-table-column prop="storageType" label="存储类型" width="100" />
        <el-table-column label="使用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isUsed === 1 ? 'success' : 'info'" size="small" round>
              {{ row.isUsed === 1 ? '已使用' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleCopyUrl(row)">复制链接</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Grid, List, Delete, Picture, CopyDocument } from '@element-plus/icons-vue'
import {
  getImageStatistics,
  queryImages,
  deleteImage,
  deleteImages
} from '@/api/image'

const statistics = ref({})
const imageList = ref([])
const loading = ref(false)
const total = ref(0)
const selectedIds = ref([])
const viewMode = ref('grid')

const queryForm = reactive({
  keyword: '',
  isUsed: null,
  storageType: '',
  page: 1,
  pageSize: 24
})

const uploadUrl = computed(() => import.meta.env.VITE_API_BASE_URL + '/api/images/upload')

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const usageRate = computed(() => {
  if (!statistics.value.totalImages || statistics.value.totalImages === 0) return 0
  return ((statistics.value.usedImages / statistics.value.totalImages) * 100).toFixed(0)
})

const statsConfig = computed(() => [
  {
    key: 'total',
    icon: '📷',
    label: '总图片数',
    desc: '已上传图片',
    value: statistics.value.totalImages || 0,
    bg: 'linear-gradient(135deg, #667eea22, #764ba222)',
    color: '#667eea'
  },
  {
    key: 'size',
    icon: '💾',
    label: '总存储',
    desc: '占用空间',
    value: formatSize(statistics.value.totalSize),
    bg: 'linear-gradient(135deg, #43e97b22, #38f9d722)',
    color: '#43e97b'
  },
  {
    key: 'used',
    icon: '✅',
    label: '已使用',
    desc: '被引用图片',
    value: statistics.value.usedImages || 0,
    bg: 'linear-gradient(135deg, #4facfe22, #00f2fe22)',
    color: '#4facfe'
  },
  {
    key: 'rate',
    icon: '📊',
    label: '使用率',
    desc: '图片使用率',
    value: usageRate.value + '%',
    bg: 'linear-gradient(135deg, #f093fb22, #f5576c22)',
    color: '#f093fb'
  },
  {
    key: 'today',
    icon: '🆕',
    label: '今日上传',
    desc: '今天新增',
    value: statistics.value.todayUploads || 0,
    bg: 'linear-gradient(135deg, #fa709a22, #fee14022)',
    color: '#fa709a'
  }
])

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

const getImageUrl = (url) => {
  if (!url) return ''
  // 如果是绝对路径（以 http:// 或 https:// 开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 如果是相对路径，拼接当前域名
  return window.location.origin + (url.startsWith('/') ? url : '/' + url)
}

const getStatistics = async () => {
  try {
    const { data } = await getImageStatistics()
    statistics.value = data || {}
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '获取统计信息失败')
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const { data } = await queryImages(queryForm)
    const records = Array.isArray(data?.records) ? data.records : []
    const totalCount = Number(data?.total || 0)
    if (records.length === 0 && totalCount > 0 && queryForm.page > 1) {
      queryForm.page -= 1
      return await handleQuery()
    }
    imageList.value = records
    total.value = totalCount
    selectedIds.value = []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '查询图片失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  selectedIds.value = []
  Object.assign(queryForm, { keyword: '', isUsed: null, storageType: '', page: 1, pageSize: 24 })
  handleQuery()
}

const handleUploadSuccess = (response) => {
  if (response?.code !== 200) {
    ElMessage.error(response?.message || '上传失败')
    return
  }
  ElMessage.success('上传成功')
  getStatistics()
  handleQuery()
}

const handleUploadError = (error) => {
  ElMessage.error(error?.response?.data?.message || '上传失败')
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const toggleSelection = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) selectedIds.value.splice(index, 1)
  else selectedIds.value.push(id)
}

const handleCopyUrl = async (row) => {
  try {
    await navigator.clipboard.writeText(getImageUrl(row.fileUrl))
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制链接失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteImage(row.id, { skipBusinessErrorMessage: true })
      ElMessage.success('删除成功')
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 张图片吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const { data } = await deleteImages(selectedIds.value, { skipBusinessErrorMessage: true })
      const deletedCount = data?.deletedCount ?? 0
      const skippedUsedCount = data?.skippedUsedCount ?? 0
      const storageFailedCount = data?.storageFailedCount ?? 0
      if (deletedCount > 0 && skippedUsedCount === 0 && storageFailedCount === 0) {
        ElMessage.success(`批量删除成功，已删除 ${deletedCount} 张图片`)
      } else if (deletedCount > 0) {
        ElMessage.warning(`已删除 ${deletedCount} 张图片，已跳过 ${skippedUsedCount} 张，存储删除失败 ${storageFailedCount} 张`)
      } else {
        ElMessage.warning('没有可以删除的图片')
      }
      selectedIds.value = []
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '批量删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getStatistics()
  handleQuery()
})
</script>

<style scoped>
.files-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100%;
}

/* 统计网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}

.stat-icon-wrap {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-emoji {
  font-size: 24px;
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.stat-desc {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}

/* 上传区域 */
.upload-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
}

.upload-dragger :deep(.el-upload-dragger) {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  transition: all 0.3s;
  background: #fafafa;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #667eea;
  background: rgba(102,126,234,0.04);
}

.upload-inner {
  padding: 32px 20px;
  text-align: center;
}

.upload-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102,126,234,0.15), rgba(118,75,162,0.15));
  margin-bottom: 12px;
}

.upload-icon {
  color: #667eea;
}

.upload-text {
  font-size: 15px;
  color: #606266;
  margin-bottom: 6px;
}

.upload-em {
  color: #667eea;
  font-style: normal;
  font-weight: 600;
}

.upload-tip {
  font-size: 13px;
  color: #c0c4cc;
}

/* 筛选栏 */
.filter-section {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-input {
  width: 240px;
}

.filter-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.filter-select {
  width: 150px;
}

.filter-select :deep(.el-select__wrapper) {
  border-radius: 10px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  border-radius: 10px !important;
  padding: 8px 20px !important;
}

.btn-default {
  border-radius: 10px !important;
  padding: 8px 20px !important;
}

/* 图库区域 */
.gallery-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.gallery-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.gallery-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-badge {
  font-size: 12px;
  background: linear-gradient(135deg, rgba(102,126,234,0.15), rgba(118,75,162,0.15));
  color: #667eea;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
}

.gallery-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 1.5px solid #dcdfe6;
  border-radius: 8px;
  background: white;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn.active,
.view-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
}

.btn-danger-sm {
  border-radius: 8px !important;
  font-size: 13px;
}

/* 网格 */
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  min-height: 300px;
}

.image-card {
  border: 2px solid transparent;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
}

.image-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102,126,234,0.2);
}

.image-card.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102,126,234,0.2);
}

.image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  background: #f5f7fa;
  overflow: hidden;
}

.image-preview {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
}

.image-preview :deep(img) {
  width: 100%; height: 100%;
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  font-size: 12px;
  gap: 8px;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.overlay-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: transform 0.2s;
}

.overlay-btn:hover {
  transform: scale(1.15);
}

.overlay-btn.copy {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.overlay-btn.delete {
  background: linear-gradient(135deg, #f5222d, #ff7875);
  color: white;
}

.image-badge {
  position: absolute;
  top: 8px; right: 8px;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  z-index: 1;
}

.image-badge.used {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
}

.image-badge.unused {
  background: rgba(144,147,153,0.8);
  color: white;
}

.image-checkbox-wrap {
  position: absolute;
  top: 8px; left: 8px;
  z-index: 2;
}

.image-checkbox :deep(.el-checkbox__inner) {
  background: white;
  border-color: #dcdfe6;
  width: 18px;
  height: 18px;
}

.image-info {
  padding: 12px;
}

.image-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.image-time {
  font-size: 11px;
  color: #c0c4cc;
}

/* 空状态 */
.empty-gallery {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 15px;
}

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

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .files-container { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .image-grid { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
  .filter-row { flex-direction: column; align-items: stretch; }
  .filter-input, .filter-select { width: 100%; }
}
</style>
