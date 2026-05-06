<template>
  <div class="resource-detail">
    <!-- 现代化返回头部 -->
    <div class="modern-header">
      <div class="header-content">
        <router-link to="/" class="back-btn-modern">
          <div class="back-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
          </div>
          <span>返回首页</span>
        </router-link>
        <div class="header-decoration">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
          <div class="deco-circle deco-3"></div>
        </div>
      </div>
    </div>

    <!-- 下载确认页头部 -->
    <div class="dl-header-modern">
      <div class="dl-content">
        <h1 class="dl-title">
          <div class="title-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
            </svg>
          </div>
          资源下载详情页
        </h1>
        <p class="dl-description">翻到页面底部即可进行下载</p>
        <div class="dl-features">
          <div class="feature-badge">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            安全无毒
          </div>
          <div class="feature-badge">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            永久有效
          </div>
          <div class="feature-badge">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
            </svg>
            高速下载
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-modern">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 资源详情 -->
    <div v-else-if="resource" class="page-container-modern">
      <!-- 页面内容 -->
      <div class="page-content">
        <!-- 资源信息卡片 -->
        <div class="resource-info-card">
          <div class="resource-header">
            <h4 class="resource-name">{{ resource.title }}</h4>
            <div class="resource-stats">
              <span class="stat-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                {{ resource.viewCount || 0 }} 次浏览
              </span>
              <span class="stat-item">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
                </svg>
                {{ resource.downloadCount || 0 }} 次下载
              </span>
            </div>
          </div>
          <p class="resource-desc">{{ resource.description || '暂无描述' }}</p>
          <div class="resource-meta">
            <span class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              更新时间：{{ formatDate(resource.updateTime || resource.createTime) }}
            </span>
          </div>
        </div>

        <!-- 资源图片轮播 -->
        <div v-if="displayImages.length > 0" class="resource-images-section">
          <h3 class="section-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            资源预览
            <span class="image-count">({{ displayImages.length }} 张)</span>
          </h3>
          <div class="carousel-wrapper">
            <button 
              v-if="displayImages.length > 1"
              class="carousel-btn carousel-btn-prev" 
              @click="prevImage"
              :disabled="currentImageIndex === 0"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"/>
              </svg>
            </button>

            <div class="carousel-container" @click="openImageModal(currentImageIndex)">
              <img 
                :src="displayImages[currentImageIndex]" 
                :alt="resource.title" 
                class="resource-image"
                loading="lazy"
              />
              <div class="image-overlay">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  <line x1="11" y1="8" x2="11" y2="14"/>
                  <line x1="8" y1="11" x2="14" y2="11"/>
                </svg>
                <span>点击查看大图</span>
              </div>
            </div>

            <button 
              v-if="displayImages.length > 1"
              class="carousel-btn carousel-btn-next" 
              @click="nextImage"
              :disabled="currentImageIndex === displayImages.length - 1"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
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
            class="advertisement-card"
            @click="handleAdClick(ad)"
          >
            <img 
              v-if="ad.imageUrl" 
              :src="ad.imageUrl" 
              :alt="ad.name"
              class="ad-image"
              loading="lazy"
            />
            <div v-else class="ad-placeholder">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
              <span>{{ ad.name }}</span>
            </div>
          </div>
        </div>

        <!-- 下载区域 -->
        <div class="download-section">
          <!-- 已下载过此资源提示 -->
          <div v-if="hasDownloaded" class="download-notice download-notice-info">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
            您今日已下载过此资源，重复下载不会计数
          </div>

          <!-- 下载次数已满提示 -->
          <div 
            v-else-if="remainingDownloads === 0" 
            ref="warningNotice"
            class="download-notice download-notice-warning"
            :class="{ 'shake': isShaking }"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            您今日下载次数已满，无法下载，请明日再下载
          </div>

          <!-- 剩余下载次数 -->
          <div class="remaining-downloads">
            <div class="remaining-card">
              <div class="remaining-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
              </div>
              <div class="remaining-info">
                <span class="remaining-label">今日剩余下载次数</span>
                <span class="remaining-value" :class="{ 'low': remainingDownloads !== null && remainingDownloads <= 1 }">{{ remainingDownloads ?? '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 下载按钮 -->
          <div class="download-buttons-container">
            <a 
              v-for="link in filteredDownloadLinks" 
              :key="link.id"
              :href="canAccessDownloadLinks ? getFullUrl(link.linkUrl) : '#'"
              :class="['download-btn-green', { 'disabled': !canAccessDownloadLinks }]"
              :target="canAccessDownloadLinks ? '_blank' : ''"
              :rel="canAccessDownloadLinks ? 'noopener noreferrer' : ''"
              @click="handleDownload(link.linkType, $event)"
            >
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
              </svg>
              <div class="btn-content">
                <span class="btn-text">立即下载</span>
                <span class="btn-sub">{{ getLinkTypeName(link.linkType) }}</span>
              </div>
            </a>
            <button 
              class="download-btn-outline"
              @click="reportInvalidLink"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              链接失效
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 无资源提示 -->
    <div v-else class="no-resource-modern">
      <div class="no-resource-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <circle cx="12" cy="12" r="10"/>
          <path d="M16 16s-1.5-2-4-2-4 2-4 2"/>
          <line x1="9" y1="9" x2="9.01" y2="9"/>
          <line x1="15" y1="9" x2="15.01" y2="9"/>
        </svg>
      </div>
      <h3>资源不存在</h3>
      <p>抱歉，您访问的资源已下架或不存在</p>
      <router-link to="/" class="back-home-btn">返回首页</router-link>
    </div>

    <!-- 图片查看器模态框 -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <button class="modal-close-btn" @click="closeImageModal">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      <button 
        v-if="displayImages.length > 1"
        class="modal-nav-btn modal-nav-prev" 
        @click.stop="prevModalImage"
        :disabled="modalImageIndex === 0"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>

      <div class="modal-content" @click.stop>
        <img :src="displayImages[modalImageIndex]" :alt="resource?.title" class="modal-image" />
        <div class="modal-image-counter">
          {{ modalImageIndex + 1 }} / {{ displayImages.length }}
        </div>
      </div>

      <button 
        v-if="displayImages.length > 1"
        class="modal-nav-btn modal-nav-next" 
        @click.stop="nextModalImage"
        :disabled="modalImageIndex === displayImages.length - 1"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </button>
    </div>

    <!-- 右下角操作按钮 -->
    <ActionButtons />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const remainingDownloads = ref(null)
const hasDownloaded = ref(false)
const isShaking = ref(false)
const warningNotice = ref(null)
const advertisements = ref([])

const canAccessDownloadLinks = computed(() => hasDownloaded.value || (remainingDownloads.value ?? 0) > 0)

const filteredDownloadLinks = computed(() => {
  if (!resource.value || !resource.value.downloadLinks) {
    return []
  }
  
  const linkType = route.query.type
  
  if (!linkType) {
    return resource.value.downloadLinks
  }
  
  const filtered = resource.value.downloadLinks.filter(
    link => link.linkType === linkType
  )
  
  return filtered.length > 0 ? filtered : resource.value.downloadLinks
})

const displayImages = computed(() => {
  if (!resource.value) return []
  
  const images = []
  
  if (resource.value.images && resource.value.images.length > 0) {
    resource.value.images.slice(0, 5).forEach(img => {
      if (img.fileUrl) {
        images.push(img.fileUrl)
      }
    })
  }
  
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
    if (res.data) {
      resource.value = res.data
    } else {
      ElMessage.error('资源不存在或已下架')
    }
  } catch (error) {
    ElMessage.error('加载资源详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const triggerShake = () => {
  isShaking.value = true
  setTimeout(() => {
    isShaking.value = false
  }, 500)
  
  if (warningNotice.value) {
    warningNotice.value.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const handleDownload = async (linkType, event) => {
  if (!hasDownloaded.value && (!remainingDownloads.value || remainingDownloads.value === 0)) {
    event.preventDefault()
    triggerShake()
    return
  }
  
  try {
    await recordDownload(resource.value.id, { skipBusinessErrorMessage: true })
    await loadRemainingDownloads()
    await checkIfDownloaded()
  } catch (error) {
    if (error.response && error.response.data) {
      const code = error.response.data.code
      if (code === 208) {
        await checkIfDownloaded()
      } else if (code === 429) {
        event.preventDefault()
        await loadRemainingDownloads()
        triggerShake()
      }
    }
  }
}

const reportInvalidLink = () => {
  ElMessage.success('感谢反馈!我们会尽快处理失效链接。')
}

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
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  return `https://${url}`
}

const getLinkTypeName = (type) => {
  return linkTypeMap.value[type] || type
}

const loadLinkTypes = async () => {
  try {
    const res = await getLinkTypes()
    linkTypes.value = res.data || []
    linkTypeMap.value = linkTypes.value.reduce((map, type) => {
      map[type.typeCode] = type.typeName
      return map
    }, {})
  } catch (error) {
    ElMessage.error('加载网盘类型失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const loadRemainingDownloads = async () => {
  try {
    const res = await getRemainingDownloads({ skipBusinessErrorMessage: true })
    if (res.code === 200) {
      remainingDownloads.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取剩余下载次数失败')
  }
}

const checkIfDownloaded = async () => {
  try {
    const resourceId = route.params.id
    const res = await checkDownloaded(resourceId, { skipBusinessErrorMessage: true })
    if (res.code === 200) {
      hasDownloaded.value = res.data
    }
  } catch (error) {
    ElMessage.error('检查下载状态失败')
  }
}

const loadAdvertisements = async () => {
  try {
    const res = await getActiveAdvertisements('download')
    if (res.code === 200 && res.data) {
      advertisements.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载广告失败')
  }
}

const handleAdClick = async (ad) => {
  try {
    await recordClick(ad.id)
    if (ad.linkUrl) {
      let url = ad.linkUrl
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'https://' + url
      }
      window.open(url, '_blank', 'noopener,noreferrer')
    }
  } catch (error) {
    ElMessage.error('记录广告点击失败')
  }
}

onMounted(async () => {
  await Promise.all([
    loadLinkTypes(),
    loadResourceDetail(),
    loadRemainingDownloads(),
    checkIfDownloaded(),
    loadAdvertisements()
  ])
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

const handleKeydown = (e) => {
  if (e.key === 'Escape' && showImageModal.value) {
    closeImageModal()
  }
}
</script>

<style scoped>
.resource-detail {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  font-family: 'Microsoft YaHei', -apple-system, sans-serif;
  padding-bottom: 60px;
}

/* 现代化头部 */
.modern-header {
  background: white;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  position: relative;
}

.back-btn-modern {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 50px;
  background: rgba(102, 126, 234, 0.1);
}

.back-btn-modern:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateX(-4px);
}

.back-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-decoration {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.deco-1 {
  width: 60px;
  height: 60px;
  background: #667eea;
  right: 0;
  top: -30px;
  animation: float 6s ease-in-out infinite;
}

.deco-2 {
  width: 40px;
  height: 40px;
  background: #f5576c;
  right: 40px;
  top: -20px;
  animation: float 6s ease-in-out infinite 2s;
}

.deco-3 {
  width: 30px;
  height: 30px;
  background: #4facfe;
  right: 80px;
  top: -15px;
  animation: float 6s ease-in-out infinite 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 下载页头部 */
.dl-header-modern {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.dl-header-modern::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.dl-content {
  position: relative;
  z-index: 1;
}

.dl-title {
  font-size: clamp(1.8rem, 5vw, 2.5rem);
  font-weight: 700;
  color: white;
  margin: 0 0 16px;
  display: inline-flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dl-description {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 24px;
}

.dl-features {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.feature-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 50px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  backdrop-filter: blur(10px);
}

/* 加载状态 */
.loading-modern {
  text-align: center;
  padding: 100px 20px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid rgba(102, 126, 234, 0.2);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 页面容器 */
.page-container-modern {
  max-width: 900px;
  margin: -30px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.page-content {
  background: white;
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

/* 资源信息卡片 */
.resource-info-card {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}

.resource-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.resource-name {
  font-size: 1.6rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.resource-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 14px;
}

.resource-desc {
  font-size: 1rem;
  color: #475569;
  line-height: 1.8;
  white-space: pre-line;
  word-wrap: break-word;
  margin: 0 0 16px;
}

.resource-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #94a3b8;
}

/* 图片区域 */
.resource-images-section {
  margin: 32px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px;
}

.image-count {
  font-weight: 400;
  color: #94a3b8;
  font-size: 14px;
}

.carousel-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.carousel-btn {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: white;
  border: 2px solid #e2e8f0;
  color: #667eea;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.carousel-btn:hover:not(:disabled) {
  background: #667eea;
  border-color: #667eea;
  color: white;
  transform: scale(1.1);
}

.carousel-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.carousel-container {
  flex: 1;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  background: #f1f5f9;
  position: relative;
}

.resource-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.carousel-container:hover .image-overlay {
  opacity: 1;
}

.carousel-container:hover .resource-image {
  transform: scale(1.05);
}

.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #cbd5e1;
  cursor: pointer;
  transition: all 0.3s;
}

.indicator-dot:hover {
  background: #94a3b8;
  transform: scale(1.2);
}

.indicator-dot.active {
  background: #667eea;
  width: 24px;
  border-radius: 5px;
}

/* 广告区域 */
.advertisement-section {
  margin: 32px 0;
}

.advertisement-card {
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.advertisement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.ad-image {
  width: 100%;
  height: auto;
  display: block;
  max-height: 160px;
  object-fit: cover;
}

.ad-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  color: #94a3b8;
  min-height: 140px;
}

/* 下载区域 */
.download-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 2px solid #f1f5f9;
}

.download-notice {
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 500;
}

.download-notice-info {
  background: #ecfdf5;
  color: #059669;
  border: 1px solid #a7f3d0;
}

.download-notice-warning {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-6px); }
  20%, 40%, 60%, 80% { transform: translateX(6px); }
}

.download-notice.shake {
  animation: shake 0.5s ease-in-out;
}

/* 剩余下载次数 */
.remaining-downloads {
  margin-bottom: 24px;
}

.remaining-card {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 24px;
  border-radius: 16px;
  color: white;
}

.remaining-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remaining-info {
  display: flex;
  flex-direction: column;
}

.remaining-label {
  font-size: 13px;
  opacity: 0.9;
}

.remaining-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.remaining-value.low {
  color: #fde68a;
}

/* 下载按钮 */
.download-buttons-container {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.download-btn-green {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 20px 32px;
  border-radius: 16px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);
  flex: 1;
  min-width: 200px;
  max-width: 300px;
  justify-content: center;
}

.download-btn-green:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.4);
  color: white;
  text-decoration: none;
}

.download-btn-green.disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  box-shadow: 0 4px 15px rgba(100, 116, 139, 0.3);
  cursor: not-allowed;
}

.download-btn-green.disabled:hover {
  transform: none;
}

.btn-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.btn-text {
  font-size: 16px;
}

.btn-sub {
  font-size: 12px;
  opacity: 0.8;
}

.download-btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: white;
  color: #f59e0b;
  padding: 20px 28px;
  border-radius: 16px;
  border: 2px solid #f59e0b;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.download-btn-outline:hover {
  background: #fffbeb;
  transform: translateY(-2px);
}

/* 无资源提示 */
.no-resource-modern {
  text-align: center;
  padding: 100px 20px;
  background: white;
  border-radius: 24px;
  max-width: 500px;
  margin: 60px auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.no-resource-icon {
  margin-bottom: 24px;
  color: #94a3b8;
}

.no-resource-modern h3 {
  font-size: 1.5rem;
  color: #1e293b;
  margin: 0 0 12px;
}

.no-resource-modern p {
  color: #64748b;
  margin: 0 0 32px;
}

.back-home-btn {
  display: inline-block;
  padding: 14px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 600;
  transition: all 0.3s;
}

.back-home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  color: white;
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
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
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
  width: 56px;
  height: 56px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 10001;
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
}

.modal-image {
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-image-counter {
  margin-top: 20px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.5);
  padding: 10px 20px;
  border-radius: 50px;
}

/* 响应式 */
@media (max-width: 768px) {
  .dl-header-modern {
    padding: 40px 20px;
  }
  
  .dl-features {
    gap: 12px;
  }
  
  .feature-badge {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .page-container-modern {
    margin-top: -20px;
  }
  
  .page-content {
    padding: 20px;
    border-radius: 16px;
  }
  
  .resource-name {
    font-size: 1.3rem;
  }
  
  .carousel-container {
    height: 280px;
  }
  
  .carousel-btn {
    width: 40px;
    height: 40px;
  }
  
  .modal-nav-btn {
    width: 44px;
    height: 44px;
  }
  
  .modal-nav-prev {
    left: 10px;
  }
  
  .modal-nav-next {
    right: 10px;
  }
  
  .download-buttons-container {
    flex-direction: column;
  }
  
  .download-btn-green,
  .download-btn-outline {
    max-width: 100%;
  }
}
</style>
