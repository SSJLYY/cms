<template>
  <div class="revenue-container">
    <!-- 顶部收益概览 -->
    <div class="overview-grid">
      <div class="overview-card">
        <div class="ov-icon-wrap" style="background: linear-gradient(135deg, #43e97b22, #38f9d722);">
          <span class="ov-emoji">💰</span>
        </div>
        <div class="ov-body">
          <div class="ov-value" style="color: #43e97b;">¥{{ overviewData.totalRevenue || '0.00' }}</div>
          <div class="ov-label">总收益</div>
        </div>
      </div>

      <div class="overview-card">
        <div class="ov-icon-wrap" style="background: linear-gradient(135deg, #667eea22, #764ba222);">
          <span class="ov-emoji">⬇️</span>
        </div>
        <div class="ov-body">
          <div class="ov-value" style="color: #667eea;">{{ overviewData.totalDownloads || 0 }}</div>
          <div class="ov-label">总下载次数</div>
        </div>
      </div>

      <div class="overview-card">
        <div class="ov-icon-wrap" style="background: linear-gradient(135deg, #f093fb22, #f5576c22);">
          <span class="ov-emoji">📊</span>
        </div>
        <div class="ov-body">
          <div class="ov-value" style="color: #f093fb;">{{ overviewData.revenueItemCount || 0 }}</div>
          <div class="ov-label">收益项数量</div>
        </div>
      </div>

      <div class="overview-card period-card">
        <div class="ov-icon-wrap" style="background: linear-gradient(135deg, #fa709a22, #fee14022);">
          <span class="ov-emoji">📅</span>
        </div>
        <div class="ov-body">
          <div class="ov-label" style="margin-bottom: 8px;">统计周期</div>
          <el-select v-model="period" size="small" style="width: 100%;" @change="handlePeriodChange">
            <el-option label="今天" value="today" />
            <el-option label="昨天" value="yesterday" />
            <el-option label="近7天" value="week" />
            <el-option label="近30天" value="month" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 收益类型卡片 -->
    <div class="revenue-types-section" v-if="revenueTypes.length > 0">
      <div class="section-header">
        <span class="section-icon">💎</span>
        收益分类概览
      </div>
      <div class="type-cards-grid">
        <div
          v-for="(item, index) in revenueTypes"
          :key="index"
          class="type-card"
          :style="{ '--card-color': iconColors[index % iconColors.length].color, '--card-bg': iconColors[index % iconColors.length].bg }"
        >
          <div class="type-icon-wrap">
            <el-icon :size="22">
              <component :is="getIconComponent(index)" />
            </el-icon>
          </div>
          <div class="type-body">
            <div class="type-amount">¥{{ item.totalAmount || '0.00' }}</div>
            <div class="type-name">{{ item.typeName }}</div>
          </div>
          <div class="type-footer">
            <span>下载: {{ item.downloadCount || 0 }}次</span>
            <span>累计: ¥{{ item.accumulatedRevenue || '0.00' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 收益明细表格 -->
    <div class="table-card">
      <div class="table-header">
        <div class="table-title">
          <span class="title-icon">📋</span>
          收益明细
        </div>
        <div class="table-actions">
          <el-button class="btn-refresh" @click="handleRefresh">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
          <el-button
            type="danger"
            class="btn-delete"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon> 批量删除 ({{ selectedIds.length }})
          </el-button>
        </div>
      </div>

      <el-table
        :data="revenueList"
        v-loading="tableLoading"
        class="modern-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="source" label="收益来源" min-width="150" />
        <el-table-column label="收益金额" width="130" align="center">
          <template #default="{ row }">
            <span class="amount-tag">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="110" align="center" />
        <el-table-column label="收益类型" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.revenueType)" size="small" round>
              {{ getTypeName(row.revenueType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagColor(row.status)" size="small" round>
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="175" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Delete,
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

const overviewData = ref({ totalRevenue: 0, totalDownloads: 0, revenueItemCount: 0 })
const revenueTypes = ref([])
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

const iconComponents = [Upload, Money, Box, FolderOpened, Files, Document, Folder, Grid, Connection]

const iconColors = [
  { bg: '#667eea22', color: '#667eea' },
  { bg: '#fa709a22', color: '#fa709a' },
  { bg: '#43e97b22', color: '#43e97b' },
  { bg: '#764ba222', color: '#764ba2' },
  { bg: '#f5222d22', color: '#f5222d' },
  { bg: '#13c2c222', color: '#13c2c2' },
  { bg: '#fadb1422', color: '#d4a017' },
  { bg: '#eb2f9622', color: '#eb2f96' },
  { bg: '#2f54eb22', color: '#2f54eb' }
]

const getIconComponent = (index) => iconComponents[index % iconComponents.length]
const getTypeName = (type) => typeNameMap[type] || type

const getTypeTagColor = (type) => ({
  'cloud_storage': 'primary', 'download_revenue': 'success',
  'mobile_cloud': 'warning', 'mobile_cloud_backup': 'info',
  'uc_cloud': 'danger', 'lanzou_cloud': 'success', 'chengtong_cloud': 'warning'
}[type] || '')

const getStatusName = (status) => ({ 'pending': '待结算', 'settled': '已结算', 'cancelled': '已取消' }[status] || status)
const getStatusTagColor = (status) => ({ 'pending': 'warning', 'settled': 'success', 'cancelled': 'info' }[status] || '')

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const loadOverview = async () => {
  try {
    const res = await getRevenueOverview(period.value)
    if (res.code === 200) overviewData.value = res.data
  } catch (error) { /* silent */ }
}

const loadRevenueTypes = async () => {
  try {
    const res = await getRevenueByType(period.value)
    if (res.code === 200) revenueTypes.value = res.data || []
  } catch (error) { /* silent */ }
}

const loadRevenueList = async () => {
  tableLoading.value = true
  try {
    const res = await getRevenueList({ pageNum: currentPage.value, pageSize: pageSize.value, period: period.value })
    if (res.code === 200) {
      revenueList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) { /* silent */ } finally {
    tableLoading.value = false
  }
}

const loadAllData = () => {
  loadOverview()
  loadRevenueTypes()
  loadRevenueList()
}

const handlePeriodChange = () => { currentPage.value = 1; loadAllData() }
const handleRefresh = () => { loadAllData(); ElMessage.success('数据已刷新') }
const handleSelectionChange = (selection) => { selectedIds.value = selection.map(item => item.id) }

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条收益记录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const res = await deleteRevenue(id)
    if (res.code === 200) { ElMessage.success('删除成功'); loadAllData() }
    else ElMessage.error(res.message || '删除失败')
  } catch (error) { /* cancel */ }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const res = await batchDeleteRevenue(selectedIds.value)
    if (res.code === 200) { ElMessage.success('批量删除成功'); selectedIds.value = []; loadAllData() }
    else ElMessage.error(res.message || '批量删除失败')
  } catch (error) { /* cancel */ }
}

onMounted(() => loadAllData())
</script>

<style scoped>
.revenue-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100%;
}

/* 概览网格 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.period-card {
  flex-direction: row;
  align-items: flex-start;
}

.ov-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ov-emoji { font-size: 24px; }

.ov-body { flex: 1; min-width: 0; }

.ov-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 4px;
}

.ov-label {
  font-size: 13px;
  color: #606266;
}

/* 收益类型 */
.revenue-types-section {
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
  margin-bottom: 16px;
}

.section-icon { font-size: 18px; }

.type-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.type-card {
  border-radius: 12px;
  padding: 16px;
  background: var(--card-bg);
  border: 1px solid rgba(0,0,0,0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.type-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

.type-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: var(--card-bg);
  border: 1px solid var(--card-color);
  color: var(--card-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.type-body { margin-bottom: 12px; }

.type-amount {
  font-size: 20px;
  font-weight: 700;
  color: var(--card-color);
  margin-bottom: 4px;
}

.type-name {
  font-size: 13px;
  color: #606266;
}

.type-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

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

.table-actions {
  display: flex;
  gap: 10px;
}

.btn-refresh {
  border-radius: 10px !important;
}

.btn-delete {
  border-radius: 10px !important;
}

.modern-table :deep(.el-table__header th) {
  background: #f8f9fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

.modern-table :deep(.el-table__row:hover td) {
  background: rgba(102,126,234,0.04);
}

.amount-tag {
  display: inline-block;
  padding: 3px 10px;
  background: linear-gradient(135deg, rgba(67,233,123,0.15), rgba(56,249,215,0.15));
  color: #2ecc71;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .overview-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .revenue-container { padding: 16px; }
  .overview-grid { grid-template-columns: 1fr 1fr; gap: 12px; }
  .type-cards-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
