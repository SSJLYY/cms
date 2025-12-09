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
          <button 
            type="button" 
            class="category-toggle"
            @click="toggleCategoryMenu"
          >
            资源分类 <i class="fas fa-chevron-down"></i>
          </button>
          
          <div class="category-nav" :class="{ 'show': showCategoryMenu }">
            <button 
              type="button" 
              :class="['category-btn', { active: selectedCategory === null }]"
              @click="selectCategory(null)"
            >
              全部
            </button>
            <button 
              v-for="category in categories" 
              :key="category.id"
              type="button" 
              :class="['category-btn', { active: selectedCategory === category.id }]"
              @click="selectCategory(category.id)"
            >
              {{ category.name }}
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
              @keypress.enter="handleSearch"
              @keydown.esc="clearSearch"
            />
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
          />
          <div v-else class="home-ad-placeholder">
            <i class="fas fa-ad"></i>
            <span>{{ ad.name }}</span>
          </div>
        </div>
      </div>

      <!-- 加载动画 -->
      <div v-if="loading" class="loading">
        搜索中<span>.</span><span>.</span><span>.</span>
      </div>

      <!-- 无搜索结果提示 -->
      <div v-if="!loading && filteredResources.length === 0 && searchKeyword" class="no-result">
        <div class="no-result-icon">🔍</div>
        <div class="no-result-text">未找到相关软件</div>
        <div class="no-result-subtext">请尝试更换关键词或清除搜索框</div>
      </div>

      <!-- 软件列表 -->
      <div v-if="!loading" class="software-grid">
        <div 
          v-for="(resource, index) in paginatedResources" 
          :key="resource.id" 
          :id="`resource-${resource.id}`"
          class="software-card"
        >
          <ChristmasHat v-if="isChristmasTheme" />
          <span class="card-number">{{ formatCardNumber((currentPage - 1) * pageSize + index + 1) }}</span>
          <div class="software-name">{{ getIcon(resource.categoryName) }} {{ resource.title }}</div>
          <div class="software-desc">{{ resource.description || '暂无描述' }}</div>
          
          <!-- 图片展示 -->
          <div v-if="resource.coverImageUrl" class="software-image-container">
            <img 
              :src="resource.coverImageUrl" 
              :alt="resource.title" 
              class="software-image"
              @click="openImageModal(resource.coverImageUrl, resource.title)"
            />
          </div>
          
          <div class="download-section">
            <div class="download-title">选择下载方式：</div>
            <div class="download-area">
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
                  {{ getLinkTypeName(link.linkType) }}
                </router-link>
              </div>
              <div class="download-info">
                推荐使用迅雷、百度网盘、夸克网盘资源较大
              </div>
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
          « 上一页
        </button>
        
        <button 
          v-for="page in totalPages" 
          :key="page"
          :class="['pagination-btn', 'pagination-number', { active: currentPage === page }]"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
        
        <button 
          class="pagination-btn"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          下一页 »
        </button>
      </div>
    </div>
    
    <!-- 图片查看器模态框 -->
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

    <!-- 友链按钮 -->
    <FriendLinkButton />

    <!-- 右下角操作按钮（反馈+帮助） -->
    <ActionButtons />

    <!-- 免责声明弹窗 -->
    <DisclaimerModal />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getResourceList, recordDownload, recordVisit, getConfig, getLinkTypes } from '../api/resource'
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
const loading = ref(false)
const showCategoryMenu = ref(false)
const showImageModal = ref(false)
const modalImageSrc = ref('')
const modalImageAlt = ref('')
const currentPage = ref(1)
const pageSize = 9
const advertisements = ref([])
const isChristmasTheme = computed(() => {
  return document.documentElement.getAttribute('data-theme') === 'christmas'
})

const filteredResources = computed(() => {
  let filtered = resources.value
  
  // 分类筛选
  if (selectedCategory.value !== null) {
    filtered = filtered.filter(resource => resource.categoryId === selectedCategory.value)
  }
  
  // 搜索筛选
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

const toggleCategoryMenu = () => {
  showCategoryMenu.value = !showCategoryMenu.value
}

const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  showCategoryMenu.value = false
  currentPage.value = 1
}

const handleSearch = () => {
  loading.value = true
  currentPage.value = 1
  setTimeout(() => {
    loading.value = false
  }, 300)
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
}

const handleDownload = async (resourceId, linkType) => {
  try {
    await recordDownload(resourceId)
    console.log(`下载资源 ${resourceId}, 类型: ${linkType}`)
  } catch (error) {
    console.error('记录下载失败', error)
  }
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

const getIcon = (categoryName) => {
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
    console.error('加载资源失败', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    // 从资源中提取唯一分类
    const uniqueCategories = [...new Map(
      resources.value.map(r => [r.categoryId, { id: r.categoryId, name: r.categoryName }])
    ).values()]
    categories.value = uniqueCategories
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadConfig = async () => {
  try {
    const res = await getConfig()
    config.value = res.data?.configs || {}
  } catch (error) {
    console.error('加载配置失败，使用默认配置', error)
    // 使用默认配置，不影响页面显示
    config.value = {
      'site.title': 'shaun・个人备份库',
      'site.announcement': '有什么需求游戏、软件等，右下角反馈给我，点图片可以放大查看',
      'site.copyright': '© 2025 个人备份库 | 仅供个人备份学习，请勿用于商业用途',
      'site.buildTime': '2025年01月01日'
    }
  }
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

const loadAdvertisements = async () => {
  try {
    const res = await getActiveAdvertisements('homepage')
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
  await loadConfig()
  await loadResources()
  await loadCategories()
  await loadLinkTypes()
  await loadAdvertisements()
})
</script>

<style scoped>
/* 基础样式重置 */
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
  background: #3498db;
  border-radius: 4px;
  transition: background 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: #2980b9;
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 现代化头部样式 */
.modern-header {
  position: relative;
  padding: 60px 0 40px;
  margin: 0 0 30px;
  text-align: center;
  overflow: hidden;
}

.header-content {
  position: relative;
  z-index: 2;
}

.site-brand {
  margin-bottom: 20px;
}

.site-title {
  margin: 0 0 16px;
  font-size: clamp(1.8rem, 5vw, 2.8rem);
  font-weight: 700;
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
  bottom: -4px;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #06b6d4);
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.6s ease;
}

.title-link:hover .title-accent {
  transform: scaleX(1);
}

/* 装饰性元素 */
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
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  top: 20%;
  right: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  top: 60%;
  left: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  top: 30%;
  left: 70%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 公告横幅 */
.announcement-banner {
  margin-top: 24px;
  background: rgba(248, 250, 252, 0.6);
  border: 1px solid rgba(226, 232, 240, 0.5);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(8px);
  opacity: 0.9;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.announcement-icon {
  width: 32px;
  height: 32px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  flex-shrink: 0;
}

.announcement-text {
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.5;
  font-weight: 400;
}

/* 搜索和分类容器 */
.search-category-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

/* 分类导航 */
.category-container {
  position: relative;
}

.category-toggle {
  display: none;
  width: 100%;
  padding: 12px 20px;
  background: white;
  border: 1px solid #e0e6ed;
  border-radius: 8px;
  font-size: 1rem;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-toggle:hover {
  border-color: #3498db;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.category-btn {
  padding: 10px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 0.95rem;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-btn:hover {
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-2px);
}

.category-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

/* 首页广告位 */
.home-advertisement-section {
  margin: 30px 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.home-advertisement-item {
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
  background: var(--card-bg);
}

.home-advertisement-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.home-ad-image {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  min-height: 150px;
  max-height: 200px;
}

.home-ad-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  gap: 12px;
  min-height: 150px;
}

.home-ad-placeholder i {
  font-size: 2.5rem;
  opacity: 0.9;
}

.home-ad-placeholder span {
  font-size: 1.2rem;
  font-weight: 600;
  text-align: center;
}

/* 搜索框样式 */
.search-wrapper {
  display: flex;
  justify-content: center;
}

.search-container {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  font-size: 1.1em;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 14px 22px 14px 45px;
  font-size: 1.05em;
  border: 2px solid var(--input-border);
  border-radius: 30px;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.05);
  background: var(--input-bg);
  color: var(--text-primary);
}

.search-input:focus {
  border-color: var(--input-focus-border);
  box-shadow: inset 0 2px 5px rgba(0, 0, 0, 0.05), 0 0 8px rgba(52, 152, 219, 0.2);
}

.search-input::placeholder {
  color: var(--text-tertiary);
  font-weight: 400;
}

/* 加载动画 */
.loading {
  text-align: center;
  padding: 50px 0;
  font-size: 1.2em;
  color: #3498db;
}

.loading span {
  display: inline-block;
  animation: dotPulse 1.4s infinite ease-in-out both;
}

.loading span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotPulse {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 无搜索结果提示 */
.no-result {
  text-align: center;
  padding: 60px 20px;
  background: var(--card-bg);
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  margin-bottom: 40px;
}

.no-result-icon {
  font-size: 3em;
  color: var(--border-color);
  margin-bottom: 15px;
}

.no-result-text {
  font-size: 1.1em;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.no-result-subtext {
  font-size: 0.9em;
  color: var(--text-tertiary);
}

/* 软件列表 */
.software-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 20px 0;
}

.software-card {
  background: var(--card-bg);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-md);
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s;
  position: relative;
  display: flex;
  flex-direction: column;
}

.software-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-number {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(52, 152, 219, 0.1);
  color: #3498db;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 600;
}

.software-name {
  font-size: 1.3em;
  color: var(--text-primary);
  margin-bottom: 12px;
  font-weight: 600;
  padding-right: 60px;
}

.software-desc {
  color: var(--text-secondary);
  font-size: 0.95em;
  margin-bottom: 15px;
  line-height: 1.6;
  text-align: justify;
}

/* 图片展示 */
.software-image-container {
  width: 100%;
  height: 200px;
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.software-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.software-image:hover {
  transform: scale(1.08);
}

.download-section {
  border-top: 1px solid #f0f2f5;
  padding-top: 15px;
  margin-top: auto;
}

.download-title {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 12px;
  font-weight: 600;
  text-align: center;
}

.download-area {
  text-align: center;
}

.download-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.download-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 12px 8px;
  font-size: 0.8em;
  font-weight: 600;
  text-decoration: none;
  border-radius: 20px;
  transition: all 0.3s ease;
  text-align: center;
  min-height: 60px;
}

.download-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.25);
  color: white;
  text-decoration: none;
}

.btn-quark {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.btn-quark:hover {
  box-shadow: 0 6px 18px rgba(245, 87, 108, 0.4);
}

.btn-xunlei {
  background: linear-gradient(135deg, #ffb347 0%, #ff8c00 100%);
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.3);
}

.btn-xunlei:hover {
  box-shadow: 0 6px 18px rgba(255, 140, 0, 0.4);
}

.btn-baidu {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
}

.btn-baidu:hover {
  box-shadow: 0 6px 18px rgba(79, 172, 254, 0.4);
}

.btn-aliyun {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3);
}

.btn-aliyun:hover {
  box-shadow: 0 6px 18px rgba(67, 233, 123, 0.4);
}

.btn-lanzou {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-lanzou:hover {
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.4);
}

.btn-mobile {
  background: linear-gradient(135deg, #00c9ff 0%, #92fe9d 100%);
  box-shadow: 0 4px 12px rgba(0, 201, 255, 0.3);
}

.btn-mobile:hover {
  box-shadow: 0 6px 18px rgba(0, 201, 255, 0.4);
}

.btn-uc {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.btn-uc:hover {
  box-shadow: 0 6px 18px rgba(240, 147, 251, 0.4);
}

.btn-direct {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-direct:hover {
  box-shadow: 0 6px 18px rgba(102, 126, 234, 0.4);
}

.download-info {
  font-size: 0.8em;
  color: #999;
  margin-top: 10px;
  text-align: center;
  line-height: 1.5;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin: 40px 0;
  flex-wrap: wrap;
}

.pagination-btn {
  padding: 10px 16px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 0.9em;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 44px;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #3498db;
  color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.2);
}

.pagination-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-number {
  min-width: 40px;
  padding: 10px 12px;
}

/* 图片模态框 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
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
  z-index: 10000;
}

.modal-close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
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
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .modern-header {
    padding: 30px 0 20px;
  }

  .site-title {
    font-size: clamp(1.6rem, 7vw, 2.2rem);
    margin-bottom: 12px;
  }

  .decoration-circle {
    display: none;
  }

  .announcement-banner {
    margin-top: 16px;
    padding: 12px 16px;
    border-radius: 10px;
  }

  .announcement-content {
    flex-direction: row;
    text-align: left;
    gap: 10px;
  }

  .announcement-icon {
    width: 28px;
    height: 28px;
  }

  .announcement-text {
    font-size: 0.9rem;
    line-height: 1.4;
  }

  .category-toggle {
    display: block;
  }

  .category-nav {
    display: none;
    flex-direction: column;
    margin-top: 10px;
  }

  .category-nav.show {
    display: flex;
  }

  .software-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .software-card {
    padding: 15px;
  }

  .card-number {
    font-size: 0.75em;
    padding: 3px 8px;
  }

  .software-name {
    font-size: 1.1em;
    padding-right: 50px;
  }

  .software-image-container {
    height: 180px;
  }

  .download-buttons {
    grid-template-columns: 1fr;
  }

  .download-btn {
    min-height: 50px;
  }

  .search-input {
    padding: 12px 20px 12px 42px;
    font-size: 1em;
  }

  .pagination {
    gap: 6px;
    margin: 30px 0;
  }

  .pagination-btn {
    padding: 8px 12px;
    font-size: 0.85em;
    min-width: 38px;
  }

  .modal-close-btn {
    top: 10px;
    right: 10px;
    width: 35px;
    height: 35px;
  }

  .home-advertisement-section {
    grid-template-columns: 1fr;
    margin: 20px 0;
    gap: 15px;
  }

  .home-ad-image {
    max-height: 150px;
    min-height: 120px;
  }

  .home-ad-placeholder {
    padding: 40px 20px;
    min-height: 120px;
  }

  .home-ad-placeholder i {
    font-size: 2rem;
  }

  .home-ad-placeholder span {
    font-size: 1rem;
  }
}

@media (max-width: 480px) {
  .modern-header {
    padding: 25px 0 15px;
  }

  .site-title {
    font-size: clamp(1.4rem, 6vw, 1.8rem);
    margin-bottom: 10px;
  }

  .announcement-banner {
    margin-top: 12px;
    padding: 10px 12px;
    border-radius: 8px;
  }

  .announcement-content {
    gap: 8px;
  }

  .announcement-icon {
    width: 24px;
    height: 24px;
  }

  .announcement-text {
    font-size: 0.85rem;
  }

  .software-grid {
    grid-template-columns: 1fr;
  }

  .software-card {
    padding: 12px;
  }

  .software-image-container {
    height: 160px;
  }

  .download-btn {
    padding: 10px 8px;
    font-size: 0.75em;
    min-height: 50px;
  }

  .pagination-btn {
    padding: 6px 10px;
    font-size: 0.8em;
    min-width: 36px;
  }

  .home-ad-image {
    max-height: 120px;
    min-height: 100px;
  }

  .home-ad-placeholder {
    padding: 30px 15px;
    min-height: 100px;
  }

  .home-ad-placeholder i {
    font-size: 1.8rem;
  }

  .home-ad-placeholder span {
    font-size: 0.9rem;
  }
}
</style>
