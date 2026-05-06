<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          <span class="wave">👋</span>
          欢迎回来，{{ username }}！
        </h1>
        <p class="welcome-desc">这里是您的数据中心，随时掌握平台动态</p>
      </div>
      <div class="welcome-time">
        <span class="time-label">当前时间</span>
        <span class="time-value">{{ currentTime }}</span>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card" v-for="(metric, index) in metricsCards" :key="metric.type" :style="{ '--delay': index * 0.1 + 's' }">
        <div class="metric-icon" :class="metric.type">
          <el-icon :size="28"><component :is="metric.icon" /></el-icon>
        </div>
        <div class="metric-info">
          <span class="metric-value">{{ metric.value }}</span>
          <span class="metric-label">{{ metric.label }}</span>
        </div>
        <div class="metric-trend" :class="metric.trendClass">
          <el-icon><component :is="metric.trendIcon" /></el-icon>
          <span>{{ metric.trend }}</span>
        </div>
        <div class="metric-bg" :class="metric.type"></div>
      </div>
    </div>

    <!-- 图表和待办区域 -->
    <div class="content-grid">
      <!-- 趋势图表 -->
      <div class="chart-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon class="title-icon"><TrendCharts /></el-icon>
            <span>数据趋势</span>
          </div>
          <div class="time-filter">
            <el-radio-group v-model="trendDays" @change="getTrendData" size="default">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="15">近15天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div class="chart-wrapper">
          <div v-if="loading" class="chart-loading">
            <div class="loading-spinner"></div>
          </div>
          <div ref="trendChartRef" style="height: 320px"></div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="side-panel">
        <!-- 待处理事项 -->
        <div class="todo-section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><Bell /></el-icon>
              <span>待处理事项</span>
            </div>
            <el-badge :value="pendingCount" :max="99" class="pending-badge" />
          </div>
          <div class="todo-list">
            <div 
              class="todo-item" 
              v-for="todo in pendingTasksList"
              :key="todo.action"
              @click="handleTodoClick(todo.action)"
            >
              <div class="todo-icon" :class="todo.type">
                <el-icon><component :is="todo.icon" /></el-icon>
              </div>
              <div class="todo-content">
                <span class="todo-label">{{ todo.label }}</span>
                <span class="todo-count">{{ todo.count }} 条</span>
              </div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="system-section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><Monitor /></el-icon>
              <span>系统状态</span>
            </div>
            <div class="status-indicator" :class="systemHealthClass">
              <span class="status-dot"></span>
              <span>{{ systemHealthText }}</span>
            </div>
          </div>
          <div class="system-charts">
            <div class="system-item" v-for="item in systemStatus" :key="item.label">
              <div class="system-header">
                <span class="system-label">{{ item.label }}</span>
                <span class="system-value">{{ item.value }}%</span>
              </div>
              <el-progress 
                :percentage="item.value" 
                :stroke-width="8"
                :color="item.color"
                :show-text="false"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最新动态 -->
    <div class="activity-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon class="title-icon"><Timer /></el-icon>
          <span>最新动态</span>
        </div>
        <el-button text type="primary" @click="refreshActivity">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="activity-timeline">
        <div class="timeline-item" v-for="(item, index) in recentActivities" :key="item.action + '-' + item.time">
          <div class="timeline-dot" :class="item.type"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <span class="timeline-action">{{ item.action }}</span>
              <span class="timeline-time">{{ item.time }}</span>
            </div>
            <p class="timeline-desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, Download, User, Folder, Warning, ChatDotRound, Message,
  TrendCharts, Bell, Monitor, Timer, ArrowRight, Refresh, Top, Bottom
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getMetrics as fetchMetrics,
  getTrendData as fetchTrendData,
  getPendingTasks as fetchPendingTasks,
  getSystemStatus as fetchSystemStatus
} from '@/api/modules/dashboard'

const router = useRouter()
const metrics = ref({})
const trendDays = ref(7)
const trendChartRef = ref(null)
const pendingTasks = ref({})
const systemStatus = ref([])
const recentActivities = ref([])
const loading = ref(false)
const currentTime = ref('')
const username = ref('管理员')

let chartInstance = null
let timeInterval = null
let autoRefreshInterval = null

// 计算属性
const metricsCards = computed(() => [
  {
    type: 'resource',
    icon: Document,
    value: metrics.value.totalResources || 0,
    label: '总资源数',
    subValue: metrics.value.todayResources || 0,
    trend: `+${metrics.value.todayResources || 0}`,
    trendIcon: Top,
    trendClass: 'up'
  },
  {
    type: 'download',
    icon: Download,
    value: formatNumber(metrics.value.totalDownloads || 0),
    label: '总下载量',
    subValue: metrics.value.todayDownloads || 0,
    trend: `+${metrics.value.todayDownloads || 0}`,
    trendIcon: Top,
    trendClass: 'up'
  },
  {
    type: 'user',
    icon: User,
    value: metrics.value.totalUsers || 0,
    label: '总用户数',
    subValue: metrics.value.todayUsers || 0,
    trend: `+${metrics.value.todayUsers || 0}`,
    trendIcon: Top,
    trendClass: 'up'
  },
  {
    type: 'category',
    icon: Folder,
    value: metrics.value.totalCategories || 0,
    label: '总分类数',
    subValue: metrics.value.pendingResources || 0,
    trend: `${metrics.value.pendingResources || 0} 待审`,
    trendIcon: Bottom,
    trendClass: 'warning'
  }
])

const pendingCount = computed(() => {
  return (pendingTasks.value.pendingResources || 0) + 
         (pendingTasks.value.pendingFeedback || 0) + 
         (pendingTasks.value.unrepliedFeedback || 0)
})

const pendingTasksList = computed(() => [
  {
    label: '待审核资源',
    count: pendingTasks.value.pendingResources || 0,
    type: 'warning',
    icon: Document,
    action: '/resources?status=pending'
  },
  {
    label: '待处理反馈',
    count: pendingTasks.value.pendingFeedback || 0,
    type: 'info',
    icon: ChatDotRound,
    action: '/feedback?type=pending'
  },
  {
    label: '待回复反馈',
    count: pendingTasks.value.unrepliedFeedback || 0,
    type: 'success',
    icon: Message,
    action: '/feedback?type=unreplied'
  }
])

const systemHealthClass = computed(() => {
  const avgUsage = systemStatus.value.length > 0
    ? systemStatus.value.reduce((sum, item) => sum + item.value, 0) / systemStatus.value.length
    : 0
  if (avgUsage < 60) return 'healthy'
  if (avgUsage < 80) return 'warning'
  return 'danger'
})

const systemHealthText = computed(() => {
  const status = systemHealthClass.value
  if (status === 'healthy') return '运行良好'
  if (status === 'warning') return '负载较高'
  return '需要关注'
})

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}

// 更新时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取数据
const getMetrics = async () => {
  try {
    const res = await fetchMetrics()
    metrics.value = res.data
  } catch (error) {
    ElMessage.error('获取核心指标失败')
  }
}

const getTrendData = async () => {
  try {
    loading.value = true
    const res = await fetchTrendData(trendDays.value)
    renderChart(res.data)
  } catch (error) {
    ElMessage.error('获取趋势数据失败')
  } finally {
    loading.value = false
  }
}

const getPendingTasks = async () => {
  try {
    const res = await fetchPendingTasks()
    pendingTasks.value = res.data
  } catch (error) {
    ElMessage.error('获取待处理事项失败')
  }
}

const getSystemStatus = async () => {
  try {
    const res = await fetchSystemStatus()
    systemStatus.value = [
      { label: 'CPU 使用率', value: res.data.cpuUsage || 0, color: getProgressColor(res.data.cpuUsage || 0) },
      { label: '内存使用率', value: res.data.memoryUsage || 0, color: getProgressColor(res.data.memoryUsage || 0) },
      { label: '磁盘使用率', value: res.data.diskUsage || 0, color: getProgressColor(res.data.diskUsage || 0) }
    ]
  } catch (error) {
    ElMessage.error('获取系统状态失败')
  }
}

// 根据数值获取进度条颜色
const getProgressColor = (value) => {
  if (value < 60) return '#67c23a'
  if (value < 80) return '#e6a23c'
  return '#f56c6c'
}

// 渲染图表
const renderChart = (trendData) => {
  nextTick(() => {
    if (!trendChartRef.value) return
    
    if (!chartInstance) {
      chartInstance = echarts.init(trendChartRef.value)
    }

    const option = {
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.95)',
        borderColor: '#e0e6ed',
        borderWidth: 1,
        textStyle: {
          color: '#333'
        },
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#999'
          }
        }
      },
      legend: {
        data: ['资源', '下载', '用户'],
        bottom: 0,
        textStyle: {
          color: '#666'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        top: '10px',
        bottom: '50px',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendData.dates,
        axisLine: {
          lineStyle: {
            color: '#e0e6ed'
          }
        },
        axisLabel: {
          color: '#666'
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          show: false
        },
        axisTick: {
          show: false
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        },
        axisLabel: {
          color: '#666'
        }
      },
      series: [
        {
          name: '资源',
          type: 'line',
          data: trendData.resourceData,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#667eea'
          },
          itemStyle: {
            color: '#667eea',
            borderWidth: 2,
            borderColor: '#fff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
              { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
            ])
          }
        },
        {
          name: '下载',
          type: 'line',
          data: trendData.downloadData,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#f093fb'
          },
          itemStyle: {
            color: '#f093fb',
            borderWidth: 2,
            borderColor: '#fff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(240, 147, 251, 0.3)' },
              { offset: 1, color: 'rgba(240, 147, 251, 0.05)' }
            ])
          }
        },
        {
          name: '用户',
          type: 'line',
          data: trendData.userData,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#4facfe'
          },
          itemStyle: {
            color: '#4facfe',
            borderWidth: 2,
            borderColor: '#fff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(79, 172, 254, 0.3)' },
              { offset: 1, color: 'rgba(79, 172, 254, 0.05)' }
            ])
          }
        }
      ]
    }

    chartInstance.setOption(option)
  })
}

// 处理待办点击
const handleTodoClick = (action) => {
  router.push(action)
}

// 刷新动态
const refreshActivity = () => {
  ElMessage.success('动态已刷新')
  // 模拟动态数据
  recentActivities.value = [
    { type: 'success', action: '资源发布', desc: 'TV软件 - XX影视TV v2.1.0 已发布', time: '5分钟前' },
    { type: 'info', action: '用户反馈', desc: '收到 3 条新的用户反馈', time: '15分钟前' },
    { type: 'warning', action: '爬虫任务', desc: '自动爬虫已完成资源采集任务', time: '30分钟前' },
    { type: 'success', action: '系统更新', desc: '系统配置已更新', time: '1小时前' }
  ]
}

// 窗口调整时重绘图表
const handleResize = () => {
  chartInstance?.resize()
}

onMounted(async () => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  
  await getMetrics()
  await getPendingTasks()
  await getSystemStatus()
  await nextTick()
  await getTrendData()
  
  window.addEventListener('resize', handleResize)
  
  refreshActivity()
  
  // 自动刷新
  autoRefreshInterval = setInterval(() => {
    getMetrics()
    getPendingTasks()
    getSystemStatus()
  }, 60000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  if (autoRefreshInterval) {
    clearInterval(autoRefreshInterval)
  }
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.wave {
  animation: wave 1s ease-in-out infinite;
  display: inline-block;
}

@keyframes wave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-15deg); }
}

.welcome-desc {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.welcome-time {
  text-align: right;
  position: relative;
  z-index: 1;
}

.time-label {
  display: block;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 4px;
}

.time-value {
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
}

/* 指标卡片 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.metric-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  animation: slideUp 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.metric-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 120px;
  height: 120px;
  border-radius: 0 0 0 100%;
  opacity: 0.1;
}

.metric-bg.resource { background: #667eea; }
.metric-bg.download { background: #f093fb; }
.metric-bg.user { background: #4facfe; }
.metric-bg.category { background: #43e97b; }

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  position: relative;
  z-index: 1;
}

.metric-icon.resource { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.metric-icon.download { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.metric-icon.user { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.metric-icon.category { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.metric-info {
  position: relative;
  z-index: 1;
}

.metric-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.metric-label {
  display: block;
  font-size: 0.9rem;
  color: #666;
  margin-top: 4px;
}

.metric-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  font-weight: 500;
  position: relative;
  z-index: 1;
}

.metric-trend.up {
  color: #67c23a;
}

.metric-trend.warning {
  color: #e6a23c;
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  margin-bottom: 24px;
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a2e;
}

.title-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1rem;
}

/* 图表区域 */
.chart-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-wrapper {
  position: relative;
}

.chart-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 侧边面板 */
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 待办事项 */
.todo-section,
.system-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: #f0f2f5;
  transform: translateX(4px);
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.todo-icon.warning { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.todo-icon.info { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.todo-icon.success { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.todo-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.todo-label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
}

.todo-count {
  font-size: 0.8rem;
  color: #999;
}

.todo-arrow {
  color: #ccc;
  transition: transform 0.3s ease;
}

.todo-item:hover .todo-arrow {
  transform: translateX(4px);
  color: #667eea;
}

/* 系统状态 */
.status-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  padding: 4px 10px;
  border-radius: 20px;
}

.status-indicator.healthy {
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-indicator.warning {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.status-indicator.danger {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.system-charts {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.system-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.system-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-label {
  font-size: 0.85rem;
  color: #666;
}

.system-value {
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
}

/* 最新动态 */
.activity-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.activity-timeline {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-left: 20px;
}

.timeline-item {
  display: flex;
  gap: 16px;
  position: relative;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 24px;
  bottom: -20px;
  width: 2px;
  background: #f0f0f0;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
  position: relative;
  z-index: 1;
}

.timeline-dot.success { background: #67c23a; }
.timeline-dot.info { background: #409eff; }
.timeline-dot.warning { background: #e6a23c; }
.timeline-dot.danger { background: #f56c6c; }

.timeline-content {
  flex: 1;
  padding-bottom: 8px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.timeline-action {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
}

.timeline-time {
  font-size: 0.8rem;
  color: #999;
}

.timeline-desc {
  font-size: 0.85rem;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .welcome-time {
    text-align: center;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
  }
  
  .metric-card {
    flex-direction: row;
    align-items: center;
  }
}
</style>
