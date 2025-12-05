<template>
  <div class="revenue-container">
    <!-- 收益概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" style="border-left: 4px solid #52c41a;">
          <div class="card-content">
            <div class="card-icon" style="background: #f6ffed; color: #52c41a;">
              <el-icon :size="32"><Wallet /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">¥{{ overviewData.totalRevenue || '0.00' }}</div>
              <div class="card-label">总收益</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" style="border-left: 4px solid #1890ff;">
          <div class="card-content">
            <div class="card-icon" style="background: #e6f7ff; color: #1890ff;">
              <el-icon :size="32"><Download /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ overviewData.totalDownloads || 0 }}</div>
              <div class="card-label">总下载次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" style="border-left: 4px solid #faad14;">
          <div class="card-content">
            <div class="card-icon" style="background: #fffbe6; color: #faad14;">
              <el-icon :size="32"><Coin /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-value">{{ overviewData.revenueItemCount || 0 }}</div>
              <div class="card-label">收益项数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" style="border-left: 4px solid #722ed1;">
          <div class="card-content">
            <div class="card-icon" style="background: #f9f0ff; color: #722ed1;">
              <el-icon :size="32"><Calendar /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">统计周期</div>
              <el-select v-model="period" size="small" style="width: 100%; margin-top: 8px;" @change="handlePeriodChange">
                <el-option label="今天" value="today" />
                <el-option label="昨天" value="yesterday" />
                <el-option label="近7天" value="week" />
                <el-option label="近30天" value="month" />
              </el-select>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 收益类型卡片 -->
    <el-row :gutter="20" class="revenue-types-row">
      <el-col 
        v-for="(item, index) in revenueTypes" 
        :key="index"
        :xs="24" :sm="12" :md="8" :lg="6"
      >
        <el-card class="revenue-type-card" :class="`type-${index % 9}`">
          <div class="type-header">
            <div class="type-icon" :style="getIconStyle(index)">
              <el-icon :size="24">
                <component :is="getIconComponent(index)" />
              </el-icon>
            </div>
            <div class="type-info">
              <div class="type-amount">¥{{ item.totalAmount || '0.00' }}</div>
              <div class="type-name">{{ item.typeName }}</div>
            </div>
          </div>
          <div class="type-footer">
            <div class="type-stat">
              <span class="stat-label">下载:</span>
              <span class="stat-value">{{ item.downloadCount || 0 }}次</span>
            </div>
            <div class="type-stat">
              <span class="stat-label">累计:</span>
              <span class="stat-value">¥{{ item.accumulatedRevenue || '0.00' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 收益明细表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><List /></el-icon> 收益明细</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="revenueList" 
        style="width: 100%" 
        v-loading="tableLoading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="source" label="收益来源" min-width="150" />
        <el-table-column label="收益金额" width="120" align="center">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="120" align="center" />
        <el-table-column label="收益类型" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.revenueType)" size="small">
              {{ getTypeName(row.revenueType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagColor(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              size="small" 
              link
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
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
          @size-change="loadRevenueList"
          @current-change="loadRevenueList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Wallet, Download, Coin, Calendar, List, Refresh, Delete,
  Upload, Money, Box, FolderOpened, Files, Document, 
  Folder, Grid, Connection
} from '@element-plus/icons-vue'
import { 
  getRevenueOverview, 
  getRevenueByType, 
  getRevenueList,
  deleteRevenue,
  batchDeleteRevenue
} from '@/api/modules/revenue'

const period = ref('today')
const tableLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 概览数据
const overviewData = ref({
  totalRevenue: 0,
  totalDownloads: 0,
  revenueItemCount: 0
})

// 收益类型数据
const revenueTypes = ref([])

// 收益明细列表
const revenueList = ref([])

const typeNameMap = {
  'cloud_storage': '云存储',
  'download_revenue': '下载收益',
  'mobile_cloud': '移动云盘',
  'mobile_cloud_backup': '移动云盘（备用）',
  'uc_cloud': 'UC网盘',
  '12_cloud': '12云盘',
  'lanzou_cloud': '蓝奏云',
  'chengtong_cloud': '城通网盘'
}

const iconComponents = [
  Upload, Money, Box, FolderOpened, 
  Files, Document, Folder, Grid, Connection
]

const iconColors = [
  { bg: '#e6f7ff', color: '#1890ff' },
  { bg: '#fff7e6', color: '#fa8c16' },
  { bg: '#f6ffed', color: '#52c41a' },
  { bg: '#f9f0ff', color: '#722ed1' },
  { bg: '#fff1f0', color: '#f5222d' },
  { bg: '#e6fffb', color: '#13c2c2' },
  { bg: '#feffe6', color: '#fadb14' },
  { bg: '#fff0f6', color: '#eb2f96' },
  { bg: '#f0f5ff', color: '#2f54eb' }
]

const getIconComponent = (index) => {
  return iconComponents[index % iconComponents.length]
}

const getIconStyle = (index) => {
  const style = iconColors[index % iconColors.length]
  return {
    background: style.bg,
    color: style.color
  }
}

const getTypeName = (type) => {
  return typeNameMap[type] || type
}

const getTypeTagColor = (type) => {
  const colors = {
    'cloud_storage': 'primary',
    'download_revenue': 'success',
    'mobile_cloud': 'warning',
    'mobile_cloud_backup': 'info',
    'uc_cloud': 'danger',
    '12_cloud': '',
    'lanzou_cloud': 'success',
    'chengtong_cloud': 'warning'
  }
  return colors[type] || ''
}

const getStatusName = (status) => {
  const names = {
    'pending': '待结算',
    'settled': '已结算',
    'cancelled': '已取消'
  }
  return names[status] || status
}

const getStatusTagColor = (status) => {
  const colors = {
    'pending': 'warning',
    'settled': 'success',
    'cancelled': 'info'
  }
  return colors[status] || ''
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

// 加载概览数据
const loadOverview = async () => {
  try {
    const res = await getRevenueOverview(period.value)
    if (res.code === 200) {
      overviewData.value = res.data
    }
  } catch (error) {
    console.error('加载概览数据失败:', error)
  }
}

// 加载收益类型数据
const loadRevenueTypes = async () => {
  try {
    const res = await getRevenueByType(period.value)
    if (res.code === 200) {
      revenueTypes.value = res.data || []
    }
  } catch (error) {
    console.error('加载收益类型数据失败:', error)
  }
}

// 加载收益明细列表
const loadRevenueList = async () => {
  tableLoading.value = true
  try {
    const res = await getRevenueList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      period: period.value
    })
    if (res.code === 200) {
      revenueList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载收益明细失败:', error)
  } finally {
    tableLoading.value = false
  }
}

// 加载所有数据
const loadAllData = () => {
  loadOverview()
  loadRevenueTypes()
  loadRevenueList()
}

// 周期变化
const handlePeriodChange = () => {
  currentPage.value = 1
  loadAllData()
}

// 刷新
const handleRefresh = () => {
  loadAllData()
  ElMessage.success('数据已刷新')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条收益记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteRevenue(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAllData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await batchDeleteRevenue(selectedIds.value)
    if (res.code === 200) {
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      loadAllData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.revenue-container {
  padding: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.card-label {
  font-size: 14px;
  color: #606266;
}

.revenue-types-row {
  margin-bottom: 20px;
}

.revenue-type-card {
  margin-bottom: 15px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.revenue-type-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.type-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-info {
  flex: 1;
}

.type-amount {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.type-name {
  font-size: 13px;
  color: #606266;
}

.type-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.type-stat {
  font-size: 12px;
}

.stat-label {
  color: #909399;
  margin-right: 4px;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}

.table-card {
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

.amount-text {
  color: #52c41a;
  font-weight: 600;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

@media (max-width: 768px) {
  .overview-row .el-col,
  .revenue-types-row .el-col {
    margin-bottom: 15px;
  }
}
</style>
