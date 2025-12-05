<template>
  <div class="promotion-container">
    <el-card class="filter-card">
      <el-space>
        <el-button type="primary" @click="handleAdd">添加广告位</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </el-space>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <el-tabs v-model="activePosition" @tab-change="handlePositionChange">
            <el-tab-pane label="不限" name="all" />
            <el-tab-pane label="下载页" name="download" />
            <el-tab-pane label="首页" name="homepage" />
            <el-tab-pane label="分类页" name="category" />
            <el-tab-pane label="自定义页" name="custom" />
          </el-tabs>
        </div>
      </template>

      <el-table :data="advertisementList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="position" label="位置" width="120">
          <template #default="{ row }">
            <el-tag :type="getPositionTagType(row.position)">
              {{ getPositionLabel(row.position) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              fit="cover"
              style="width: 80px; height: 50px; cursor: pointer"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="clickCount" label="点击数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleCopy(row)">复制</el-button>
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
        @size-change="loadAdvertisements"
        @current-change="loadAdvertisements"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="广告名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入广告名称" />
        </el-form-item>
        
        <el-form-item label="广告位置" prop="position">
          <el-select v-model="form.position" placeholder="请选择广告位置" style="width: 100%">
            <el-option label="首页" value="homepage" />
            <el-option label="下载页" value="download" />
            <el-option label="分类页" value="category" />
            <el-option label="自定义页" value="custom" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="广告类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择广告类型" style="width: 100%">
            <el-option label="图片广告" value="image" />
            <el-option label="文字广告" value="text" />
            <el-option label="视频广告" value="video" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="广告图片" prop="imageUrl" v-if="form.type === 'image'">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
          <div class="image-preview" v-if="form.imageUrl">
            <el-image :src="form.imageUrl" fit="contain" style="max-width: 100%; max-height: 200px" />
          </div>
        </el-form-item>
        
        <el-form-item label="广告内容" prop="content" v-if="form.type === 'text'">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入广告内容"
          />
        </el-form-item>
        
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdvertisementList,
  createAdvertisement,
  updateAdvertisement,
  deleteAdvertisement,
  updateStatus
} from '../api/modules/promotion'

const advertisementList = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const activePosition = ref('all')

const dialogVisible = ref(false)
const dialogTitle = ref('添加广告')
const formRef = ref(null)
const form = ref({
  name: '',
  position: 'homepage',
  type: 'image',
  imageUrl: '',
  linkUrl: '',
  content: '',
  status: 1,
  sortOrder: 0,
  startTime: null,
  endTime: null
})

const rules = {
  name: [{ required: true, message: '请输入广告名称', trigger: 'blur' }],
  position: [{ required: true, message: '请选择广告位置', trigger: 'change' }],
  type: [{ required: true, message: '请选择广告类型', trigger: 'change' }]
}

const loadAdvertisements = async () => {
  loading.value = true
  try {
    const { data } = await getAdvertisementList({
      page: page.value,
      pageSize: pageSize.value,
      position: activePosition.value === 'all' ? null : activePosition.value
    })
    advertisementList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('加载广告列表失败')
  } finally {
    loading.value = false
  }
}

const handlePositionChange = () => {
  page.value = 1
  loadAdvertisements()
}

const handleAdd = () => {
  dialogTitle.value = '添加广告'
  form.value = {
    name: '',
    position: 'homepage',
    type: 'image',
    imageUrl: '',
    linkUrl: '',
    content: '',
    status: 1,
    sortOrder: 0,
    startTime: null,
    endTime: null
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑广告'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleCopy = (row) => {
  dialogTitle.value = '复制广告'
  form.value = { ...row, id: null, name: row.name + ' (副本)' }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await updateAdvertisement(form.value.id, form.value)
          ElMessage.success('更新成功')
        } else {
          await createAdvertisement(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadAdvertisements()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    await updateStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这个广告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAdvertisement(row.id)
      ElMessage.success('删除成功')
      loadAdvertisements()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleRefresh = () => {
  loadAdvertisements()
}

const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const getPositionLabel = (position) => {
  const map = {
    homepage: '首页',
    download: '下载页',
    category: '分类页',
    custom: '自定义页'
  }
  return map[position] || position
}

const getPositionTagType = (position) => {
  const map = {
    homepage: 'success',
    download: 'primary',
    category: 'warning',
    custom: 'info'
  }
  return map[position] || ''
}

const getTypeLabel = (type) => {
  const map = {
    image: '图片广告',
    text: '文字广告',
    video: '视频广告'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    image: 'success',
    text: 'primary',
    video: 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  loadAdvertisements()
})
</script>

<style scoped>
.promotion-container {
  padding: 20px;
}

.filter-card,
.table-card {
  margin-bottom: 20px;
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

.image-preview {
  margin-top: 10px;
  text-align: center;
}
</style>
