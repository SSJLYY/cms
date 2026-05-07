<template>
  <div class="home">
    <!-- 现代化头部 -->
    <header class="modern-header">
      <div class="header-content">
        <div class="site-brand">
          <h1 class="site-title">
            <a href="/" class="title-link">
              <span class="title-text">{{ config['site.title'] || 'shaun・个人备份库' }}</span>
              <div class="title-accent"></div>
            </a>
          </h1>
        </div>
        
        <!-- 装饰性元素 -->
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
          <div class="decoration-grid"></div>
        </div>
      </div>
      
      <!-- 公告区域 -->
      <div class="announcement-banner">
        <div class="announcement-content">
          <div class="announcement-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"></path>
            </svg>
          </div>
          <div class="announcement-text">{{ config['site.announcement'] || '有什么需求游戏、软件等，右下角反馈给我，点图片可以放大查看' }}</div>
        </div>
      </div>
    </header>

    <div class="container">
      <!-- 搜索和分类容器 -->
      <div class="search-category-container">
        <!-- 分类导航 -->
        <div class="category-container">
          <div class="category-nav">
            <button 
              type="button" 
              :class="['category-btn', { active: selectedCategory === null }]"
              @click="selectCategory(null)"
            >
              <span class="category-icon">🏠</span>
              <span>全部</span>
            </button>
            <button 
              v-for="category in categories" 
              :key="category.id"
              type="button" 
              :class="['category-btn', { active: selectedCategory === category.id }]"
              @click="selectCategory(category.id)"
            >
              <span class="category-icon">{{ getCategoryIcon(category.name) }}</span>
              <span>{{ category.name }}</span>
            </button>
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="search-wrapper">
          <div class="search-container">
            <i class="fas fa-search search-icon"></i>
            <input 
              v-model="searchKeyword" 
              type="text" 
              class="search-input"
              placeholder="支持模糊匹配和编号搜索" 
              @input="handleSearch"
              @keydown.esc="clearSearch"
            />
            <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- 广告位 -->
      <div v-if="advertisements.length > 0" class="home-advertisement-section">
        <div 
          v-for="ad in advertisements" 
          :key="ad.id"
          class="home-advertisement-item"
          @click="handleAdClick(ad)"
        >
          <img 
            v-if="ad.imageUrl" 
            :src="ad.imageUrl" 
            :alt="ad.name"
            class="home-ad-image"
            loading="lazy"
          />
          <div v-else class="home-ad-placeholder">
            <i class="fas fa-ad"></i>
            <span>{{ ad.name }}</span>
          </div>
        </div>
      </div>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-skeleton">
        <div v-for="i in 6" :key="i" class="skeleton-card">
          <div class="skeleton-image"></div>
          <div class="skeleton-title"></div>
          <div class="skeleton-desc"></div>
          <div class="skeleton-btn"></div>
        </div>
      </div>

      <!-- 无搜索结果提示 -->
      <div v-if="!loading && filteredResources.length === 0 && searchKeyword" class="no-result">
        <div class="no-result-icon">🔍</div>
        <div class="no-result-text">未找到相关资源</div>
        <div class="no-result-subtext">请尝试更换关键词或清除搜索框</div>
        <button class="clear-search-btn" @click="clearSearch">清除搜索</button>
      </div>

      <!-- 空状态提示 -->
      <div v-if="!loading && filteredResources.length === 0 && !searchKeyword" class="empty-state">
        <div class="empty-icon">📦</div>
        <div class="empty-text">暂无资源</div>
        <div class="empty-subtext">稍后再来看看吧</div>
      </div>

      <!-- 软件列表 -->
      <div v-if="!loading && filteredResources.length > 0" class="software-grid">
        <div 
          v-for="(resource, index) in paginatedResources" 
          :key="resource.id" 
          :id="`resource-${resource.id}`"
          class="software-card"
          :style="{ '--delay': (index % 9) * 0.05 + 's' }"
        >
          <ChristmasHat v-if="isChristmasTheme" />
          <span class="card-number">{{ formatCardNumber((currentPage - 1) * pageSize + index + 1) }}</span>
          
          <div class="software-name">
            <span class="category-badge" :class="getCategoryClass(resource.categoryName)">
              {{ getCategoryIcon(resource.categoryName) }}
            </span>
            {{ resource.title }}
          </div>
          <div class="software-desc">{{ resource.description || '暂无描述' }}</div>
          
          <!-- 图片展示 -->
          <div v-if="resource.coverImageUrl" class="software-image-container">
            <img 
              :src="resource.coverImageUrl" 
              :alt="resource.title" 
              class="software-image"
              loading="lazy"
              @click="openImageModal(resource.coverImageUrl, resource.title)"
            />
            <div class="image-overlay">
              <i class="fas fa-search-plus"></i>
            </div>
          </div>
          
          <div class="download-section">
            <div class="download-buttons">
              <router-link 
                v-for="link in resource.downloadLinks" 
                :key="link.id"
                :to="{ 
                  path: `/resource/${resource.id}`, 
                  query: { type: link.linkType } 
                }"
                :class="['download-btn', getBtnClass(link.linkType)]"
              >
                <span class="btn-icon">
                  <i :class="getLinkTypeIcon(link.linkType)"></i>
                </span>
                <span class="btn-text">{{ getLinkTypeName(link.linkType) }}</span>
              </router-link>
            </div>
            <div class="download-hint">
              <i class="fas fa-info-circle"></i>
              资源较大时推荐使用对应客户端
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && filteredResources.length > 0" class="pagination">
        <button 
          class="pagination-btn"
          :disabled="currentPage === 1"
          @click="goToPage(1)"
        >
          <i class="fas fa-angle-double-left"></i>
        </button>
        <button 
          class="pagination-btn"
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          <i class="fas fa-angle-left"></i>
        </button>
        
        <template v-for="page in visiblePages" :key="page">
          <button 
            v-if="page !== '...'"
            :class="['pagination-btn', 'pagination-number', { active: currentPage === page }]"
            @click="goToPage(page)"
          >
            {{ page }}
          </button>
          <span v-else class="pagination-ellipsis">...</span>
        </template>
        
        <button 
          class="pagination-btn"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          <i class="fas fa-angle-right"></i>
        </button>
        <button 
          class="pagination-btn"
          :disabled="currentPage === totalPages"
          @click="goToPage(totalPages)"
        >
          <i class="fas fa-angle-double-right"></i>
        </button>
      </div>

      <!-- 分页信息 -->
      <div v-if="!loading && filteredResources.length > 0" class="pagination-info">
        显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredResources.length) }} 条，共 {{ filteredResources.length }} 条资源
      </div>
    </div>
    
    <!-- 图片查看器模态框 -->
    <Transition name="modal">
      <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
        <button class="modal-close-btn" @click="closeImageModal">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div class="modal-content" @click.stop>
          <img :src="modalImageSrc" :alt="modalImageAlt" class="modal-image" />
        </div>
      </div>
    </Transition>

    <!-- 友链按钮 -->
    <FriendLinkButton />

    <!-- 右下角操作按钮（反馈+帮助） -->
    <ActionButtons />

    <!-- 免责声明弹窗 -->
    <DisclaimerModal />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getResourceList, getConfig, getLinkTypes } from '../api/resource'
import { getActiveAdvertisements, recordClick } from '../api/promotion'
import FriendLinkButton from '../components/FriendLinkButton.vue'
import ActionButtons from '../components/ActionButtons.vue'
import DisclaimerModal from '../components/DisclaimerModal.vue'
import ChristmasHat from '../components/ChristmasHat.vue'

const resources = ref([])
const categories = ref([])
const linkTypes = ref([])
const linkTypeMap = ref({})
const searchKeyword = ref('')
const selectedCategory = ref(null)
const config = ref({})
const loading = ref(true)
const showImageModal = ref(false)
const modalImageSrc = ref('')
const modalImageAlt = ref('')
const currentPage = ref(1)
const pageSize = 9
const advertisements = ref([])

const normalizeAdvertisement = (ad = {}) => ({
  ...ad,
  name: ad.name || ad.title || '',
  title: ad.title || ad.name || ''
})

const isChristmasTheme = computed(() => {
  return document.documentElement.getAttribute('data-theme') === 'christmas'
})

const filteredResources = computed(() => {
  let filtered = resources.value
  
  if (selectedCategory.value !== null) {
    filtered = filtered.filter(resource => resource.categoryId === selectedCategory.value)
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(resource => 
      resource.title.toLowerCase().includes(keyword) ||
      (resource.description && resource.description.toLowerCase().includes(keyword)) ||
      (resource.categoryName && resource.categoryName.toLowerCase().includes(keyword)) ||
      (resource.id && resource.id.toString().includes(keyword))
    )
  }
  
  return filtered
})

const paginatedResources = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredResources.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredResources.value.length / pageSize)
})

const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push('...')
      pages.push(total)
    }
  }
  
  return pages
})

const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const clearSearch = () => {
  searchKeyword.value = ''
  selectedCategory.value = null
  currentPage.value = 1
}

const openImageModal = (src, alt) => {
  modalImageSrc.value = src
  modalImageAlt.value = alt
  showImageModal.value = true
  document.body.style.overflow = 'hidden'
}

const closeImageModal = () => {
  showImageModal.value = false
  document.body.style.overflow = ''
}

const formatCardNumber = (num) => {
  return 'A' + num.toString().padStart(3, '0')
}

const getLinkTypeName = (type) => {
  return linkTypeMap.value[type] || type
}

const getLinkTypeIcon = (type) => {
  const iconMap = {
    'quark': 'fas fa-cloud',
    'xunlei': 'fas fa-bolt',
    'baidu': 'fas fa-database',
    'aliyun': 'fas fa-cloud-sun',
    'lanzou': 'fas fa-link',
    'mobile': 'fas fa-mobile-alt',
    'uc': 'fas fa-compass',
    'direct': 'fas fa-download'
  }
  return iconMap[type] || 'fas fa-download'
}

const getBtnClass = (type) => {
  const classMap = {
    'quark': 'btn-quark',
    'xunlei': 'btn-xunlei',
    'baidu': 'btn-baidu',
    'aliyun': 'btn-aliyun',
    'lanzou': 'btn-lanzou',
    'mobile': 'btn-mobile',
    'uc': 'btn-uc',
    'direct': 'btn-direct'
  }
  return classMap[type] || 'btn-quark'
}

const getCategoryIcon = (categoryName) => {
  const iconMap = {
    'TV软件': '📺',
    '电脑软件': '💻',
    '安卓软件': '📱',
    '游戏娱乐': '🎮',
    '多端壁纸': '🖼️',
    '学习资源': '📚',
    '系统工具': '🔧',
    '其他综合': '📦'
  }
  return iconMap[categoryName] || '📦'
}

const getCategoryClass = (categoryName) => {
  const classMap = {
    'TV软件': 'cat-tv',
    '电脑软件': 'cat-pc',
    '安卓软件': 'cat-android',
    '游戏娱乐': 'cat-game',
    '多端壁纸': 'cat-wallpaper',
    '学习资源': 'cat-study',
    '系统工具': 'cat-tool',
    '其他综合': 'cat-other'
  }
  return classMap[categoryName] || 'cat-other'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const loadResources = async () => {
  try {
    loading.value = true
    const res = await getResourceList()
    resources.value = res.data || []
  } catch (error) {
    ElMessage.error('加载资源失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadCategories = () => {
  const uniqueCategories = [...new Map(
    resources.value.map(r => [r.categoryId, { id: r.categoryId, name: r.categoryName }])
  ).values()]
  categories.value = uniqueCategories
}

const loadConfig = async () => {
  try {
    const res = await getConfig()
    config.value = res.data?.configs || {}
  } catch (error) {
    config.value = {
      'site.title': 'shaun・个人备份库',
      'site.announcement': '有什么需求游戏、软件等，右下角反馈给我，点图片可以放大查看'
    }
  }
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
    // 静默处理，使用默认空映射
  }
}

const loadAdvertisements = async () => {
  try {
    const res = await getActiveAdvertisements('homepage')
    if (res.code === 200 && res.data) {
        advertisements.value = res.data.map(normalizeAdvertisement)
    }
  } catch (error) {
    // 静默处理，广告加载失败不影响核心功能
  }
}

const normalizeExternalUrl = (url) => {
  const value = typeof url === 'string' ? url.trim() : ''
  if (!value) return ''

  const lowerValue = value.toLowerCase()
  if (lowerValue.startsWith('javascript:') || lowerValue.startsWith('data:')) {
    return ''
  }

  if (lowerValue.startsWith('http://') || lowerValue.startsWith('https://')) {
    return value
  }

  return `https://${value}`
}

const handleAdClick = async (ad) => {
  try {
    await recordClick(ad.id)
    if (ad.linkUrl) {
      const url = normalizeExternalUrl(ad.linkUrl)
      if (!url) {
        ElMessage.error('链接地址无效')
        return
      }
      window.open(url, '_blank', 'noopener,noreferrer')
    }
  } catch (error) {
    // 记录点击失败不影响跳转
  }
}

watch([searchKeyword, selectedCategory], () => {
  currentPage.value = 1
})

onMounted(async () => {
  const configPromise = loadConfig()
  const resourcesPromise = loadResources()
  const linkTypesPromise = loadLinkTypes()
  const adsPromise = loadAdvertisements()

  await configPromise
  await resourcesPromise
  loadCategories()
  await Promise.all([linkTypesPromise, adsPromise])
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
* {
  scrollbar-width: thin;
  scrollbar-color: #3498db #f5f5f5;
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

::selection {
  background: rgba(102, 126, 234, 0.3);
}

.home {
  min-height: 100vh;
  background-color: var(--bg-primary);
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  color: var(--text-primary);
  line-height: 1.6;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

.modern-header {
  position: relative;
  padding: 80px 0 50px;
  margin: 0 0 40px;
  text-align: center;
  overflow: hidden;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.header-content {
  position: relative;
  z-index: 2;
}

.site-brand {
  margin-bottom: 24px;
}

.site-title {
  margin: 0 0 20px;
  font-size: clamp(2rem, 6vw, 3.2rem);
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.1;
}

.title-link {
  text-decoration: none;
  color: inherit;
  position: relative;
  display: inline-block;
  transition: all 0.3s ease;
}

.title-link:hover {
  transform: translateY(-2px);
}

.title-text {
  position: relative;
  background: linear-gradient(135deg, #1e293b 0%, #475569 50%, #64748b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: inline-block;
}

.title-accent {
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #06b6d4);
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.6s ease;
}

.title-link:hover .title-accent {
  transform: scaleX(1);
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  top: -100px;
  right: 10%;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  top: 60%;
  left: 5%;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  top: 20%;
  left: 60%;
  animation-delay: 4s;
}

.decoration-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(102, 126, 234, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(102, 126, 234, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(180deg); }
}

.announcement-banner {
  margin-top: 32px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 16px;
  padding: 20px 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(8px);
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: 14px;
}

.announcement-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.announcement-text {
  color: #64748b;
  font-size: 1rem;
  line-height: 1.6;
  font-weight: 400;
  text-align: left;
}

.search-category-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

.category-container {
  position: relative;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.category-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 50px;
  font-size: 0.9rem;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-btn:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.category-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.category-icon {
  font-size: 1rem;
}

.search-wrapper {
  display: flex;
  justify-content: center;
}

.search-container {
  position: relative;
  width: 100%;
  max-width: 640px;
}

.search-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1em;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 18px 50px 18px 52px;
  font-size: 1.05em;
  border: 2px solid var(--border-color);
  border-radius: 50px;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  background: var(--card-bg);
  color: var(--text-primary);
}

.search-input:focus {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.clear-btn {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: #f1f5f9;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.home-advertisement-section {
  margin: 0 0 32px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.home-advertisement-item {
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: var(--card-bg);
}

.home-advertisement-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.home-ad-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  min-height: 160px;
  max-height: 220px;
}

.home-ad-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  gap: 14px;
  min-height: 160px;
}

.home-ad-placeholder i {
  font-size: 3rem;
  opacity: 0.9;
}

.home-ad-placeholder span {
  font-size: 1.2rem;
  font-weight: 600;
  text-align: center;
}

.loading-skeleton {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin: 24px 0;
}

.skeleton-card {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.skeleton-image {
  width: 100%;
  height: 180px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 12px;
  margin-bottom: 16px;
}

.skeleton-title {
  height: 24px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  margin-bottom: 12px;
  width: 80%;
}

.skeleton-desc {
  height: 16px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 20px;
}

.skeleton-btn {
  height: 48px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 24px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.no-result,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--card-bg);
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 40px;
}

.no-result-icon,
.empty-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.no-result-text,
.empty-text {
  font-size: 1.3em;
  color: var(--text-secondary);
  margin-bottom: 10px;
  font-weight: 600;
}

.no-result-subtext,
.empty-subtext {
  font-size: 0.95em;
  color: var(--text-tertiary);
  margin-bottom: 24px;
}

.clear-search-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.software-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  margin: 28px 0;
}

.software-card {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.4s ease, background-color 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  animation: cardFadeIn 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.software-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
}

.card-number {
  position: absolute;
  top: 16px;
  right: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 600;
}

.software-name {
  font-size: 1.25em;
  color: var(--text-primary);
  margin-bottom: 12px;
  font-weight: 700;
  padding-right: 70px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.category-badge {
  font-size: 0.75em;
  padding: 2px 8px;
  border-radius: 6px;
  background: #f1f5f9;
}

.category-badge.cat-tv { background: #dbeafe; color: #1d4ed8; }
.category-badge.cat-pc { background: #dcfce7; color: #15803d; }
.category-badge.cat-android { background: #fce7f3; color: #be185d; }
.category-badge.cat-game { background: #fef3c7; color: #b45309; }
.category-badge.cat-wallpaper { background: #e0e7ff; color: #4338ca; }
.category-badge.cat-study { background: #ccfbf1; color: #0f766e; }
.category-badge.cat-tool { background: #f3e8ff; color: #7c3aed; }

.software-desc {
  color: var(--text-secondary);
  font-size: 0.95em;
  margin-bottom: 18px;
  line-height: 1.7;
  text-align: justify;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.software-image-container {
  width: 100%;
  height: 200px;
  margin-bottom: 18px;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.software-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.software-card:hover .software-image {
  transform: scale(1.08);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-overlay i {
  font-size: 2rem;
  color: white;
}

.software-image-container:hover .image-overlay {
  opacity: 1;
}

.download-section {
  border-top: 1px solid var(--border-color);
  padding-top: 18px;
  margin-top: auto;
}

.download-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 14px;
}

.download-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 14px 8px;
  font-size: 0.75em;
  font-weight: 600;
  text-decoration: none;
  border-radius: 14px;
  transition: all 0.3s ease;
  min-height: 64px;
  gap: 6px;
}

.download-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
  color: white;
  text-decoration: none;
}

.btn-icon {
  font-size: 1.2em;
}

.btn-text {
  font-size: 0.8em;
}

.btn-quark {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 14px rgba(245, 87, 108, 0.3);
}

.btn-xunlei {
  background: linear-gradient(135deg, #ffb347 0%, #ff8c00 100%);
  box-shadow: 0 4px 14px rgba(255, 140, 0, 0.3);
}

.btn-baidu {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 14px rgba(79, 172, 254, 0.3);
}

.btn-aliyun {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 4px 14px rgba(67, 233, 123, 0.3);
}

.btn-lanzou {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
}

.btn-mobile {
  background: linear-gradient(135deg, #00c9ff 0%, #92fe9d 100%);
  box-shadow: 0 4px 14px rgba(0, 201, 255, 0.3);
}

.btn-uc {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 14px rgba(240, 147, 251, 0.3);
}

.btn-direct {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.3);
}

.download-hint {
  font-size: 0.8em;
  color: var(--text-tertiary);
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin: 40px 0 16px;
  flex-wrap: wrap;
}

.pagination-btn {
  min-width: 44px;
  height: 44px;
  padding: 0 12px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  font-size: 0.9em;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.pagination-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-number {
  font-weight: 500;
}

.pagination-ellipsis {
  color: var(--text-tertiary);
  padding: 0 8px;
}

.pagination-info {
  text-align: center;
  font-size: 0.85em;
  color: var(--text-tertiary);
  margin-bottom: 40px;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.92);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.modal-close-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10000;
}

.modal-close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: rotate(90deg);
}

.modal-content {
  max-width: 90%;
  max-height: 90%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-image {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

@media (max-width: 1024px) {
  .software-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .loading-skeleton {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }

  .modern-header {
    padding: 50px 0 30px;
  }

  .site-title {
    font-size: clamp(1.6rem, 8vw, 2.4rem);
  }

  .decoration-circle {
    display: none;
  }

  .announcement-banner {
    margin-top: 20px;
    padding: 16px 20px;
    border-radius: 12px;
  }

  .announcement-icon {
    width: 36px;
    height: 36px;
  }

  .announcement-text {
    font-size: 0.9rem;
  }

  .software-grid,
  .loading-skeleton {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .software-card {
    padding: 20px;
  }

  .card-number {
    font-size: 0.75em;
    padding: 4px 10px;
  }

  .software-name {
    font-size: 1.1em;
    padding-right: 60px;
  }

  .software-image-container {
    height: 180px;
  }

  .download-buttons {
    grid-template-columns: 1fr;
  }

  .download-btn {
    flex-direction: row;
    min-height: 48px;
    gap: 10px;
  }

  .pagination-btn {
    min-width: 40px;
    height: 40px;
  }

  .home-advertisement-section {
    grid-template-columns: 1fr;
    margin: 0 0 24px;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 12px;
  }

  .modern-header {
    padding: 40px 0 24px;
  }

  .site-title {
    font-size: clamp(1.4rem, 7vw, 2rem);
    margin-bottom: 16px;
  }

  .announcement-content {
    flex-direction: row;
    gap: 12px;
  }

  .announcement-icon {
    width: 32px;
    height: 32px;
  }

  .announcement-text {
    font-size: 0.85rem;
  }

  .category-btn {
    padding: 8px 16px;
    font-size: 0.85rem;
  }

  .search-input {
    padding: 14px 44px 14px 46px;
    font-size: 1em;
  }

  .software-card {
    padding: 16px;
  }

  .software-image-container {
    height: 160px;
    margin-bottom: 14px;
  }

  .pagination {
    gap: 6px;
  }

  .pagination-btn {
    min-width: 36px;
    height: 36px;
    font-size: 0.85em;
  }
}
</style>
