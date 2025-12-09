<template>
  <div class="crawler-container">
    <el-card class="header-card">
      <div class="header-content">
        <div class="header-left">
          <h2>🕷️ 爬虫管理</h2>
          <p class="subtitle">自动化采集网站资源</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建任务
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="任务名称">
          <el-input v-model="queryForm.name" placeholder="请输入任务名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTasks">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="table-card">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" min-width="150" />
        <el-table-column prop="targetUrl" label="目标URL" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="爬取间隔" width="120">
          <template #default="{ row }">
            {{ row.crawlInterval }}小时
          </template>
        </el-table-column>
        <el-table-column label="统计" width="180">
          <template #default="{ row }">
            <div class="stats">
              <span>总计: {{ row.totalCrawled }}</span>
              <span>成功: {{ row.totalSuccess }}</span>
              <span>失败: {{ row.totalFailed }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="成功率" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="calculateSuccessRate(row)" 
              :color="getProgressColor(row)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="下次执行" width="180">
          <template #default="{ row }">
            {{ formatTime(row.nextExecuteTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="primary" @click="handleTrigger(row)">
              立即爬取
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTasks"
          @current-change="loadTasks"
        />
      </div>
    </el-card>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="taskForm" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="目标URL" prop="targetUrl">
          <el-input v-model="taskForm.targetUrl" placeholder="请输入目标URL">
            <template #append>
              <el-button @click="handleValidateUrl" :loading="validating">验证</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="爬取间隔" prop="crawlInterval">
          <el-input-number 
            v-model="taskForm.crawlInterval" 
            :min="1" 
            :max="168"
            placeholder="小时"
          />
          <span class="form-tip">单位：小时，范围：1-168</span>
        </el-form-item>

        <el-form-item label="最大深度" prop="maxDepth">
          <el-input-number 
            v-model="taskForm.maxDepth" 
            :min="1" 
            :max="5"
          />
          <span class="form-tip">爬取深度，范围：1-5</span>
        </el-form-item>

        <el-form-item label="智能模式" prop="intelligentMode">
          <el-switch v-model="taskForm.intelligentMode" />
          <span class="form-tip">启用智能解析，自动识别网站结构</span>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="taskForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 分类映射 -->
        <el-form-item label="分类映射">
          <el-button size="small" @click="showCategoryMapping = !showCategoryMapping">
            {{ showCategoryMapping ? '隐藏' : '配置' }}分类映射
          </el-button>
        </el-form-item>

        <div v-if="showCategoryMapping" class="mapping-section">
          <el-form-item 
            v-for="(item, index) in taskForm.categoryMapping" 
            :key="index"
            :label="`映射${index + 1}`"
          >
            <el-row :gutter="10">
              <el-col :span="10">
                <el-input v-model="item.sourceCategory" placeholder="源分类" />
              </el-col>
              <el-col :span="10">
                <el-input-number v-model="item.targetCategoryId" placeholder="目标分类ID" />
              </el-col>
              <el-col :span="4">
                <el-button type="danger" size="small" @click="removeCategoryMapping(index)">
                  删除
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <el-button size="small" @click="addCategoryMapping">添加映射</el-button>
        </div>

        <!-- 自定义规则 -->
        <el-form-item label="自定义规则">
          <el-button size="small" @click="showCustomRules = !showCustomRules">
            {{ showCustomRules ? '隐藏' : '配置' }}自定义规则
          </el-button>
        </el-form-item>

        <div v-if="showCustomRules" class="rules-section">
          <el-form-item label="列表页选择器">
            <el-input v-model="taskForm.customRules.listPageSelector" placeholder="CSS选择器" />
          </el-form-item>
          <el-form-item label="详情页选择器">
            <el-input v-model="taskForm.customRules.detailPageSelector" placeholder="CSS选择器" />
          </el-form-item>
          <el-form-item label="标题选择器">
            <el-input v-model="taskForm.customRules.titleSelector" placeholder="CSS选择器" />
          </el-form-item>
          <el-form-item label="描述选择器">
            <el-input v-model="taskForm.customRules.descriptionSelector" placeholder="CSS选择器" />
          </el-form-item>
          <el-form-item label="图片选择器">
            <el-input v-model="taskForm.customRules.imageSelector" placeholder="CSS选择器" />
          </el-form-item>
          <el-form-item label="下载链接选择器">
            <el-input v-model="taskForm.customRules.downloadLinkSelector" placeholder="CSS选择器" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行日志对话框 -->
    <el-dialog v-model="logDialogVisible" title="执行日志" width="1000px">
      <el-table :data="logs" v-loading="logLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getLogStatusType(row.status)">
              {{ getLogStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="200">
          <template #default="{ row }">
            <div class="stats">
              <span>爬取: {{ row.crawledCount }}</span>
              <span>成功: {{ row.successCount }}</span>
              <span>失败: {{ row.failedCount }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(秒)" width="100" />
        <el-table-column prop="errorType" label="错误类型" width="120" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
        <el-table-column label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  queryTasks,
  createTask,
  updateTask,
  deleteTask,
  toggleTaskStatus,
  triggerTask,
  validateUrl,
  queryLogs
} from '../api/modules/crawler'

// 数据
const loading = ref(false)
const tasks = ref([])
const total = ref(0)

const queryForm = reactive({
  page: 1,
  size: 10,
  name: '',
  status: null
})

// 对话框
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

// 日志
const logDialogVisible = ref(false)
const logLoading = ref(false)
const logs = ref([])

// 方法
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await queryTasks(queryForm)
    tasks.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载任务列表失败')
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
    status: row.status,
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
  taskForm.name = ''
  taskForm.targetUrl = ''
  taskForm.crawlInterval = 24
  taskForm.maxDepth = 2
  taskForm.intelligentMode = true
  taskForm.status = 1
  taskForm.categoryMapping = []
  taskForm.customRules = {
    listPageSelector: '',
    detailPageSelector: '',
    titleSelector: '',
    descriptionSelector: '',
    imageSelector: '',
    downloadLinkSelector: ''
  }
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
    const res = await validateUrl(taskForm.targetUrl)
    if (res.data.valid) {
      ElMessage.success('URL验证成功')
    } else {
      ElMessage.error('URL无效或无法访问')
    }
  } catch (error) {
    ElMessage.error('URL验证失败')
  } finally {
    validating.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateTask(editId.value, taskForm)
      ElMessage.success('更新成功')
    } else {
      await createTask(taskForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleTaskStatus(row.id)
    ElMessage.success('状态切换成功')
    loadTasks()
  } catch (error) {
    ElMessage.error('状态切换失败')
  }
}

const handleTrigger = async (row) => {
  try {
    await triggerTask(row.id)
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
      await deleteTask(row.id, false)
      ElMessage.success('删除成功')
      loadTasks()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
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

const getLogStatusType = (status) => {
  const map = { 1: 'info', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getLogStatusText = (status) => {
  const map = { 1: '执行中', 2: '成功', 3: '失败' }
  return map[status] || '未知'
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.crawler-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.mapping-section,
.rules-section {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}
</style>
