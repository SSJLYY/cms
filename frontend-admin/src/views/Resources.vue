<template>
  <div class="resources-container">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>
        添加资源
      </el-button>
      
      <div class="toolbar-right">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索标题/描述"
          style="width: 200px"
          clearable
          @clear="loadResources"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="queryParams.categoryId"
          placeholder="选择分类"
          clearable
          style="width: 150px"
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
          style="width: 120px"
          @change="loadResources"
        >
          <el-option label="全部状态" :value="null" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        
        <el-button type="primary" @click="loadResources">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <!-- 资源列表表格 -->
    <el-table 
      :data="resources" 
      style="width: 100%"
      v-loading="tableLoading"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column label="编号" width="100" align="center">
        <template #default="{ row }">
          A{{ String(row.id).padStart(3, '0') }}
        </template>
      </el-table-column>
      <el-table-column label="图片" width="80" align="center">
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
          <el-tag size="small">{{ row.categoryName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="downloadCount" label="下载量" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '已发布' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="添加时间" width="180" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="阅读数" width="100" align="center" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
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
            type="info" 
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
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadResources"
        @current-change="loadResources"
      />
    </div>

    <!-- 图片选择器对话框 -->
    <el-dialog
      v-model="showImageSelector"
      title="选择封面图片"
      width="900px"
      :close-on-click-modal="false"
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
              />
              <div class="image-name">{{ img.originalName }}</div>
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
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
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
                  <el-tag v-else type="success" size="small">封面</el-tag>
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
        
        <el-divider content-position="left">下载链接</el-divider>
        
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
        <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Switch, Picture } from '@element-plus/icons-vue'
import { getResourceList, createResource, updateResource, deleteResource, toggleResourceStatus } from '../api/resource'
import { getCategoryList } from '../api/category'
import { queryImages } from '../api/image'
import { getLinkTypes } from '../api/linkType'

const resources = ref([])
const categories = ref([])
const linkTypes = ref([])
const availableImages = ref([])
const dialogVisible = ref(false)
const showImageSelector = ref(false)
const tableLoading = ref(false)
const saveLoading = ref(false)
const total = ref(0)
const formRef = ref(null)

const queryParams = reactive({
  keyword: '',
  categoryId: null,
  status: null,
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
  imageIds: [],  // 资源关联的图片ID列表
  images: []  // 资源关联的图片对象列表（用于显示）
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
    console.error('加载资源失败', error)
    ElMessage.error('加载资源失败')
  } finally {
    tableLoading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    // 将树形结构扁平化为列表
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
    console.error('加载分类失败', error)
  }
}

const loadLinkTypes = async () => {
  try {
    const res = await getLinkTypes()
    linkTypes.value = res.data || []
  } catch (error) {
    console.error('加载网盘类型失败', error)
  }
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.categoryId = null
  queryParams.status = null
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
    console.error('加载图片失败', error)
  }
}

const selectCoverImage = (img) => {
  // 添加图片到资源图片列表（如果还没有）
  if (!form.images.find(i => i.id === img.id)) {
    if (form.images.length >= 5) {
      ElMessage.warning('最多只能添加5张图片')
      return
    }
    form.images.push(img)
    form.imageIds.push(img.id)
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
  
  // 如果删除的是封面图片，清除封面设置
  if (removedImage.id === form.coverImageId) {
    form.coverImageId = null
    form.coverImageUrl = ''
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
    
    // 准备提交的数据
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
      console.error('保存失败', error)
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
    console.error('状态切换失败', error)
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
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
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
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.resource-icon {
  font-size: 32px;
  line-height: 1;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.desc-text {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  background: #fff;
  border-radius: 4px;
}

:deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

/* 封面图片选择器样式 */
.cover-image-selector {
  width: 100%;
}

.cover-preview {
  position: relative;
  display: inline-block;
}

.remove-cover-btn {
  position: absolute;
  top: -8px;
  right: -8px;
}

.image-selector {
  padding: 10px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.image-item {
  position: relative;
  width: 100%;
  height: 150px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.image-item:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.image-item.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.image-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 12px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 资源图片管理样式 */
.resource-images-container {
  width: 100%;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-card {
  position: relative;
  width: 100%;
  height: 120px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.image-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.image-card.is-cover {
  border-color: #67c23a;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.2);
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: rgba(0, 0, 0, 0.7);
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
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.add-image-card:hover {
  border-color: #409eff;
  color: #409eff;
}

.add-image-card .tip {
  font-size: 12px;
  margin-top: 4px;
  color: #c0c4cc;
}
</style>
