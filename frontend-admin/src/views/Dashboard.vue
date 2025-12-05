<template>
  <div class="dashboard-container">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="metrics-row">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-icon resource">
            <el-icon><document /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-label">总资源数</div>
            <div class="metric-value">{{ metrics.totalResources || 0 }}</div>
            <div class="metric-sub">今日新增: {{ metrics.todayResources || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-icon download">
            <el-icon><download /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-label">总下载量</div>
            <div class="metric-value">{{ metrics.totalDownloads || 0 }}</div>
            <div class="metric-sub">今日下载: {{ metrics.todayDownloads || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-icon user">
            <el-icon><user /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-label">总用户数</div>
            <div class="metric-value">{{ metrics.totalUsers || 0 }}</div>
            <div class="metric-sub">今日新增: {{ metrics.todayUsers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-icon category">
            <el-icon><folder /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-label">总分类数</div>
            <div class="metric-value">{{ metrics.totalCategories || 0 }}</div>
            <div class="metric-sub">待审核: {{ metrics.pendingResources || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>数据趋势</span>
              <el-radio-group v-model="trendDays" @change="getTrendData">
                <el-radio-button :label="7">近7天</el-radio-button>
                <el-radio-button :label="15">近15天</el-radio-button>
                <el-radio-button :label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="pending-card">
          <template #header>
            <span>待处理事项</span>
          </template>
          <div class="pending-list">
            <div class="pending-item">
              <div class="pending-label">
                <el-icon><warning /></el-icon>
                待审核资源
              </div>
              <div class="pending-value">{{ pendingTasks.pendingResources || 0 }}</div>
            </div>
            <div class="pending-item">
              <div class="pending-label">
                <el-icon><chat-dot-round /></el-icon>
                待处理反馈
              </div>
              <div class="pending-value">{{ pendingTasks.pendingFeedback || 0 }}</div>
            </div>
            <div class="pending-item">
              <div class="pending-label">
                <el-icon><message /></el-icon>
                待回复反馈
              </div>
              <div class="pending-value">{{ pendingTasks.unrepliedFeedback || 0 }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="system-card">
          <template #header>
            <span>系统状态</span>
          </template>
          <div class="system-list">
            <div class="system-item">
              <div class="system-label">CPU使用率</div>
              <el-progress :percentage="systemStatus.cpuUsage || 0" />
            </div>
            <div class="system-item">
              <div class="system-label">内存使用率</div>
              <el-progress :percentage="systemStatus.memoryUsage || 0" status="success" />
            </div>
            <div class="system-item">
              <div class="system-label">磁盘使用率</div>
              <el-progress :percentage="systemStatus.diskUsage || 0" status="warning" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Download, User, Folder, Warning, ChatDotRound, Message } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getMetrics as fetchMetrics, getTrendData as fetchTrendData, getPendingTasks as fetchPendingTasks, getSystemStatus as fetchSystemStatus } from '@/api/modules/dashboard'

const metrics = ref({})
const trendDays = ref(7)
const trendChart = ref(null)
const pendingTasks = ref({})
const systemStatus = ref({})
const loading = ref(false)

let chartInstance = null

const getMetrics = async () => {
  try {
    const res = await fetchMetrics()
    metrics.value = res.data
  } catch (error) {
    console.error('获取核心指标失败:', error)
    ElMessage.error('获取核心指标失败')
  }
}

const getTrendData = async () => {
  try {
    loading.value = true
    const res = await fetchTrendData(trendDays.value)
    renderChart(res.data)
  } catch (error) {
    console.error('获取趋势数据失败:', error)
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
    console.error('获取待处理事项失败:', error)
    ElMessage.error('获取待处理事项失败')
  }
}

const getSystemStatus = async () => {
  try {
    const res = await fetchSystemStatus()
    systemStatus.value = res.data
  } catch (error) {
    console.error('获取系统状态失败:', error)
    ElMessage.error('获取系统状态失败')
  }
}

const renderChart = (trendData) => {
  if (!chartInstance) {
    chartInstance = echarts.init(trendChart.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['资源', '下载', '用户']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '资源',
        type: 'line',
        data: trendData.resourceData,
        smooth: true
      },
      {
        name: '下载',
        type: 'line',
        data: trendData.downloadData,
        smooth: true
      },
      {
        name: '用户',
        type: 'line',
        data: trendData.userData,
        smooth: true
      }
    ]
  }

  chartInstance.setOption(option)
}

onMounted(async () => {
  await getMetrics()
  await getPendingTasks()
  await getSystemStatus()
  await nextTick()
  await getTrendData()

  // 自动刷新
  setInterval(() => {
    getMetrics()
    getPendingTasks()
    getSystemStatus()
  }, 60000) // 每分钟刷新一次
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.metric-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
  margin-right: 20px;
}

.metric-icon.resource {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-icon.download {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.user {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-icon.category {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-content {
  flex: 1;
}

.metric-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.metric-sub {
  font-size: 12px;
  color: #999;
}

.chart-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending-card,
.system-card {
  margin-bottom: 20px;
}

.pending-list,
.system-list {
  padding: 10px 0;
}

.pending-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.pending-item:last-child {
  border-bottom: none;
}

.pending-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.pending-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.system-item {
  margin-bottom: 20px;
}

.system-item:last-child {
  margin-bottom: 0;
}

.system-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}
</style>
