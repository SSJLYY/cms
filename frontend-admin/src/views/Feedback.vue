<template>
  <div class="feedback-container">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <div class="stat-card stat-primary">
        <div class="stat-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.totalFeedback || 0 }}</div>
          <div class="stat-label">总反馈数</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.pendingFeedback || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.processedFeedback || 0 }}</div>
          <div class="stat-label">已处理</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.todayFeedback || 0 }}</div>
          <div class="stat-label">今日反馈</div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-select
          v-model="queryForm.type"
          placeholder="类型"
          clearable
          class="filter-select"
        >
          <el-option label="建议" value="SUGGESTION" />
          <el-option label="问题" value="ISSUE" />
          <el-option label="投诉" value="COMPLAINT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select
          v-model="queryForm.status"
          placeholder="状态"
          clearable
          class="filter-select"
        >
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-input
          v-model="queryForm.keyword"
          placeholder="搜索内容"
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
      </div>
    </div>

    <!-- 反馈列表 -->
    <div class="table-card">
      <el-table 
        :data="feedbackList" 
        v-loading="loading" 
        stripe 
        class="modern-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small" effect="dark">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="300">
          <template #default="{ row }">
            <div class="feedback-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="contactEmail" label="联系邮箱" width="180">
          <template #default="{ row }">
            <div class="contact-info">
              <el-icon><Message /></el-icon>
              {{ row.contactEmail || '未填写' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small" effect="dark">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-value">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button type="success" link size="small" @click="handleReply(row)">
                <el-icon><ChatLineSquare /></el-icon>
                回复
              </el-button>
              <el-button type="warning" link size="small" @click="handleChangeStatus(row)">
                <el-icon><Setting /></el-icon>
                状态
              </el-button>
            </div>
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

    <!-- 反馈详情对话框 -->
    <el-dialog v-model="detailVisible" title="反馈详情" width="700px" class="detail-dialog" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentFeedback" class="detail-descriptions">
        <el-descriptions-item label="ID">{{ currentFeedback.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeColor(currentFeedback.type)" effect="dark">
            {{ getTypeLabel(currentFeedback.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentFeedback.status)" effect="dark">
            {{ getStatusLabel(currentFeedback.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentFeedback.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentFeedback.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="反馈内容" :span="2">
          <div class="content-block">{{ currentFeedback.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" :span="2" v-if="currentFeedback.reply">
          <div class="reply-block">{{ currentFeedback.reply }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentFeedback.createTime }}</el-descriptions-item>
        <el-descriptions-item label="回复时间" v-if="currentFeedback.replyTime">
          {{ currentFeedback.replyTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyVisible" title="回复反馈" width="600px" class="reply-dialog" destroy-on-close>
      <el-form :model="replyForm" label-width="80px" class="reply-form">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply" class="submit-btn">提交</el-button>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog v-model="statusVisible" title="修改状态" width="400px" class="status-dialog" destroy-on-close>
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" class="full-width">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, Clock, CircleCheck, Calendar, Search, Refresh,
  View, ChatLineSquare, Setting, Message
} from '@element-plus/icons-vue'
import { 
  getFeedbackStatistics, 
  queryFeedback, 
  replyFeedback, 
  updateFeedbackStatus 
} from '@/api/modules/feedback'

const statistics = ref({})
const feedbackList = ref([])
const loading = ref(false)
const total = ref(0)
const detailVisible = ref(false)
const replyVisible = ref(false)
const statusVisible = ref(false)
const currentFeedback = ref(null)

const queryForm = reactive({
  type: '',
  status: '',
  keyword: '',
  page: 1,
  pageSize: 20
})

const replyForm = reactive({
  id: null,
  reply: ''
})

const statusForm = reactive({
  id: null,
  status: ''
})

const typeMap = {
  SUGGESTION: '建议',
  ISSUE: '问题',
  COMPLAINT: '投诉',
  OTHER: '其他'
}

const statusMap = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  COMPLETED: '已完成',
  CLOSED: '已关闭'
}

const getTypeLabel = (type) => typeMap[type] || type
const getStatusLabel = (status) => statusMap[status] || status

const getTypeColor = (type) => {
  const colorMap = {
    SUGGESTION: 'success',
    ISSUE: 'warning',
    COMPLAINT: 'danger',
    OTHER: 'info'
  }
  return colorMap[type] || 'info'
}

const getStatusColor = (status) => {
  const colorMap = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    COMPLETED: 'success',
    CLOSED: 'info'
  }
  return colorMap[status] || 'info'
}

const getStatistics = async () => {
  try {
    const response = await getFeedbackStatistics()
    statistics.value = response.data || {}
  } catch (error) {
    console.error('获取统计信息失败:', error)
    ElMessage.error('获取统计信息失败')
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const response = await queryFeedback(queryForm)
    feedbackList.value = Array.isArray(response?.data?.records) ? response.data.records : []
    total.value = Number(response?.data?.total || 0)
  } catch (error) {
    console.error('查询反馈失败:', error)
    ElMessage.error(error.response?.data?.message || '查询反馈失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    type: '',
    status: '',
    keyword: '',
    page: 1,
    pageSize: 20
  })
  handleQuery()
}

const handleView = (row) => {
  currentFeedback.value = row
  detailVisible.value = true
}

const handleReply = (row) => {
  replyForm.id = row.id
  replyForm.reply = row.reply || ''
  replyVisible.value = true
}

const handleSubmitReply = async () => {
  if (!replyForm.reply || !replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await replyFeedback(replyForm.id, replyForm.reply, { skipBusinessErrorMessage: true })
    ElMessage.success('回复成功')
    replyVisible.value = false
    replyForm.reply = ''
    handleQuery()
    getStatistics()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error.response?.data?.message || '回复失败')
  }
}

const handleChangeStatus = (row) => {
  statusForm.id = row.id
  statusForm.status = row.status
  statusVisible.value = true
}

const handleSubmitStatus = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择状态')
    return
  }
  
  try {
    await updateFeedbackStatus(statusForm.id, statusForm.status, { skipBusinessErrorMessage: true })
    ElMessage.success('状态修改成功')
    statusVisible.value = false
    handleQuery()
    getStatistics()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '状态修改失败')
  }
}

onMounted(() => {
  getStatistics()
  handleQuery()
})
</script>

<style scoped>
.feedback-container {
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

.stat-warning .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-success .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-info .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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
  width: 240px;
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

.feedback-content {
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
}

.time-value {
  font-size: 13px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 4px;
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

.content-block {
  padding: 12px;
  background: #f8f9fc;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.6;
}

.reply-block {
  padding: 12px;
  background: #ecfdf5;
  border-radius: 8px;
  color: #059669;
  white-space: pre-wrap;
  line-height: 1.6;
}

.reply-form :deep(.el-textarea__inner) {
  border-radius: 12px;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.full-width {
  width: 100%;
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
}
</style>
