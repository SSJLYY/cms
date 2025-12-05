<template>
  <div class="statistics-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" style="border-left: 4px solid #52c41a;">
          <div class="stat-icon" style="background: #f6ffed; color: #52c41a;">
            <el-icon :size="28"><Download /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewData.totalDownloads }}</div>
            <div class="stat-label">总下载量</div>
            <div class="stat-desc">平均每日下载</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" style="border-left: 4px solid #faad14;">
          <div class="stat-icon" style="background: #fffbe6; color: #faad14;">
            <el-icon :size="28"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewData.totalVisits }}</div>
            <div class="stat-label">总访问量</div>
            <div class="stat-desc">独立访客IP数量</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" style="border-left: 4px solid #f5222d;">
          <div class="stat-icon" style="background: #fff1f0; color: #f5222d;">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewData.newVisits }}</div>
            <div class="stat-label">新增访问</div>
            <div class="stat-desc">今日新增</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card" style="border-left: 4px solid #1890ff;">
          <div class="stat-icon" style="background: #e6f7ff; color: #1890ff;">
            <el-icon :size="28"><Setting /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">统计周期</div>
            <el-select v-model="statsPeriod" size="small" style="width: 100%; margin-top: 5px;">
              <el-option label="今天" value="today" />
              <el-option label="昨天" value="yesterday" />
              <el-option label="近7天" value="week" />
              <el-option label="近30天" value="month" />
            </el-select>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 网站下载分布 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><PieChart /></el-icon> 网站下载分布</span>
              <el-button type="text" size="small" @click="handleRefreshChart">刷新</el-button>
            </div>
          </template>
          <div ref="downloadChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataLine /></el-icon> 下载排行榜（前5名）</span>
              <el-button type="text" size="small">实时刷新</el-button>
            </div>
          </template>
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
                  <el-tag size="small" type="info">{{ item.category }}</el-tag>
                  <span class="ranking-count">{{ item.downloads }}次</span>
                </div>
              </div>
              <div class="ranking-badge">
                <el-icon :size="20" :color="item.trend === 'up' ? '#52c41a' : '#f5222d'">
                  <Top v-if="item.trend === 'up'" />
                  <Bottom v-else />
                </el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 访问统计表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><DataAnalysis /></el-icon> 访问统计详情</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button type="success" size="small" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="visitStats" style="width: 100%" v-loading="tableLoading">
        <el-table-column type="index" label="排名" width="80" align="center">
          <template #default="{ $index }">
            <el-tag 
              :type="$index < 3 ? 'danger' : 'info'" 
              size="small"
              effect="dark"
            >
              {{ $index + 1 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resource" label="资源" min-width="200">
          <template #default="{ row }">
            <div class="resource-cell">
              <el-icon><Document /></el-icon>
              <span>{{ row.resource }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="来源地址" min-width="250">
          <template #default="{ row }">
            <el-link :href="row.referer" target="_blank" type="primary" :underline="false">
              <el-icon><Link /></el-icon>
              {{ row.referer }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="浏览器" width="150" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.browser }}</el-tag>
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
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 实时访问监控 -->
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><Monitor /></el-icon> 实时访问监控</span>
          <el-tag type="success" size="small">
            <el-icon><View /></el-icon>
            实时更新
          </el-tag>
        </div>
      </template>
      
      <div class="monitor-content">
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in realtimeActivities"
            :key="index"
            :timestamp="activity.timestamp"
            :type="activity.type"
            placement="top"
          >
            <el-card>
              <div class="activity-item">
                <el-icon :size="20" :color="getActivityColor(activity.type)">
                  <User v-if="activity.type === 'visit'" />
                  <Download v-else-if="activity.type === 'download'" />
                  <Search v-else />
                </el-icon>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-desc">{{ activity.description }}</div>
                </div>
                <el-tag :type="activity.type === 'download' ? 'success' : 'info'" size="small">
                  {{ getActivityTypeName(activity.type) }}
                </el-tag>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, Warning, TrendCharts, Setting, PieChart, DataLine, 
  DataAnalysis, Refresh, Document, Link, Monitor, View, User, 
  Search, Top, Bottom 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
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

// 统计概览数据
const overviewData = ref({
  totalDownloads: 0,
  totalVisits: 0,
  newVisits: 0
})

// 下载排行榜数据
const topDownloads = ref([])

// 访问统计数据
const visitStats = ref([])

// 实时活动数据
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

const getActivityColor = (type) => {
  const colors = {
    visit: '#1890ff',
    download: '#52c41a',
    search: '#faad14'
  }
  return colors[type] || '#909399'
}

const getActivityTypeName = (type) => {
  const names = {
    visit: '访问',
    download: '下载',
    search: '搜索'
  }
  return names[type] || '未知'
}

// 加载统计概览
const loadOverview = async () => {
  try {
    const res = await getStatisticsOverview(statsPeriod.value)
    if (res.code === 200) {
      overviewData.value = res.data
    }
  } catch (error) {
    console.error('加载统计概览失败:', error)
  }
}

// 加载下载分布数据
const loadDownloadDistribution = async () => {
  try {
    const res = await getDownloadDistribution(statsPeriod.value)
    if (res.code === 200) {
      const data = res.data || []
      
      // 更新排行榜（取前5名）
      topDownloads.value = data.slice(0, 5).map((item, index) => ({
        name: item.name,
        category: '电脑软件',
        downloads: item.value,
        trend: index % 2 === 0 ? 'up' : 'down'
      }))
      
      // 更新图表
      initDownloadChart(data)
    }
  } catch (error) {
    console.error('加载下载分布失败:', error)
  }
}

// 加载访问统计详情
const loadVisitDetails = async () => {
  tableLoading.value = true
  try {
    const res = await getVisitDetails({
      period: statsPeriod.value,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      visitStats.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载访问统计失败:', error)
  } finally {
    tableLoading.value = false
  }
}

// 加载实时活动
const loadRealtimeActivities = async () => {
  try {
    const res = await getRealtimeActivities(10)
    if (res.code === 200) {
      realtimeActivities.value = res.data || []
    }
  } catch (error) {
    console.error('加载实时活动失败:', error)
  }
}

// 初始化图表
const initDownloadChart = (data) => {
  if (!downloadChartRef.value) return
  
  if (!downloadChart) {
    downloadChart = echarts.init(downloadChartRef.value)
  }
  
  const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']
  
  const chartData = data.map((item, index) => ({
    value: item.value,
    name: item.name,
    itemStyle: { color: colors[index % colors.length] }
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: {
        fontSize: 12
      }
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
          borderWidth: 2
        },
        label: {
          show: false
        },
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

// 加载所有数据
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

// 监听统计周期变化
watch(statsPeriod, () => {
  loadAllData()
})

// 监听分页变化
watch([currentPage, pageSize], () => {
  loadVisitDetails()
})

onMounted(() => {
  // 初始加载数据
  loadAllData()
  
  // 定时刷新实时活动（每30秒）
  const timer = setInterval(() => {
    loadRealtimeActivities()
  }, 30000)
  
  window.addEventListener('resize', handleResize)
  
  // 清理定时器
  onUnmounted(() => {
    clearInterval(timer)
  })
})

onUnmounted(() => {
  downloadChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 3px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.chart-card, .table-card, .monitor-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.ranking-list {
  padding: 10px 0;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.ranking-item:hover {
  background: #e6e8eb;
  transform: translateX(5px);
}

.ranking-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  margin-right: 15px;
  background: #909399;
  color: #fff;
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
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
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

.ranking-badge {
  margin-left: 10px;
}

.resource-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.monitor-content {
  max-height: 500px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 12px;
  color: #909399;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .stats-row .el-col {
    margin-bottom: 15px;
  }
}
</style>
