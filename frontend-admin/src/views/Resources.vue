<template>
  <div class="resources-container">
    <!-- 现代化工具栏 -->
    <div class="modern-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" class="gradient-btn" @click="showDialog()">
          <el-icon><Plus /></el-icon>
          添加资源
        </el-button>
        
        <!-- 批量操作按钮 -->
        <transition name="fade-slide">
          <div class="batch-operations" v-if="selectedResources.length > 0">
            <span class="batch-count">{{ selectedResources.length }} 项已选中</span>
            <el-button type="success" @click="handleBatchPublish">
              <el-icon><Check /></el-icon>
              批量发布
            </el-button>
            <el-button type="warning" @click="handleBatchUnpublish">
              <el-icon><Close /></el-icon>
              批量下架
            </el-button>
            <el-button type="info" @click="showBatchMoveDialog = true">
              <el-icon><FolderOpened /></el-icon>
              移动
            </el-button>
            <el-button type="danger" @click="handleBatchDelete">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </transition>
      </div>
      
      <div class="toolbar-right">
        <div class="search-wrapper">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索标题/描述"
            clearable
            @clear="loadResources"
            @keyup.enter="loadResources"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <el-select
          v-model="queryParams.categoryId"
          placeholder="选择分类"
          clearable
          @change="loadResources"
        >
          <el-option label="全部分类" :value="null" />
          <el-option 
            v-for="cat in categories" 
            :key="cat.id" 
            :label="cat.name" 
            :value="cat.id" 
          />
        </el-select>
        
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          @change="loadResources"
        >
          <el-option label="全部状态" :value="null" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        
        <el-select
          v-model="queryParams.source"
          placeholder="来源"
          clearable
          @change="loadResources"
        >
          <el-option label="全部来源" :value="null" />
          <el-option label="爬虫采集" value="crawler" />
          <el-option label="手动添加" value="manual" />
        </el-select>
        
        <el-button type="primary" @click="loadResources" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="handleReset" class="reset-btn">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 资源列表表格 -->
    <div class="table-card">
      <el-table 
        :data="resources" 
        style="width: 100%"
        v-loading="tableLoading"
        @selection-change="handleSelectionChange"
        stripe
        class="modern-table"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="编号" width="100" align="center">
          <template #default="{ row }">
            <span class="resource-code">A{{ String(row.id).padStart(3, '0') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图标" width="80" align="center">
          <template #default="{ row }">
            <div class="resource-icon">{{ getIcon(row.categoryName) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <div class="title-text">{{ row.title }}</div>
              <div class="desc-text">{{ row.description || '暂无描述' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="primary" effect="plain">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.crawlerTaskId" type="warning" size="small" effect="dark">
              <el-icon><Compass /></el-icon>
              爬虫
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">
              <el-icon><Edit /></el-icon>
              手动
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载量" width="100" align="center">
          <template #default="{ row }">
            <span class="stat-number">{{ row.downloadCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="添加时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读数" width="100" align="center">
          <template #default="{ row }">
            <span class="stat-number">{{ row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="primary" 
                link
                @click="showDialog(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                size="small" 
                type="warning" 
                link
                @click="handleToggleStatus(row)"
              >
                <el-icon><Switch /></el-icon>
                {{ row.status === 1 ? '下架' : '发布' }}
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                link
                @click="handleDelete(row.id)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadResources"
          @current-change="loadResources"
          background
        />
      </div>
    </div>

    <!-- 图片选择器对话框 -->
    <el-dialog
      v-model="showImageSelector"
      title="选择封面图片"
      width="900px"
      :close-on-click-modal="false"
      class="image-selector-dialog"
      destroy-on-close
    >
      <div class="image-selector">
        <el-scrollbar height="500px">
          <div class="image-grid">
            <div 
              v-for="img in availableImages" 
              :key="img.id"
              class="image-item"
              :class="{ selected: form.coverImageId === img.id }"
              @click="selectCoverImage(img)"
            >
              <el-image 
                :src="img.thumbnailUrl || img.fileUrl" 
                fit="cover"
                style="width: 100%; height: 100%"
                loading="lazy"
              />
              <div class="image-name">{{ img.originalName }}</div>
              <div class="image-overlay">
                <el-icon><Check /></el-icon>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button @click="showImageSelector = false">取消</el-button>
        <el-button type="primary" @click="confirmCoverImage">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑资源' : '添加资源'" 
      width="700px"
      :close-on-click-modal="false"
      class="resource-dialog"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="resource-form">
        <el-form-item label="资源标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资源标题" />
        </el-form-item>
        <el-form-item label="资源描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入资源描述"
          />
        </el-form-item>
        <el-form-item label="资源分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资源图片">
          <div class="resource-images-container">
            <div class="images-grid">
              <div 
                v-for="(img, index) in form.images" 
                :key="img.id"
                class="image-card"
                :class="{ 'is-cover': img.id === form.coverImageId }"
              >
                <el-image 
                  :src="img.thumbnailUrl || img.fileUrl" 
                  fit="cover"
                  style="width: 100%; height: 100%"
                />
                <div class="image-actions">
                  <el-button 
                    v-if="img.id !== form.coverImageId"
                    type="primary" 
                    size="small"
                    @click="setCoverImage(img.id)"
                  >
                    设为封面
                  </el-button>
                  <el-tag v-else type="success" size="small" effect="dark">封面</el-tag>
                  <el-button 
                    type="danger" 
                    size="small" 
                    :icon="Delete"
                    @click="removeImage(index)"
                  />
                </div>
              </div>
              <div 
                v-if="form.images.length < 5"
                class="add-image-card"
                @click="showImageSelector = true"
              >
                <el-icon :size="40"><Plus /></el-icon>
                <div>添加图片</div>
                <div class="tip">最多5张</div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-divider content-position="left">
          <el-icon><Link /></el-icon>
          下载链接
        </el-divider>
        
        <el-form-item 
          v-for="(link, index) in form.downloadLinks" 
          :key="index"
          :label="`链接${index + 1}`"
        >
          <div style="display: flex; gap: 10px; width: 100%; flex-wrap: wrap">
            <el-input v-model="link.linkName" placeholder="链接名称" style="width: 150px" />
            <el-select v-model="link.linkType" placeholder="类型" style="width: 150px">
              <el-option 
                v-for="type in linkTypes" 
                :key="type.typeCode" 
                :label="type.typeName" 
                :value="type.typeCode" 
              />
            </el-select>
            <el-input v-model="link.linkUrl" placeholder="下载链接URL" style="flex: 1; min-width: 200px" />
            <el-input v-model="link.password" placeholder="提取码(可选)" style="width: 120px" />
            <el-button 
              type="danger" 
              :icon="Delete" 
              circle
              @click="removeLink(index)"
            />
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :icon="Plus" @click="addLink">添加下载链接</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading" class="save-btn">保存</el-button>
      </template>
    </el-dialog>

    <!-- 批量移动分类对话框 -->
    <el-dialog
      v-model="showBatchMoveDialog"
      title="批量移动到分类"
      width="400px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form>
        <el-form-item label="目标分类">
          <el-select v-model="batchMoveTargetCategory" placeholder="请选择目标分类" style="width: 100%">
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <div class="batch-info">
            将移动 <strong>{{ selectedResources.length }}</strong> 个资源到选定分类
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchMoveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmBatchMove" :loading="batchLoading">确定移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Switch, Picture, Compass, Check, Close, FolderOpened, Link } from '@element-plus/icons-vue'
import { getResourceList, createResource, updateResource, deleteResource, toggleResourceStatus, batchPublishResources, batchUnpublishResources, batchDeleteResources, batchMoveToCategory } from '../api/resource'
import { getCategoryList } from '../api/category'
import { queryImages } from '../api/image'
import { getLinkTypes } from '../api/linkType'

const resources = ref([])
const categories = ref([])
const linkTypes = ref([])
const availableImages = ref([])
const dialogVisible = ref(false)
const showImageSelector = ref(false)
const showBatchMoveDialog = ref(false)
const tableLoading = ref(false)
const saveLoading = ref(false)
const batchLoading = ref(false)
const total = ref(0)
const formRef = ref(null)
const selectedResources = ref([])
const batchMoveTargetCategory = ref(null)

const queryParams = reactive({
  keyword: '',
  categoryId: null,
  status: null,
  source: null,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  title: '',
  description: '',
  coverImageId: null,
  coverImageUrl: '',
  categoryId: null,
  status: 1,
  downloadLinks: [],
  imageIds: [],
  images: []
})

const rules = {
  title: [{ required: true, message: '请输入资源标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const getIcon = (categoryName) => {
  const iconMap = {
    'TV端': '📺',
    'PC工具': '🗑️',
    '办公': '🖼️',
    '安全': '🔒',
    '工具': '📁',
    '插件': '🌐',
    '多端': '🎵'
  }
  return iconMap[categoryName] || '📦'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadResources = async () => {
  try {
    tableLoading.value = true
    const res = await getResourceList(queryParams)
    resources.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载资源失败')
  } finally {
    tableLoading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    const flattenCategories = (tree, result = []) => {
      tree.forEach(node => {
        result.push({ id: node.id, name: node.name })
        if (node.children && node.children.length > 0) {
          flattenCategories(node.children, result)
        }
      })
      return result
    }
    categories.value = flattenCategories(res.data || [])
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

const loadLinkTypes = async () => {
  try {
    const res = await getLinkTypes()
    linkTypes.value = res.data || []
  } catch (error) {
    ElMessage.error('加载网盘类型失败')
  }
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.categoryId = null
  queryParams.status = null
  queryParams.source = null
  queryParams.pageNum = 1
  loadResources()
}

const loadImages = async () => {
  try {
    const res = await queryImages({
      page: 1,
      pageSize: 100,
      isUsed: null
    })
    availableImages.value = res.data.records || []
  } catch (error) {
    ElMessage.error('加载图片失败')
  }
}

const selectCoverImage = (img) => {
  if (!form.images.find(i => i.id === img.id)) {
    if (form.images.length >= 5) {
      ElMessage.warning('最多只能添加5张图片')
      return
    }
    form.images.push(img)
    form.imageIds.push(img.id)
    if (!form.coverImageId) {
      form.coverImageId = img.id
      form.coverImageUrl = img.fileUrl
    }
  }
  showImageSelector.value = false
}

const confirmCoverImage = () => {
  showImageSelector.value = false
}

const removeCoverImage = () => {
  form.coverImageId = null
  form.coverImageUrl = ''
}

const setCoverImage = (imageId) => {
  form.coverImageId = imageId
  const img = form.images.find(i => i.id === imageId)
  if (img) {
    form.coverImageUrl = img.fileUrl
  }
}

const removeImage = (index) => {
  const removedImage = form.images[index]
  form.images.splice(index, 1)
  form.imageIds.splice(index, 1)
  
  if (removedImage.id === form.coverImageId) {
    const nextCover = form.images[0]
    form.coverImageId = nextCover ? nextCover.id : null
    form.coverImageUrl = nextCover ? nextCover.fileUrl : ''
  }
}

const showDialog = (row) => {
  if (row) {
    Object.assign(form, {
      ...row,
      downloadLinks: row.downloadLinks || [],
      images: row.images || [],
      imageIds: row.images ? row.images.map(img => img.id) : []
    })
  } else {
    Object.assign(form, {
      id: null,
      title: '',
      description: '',
      coverImageId: null,
      coverImageUrl: '',
      categoryId: null,
      status: 1,
      downloadLinks: [],
      imageIds: [],
      images: []
    })
  }
  dialogVisible.value = true
  loadImages()
}

const addLink = () => {
  form.downloadLinks.push({
    linkName: '',
    linkType: 'quark',
    linkUrl: '',
    password: ''
  })
}

const removeLink = (index) => {
  form.downloadLinks.splice(index, 1)
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    saveLoading.value = true
    
    const submitData = {
      ...form,
      imageIds: form.imageIds,
      coverImageId: form.coverImageId
    }
    
    if (form.id) {
      await updateResource(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createResource(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadResources()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  } finally {
    saveLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  try {
    await toggleResourceStatus(row.id)
    ElMessage.success('状态切换成功')
    loadResources()
  } catch (error) {
    ElMessage.error('状态切换失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该资源吗？删除后无法恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await deleteResource(id)
    ElMessage.success('删除成功')
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedResources.value = selection
}

const handleBatchPublish = async () => {
  if (selectedResources.value.length === 0) {
    ElMessage.warning('请先选择要发布的资源')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要发布选中的 ${selectedResources.value.length} 个资源吗？`,
      '批量发布确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchLoading.value = true
    const ids = selectedResources.value.map(item => item.id)
    const result = await batchPublishResources(ids)
    
    ElMessage.success(`成功发布 ${result.data} 个资源`)
    selectedResources.value = []
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量发布失败')
    }
  } finally {
    batchLoading.value = false
  }
}

const handleBatchUnpublish = async () => {
  if (selectedResources.value.length === 0) {
    ElMessage.warning('请先选择要下架的资源')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要下架选中的 ${selectedResources.value.length} 个资源吗？`,
      '批量下架确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    batchLoading.value = true
    const ids = selectedResources.value.map(item => item.id)
    const result = await batchUnpublishResources(ids)
    
    ElMessage.success(`成功下架 ${result.data} 个资源`)
    selectedResources.value = []
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量下架失败')
    }
  } finally {
    batchLoading.value = false
  }
}

const handleBatchDelete = async () => {
  if (selectedResources.value.length === 0) {
    ElMessage.warning('请先选择要删除的资源')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedResources.value.length} 个资源吗？删除后无法恢复！`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    batchLoading.value = true
    const ids = selectedResources.value.map(item => item.id)
    const result = await batchDeleteResources(ids)
    
    ElMessage.success(`成功删除 ${result.data} 个资源`)
    selectedResources.value = []
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchLoading.value = false
  }
}

const handleConfirmBatchMove = async () => {
  if (!batchMoveTargetCategory.value) {
    ElMessage.warning('请选择目标分类')
    return
  }
  
  try {
    batchLoading.value = true
    const ids = selectedResources.value.map(item => item.id)
    const result = await batchMoveToCategory(ids, batchMoveTargetCategory.value)
    
    ElMessage.success(`成功移动 ${result.data} 个资源`)
    selectedResources.value = []
    showBatchMoveDialog.value = false
    batchMoveTargetCategory.value = null
    loadResources()
  } catch (error) {
    ElMessage.error('批量移动失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(() => {
  loadResources()
  loadCategories()
  loadLinkTypes()
})
</script>


<style scoped>
.resources-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 现代化工具栏 */
.modern-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-wrapper :deep(.el-input__wrapper) {
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: none;
}

.search-btn {
  border-radius: 50px;
}

.reset-btn {
  border-radius: 50%;
  width: 36px;
  height: 36px;
  padding: 0;
}

/* 渐变按钮 */
.gradient-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(245, 87, 108, 0.4);
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 87, 108, 0.5);
}

/* 批量操作 */
.batch-operations {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 16px;
  border-left: 2px solid rgba(255, 255, 255, 0.3);
}

.batch-count {
  color: white;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}

/* 表格卡片 */
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

.resource-code {
  font-weight: 600;
  color: #667eea;
  font-family: 'Monaco', monospace;
}

.resource-icon {
  font-size: 28px;
  line-height: 1;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.desc-text {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}

.stat-number {
  font-weight: 600;
  color: #667eea;
}

.time-text {
  font-size: 13px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper :deep(.el-pagination__total) {
  font-weight: 500;
}

/* 图片选择器 */
.image-selector {
  padding: 10px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.image-item {
  position: relative;
  width: 100%;
  height: 150px;
  border: 3px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.image-item:hover {
  border-color: #667eea;
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.image-item.selected {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.3);
}

.image-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: white;
  font-size: 12px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(102, 126, 234, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item.selected .image-overlay {
  opacity: 1;
}

.image-overlay .el-icon {
  font-size: 32px;
  color: white;
}

/* 资源图片管理 */
.resource-images-container {
  width: 100%;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.image-card {
  position: relative;
  width: 100%;
  height: 120px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.image-card:hover {
  border-color: #667eea;
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.image-card.is-cover {
  border-color: #67c23a;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.3);
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 4px;
}

.add-image-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.add-image-card:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.add-image-card .tip {
  font-size: 12px;
  margin-top: 4px;
  color: #c0c4cc;
}

/* 批量信息 */
.batch-info {
  color: #606266;
  font-size: 14px;
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

/* 保存按钮 */
.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式 */
@media (max-width: 1200px) {
  .modern-toolbar {
    flex-direction: column;
    gap: 16px;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    flex-wrap: wrap;
  }
}

@media (max-width: 768px) {
  .resources-container {
    padding: 16px;
  }
  
  .toolbar-right {
    flex-wrap: wrap;
  }
  
  .batch-operations {
    width: 100%;
    flex-wrap: wrap;
    border-left: none;
    padding-left: 0;
    padding-top: 12px;
    border-top: 1px solid rgba(255, 255, 255, 0.3);
    margin-top: 12px;
  }
}
</style>
