<template>
  <div class="feedback-container">
    <el-card class="statistics-card">
      <div class="statistics-grid">
        <div class="stat-item">
          <div class="stat-label">总反馈数</div>
          <div class="stat-value">{{ statistics.totalFeedback || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待处理</div>
          <div class="stat-value warning">{{ statistics.pendingFeedback || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">已处理</div>
          <div class="stat-value success">{{ statistics.processedFeedback || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">今日反馈</div>
          <div class="stat-value">{{ statistics.todayFeedback || 0 }}</div>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="请选择类型" clearable>
            <el-option label="建议" value="SUGGESTION" />
            <el-option label="问题" value="ISSUE" />
            <el-option label="投诉" value="COMPLAINT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="搜索内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="feedbackList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="contactEmail" label="联系邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" @click="handleReply(row)">回复</el-button>
            <el-button type="text" @click="handleChangeStatus(row)">修改状态</el-button>
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

    <!-- 反馈详情对话框 -->
    <el-dialog v-model="detailVisible" title="反馈详情" width="700px">
      <el-descriptions :column="2" border v-if="currentFeedback">
        <el-descriptions-item label="ID">{{ currentFeedback.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeColor(currentFeedback.type)">
            {{ getTypeLabel(currentFeedback.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentFeedback.status)">
            {{ getStatusLabel(currentFeedback.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentFeedback.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentFeedback.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="反馈内容" :span="2">
          <div style="white-space: pre-wrap">{{ currentFeedback.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" :span="2" v-if="currentFeedback.reply">
          <div style="white-space: pre-wrap">{{ currentFeedback.reply }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentFeedback.createTime }}</el-descriptions-item>
        <el-descriptions-item label="回复时间" v-if="currentFeedback.replyTime">
          {{ currentFeedback.replyTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyVisible" title="回复反馈" width="600px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply">提交</el-button>
      </template>
    </el-dialog>

    <!-- 修改状态对话框 -->
    <el-dialog v-model="statusVisible" title="修改状态" width="400px">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态">
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
    feedbackList.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('查询反馈失败:', error)
    ElMessage.error('查询反馈失败')
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
    await replyFeedback(replyForm.id, replyForm.reply)
    ElMessage.success('回复成功')
    replyVisible.value = false
    replyForm.reply = ''
    handleQuery()
    getStatistics()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
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
    await updateFeedbackStatus(statusForm.id, statusForm.status)
    ElMessage.success('状态修改成功')
    statusVisible.value = false
    handleQuery()
    getStatistics()
  } catch (error) {
    console.error('状态修改失败:', error)
    ElMessage.error('状态修改失败')
  }
}

onMounted(() => {
  getStatistics()
  handleQuery()
})
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}

.statistics-card,
.filter-card,
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

.stat-value.warning {
  color: #e6a23c;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
