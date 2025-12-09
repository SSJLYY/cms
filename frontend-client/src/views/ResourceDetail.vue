<template>
  <div class="resource-detail">
    <!-- 返回首页链接 -->
    <div class="back-link-container">
      <router-link to="/" class="back-btn">
        <i class="fas fa-arrow-left"></i>
        返回首页
      </router-link>
    </div>

    <!-- 下载确认页头部 -->
    <div class="dl-header">
      <div class="dl-content">
        <h1 class="dl-title"><i class="fas fa-download"></i>资源下载详情页</h1>
        <p class="dl-description">翻到页面底部即可进行下载</p>
        <div class="dl-decoration">
          <div class="dl-dec-circle dl-c1"></div>
          <div class="dl-dec-circle dl-c2"></div>
          <div class="dl-dec-circle dl-c3"></div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 资源详情 -->
    <div v-else-if="resource" class="page-container">
      <!-- 页面内容 -->
      <div class="page-content">
        <!-- 资源信息 -->
        <div class="resource-info">
          <h4 class="resource-name">{{ resource.title }}</h4>
          <p class="resource-desc">{{ resource.description || '暂无描述' }}</p>
        </div>

        <!-- 资源图片轮播 -->
        <div v-if="displayImages.length > 0" class="resource-images-carousel">
          <div class="carousel-wrapper">
            <!-- 左侧切换按钮 -->
            <button 
              v-if="displayImages.length > 1"
              class="carousel-btn carousel-btn-prev" 
              @click="prevImage"
              :disabled="currentImageIndex === 0"
            >
              <i class="fas fa-chevron-left"></i>
            </button>

            <!-- 图片容器 -->
            <div class="carousel-container">
              <img 
                :src="displayImages[currentImageIndex]" 
                :alt="resource.title" 
                class="resource-image"
                @click="openImageModal(currentImageIndex)"
              />
            </div>

            <!-- 右侧切换按钮 -->
            <button 
              v-if="displayImages.length > 1"
              class="carousel-btn carousel-btn-next" 
              @click="nextImage"
              :disabled="currentImageIndex === displayImages.length - 1"
            >
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>

          <!-- 图片说明和指示器 -->
          <div class="image-info">
            <small class="text-muted">
              <i class="fas fa-images"></i>
              资源说明图片（{{ displayImages.length }} 张）- 点击图片查看大图，左右滑动切换
            </small>
          </div>

          <!-- 图片指示器 -->
          <div v-if="displayImages.length > 1" class="carousel-indicators">
            <span 
              v-for="(img, index) in displayImages" 
              :key="index"
              class="indicator-dot"
              :class="{ active: index === currentImageIndex }"
              @click="goToImage(index)"
            ></span>
          </div>
        </div>

        <!-- 广告位 -->
        <div v-if="advertisements.length > 0" class="advertisement-section">
          <div 
            v-for="ad in advertisements" 
            :key="ad.id"
            class="advertisement-item"
            @click="handleAdClick(ad)"
          >
            <img 
              v-if="ad.imageUrl" 
              :src="ad.imageUrl" 
              :alt="ad.name"
              class="ad-image"
            />
            <div v-else class="ad-placeholder">
              <i class="fas fa-image"></i>
              <span>{{ ad.name }}</span>
            </div>
          </div>
        </div>

        <!-- 底部信息栏 -->
        <!-- 已下载过此资源提示 -->
        <div v-if="hasDownloaded" class="download-notice download-notice-info">
          <i class="fas fa-info-circle"></i>
          您今日已下载过此资源，重复下载不会计数
        </div>

        <!-- 下载次数已满提示 -->
        <div 
          v-else-if="remainingDownloads === 0" 
          ref="warningNotice"
          class="download-notice download-notice-warning"
          :class="{ 'shake': isShaking }"
        >
          <i class="fas fa-exclamation-circle"></i>
          您今日下载次数已满，无法下载，请明日再下载
        </div>

        <div class="bottom-info-bar">
          <div class="update-time-info">
            更新时间：{{ formatDate(resource.updateTime || resource.createTime) }}
          </div>
          <div class="download-count-info">
            下载次数：{{ resource.downloadCount || 0 }}
          </div>
          <div class="remaining-downloads-info">
            <i class="fas fa-clock"></i>
            今日剩余下载次数：{{ remainingDownloads }}
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="page-footer">
          <div class="download-buttons-container">
            <a 
              v-for="link in filteredDownloadLinks" 
              :key="link.id"
              :href="remainingDownloads > 0 ? getFullUrl(link.linkUrl) : 'javascript:void(0)'"
              :class="['download-btn-green', { 'disabled': remainingDownloads === 0 }]"
              :target="remainingDownloads > 0 ? '_blank' : ''"
              :rel="remainingDownloads > 0 ? 'noopener noreferrer' : ''"
              @click="handleDownload(link.linkType, $event)"
            >
              <i class="fas fa-download"></i>
              立即下载
            </a>
            <button 
              class="download-btn-blue"
              @click="reportInvalidLink"
            >
              <i class="fas fa-exclamation-triangle"></i>
              链接失效
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 无资源提示 -->
    <div v-else class="no-resource">
      <div class="no-resource-icon">😕</div>
      <div class="no-resource-text">资源不存在</div>
      <router-link to="/" class="back-home-btn">返回首页</router-link>
    </div>

    <!-- 图片查看器模态框 -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <button class="modal-close-btn" @click="closeImageModal">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <!-- 模态框左侧切换按钮 -->
      <button 
        v-if="displayImages.length > 1"
        class="modal-nav-btn modal-nav-prev" 
        @click.stop="prevModalImage"
        :disabled="modalImageIndex === 0"
      >
        <i class="fas fa-chevron-left"></i>
      </button>

      <div class="modal-content" @click.stop>
        <img :src="displayImages[modalImageIndex]" :alt="resource?.title" class="modal-image" />
        <div class="modal-image-counter">
          {{ modalImageIndex + 1 }} / {{ displayImages.length }}
        </div>
      </div>

      <!-- 模态框右侧切换按钮 -->
      <button 
        v-if="displayImages.length > 1"
        class="modal-nav-btn modal-nav-next" 
        @click.stop="nextModalImage"
        :disabled="modalImageIndex === displayImages.length - 1"
      >
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <!-- 右下角操作按钮 -->
    <ActionButtons />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getResourceDetail, recordDownload, getLinkTypes, getRemainingDownloads, checkDownloaded } from '../api/resource'
import { getActiveAdvertisements, recordClick } from '../api/promotion'
import ActionButtons from '../components/ActionButtons.vue'

const route = useRoute()
const resource = ref(null)
const loading = ref(true)
const showImageModal = ref(false)
const modalImageIndex = ref(0)
const currentImageIndex = ref(0)
const linkTypes = ref([])
const linkTypeMap = ref({})
const remainingDownloads = ref(2)
const hasDownloaded = ref(false)
const isShaking = ref(false)
const warningNotice = ref(null)
const advertisements = ref([])
const currentAdIndex = ref(0)

// 根据URL参数过滤下载链接
const filteredDownloadLinks = computed(() => {
  if (!resource.value || !resource.value.downloadLinks) {
    return []
  }
  
  const linkType = route.query.type
  
  // 如果没有type参数,返回所有链接
  if (!linkType) {
    return resource.value.downloadLinks
  }
  
  // 过滤出匹配的链接类型
  const filtered = resource.value.downloadLinks.filter(
    link => link.linkType === linkType
  )
  
  // 如果没有匹配的链接,返回所有链接(处理无效参数或资源不存在该类型的情况)
  return filtered.length > 0 ? filtered : resource.value.downloadLinks
})

// 获取要展示的图片列表（最多5张）
const displayImages = computed(() => {
  if (!resource.value) return []
  
  const images = []
  
  // 如果有关联的图片列表，优先使用
  if (resource.value.images && resource.value.images.length > 0) {
    // 取前5张图片
    resource.value.images.slice(0, 5).forEach(img => {
      if (img.fileUrl) {
        images.push(img.fileUrl)
      }
    })
  }
  
  // 如果没有关联图片但有封面图，使用封面图
  if (images.length === 0 && resource.value.coverImageUrl) {
    images.push(resource.value.coverImageUrl)
  }
  
  return images
})

const loadResourceDetail = async () => {
  try {
    loading.value = true
    const resourceId = route.params.id
    const res = await getResourceDetail(resourceId)
    resource.value = res.data
  } catch (error) {
    console.error('加载资源详情失败', error)
  } finally {
    loading.value = false
  }
}

const triggerShake = () => {
  isShaking.value = true
  setTimeout(() => {
    isShaking.value = false
  }, 500)
  
  // 滚动到提示框位置
  if (warningNotice.value) {
    warningNotice.value.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const handleDownload = async (linkType, event) => {
  // 如果下载次数已满，阻止默认行为并触发抖动
  if (remainingDownloads.value === 0) {
    event.preventDefault()
    triggerShake()
    return
  }
  
  try {
    await recordDownload(resource.value.id)
    console.log(`下载资源 ${resource.value.id}, 类型: ${linkType}`)
    // 下载后更新状态
    await loadRemainingDownloads()
    await checkIfDownloaded()
  } catch (error) {
    console.error('记录下载失败', error)
    if (error.response && error.response.data) {
      const code = error.response.data.code
      if (code === 208) {
        // 已下载过，不显示错误，只更新状态
        await checkIfDownloaded()
      } else if (code === 429) {
        // 下载次数已满
        event.preventDefault()
        await loadRemainingDownloads()
        triggerShake()
      }
    }
  }
}

const reportInvalidLink = () => {
  alert('感谢反馈!我们会尽快处理失效链接。')
  // 这里可以添加实际的反馈API调用
}

// 图片轮播控制
const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const nextImage = () => {
  if (currentImageIndex.value < displayImages.value.length - 1) {
    currentImageIndex.value++
  }
}

const goToImage = (index) => {
  currentImageIndex.value = index
}

// 模态框图片控制
const openImageModal = (index) => {
  modalImageIndex.value = index
  showImageModal.value = true
  document.body.style.overflow = 'hidden'
}

const closeImageModal = () => {
  showImageModal.value = false
  document.body.style.overflow = ''
}

const prevModalImage = () => {
  if (modalImageIndex.value > 0) {
    modalImageIndex.value--
  }
}

const nextModalImage = () => {
  if (modalImageIndex.value < displayImages.value.length - 1) {
    modalImageIndex.value++
  }
}

const getFullUrl = (url) => {
  if (!url) return '#'
  // 如果URL已经是完整的（包含协议），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果URL不包含协议，添加 https://
  return `https://${url}`
}

const getLinkTypeName = (type) => {
  return linkTypeMap.value[type] || type
}

const loadLinkTypes = async () => {
  try {
    const res = await getLinkTypes()
    linkTypes.value = res.data || []
    // 创建类型代码到名称的映射
    linkTypeMap.value = linkTypes.value.reduce((map, type) => {
      map[type.typeCode] = type.typeName
      return map
    }, {})
  } catch (error) {
    console.error('加载网盘类型失败', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const loadRemainingDownloads = async () => {
  try {
    const res = await getRemainingDownloads()
    if (res.code === 200) {
      remainingDownloads.value = res.data
    }
  } catch (error) {
    console.error('获取剩余下载次数失败', error)
  }
}

const checkIfDownloaded = async () => {
  try {
    const resourceId = route.params.id
    const res = await checkDownloaded(resourceId)
    if (res.code === 200) {
      hasDownloaded.value = res.data
    }
  } catch (error) {
    console.error('检查下载状态失败', error)
  }
}

const loadAdvertisements = async () => {
  try {
    const res = await getActiveAdvertisements('download')
    if (res.code === 200 && res.data) {
      advertisements.value = res.data
    }
  } catch (error) {
    console.error('加载广告失败', error)
  }
}

const handleAdClick = async (ad) => {
  try {
    await recordClick(ad.id)
    if (ad.linkUrl) {
      // 确保URL格式正确
      let url = ad.linkUrl
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'https://' + url
      }
      window.open(url, '_blank', 'noopener,noreferrer')
    }
  } catch (error) {
    console.error('记录广告点击失败', error)
  }
}

onMounted(async () => {
  await loadLinkTypes()
  await loadResourceDetail()
  await loadRemainingDownloads()
  await checkIfDownloaded()
  await loadAdvertisements()
})
</script>

<style scoped>
.resource-detail {
  min-height: 100vh;
  background-color: var(--bg-primary);
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  padding-bottom: 40px;
  transition: background-color 0.3s ease;
}

/* 返回链接 */
.back-link-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 20px 0;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.back-btn:hover {
  color: #2563eb;
}

/* 下载确认页头部 */
.dl-header {
  position: relative;
  padding: 60px 0 40px;
  margin: 0 0 30px;
  text-align: center;
  overflow: hidden;
}

.dl-content {
  position: relative;
  z-index: 2;
}

.dl-title {
  margin: 0 0 16px;
  font-size: clamp(1.8rem, 5vw, 2.6rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.1;
  color: var(--text-primary);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.dl-title i {
  color: #3b82f6;
  font-size: 0.9em;
}

.dl-description {
  font-size: 1.05rem;
  color: var(--text-secondary);
  margin: 0;
  opacity: 0.9;
}

.dl-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.dl-dec-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: dl-float 6s ease-in-out infinite;
}

.dl-c1 {
  width: 120px;
  height: 120px;
  background: #3b82f6;
  top: 18%;
  right: 12%;
  animation-delay: 0s;
}

.dl-c2 {
  width: 80px;
  height: 80px;
  background: #8b5cf6;
  top: 60%;
  left: 14%;
  animation-delay: 2s;
}

.dl-c3 {
  width: 60px;
  height: 60px;
  background: #06b6d4;
  top: 32%;
  left: 68%;
  animation-delay: 4s;
}

@keyframes dl-float {
  0%, 100% {
    transform: translateY(0) rotate(0);
  }
  50% {
    transform: translateY(-18px) rotate(180deg);
  }
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 100px 20px;
  color: var(--text-primary);
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--border-color);
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 页面容器 */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.page-content {
  padding: 32px;
  background: var(--bg-tertiary);
  transition: background-color 0.3s ease;
}

/* 资源信息 */
.resource-info {
  margin-bottom: 24px;
}

.resource-name {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.resource-desc {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-line;
  word-wrap: break-word;
}

/* 资源图片轮播 */
.resource-images-carousel {
  margin: 24px 0;
}

.carousel-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.carousel-btn {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.9);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  z-index: 10;
}

.carousel-btn:hover:not(:disabled) {
  background: rgba(37, 99, 235, 1);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.5);
}

.carousel-btn:disabled {
  background: rgba(156, 163, 175, 0.5);
  cursor: not-allowed;
  box-shadow: none;
}

.carousel-container {
  flex: 1;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resource-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.resource-image:hover {
  transform: scale(1.05);
}

.image-info {
  text-align: center;
  margin-top: 12px;
}

.image-info small {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d1d5db;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator-dot:hover {
  background: #9ca3af;
  transform: scale(1.2);
}

.indicator-dot.active {
  background: #3b82f6;
  width: 24px;
  border-radius: 5px;
}

/* 广告位 */
.advertisement-section {
  margin: 24px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.advertisement-item {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
  background: var(--card-bg);
}

.advertisement-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.ad-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: contain;
  max-height: 140px;
}

.ad-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-tertiary);
  min-height: 120px;
  color: var(--text-tertiary);
  gap: 12px;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.ad-placeholder i {
  font-size: 2rem;
  opacity: 0.6;
}

.ad-placeholder span {
  font-size: 1rem;
  font-weight: 500;
}

/* 底部信息栏 */
.bottom-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  padding: 12px 15px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 12px;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.update-time-info,
.download-count-info,
.remaining-downloads-info {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.remaining-downloads-info {
  color: #3b82f6;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.remaining-downloads-info i {
  font-size: 0.9rem;
}

/* 下载提示 */
.download-notice {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  font-weight: 500;
}

.download-notice i {
  font-size: 1.1rem;
}

.download-notice-info {
  background: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.download-notice-warning {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}

/* 抖动动画 */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-10px); }
  20%, 40%, 60%, 80% { transform: translateX(10px); }
}

.download-notice.shake {
  animation: shake 0.5s ease-in-out;
}

/* 底部按钮 */
.page-footer {
  padding: 24px 0 0;
  background: transparent;
  border-top: 1px solid var(--border-color);
  margin-top: 24px;
  transition: border-color 0.3s ease;
}

.download-buttons-container {
  display: flex;
  gap: 20px;
  max-width: 600px;
  margin: 0 auto;
  justify-content: center;
}

.download-btn-green,
.download-btn-blue {
  border: none;
  color: #fff;
  padding: 16px 40px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  text-decoration: none;
  min-width: 180px;
}

.download-btn-green {
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  box-shadow: 0 4px 15px rgba(34, 197, 94, 0.3);
}

.download-btn-green:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.4);
  color: white;
  text-decoration: none;
}

.download-btn-green.disabled {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  box-shadow: 0 4px 15px rgba(107, 114, 128, 0.3);
  cursor: not-allowed;
  opacity: 0.6;
}

.download-btn-green.disabled:hover {
  transform: none;
  box-shadow: 0 4px 15px rgba(107, 114, 128, 0.3);
}

.download-btn-blue {
  background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%);
  box-shadow: 0 4px 15px rgba(6, 182, 212, 0.3);
}

.download-btn-blue:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(6, 182, 212, 0.4);
  color: white;
  text-decoration: none;
}

.download-btn-green i,
.download-btn-blue i {
  font-size: 18px;
}

/* 无资源提示 */
.no-resource {
  text-align: center;
  padding: 100px 20px;
  background: var(--card-bg);
  border-radius: 12px;
  max-width: 600px;
  margin: 40px auto;
  box-shadow: var(--shadow-md);
  transition: background-color 0.3s ease;
}

.no-resource-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.no-resource-text {
  font-size: 1.5em;
  color: var(--text-secondary);
  margin-bottom: 30px;
}

.back-home-btn {
  display: inline-block;
  padding: 12px 30px;
  background: #3b82f6;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

/* 图片模态框 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10001;
}

.modal-close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.modal-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10001;
  font-size: 20px;
}

.modal-nav-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

.modal-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.modal-nav-prev {
  left: 30px;
}

.modal-nav-next {
  right: 30px;
}

.modal-content {
  max-width: 90%;
  max-height: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.modal-image {
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.modal-image-counter {
  margin-top: 16px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 16px;
  border-radius: 20px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .dl-header {
    padding: 30px 0 20px;
  }

  .dl-title {
    font-size: clamp(1.6rem, 7vw, 2.1rem);
  }

  .dl-description {
    font-size: 0.95rem;
  }

  .dl-dec-circle {
    display: none;
  }

  .page-container {
    border-radius: 0;
    box-shadow: none;
    border: none;
  }

  .page-content {
    padding: 20px;
  }

  .resource-name {
    font-size: 1.4rem;
  }

  .resource-desc {
    font-size: 0.95rem;
  }

  .carousel-wrapper {
    gap: 8px;
  }

  .carousel-btn {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  .carousel-container {
    height: 240px;
  }

  .modal-nav-btn {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .modal-nav-prev {
    left: 15px;
  }

  .modal-nav-next {
    right: 15px;
  }

  .bottom-info-bar {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .download-buttons-container {
    flex-direction: column;
    max-width: 100%;
  }

  .download-btn-green,
  .download-btn-blue {
    width: 100%;
    min-height: 60px;
  }

  .modal-close-btn {
    top: 10px;
    right: 10px;
    width: 35px;
    height: 35px;
  }

  .ad-image {
    max-height: 120px;
  }

  .ad-placeholder {
    padding: 30px 20px;
    min-height: 100px;
  }
}

@media (max-width: 480px) {
  .dl-header {
    padding: 25px 0 15px;
  }

  .dl-title {
    font-size: clamp(1.4rem, 6vw, 1.8rem);
  }

  .dl-description {
    font-size: 0.9rem;
  }

  .resource-name {
    font-size: 1.2rem;
  }

  .carousel-wrapper {
    gap: 6px;
  }

  .carousel-btn {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .carousel-container {
    height: 200px;
  }

  .modal-nav-btn {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .modal-nav-prev {
    left: 10px;
  }

  .modal-nav-next {
    right: 10px;
  }

  .modal-image-counter {
    font-size: 14px;
    padding: 6px 12px;
  }

  .ad-image {
    max-height: 100px;
  }

  .ad-placeholder {
    padding: 20px 16px;
    min-height: 80px;
  }

  .ad-placeholder i {
    font-size: 1.5rem;
  }

  .ad-placeholder span {
    font-size: 0.9rem;
  }
}
</style>
