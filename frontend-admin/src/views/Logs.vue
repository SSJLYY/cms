<template>
  <div class="logs-container">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
          <div class="stat-label">总日志数</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.todayLogs || 0 }}</div>
          <div class="stat-label">今日日志</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.successLogs || 0 }}</div>
          <div class="stat-label">成功日志</div>
        </div>
      </div>
      <div class="stat-card stat-danger">
        <div class="stat-icon">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.errorLogs || 0 }}</div>
          <div class="stat-label">失败日志</div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input
          v-model="queryForm.module"
          placeholder="模块"
          clearable
          class="filter-input"
        />
        <el-input
          v-model="queryForm.type"
          placeholder="类型"
          clearable
          class="filter-input"
        />
        <el-select
          v-model="queryForm.status"
          placeholder="状态"
          clearable
          class="filter-select"
        >
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="ERROR" />
        </el-select>
        <el-input
          v-model="queryForm.keyword"
          placeholder="搜索关键词"
          clearable
          class="filter-input search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleQuery" class="gradient-btn">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
        <el-button type="danger" @click="handleClean">
          <el-icon><Delete /></el-icon>
          清理
        </el-button>
      </div>
    </div>

    <!-- 日志表格 -->
    <div class="table-card">
      <el-table 
        :data="logList" 
        v-loading="loading" 
        stripe 
        class="modern-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="module" label="模块" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary" effect="plain">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <span class="type-label">{{ row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="url-code">{{ row.requestUrl }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span class="duration-value" :class="{ 'slow': row.duration > 1000 }">
              {{ row.duration }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-value">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
          background
        />
      </div>
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="800px" class="detail-dialog" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentLog" class="detail-descriptions">
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentLog.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'" effect="dark">
            {{ currentLog.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">
          <code>{{ currentLog.requestUrl }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="code-block">{{ currentLog.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2" v-if="currentLog.responseData">
          <pre class="code-block">{{ currentLog.responseData }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <el-text type="danger" class="error-text">{{ currentLog.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document, Calendar, CircleCheck, CircleClose, Search, Refresh,
  Download, Delete, View
} from '@element-plus/icons-vue'
import {
  getLogStatistics,
  queryLogs,
  cleanLogs,
  exportLogs
} from '@/api/modules/logs'

const statistics = ref({})
const logList = ref([])
const loading = ref(false)
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref(null)

const queryForm = reactive({
  module: '',
  type: '',
  status: '',
  keyword: '',
  page: 1,
  pageSize: 20
})

const getStatistics = async () => {
  try {
    const { data } = await getLogStatistics()
    statistics.value = data
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const { data } = await queryLogs(queryForm)
    logList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('查询日志失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    module: '',
    type: '',
    status: '',
    keyword: '',
    page: 1,
    pageSize: 20
  })
  handleQuery()
}

const handleView = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleExport = async () => {
  try {
    const { data } = await exportLogs(queryForm, { skipBusinessErrorMessage: true })
    ElMessage.success('导出成功: ' + data)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '导出失败')
  }
}

const handleClean = () => {
  ElMessageBox.prompt('请输入要清理的日期（清理此日期之前的日志）', '清理日志', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d{4}-\d{2}-\d{2}$/,
    inputErrorMessage: '日期格式不正确'
  }).then(async ({ value }) => {
    try {
      const { data } = await cleanLogs(value + ' 00:00:00', { skipBusinessErrorMessage: true })
      ElMessage.success(`成功清理 ${data} 条日志`)
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '清理失败')
    }
  })
}

onMounted(() => {
  getStatistics()
  handleQuery()
})
</script>

<style scoped>
.logs-container {
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

.stat-info .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-danger .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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

/* 筛选工具栏 */
.filter-bar {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-input {
  width: 160px;
}

.search-input {
  width: 220px;
}

.filter-select {
  width: 140px;
}

.filter-actions {
  display: flex;
  gap: 12px;
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

.type-label {
  font-weight: 500;
  color: #667eea;
}

.url-code {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  color: #667eea;
}

.duration-value {
  font-weight: 600;
  color: #67c23a;
}

.duration-value.slow {
  color: #f56c6c;
}

.time-value {
  font-size: 13px;
  color: #909399;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情对话框 */
.detail-descriptions :deep(.el-descriptions__label) {
  font-weight: 600;
  background: #f8f9fc;
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 8px;
  max-height: 200px;
  overflow: auto;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  margin: 0;
}

.error-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
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
  
  .filter-row {
    flex-direction: column;
  }
  
  .filter-input,
  .filter-select,
  .search-input {
    width: 100%;
  }
  
  .filter-actions {
    flex-wrap: wrap;
  }
}
</style>
