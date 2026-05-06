<template>
  <div class="seo-container">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statsConfig" :key="stat.key">
        <div class="stat-icon-wrap" :style="{ background: stat.bg }">
          <span class="stat-emoji">{{ stat.icon }}</span>
        </div>
        <div class="stat-body">
          <div class="stat-value" :style="{ color: stat.color }">{{ stat.val }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <div class="section-header">
        <span class="section-icon">⚡</span>
        SEO 操作
      </div>
      <div class="action-buttons">
        <button class="action-btn sitemap" @click="handleGenerateSitemap">
          <span class="btn-icon">🗺️</span>
          <span class="btn-label">生成网站地图</span>
          <span class="btn-sub">sitemap.xml</span>
        </button>
        <button class="action-btn baidu" @click="handleSubmitBaidu">
          <span class="btn-icon">🔵</span>
          <span class="btn-label">提交到百度</span>
          <span class="btn-sub">Baidu SEO</span>
        </button>
        <button class="action-btn bing" @click="handleSubmitBing">
          <span class="btn-icon">🟢</span>
          <span class="btn-label">提交到必应</span>
          <span class="btn-sub">Bing SEO</span>
        </button>
        <button class="action-btn batch" @click="handleBatchSubmit">
          <span class="btn-icon">🚀</span>
          <span class="btn-label">批量提交</span>
          <span class="btn-sub">全搜索引擎</span>
        </button>
      </div>
    </div>

    <!-- Nginx配置 -->
    <div class="config-section">
      <div class="section-header">
        <span class="section-icon">⚙️</span>
        Nginx 配置示例
        <el-tag type="info" size="small" round style="margin-left:8px;">参考配置</el-tag>
      </div>
      <div class="config-tip">
        <el-icon style="color:#667eea;margin-right:6px;"><InfoFilled /></el-icon>
        将以下配置添加到 Nginx 配置文件中，使 sitemap.xml 可访问
      </div>
      <pre class="nginx-code"><span class="code-keyword">location</span> <span class="code-path">/sitemap.xml</span> {
    <span class="code-key">alias</span> <span class="code-value">/path/to/sitemap.xml</span>;
    <span class="code-key">add_header</span> <span class="code-value">Content-Type application/xml</span>;
}</pre>
    </div>

    <!-- 提交历史表格 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <span class="title-icon">📜</span>
          提交历史
        </div>
        <el-button class="btn-refresh" @click="loadHistory">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>

      <el-table :data="historyList" v-loading="loading" class="modern-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="搜索引擎" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.engine === 'baidu' ? 'success' : 'warning'"
              round size="small"
            >
              {{ row.engine === 'baidu' ? '🔵 百度' : '🟢 必应' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="提交URL" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'SUCCESS' ? 'success' : 'danger'"
              round size="small"
            >
              {{ row.status === 'SUCCESS' ? '✅ 成功' : '❌ 失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseMessage" label="响应消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleResubmit(row)">
              <el-icon><Refresh /></el-icon> 重新提交
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
          @size-change="loadHistory"
          @current-change="loadHistory"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, InfoFilled } from '@element-plus/icons-vue'
import request from '@/api/request'

const statistics = ref({})
const historyList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const statsConfig = computed(() => [
  {
    key: 'total', icon: '📊', label: '总提交数',
    val: statistics.value.totalSubmissions || 0,
    bg: 'linear-gradient(135deg, #667eea22, #764ba222)', color: '#667eea'
  },
  {
    key: 'success', icon: '✅', label: '成功提交',
    val: statistics.value.successSubmissions || 0,
    bg: 'linear-gradient(135deg, #43e97b22, #38f9d722)', color: '#43e97b'
  },
  {
    key: 'failed', icon: '❌', label: '失败提交',
    val: statistics.value.failedSubmissions || 0,
    bg: 'linear-gradient(135deg, #f5222d22, #ff787522)', color: '#f5222d'
  },
  {
    key: 'today', icon: '🆕', label: '今日提交',
    val: statistics.value.todaySubmissions || 0,
    bg: 'linear-gradient(135deg, #fa709a22, #fee14022)', color: '#fa709a'
  }
])

const getStatistics = async () => {
  try {
    const { data } = await request({ url: '/api/seo/statistics', method: 'get', skipBusinessErrorMessage: true })
    statistics.value = data
  } catch (error) {
    ElMessage.error('获取统计信息失败')
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
      const { data } = await request({
        url: '/api/seo/history', method: 'get',
        params: { page: page.value, pageSize: pageSize.value },
        skipBusinessErrorMessage: true
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
    const { data } = await request({ url: '/api/seo/sitemap/generate', method: 'post', skipBusinessErrorMessage: true })
    ElMessage.success('网站地图生成成功: ' + data)
  } catch (error) {
    ElMessage.error('生成网站地图失败')
  }
}

const handleSubmitBaidu = async () => {
  try {
    await request({ url: '/api/seo/submit/baidu', method: 'post', skipBusinessErrorMessage: true })
    ElMessage.success('提交到百度成功')
    loadHistory(); getStatistics()
  } catch (error) {
    ElMessage.error('提交到百度失败')
  }
}

const handleSubmitBing = async () => {
  try {
    await request({ url: '/api/seo/submit/bing', method: 'post', skipBusinessErrorMessage: true })
    ElMessage.success('提交到必应成功')
    loadHistory(); getStatistics()
  } catch (error) {
    ElMessage.error('提交到必应失败')
  }
}

const handleBatchSubmit = async () => {
  ElMessageBox.confirm('确定要批量提交到所有搜索引擎吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await request({ url: '/api/seo/submit/batch', method: 'post', skipBusinessErrorMessage: true })
      ElMessage.success('批量提交成功')
      loadHistory(); getStatistics()
    } catch (error) {
      ElMessage.error('批量提交失败')
    }
  })
}

const handleResubmit = async (row) => {
  try {
    await request({ url: `/api/seo/resubmit/${row.id}`, method: 'post', skipBusinessErrorMessage: true })
    ElMessage.success('重新提交成功')
    loadHistory()
  } catch (error) {
    ElMessage.error('重新提交失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      await request({ url: `/api/seo/history/${row.id}`, method: 'delete', skipBusinessErrorMessage: true })
      ElMessage.success('删除成功')
      loadHistory(); getStatistics()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => { getStatistics(); loadHistory() })
</script>

<style scoped>
.seo-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100%;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
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

.stat-emoji { font-size: 24px; }

.stat-body { flex: 1; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #606266;
}

/* 操作区域 */
.action-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.section-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.section-icon { font-size: 18px; }

.action-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px 16px;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.action-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.action-btn.sitemap {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.action-btn.baidu {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.action-btn.bing {
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  color: white;
}

.action-btn.batch {
  background: linear-gradient(135deg, #fa709a, #fee140);
  color: white;
}

.btn-icon { font-size: 28px; }
.btn-label { font-size: 14px; font-weight: 600; }
.btn-sub { font-size: 11px; opacity: 0.85; }

/* 配置区域 */
.config-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.config-tip {
  display: flex;
  align-items: center;
  background: rgba(102,126,234,0.08);
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}

.nginx-code {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 20px 24px;
  border-radius: 12px;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.8;
  overflow-x: auto;
  margin: 0;
}

.code-keyword { color: #cba6f7; font-weight: 600; }
.code-path { color: #89b4fa; }
.code-key { color: #89dceb; }
.code-value { color: #a6e3a1; }

/* 表格卡片 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon { font-size: 18px; }

.btn-refresh { border-radius: 10px !important; }

.modern-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

.modern-table :deep(.el-table__row:hover td) {
  background: rgba(102,126,234,0.04);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .action-buttons { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .seo-container { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .action-buttons { grid-template-columns: 1fr 1fr; }
}
</style>
