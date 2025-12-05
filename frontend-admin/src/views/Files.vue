<template>
  <div class="files-container">
    <el-card class="statistics-card">
      <div class="statistics-grid">
        <div class="stat-item">
          <div class="stat-icon">📷</div>
          <div class="stat-label">总图片数</div>
          <div class="stat-value">{{ statistics.totalImages || 0 }}</div>
          <div class="stat-desc">已上传图片</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">💾</div>
          <div class="stat-label">总存储</div>
          <div class="stat-value">{{ formatSize(statistics.totalSize) }}</div>
          <div class="stat-desc">占用空间</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">✅</div>
          <div class="stat-label">已使用</div>
          <div class="stat-value success">{{ statistics.usedImages || 0 }}</div>
          <div class="stat-desc">被引用图片</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">📊</div>
          <div class="stat-label">使用率</div>
          <div class="stat-value">{{ usageRate }}%</div>
          <div class="stat-desc">图片使用率</div>
        </div>
        <div class="stat-item">
          <div class="stat-icon">🆕</div>
          <div class="stat-label">今日上传</div>
          <div class="stat-value">{{ statistics.todayUploads || 0 }}</div>
          <div class="stat-desc">今天新增</div>
        </div>
      </div>
    </el-card>

    <el-card class="upload-card">
      <el-upload
        class="upload-demo"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        multiple
        accept="image/*"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 jpg/png/gif/webp 格式，单个文件不超过 10MB
          </div>
        </template>
      </el-upload>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="搜索文件名" clearable />
        </el-form-item>
        <el-form-item label="使用状态">
          <el-select v-model="queryForm.isUsed" placeholder="请选择" clearable>
            <el-option label="已使用" :value="1" />
            <el-option label="未使用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储类型">
          <el-select v-model="queryForm.storageType" placeholder="请选择" clearable>
            <el-option label="本地存储" value="local" />
            <el-option label="OSS" value="oss" />
            <el-option label="COS" value="cos" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            批量删除
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="gallery-card">
      <div class="gallery-header">
        <div class="gallery-actions">
          <el-button type="primary" size="small" @click="viewMode = 'grid'" :class="{ active: viewMode === 'grid' }">
            <el-icon><Grid /></el-icon> 网格视图
          </el-button>
          <el-button type="primary" size="small" @click="viewMode = 'list'" :class="{ active: viewMode === 'list' }">
            <el-icon><List /></el-icon> 列表视图
          </el-button>
          <el-button type="danger" size="small" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
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
              @click.stop="handlePreview(image)"
              lazy
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <div class="image-overlay">
              <el-button type="primary" size="small" circle @click.stop="handleCopyUrl(image)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
              <el-button type="danger" size="small" circle @click.stop="handleDelete(image)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div v-if="image.isUsed === 1" class="image-badge used">已使用</div>
            <div v-else class="image-badge unused">未使用</div>
            <el-checkbox
              v-model="selectedIds"
              :value="image.id"
              class="image-checkbox"
              @click.stop
            />
          </div>
          <div class="image-info">
            <div class="image-name" :title="image.originalName">{{ image.originalName }}</div>
            <div class="image-meta">
              <span>{{ formatSize(image.fileSize) }}</span>
              <span>{{ image.width }} × {{ image.height }}</span>
            </div>
            <div class="image-time">{{ image.createTime }}</div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <el-table
        v-if="viewMode === 'list'"
        :data="imageList"
        v-loading="loading"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="缩略图" width="100">
          <template #default="{ row }">
            <el-image
              :src="getImageUrl(row.thumbnailUrl || row.fileUrl)"
              :preview-src-list="[getImageUrl(row.fileUrl)]"
              fit="cover"
              style="width: 60px; height: 60px; cursor: pointer; border-radius: 4px;"
              lazy
            >
              <template #error>
                <div style="display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; background: #f5f7fa;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="尺寸" width="120">
          <template #default="{ row }">
            {{ row.width }} × {{ row.height }}
          </template>
        </el-table-column>
        <el-table-column prop="storageType" label="存储类型" width="100" />
        <el-table-column prop="isUsed" label="使用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isUsed === 1 ? 'success' : 'info'" size="small">
              {{ row.isUsed === 1 ? '已使用' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleCopyUrl(row)">复制链接</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[12, 24, 48, 96]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        class="pagination"
      />
    </el-card>
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
const viewMode = ref('grid') // 'grid' or 'list'

const queryForm = reactive({
  keyword: '',
  isUsed: null,
  storageType: '',
  page: 1,
  pageSize: 24 // 网格视图更适合 24 的倍数
})

const uploadUrl = computed(() => {
  return import.meta.env.VITE_API_BASE_URL + '/api/images/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    Authorization: `Bearer ${token}`
  }
})

const usageRate = computed(() => {
  if (!statistics.value.totalImages || statistics.value.totalImages === 0) {
    return 0
  }
  return ((statistics.value.usedImages / statistics.value.totalImages) * 100).toFixed(0)
})

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

// 处理图片URL，确保可以正确访问
const getImageUrl = (url) => {
  if (!url) return ''
  
  // 如果包含 localhost，替换为当前域名
  if (url.includes('localhost')) {
    // 提取路径部分（/uploads/...）
    const pathMatch = url.match(/\/uploads\/.+/)
    if (pathMatch) {
      return window.location.origin + pathMatch[0]
    }
  }
  
  // 如果是完整URL（非localhost），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 如果是相对路径，使用当前域名
  return window.location.origin + (url.startsWith('/') ? url : '/' + url)
}

const getStatistics = async () => {
  try {
    const { data } = await getImageStatistics()
    statistics.value = data
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const { data } = await queryImages(queryForm)
    imageList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('查询图片失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    keyword: '',
    isUsed: null,
    storageType: '',
    page: 1,
    pageSize: 24
  })
  handleQuery()
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  getStatistics()
  handleQuery()
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const toggleSelection = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

const handlePreview = (image) => {
  // 预览功能由 el-image 的 preview-src-list 处理
}

const handleCopyUrl = (row) => {
  navigator.clipboard.writeText(row.fileUrl)
  ElMessage.success('链接已复制到剪贴板')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这张图片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteImage(row.id)
      ElMessage.success('删除成功')
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 张图片吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteImages(selectedIds.value)
      ElMessage.success('批量删除成功')
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  })
}

onMounted(() => {
  getStatistics()
  handleQuery()
})
</script>

<style scoped>
.files-container {
  padding: 20px;
}

.statistics-card,
.upload-card,
.filter-card,
.table-card {
  margin-bottom: 20px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-value.success {
  color: #67c23a;
}

.stat-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.gallery-card {
  min-height: 600px;
}

.gallery-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gallery-actions {
  display: flex;
  gap: 10px;
}

.gallery-actions .el-button.active {
  background-color: #409eff;
  color: white;
}

/* 网格视图样式 */
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.image-card {
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.image-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.image-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%; /* 4:3 比例 */
  background: #f5f7fa;
  overflow: hidden;
}

.image-preview {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: zoom-in;
}

.image-preview :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 14px;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  z-index: 1;
}

.image-badge.used {
  background: #67c23a;
  color: white;
}

.image-badge.unused {
  background: #909399;
  color: white;
}

.image-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
}

.image-checkbox :deep(.el-checkbox__inner) {
  background: white;
  border-color: #dcdfe6;
}

.image-info {
  padding: 12px;
  background: white;
}

.image-name {
  font-size: 14px;
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
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.image-time {
  font-size: 12px;
  color: #c0c4cc;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .statistics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
}
</style>
