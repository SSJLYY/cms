<template>
  <div class="statistics-container">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overviewData.totalDownloads || 0 }}</div>
          <div class="stat-label">总下载量</div>
        </div>
        <div class="stat-trend trend-up">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
      </div>
      
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overviewData.totalVisits || 0 }}</div>
          <div class="stat-label">总访问量</div>
        </div>
        <div class="stat-trend trend-up">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
      </div>
      
      <div class="stat-card stat-danger">
        <div class="stat-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overviewData.newVisits || 0 }}</div>
          <div class="stat-label">新增访问</div>
        </div>
        <div class="stat-badge">今日</div>
      </div>
      
      <div class="stat-card stat-primary">
        <div class="stat-content">
          <div class="stat-label">统计周期</div>
          <el-select v-model="statsPeriod" size="large" class="period-select">
            <el-option label="今天" value="today" />
            <el-option label="昨天" value="yesterday" />
            <el-option label="近7天" value="week" />
            <el-option label="近30天" value="month" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 下载分布图 -->
      <div class="chart-card chart-large">
        <div class="card-header">
          <h3>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.21 15.89A10 10 0 1 1 8 2.83"/>
              <path d="M22 12A10 10 0 0 0 12 2v10z"/>
            </svg>
            网站下载分布
          </h3>
          <el-button type="primary" link @click="handleRefreshChart">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            刷新
          </el-button>
        </div>
        <div ref="downloadChartRef" style="height: 350px"></div>
      </div>
      
      <!-- 下载排行榜 -->
      <div class="chart-card">
        <div class="card-header">
          <h3>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            下载排行榜
          </h3>
          <el-tag type="success" size="small" effect="dark">TOP 5</el-tag>
        </div>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in topDownloads" 
            :key="index"
            class="ranking-item"
          >
            <div class="ranking-number" :class="`rank-${index + 1}`">
              {{ index + 1 }}
            </div>
            <div class="ranking-content">
              <div class="ranking-title">{{ item.name }}</div>
              <div class="ranking-meta">
                <el-tag size="small" type="info" effect="plain">{{ item.category }}</el-tag>
                <span class="ranking-count">{{ item.downloads }}次</span>
              </div>
            </div>
            <div class="ranking-trend" :class="item.trend === 'up' ? 'trend-up' : 'trend-down'">
              <svg v-if="item.trend === 'up'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="18 15 12 9 6 15"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 访问统计表格 -->
    <div class="table-card">
      <div class="card-header">
        <h3>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
          </svg>
          访问统计详情
        </h3>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="handleExport" class="export-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            导出数据
          </el-button>
          <el-button type="success" size="small" @click="handleRefresh">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table :data="visitStats" style="width: 100%" v-loading="tableLoading" stripe class="modern-table">
        <el-table-column type="index" label="排名" width="80" align="center">
          <template #default="{ $index }">
            <el-tag 
              :type="$index < 3 ? 'danger' : 'info'" 
              size="small"
              effect="dark"
              class="rank-tag"
            >
              {{ $index + 1 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" min-width="200">
          <template #default="{ row }">
            <div class="resource-cell">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              <span>{{ row.resource }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="来源地址" min-width="250">
          <template #default="{ row }">
            <el-link
              v-if="isExternalReferer(row.referer)"
              :href="row.referer"
              target="_blank"
              type="primary"
              :underline="false"
              class="referer-link"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              {{ row.referer }}
            </el-link>
            <span v-else>{{ row.referer }}</span>
          </template>
        </el-table-column>
        <el-table-column label="浏览器" width="150" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.browser }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getCategoryColor(row.category)" size="small">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visits" label="访问次数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="success" effect="dark">{{ row.visits }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>

    <!-- 实时访问监控 -->
    <div class="monitor-card">
      <div class="card-header">
        <h3>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
          实时访问监控
        </h3>
        <el-tag type="success" size="small" effect="dark" class="realtime-tag">
          <span class="pulse"></span>
          实时更新
        </el-tag>
      </div>
      
      <div class="monitor-content">
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in realtimeActivities"
            :key="index"
            :timestamp="activity.timestamp"
            :type="getActivityTimelineType(activity.type)"
            placement="top"
            hollow
          >
            <el-card class="activity-card">
              <div class="activity-item">
                <div class="activity-icon" :class="`activity-${activity.type}`">
                  <svg v-if="activity.type === 'visit'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <svg v-else-if="activity.type === 'download'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  </svg>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                </div>
                <el-tag :type="activity.type === 'download' ? 'success' : 'info'" size="small" effect="dark">
                  {{ getActivityTypeName(activity.type) }}
                </el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getStatisticsOverview, 
  getDownloadDistribution, 
  getVisitDetails, 
  getRealtimeActivities 
} from '@/api/modules/statistics'

const statsPeriod = ref('today')
const downloadChartRef = ref(null)
let downloadChart = null
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const overviewData = ref({
  totalDownloads: 0,
  totalVisits: 0,
  newVisits: 0
})

const topDownloads = ref([])
const visitStats = ref([])
const realtimeActivities = ref([])

const getCategoryColor = (category) => {
  const colors = {
    '电脑软件': 'primary',
    'TV软件': 'success',
    '办公软件': 'warning',
    '安全软件': 'danger'
  }
  return colors[category] || 'info'
}

const getActivityTimelineType = (type) => {
  const types = {
    visit: 'primary',
    download: 'success',
    search: 'warning'
  }
  return types[type] || 'info'
}

const getActivityTypeName = (type) => {
  const names = {
    visit: '访问',
    download: '下载',
    search: '搜索'
  }
  return names[type] || '未知'
}

const isExternalReferer = (referer) => {
  return typeof referer === 'string' && /^(https?:)?\/\//i.test(referer.trim())
}

const loadOverview = async () => {
  try {
    const res = await getStatisticsOverview(statsPeriod.value)
    overviewData.value = res?.data || {
      totalDownloads: 0,
      totalVisits: 0,
      newVisits: 0
    }
  } catch (error) {
    ElMessage.error('加载统计概览失败')
  }
}

const loadDownloadDistribution = async () => {
  try {
    const res = await getDownloadDistribution(statsPeriod.value)
    const data = Array.isArray(res?.data) ? res.data : []

    topDownloads.value = data.slice(0, 5).map((item, index) => ({
      name: item.name,
      category: '电脑软件',
      downloads: item.value,
      trend: index % 2 === 0 ? 'up' : 'down'
    }))

    initDownloadChart(data)
  } catch (error) {
    ElMessage.error('加载下载分布失败')
  }
}

const loadVisitDetails = async () => {
  tableLoading.value = true
  try {
    const res = await getVisitDetails({
      period: statsPeriod.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    const records = res?.data?.records
    visitStats.value = Array.isArray(records) ? records : []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error('加载访问统计失败')
  } finally {
    tableLoading.value = false
  }
}

const loadRealtimeActivities = async () => {
  try {
    const res = await getRealtimeActivities(10)
    realtimeActivities.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    ElMessage.error('加载实时活动失败')
  }
}

const initDownloadChart = (data) => {
  if (!downloadChartRef.value) return
  
  if (!downloadChart) {
    downloadChart = echarts.init(downloadChartRef.value)
  }
  
  const colors = ['#667eea', '#f093fb', '#4facfe', '#00f2fe', '#11998e', '#38ef7d', '#f5576c', '#fa8c16']
  
  const chartData = data.map((item, index) => ({
    value: item.value,
    name: item.name,
    itemStyle: { color: colors[index % colors.length] }
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      textStyle: { color: '#303133' }
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { fontSize: 12 }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: chartData
      }
    ]
  }
  downloadChart.setOption(option)
}

const loadAllData = () => {
  loadOverview()
  loadDownloadDistribution()
  loadVisitDetails()
  loadRealtimeActivities()
}

const handleRefreshChart = () => {
  loadDownloadDistribution()
  ElMessage.success('图表已刷新')
}

const handleExport = () => {
  ElMessage.success('数据导出功能开发中...')
}

const handleRefresh = () => {
  loadAllData()
  ElMessage.success('数据已刷新')
}

const handleResize = () => {
  downloadChart?.resize()
}

watch(statsPeriod, () => {
  currentPage.value = 1
  loadAllData()
})

watch([currentPage, pageSize], () => {
  loadVisitDetails()
})

// 组件级定时器变量，确保 onUnmounted 能正确清理
let realtimeTimer = null

onMounted(() => {
  loadAllData()

  realtimeTimer = setInterval(() => {
    loadRealtimeActivities()
  }, 30000)

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
  }
  downloadChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.statistics-container {
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
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
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
  color: white;
  flex-shrink: 0;
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-warning .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-danger .stat-icon {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.stat-primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-trend {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trend-up {
  background: #ecfdf5;
  color: #10b981;
}

.trend-down {
  background: #fef2f2;
  color: #ef4444;
}

.stat-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #fef3c7;
  color: #d97706;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.period-select {
  width: 100%;
  margin-top: 8px;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.export-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
}

/* 排行榜 */
.ranking-list {
  padding: 8px 0;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fc;
  border-radius: 12px;
  transition: all 0.3s;
}

.ranking-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.ranking-number {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  margin-right: 16px;
  background: #e2e8f0;
  color: white;
}

.ranking-number.rank-1 {
  background: linear-gradient(135deg, #f5222d 0%, #ff7875 100%);
}

.ranking-number.rank-2 {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
}

.ranking-number.rank-3 {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.ranking-content {
  flex: 1;
}

.ranking-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ranking-count {
  font-size: 12px;
  color: #909399;
}

.ranking-trend {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ranking-trend.trend-up {
  background: #ecfdf5;
  color: #10b981;
}

.ranking-trend.trend-down {
  background: #fef2f2;
  color: #ef4444;
}

/* 表格 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
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

.rank-tag {
  font-weight: 700;
}

.resource-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
}

.referer-link {
  display: flex;
  align-items: center;
  gap: 6px;
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 实时监控 */
.monitor-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.realtime-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pulse {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.monitor-content {
  max-height: 400px;
  overflow-y: auto;
}

.activity-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-visit {
  background: #e0f2fe;
  color: #0284c7;
}

.activity-download {
  background: #dcfce7;
  color: #16a34a;
}

.activity-search {
  background: #fef3c7;
  color: #d97706;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 12px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 1200px) {
  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .statistics-cards {
    grid-template-columns: 1fr;
  }
}
</style>
