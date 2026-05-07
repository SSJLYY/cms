<template>
  <div class="crawler-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 8V7l-3 2-3-2v1l3 2 3-2z"/>
              <path d="M3 16v3h3"/>
              <path d="M21 19v-3h-3"/>
              <path d="M3 11v-3h3"/>
              <path d="M21 14v3h-3"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
            爬虫管理
          </h2>
          <p class="subtitle">自动化采集网站资源，智能识别页面结构</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showCreateDialog" class="gradient-btn">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            新建任务
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input
          v-model="queryForm.name"
          placeholder="任务名称"
          clearable
          class="filter-input"
        />
        <el-select
          v-model="queryForm.status"
          placeholder="状态"
          clearable
          class="filter-select"
        >
          <el-option label="全部" :value="null" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="loadTasks" class="search-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          查询
        </el-button>
        <el-button @click="resetQuery">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="1 4 1 10 7 10"/>
            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
          </svg>
          重置
        </el-button>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="table-card">
      <el-table :data="tasks" v-loading="loading" stripe class="modern-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="任务名称" min-width="150">
          <template #default="{ row }">
            <div class="task-name">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4"/>
              </svg>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="targetUrl" label="目标URL" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="url-code">{{ row.targetUrl }}</code>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="dark">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="爬取间隔" width="100" align="center">
          <template #default="{ row }">
            <span class="interval-value">{{ row.crawlInterval }}h</span>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="180">
          <template #default="{ row }">
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-label">总计</span>
                <span class="stat-value">{{ row.totalCrawled || 0 }}</span>
              </div>
              <div class="stat-item success">
                <span class="stat-label">成功</span>
                <span class="stat-value">{{ row.totalSuccess || 0 }}</span>
              </div>
              <div class="stat-item danger">
                <span class="stat-label">失败</span>
                <span class="stat-value">{{ row.totalFailed || 0 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="calculateSuccessRate(row)"
              :color="getProgressColor(row)"
              :stroke-width="8"
              style="width: 80px; display: inline-block;"
            />
          </template>
        </el-table-column>
        <el-table-column label="下次执行" width="160" align="center">
          <template #default="{ row }">
            <span class="time-value">{{ formatTime(row.nextExecuteTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="showEditDialog(row)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9"/>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
                编辑
              </el-button>
              <el-button size="small" type="success" link @click="handleTrigger(row)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
                爬取
              </el-button>
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTasks"
          @current-change="loadTasks"
          background
        />
      </div>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
      class="task-dialog"
      destroy-on-close
    >
      <el-form :model="taskForm" :rules="rules" ref="formRef" label-width="120px" class="task-form">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="目标URL" prop="targetUrl">
          <el-input v-model="taskForm.targetUrl" placeholder="https://example.com">
            <template #append>
              <el-button @click="handleValidateUrl" :loading="validating" class="validate-btn">验证</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="爬取间隔" prop="crawlInterval">
          <el-input-number v-model="taskForm.crawlInterval" :min="1" :max="168" />
          <span class="form-tip">小时（1-168）</span>
        </el-form-item>

        <el-form-item label="最大深度" prop="maxDepth">
          <el-input-number v-model="taskForm.maxDepth" :min="1" :max="5" />
          <span class="form-tip">深度（1-5）</span>
        </el-form-item>

        <el-form-item label="智能模式" prop="intelligentMode">
          <el-switch v-model="taskForm.intelligentMode" />
          <span class="form-tip">自动识别网站结构</span>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="taskForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 分类映射 -->
        <el-divider content-position="left">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
          分类映射
        </el-divider>

        <div class="mapping-section">
          <el-form-item
            v-for="(item, index) in taskForm.categoryMapping"
            :key="index"
            :label="`映射${index + 1}`"
          >
            <el-row :gutter="10">
              <el-col :span="9">
                <el-input v-model="item.sourceCategory" placeholder="源分类" />
              </el-col>
              <el-col :span="9">
                <el-input-number v-model="item.targetCategoryId" placeholder="目标分类ID" style="width: 100%" />
              </el-col>
              <el-col :span="6">
                <el-button type="danger" size="small" @click="removeCategoryMapping(index)">删除</el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-button size="small" @click="addCategoryMapping" class="add-mapping-btn">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加映射
          </el-button>
        </div>

        <!-- 自定义规则 -->
        <el-divider content-position="left">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 18 22 12 16 6"/>
            <polyline points="8 6 2 12 8 18"/>
          </svg>
          自定义规则
        </el-divider>

        <div class="rules-section">
          <el-form-item label="列表页选择器">
            <el-input v-model="taskForm.customRules.listPageSelector" placeholder=".list-item" />
          </el-form-item>
          <el-form-item label="详情页选择器">
            <el-input v-model="taskForm.customRules.detailPageSelector" placeholder=".detail-page" />
          </el-form-item>
          <el-form-item label="标题选择器">
            <el-input v-model="taskForm.customRules.titleSelector" placeholder=".title" />
          </el-form-item>
          <el-form-item label="描述选择器">
            <el-input v-model="taskForm.customRules.descriptionSelector" placeholder=".description" />
          </el-form-item>
          <el-form-item label="图片选择器">
            <el-input v-model="taskForm.customRules.imageSelector" placeholder="img.src" />
          </el-form-item>
          <el-form-item label="下载链接选择器">
            <el-input v-model="taskForm.customRules.downloadLinkSelector" placeholder=".download-link" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting" class="submit-btn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  queryTasks,
  createTask,
  updateTask,
  deleteTask,
  toggleTaskStatus,
  triggerTask,
  validateUrl
} from '../api/modules/crawler'

const loading = ref(false)
const tasks = ref([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  name: '',
  status: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const validating = ref(false)

const taskForm = reactive({
  name: '',
  targetUrl: '',
  crawlInterval: 24,
  maxDepth: 2,
  intelligentMode: true,
  status: 1,
  categoryMapping: [],
  customRules: {
    listPageSelector: '',
    detailPageSelector: '',
    titleSelector: '',
    descriptionSelector: '',
    imageSelector: '',
    downloadLinkSelector: ''
  }
})

const showCategoryMapping = ref(false)
const showCustomRules = ref(false)

const formRef = ref(null)
const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  targetUrl: [
    { required: true, message: '请输入目标URL', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL', trigger: 'blur' }
  ],
  crawlInterval: [{ required: true, message: '请输入爬取间隔', trigger: 'blur' }],
  maxDepth: [{ required: true, message: '请输入最大深度', trigger: 'blur' }]
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await queryTasks(queryForm)
    tasks.value = Array.isArray(res?.data?.records) ? res.data.records : []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.name = ''
  queryForm.status = null
  queryForm.page = 1
  loadTasks()
}

const showCreateDialog = () => {
  dialogTitle.value = '新建任务'
  isEdit.value = false
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogTitle.value = '编辑任务'
  isEdit.value = true
  editId.value = row.id

  Object.assign(taskForm, {
    name: row.name,
    targetUrl: row.targetUrl,
    crawlInterval: row.crawlInterval,
    maxDepth: row.maxDepth,
    intelligentMode: row.intelligentMode,
    status: row.status ? 1 : 0,
    categoryMapping: row.categoryMapping || [],
    customRules: row.customRules || {
      listPageSelector: '',
      detailPageSelector: '',
      titleSelector: '',
      descriptionSelector: '',
      imageSelector: '',
      downloadLinkSelector: ''
    }
  })

  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(taskForm, {
    name: '',
    targetUrl: '',
    crawlInterval: 24,
    maxDepth: 2,
    intelligentMode: true,
    status: 1,
    categoryMapping: [],
    customRules: {
      listPageSelector: '',
      detailPageSelector: '',
      titleSelector: '',
      descriptionSelector: '',
      imageSelector: '',
      downloadLinkSelector: ''
    }
  })
  showCategoryMapping.value = false
  showCustomRules.value = false
}

const handleValidateUrl = async () => {
  if (!taskForm.targetUrl) {
    ElMessage.warning('请先输入URL')
    return
  }

  validating.value = true
  try {
    const res = await validateUrl(taskForm.targetUrl, { skipBusinessErrorMessage: true })
    if (res?.data?.valid) {
      ElMessage.success(res?.message || 'URL验证成功')
    } else {
      ElMessage.error(res?.message || 'URL无效或无法访问')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'URL验证失败')
  } finally {
    validating.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTask(editId.value, taskForm, { skipBusinessErrorMessage: true })
      ElMessage.success('更新成功')
    } else {
      await createTask(taskForm, { skipBusinessErrorMessage: true })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleTaskStatus(row.id, { skipBusinessErrorMessage: true })
    ElMessage.success('状态切换成功')
    loadTasks()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '状态切换失败')
  }
}

const handleTrigger = async (row) => {
  try {
    await triggerTask(row.id, { skipBusinessErrorMessage: true })
    ElMessage.success('任务已触发，正在执行中...')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '触发失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTask(row.id, false, { skipBusinessErrorMessage: true })
      ElMessage.success('删除成功')
      loadTasks()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const addCategoryMapping = () => {
  taskForm.categoryMapping.push({
    sourceCategory: '',
    targetCategoryId: null
  })
}

const removeCategoryMapping = (index) => {
  taskForm.categoryMapping.splice(index, 1)
}

const calculateSuccessRate = (row) => {
  if (row.totalCrawled === 0) return 0
  return Math.round((row.totalSuccess / row.totalCrawled) * 100)
}

const getProgressColor = (row) => {
  const rate = calculateSuccessRate(row)
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.crawler-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 12px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.gradient-btn {
  background: white;
  border: none;
  color: #11998e;
  font-weight: 600;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

/* 筛选工具栏 */
.filter-bar {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-row {
  display: flex;
  gap: 12px;
}

.filter-input {
  width: 200px;
}

.filter-select {
  width: 140px;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

/* 表格 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.modern-table :deep(.el-table__header th) {
  background: #f8f9fc;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
}

.modern-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.modern-table :deep(.el-table__row:hover) {
  background: #f5f7fa;
}

.task-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
  font-weight: 600;
}

.url-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  color: #667eea;
}

.interval-value {
  font-weight: 600;
  color: #667eea;
}

.stats-grid {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 11px;
  color: #909399;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
}

.stat-item.success .stat-value {
  color: #67c23a;
}

.stat-item.danger .stat-value {
  color: #f56c6c;
}

.time-value {
  font-size: 13px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 对话框 */
.task-form :deep(.el-input__wrapper),
.task-form :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.validate-btn {
  font-weight: 600;
}

.mapping-section,
.rules-section {
  padding: 16px;
  background: #f8f9fc;
  border-radius: 12px;
  margin-bottom: 16px;
}

.add-mapping-btn {
  margin-top: 8px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

@media (max-width: 1200px) {
  .filter-bar {
    flex-direction: column;
    gap: 16px;
  }

  .filter-row,
  .filter-actions {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}
</style>
