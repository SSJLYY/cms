<template>
  <div class="logs-container">
    <el-card class="statistics-card">
      <div class="statistics-grid">
        <div class="stat-item">
          <div class="stat-label">总日志数</div>
          <div class="stat-value">{{ statistics.totalLogs || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日日志</div>
          <div class="stat-value">{{ statistics.todayLogs || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">成功日志</div>
          <div class="stat-value success">{{ statistics.successLogs || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">失败日志</div>
          <div class="stat-value error">{{ statistics.errorLogs || 0 }}</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="模块">
          <el-input v-model="queryForm.module" placeholder="请输入模块" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="queryForm.type" placeholder="请输入类型" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="搜索关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="warning" @click="handleExport">导出</el-button>
          <el-button type="danger" @click="handleClean">清理日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentLog.type }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.duration }}ms</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre>{{ currentLog.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应数据" :span="2" v-if="currentLog.responseData">
          <pre>{{ currentLog.responseData }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <el-text type="danger">{{ currentLog.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
    const { data } = await exportLogs(queryForm)
    ElMessage.success('导出成功: ' + data)
  } catch (error) {
    ElMessage.error('导出失败')
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
      const { data } = await cleanLogs(value + ' 00:00:00')
      ElMessage.success(`成功清理 ${data} 条日志`)
      handleQuery()
      getStatistics()
    } catch (error) {
      ElMessage.error('清理失败')
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
  padding: 20px;
}

.statistics-card {
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

.stat-value.error {
  color: #f56c6c;
}

.filter-card,
.table-card {
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

pre {
  max-height: 300px;
  overflow: auto;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
