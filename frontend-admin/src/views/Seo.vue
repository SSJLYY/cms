<template>
  <div class="seo-container">
    <el-card class="statistics-card">
      <div class="statistics-grid">
        <div class="stat-item">
          <div class="stat-label">总提交数</div>
          <div class="stat-value">{{ statistics.totalSubmissions || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">成功提交</div>
          <div class="stat-value success">{{ statistics.successSubmissions || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">失败提交</div>
          <div class="stat-value error">{{ statistics.failedSubmissions || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日提交</div>
          <div class="stat-value">{{ statistics.todaySubmissions || 0 }}</div>
        </div>
      </div>
    </el-card>

    <el-card class="action-card">
      <el-space>
        <el-button type="primary" @click="handleGenerateSitemap">生成网站地图</el-button>
        <el-button type="success" @click="handleSubmitBaidu">提交到百度</el-button>
        <el-button type="warning" @click="handleSubmitBing">提交到必应</el-button>
        <el-button @click="handleBatchSubmit">批量提交</el-button>
      </el-space>
    </el-card>

    <el-card class="config-card">
      <template #header>
        <span>Nginx配置示例</span>
      </template>
      <el-alert
        title="将以下配置添加到Nginx配置文件中，使sitemap.xml可访问"
        type="info"
        :closable="false"
      />
      <pre class="nginx-config">
location /sitemap.xml {
    alias /path/to/sitemap.xml;
    add_header Content-Type application/xml;
}
      </pre>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>提交历史</span>
          <el-button type="text" @click="loadHistory">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="historyList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="engine" label="搜索引擎" width="120">
          <template #default="{ row }">
            <el-tag :type="row.engine === 'baidu' ? 'success' : 'warning'">
              {{ row.engine === 'baidu' ? '百度' : '必应' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="提交URL" min-width="300" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseMessage" label="响应消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleResubmit(row)">重新提交</el-button>
            <el-button type="text" @click="handleDelete(row)" style="color: #f56c6c">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadHistory"
        @current-change="loadHistory"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/index'

const statistics = ref({})
const historyList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getStatistics = async () => {
  try {
    const { data } = await request({
      url: '/api/seo/statistics',
      method: 'get'
    })
    statistics.value = data
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    const { data } = await request({
      url: '/api/seo/history',
      method: 'get',
      params: { page: page.value, pageSize: pageSize.value }
    })
    historyList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载提交历史失败')
  } finally {
    loading.value = false
  }
}

const handleGenerateSitemap = async () => {
  try {
    const { data } = await request({
      url: '/api/seo/sitemap/generate',
      method: 'post'
    })
    ElMessage.success('网站地图生成成功: ' + data)
  } catch (error) {
    ElMessage.error('生成网站地图失败')
  }
}

const handleSubmitBaidu = async () => {
  try {
    await request({
      url: '/api/seo/submit/baidu',
      method: 'post'
    })
    ElMessage.success('提交到百度成功')
    loadHistory()
    getStatistics()
  } catch (error) {
    ElMessage.error('提交到百度失败')
  }
}

const handleSubmitBing = async () => {
  try {
    await request({
      url: '/api/seo/submit/bing',
      method: 'post'
    })
    ElMessage.success('提交到必应成功')
    loadHistory()
    getStatistics()
  } catch (error) {
    ElMessage.error('提交到必应失败')
  }
}

const handleBatchSubmit = async () => {
  ElMessageBox.confirm('确定要批量提交到所有搜索引擎吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: '/api/seo/submit/batch',
        method: 'post'
      })
      ElMessage.success('批量提交成功')
      loadHistory()
      getStatistics()
    } catch (error) {
      ElMessage.error('批量提交失败')
    }
  })
}

const handleResubmit = async (row) => {
  try {
    await request({
      url: `/api/seo/resubmit/${row.id}`,
      method: 'post'
    })
    ElMessage.success('重新提交成功')
    loadHistory()
  } catch (error) {
    ElMessage.error('重新提交失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/api/seo/history/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadHistory()
      getStatistics()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  getStatistics()
  loadHistory()
})
</script>

<style scoped>
.seo-container {
  padding: 20px;
}

.statistics-card,
.action-card,
.config-card,
.table-card {
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

.nginx-config {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
  overflow-x: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
