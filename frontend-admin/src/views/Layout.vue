<template>
  <div class="layout-wrapper">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': isSidebarCollapsed }">
      <!-- Logo区域 -->
      <div class="sidebar-header">
        <div class="logo-area">
          <div class="logo-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="currentColor" opacity="0.9"/>
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <transition name="fade-slide">
            <div v-if="!isSidebarCollapsed" class="logo-text">
              <span class="logo-title">资源管理</span>
              <span class="logo-subtitle">Resource Platform</span>
            </div>
          </transition>
        </div>
        <button class="collapse-btn" @click="toggleSidebar">
          <el-icon :size="18">
            <component :is="isSidebarCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
        </button>
      </div>

      <!-- 菜单区域 -->
      <div class="sidebar-menu">
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="$route.path"
            router
            :collapse="isSidebarCollapsed"
            :collapse-transition="false"
            background-color="transparent"
            text-color="#a0aec0"
            active-text-color="#ffffff"
            :default-openeds="defaultOpeneds"
          >
            <!-- 控制面板 -->
            <el-menu-item index="/dashboard" class="menu-item-primary">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>控制面板</template>
            </el-menu-item>

            <!-- 统计管理 -->
            <el-menu-item index="/statistics">
              <el-icon><TrendCharts /></el-icon>
              <template #title>统计管理</template>
            </el-menu-item>

            <!-- 收益概况 -->
            <el-menu-item index="/revenue">
              <el-icon><Money /></el-icon>
              <template #title>收益概况</template>
            </el-menu-item>

            <!-- 内容管理 -->
            <el-sub-menu index="content-group">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>内容管理</span>
              </template>
              <el-menu-item index="/resources">
                <el-icon><Collection /></el-icon>
                <span>资源管理</span>
              </el-menu-item>
              <el-menu-item index="/categories">
                <el-icon><FolderOpened /></el-icon>
                <span>分类管理</span>
              </el-menu-item>
              <el-menu-item index="/files">
                <el-icon><Picture /></el-icon>
                <span>图片管理</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 交互与运营 -->
            <el-sub-menu index="operation-group">
              <template #title>
                <el-icon><ChatDotRound /></el-icon>
                <span>交互与运营</span>
              </template>
              <el-menu-item index="/feedback">
                <el-icon><ChatLineSquare /></el-icon>
                <span>用户反馈</span>
              </el-menu-item>
              <el-menu-item index="/promotion">
                <el-icon><Promotion /></el-icon>
                <span>推广管理</span>
              </el-menu-item>
              <el-menu-item index="/friendlink">
                <el-icon><Connection /></el-icon>
                <span>友联管理</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 插件管理 -->
            <el-sub-menu index="plugin-group">
              <template #title>
                <el-icon><Grid /></el-icon>
                <span>插件管理</span>
              </template>
              <el-menu-item index="/crawler">
                <el-icon><Compass /></el-icon>
                <span>爬虫管理</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 系统与配置 -->
            <el-sub-menu index="system-group">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统与配置</span>
              </template>
              <el-menu-item index="/config">
                <el-icon><Tools /></el-icon>
                <span>基本设置</span>
              </el-menu-item>
              <el-menu-item index="/theme-settings">
                <el-icon><Brush /></el-icon>
                <span>主题设置</span>
              </el-menu-item>
              <el-menu-item index="/seo">
                <el-icon><Search /></el-icon>
                <span>SEO管理</span>
              </el-menu-item>
              <el-menu-item index="/logs">
                <el-icon><Tickets /></el-icon>
                <span>日志管理</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </div>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <div class="user-section" @click="handleUserAction">
          <el-avatar :size="36" class="user-avatar">
            {{ username ? username.charAt(0).toUpperCase() : 'A' }}
          </el-avatar>
          <transition name="fade-slide">
            <div v-if="!isSidebarCollapsed" class="user-info">
              <span class="user-name">{{ username }}</span>
              <span class="user-role">管理员</span>
            </div>
          </transition>
          <transition name="fade-slide">
            <el-icon v-if="!isSidebarCollapsed" class="logout-icon"><SwitchButton /></el-icon>
          </transition>
        </div>
      </div>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-wrapper" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
      <!-- 顶部导航 -->
      <header class="top-header">
        <div class="header-left">
          <!-- 面包屑导航 -->
          <div class="breadcrumb">
            <el-icon class="breadcrumb-icon"><HomeFilled /></el-icon>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-current">{{ getBreadcrumb() }}</span>
          </div>
        </div>
        
        <div class="header-right">
          <!-- 快捷操作 -->
          <div class="quick-actions">
            <el-tooltip content="刷新数据" placement="bottom">
              <button class="action-btn" @click="refreshData">
                <el-icon :class="{ 'is-loading': isRefreshing }"><Refresh /></el-icon>
              </button>
            </el-tooltip>
          </div>

          <!-- 用户菜单 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-menu">
              <el-avatar :size="32" class="header-avatar">
                {{ username ? username.charAt(0).toUpperCase() : 'A' }}
              </el-avatar>
              <span class="header-username">{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  账号设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis, Document, FolderOpened, Setting, User, ArrowDown, SwitchButton,
  ChatDotRound, Tickets, Picture, Search, TrendCharts, Connection, Money,
  Promotion, Brush, Grid, Compass, Collection, ChatLineSquare, Tools,
  HomeFilled, Refresh, Expand, Fold
} from '@element-plus/icons-vue'
import { getCurrentUser, logout } from '../api/user'

const router = useRouter()
const route = useRoute()
const username = ref('')
const isSidebarCollapsed = ref(false)
const isRefreshing = ref(false)

// 默认展开的菜单组
const defaultOpeneds = ref(['content-group', 'operation-group', 'plugin-group', 'system-group'])

const breadcrumbMap = {
  '/dashboard': '控制面板',
  '/resources': '资源管理',
  '/categories': '分类管理',
  '/config': '基本设置',
  '/theme-settings': '主题设置',
  '/feedback': '用户反馈',
  '/logs': '日志管理',
  '/files': '图片管理',
  '/seo': 'SEO管理',
  '/statistics': '统计管理',
  '/revenue': '收益概况',
  '/promotion': '推广管理',
  '/friendlink': '友联管理',
  '/crawler': '爬虫管理'
}

const getBreadcrumb = () => {
  return breadcrumbMap[route.path] || '控制面板'
}

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
  localStorage.setItem('sidebarCollapsed', isSidebarCollapsed.value)
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    username.value = res.data.username
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await logout()
      localStorage.removeItem('token')
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        localStorage.removeItem('token')
        router.push('/login')
      }
    }
  }
}

const handleUserAction = async () => {
  await handleCommand('logout')
}

const refreshData = () => {
  isRefreshing.value = true
  // 使用router.go(0)进行SPA内刷新，比window.location.reload()更轻量
  router.go(0)
}

onMounted(() => {
  loadUserInfo()
  // 恢复侧边栏状态
  const savedState = localStorage.getItem('sidebarCollapsed')
  if (savedState) {
    isSidebarCollapsed.value = savedState === 'true'
  }
})
</script>

<style scoped>
/* 布局容器 */
.layout-wrapper {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  transition: width 0.3s ease;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
}

.sidebar.collapsed {
  width: 72px;
}

/* Logo区域 */
.sidebar-header {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.logo-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.5px;
}

.logo-subtitle {
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 8px;
  color: #a0aec0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* 菜单区域 */
.sidebar-menu {
  flex: 1;
  overflow: hidden;
  padding: 12px 0;
}

.menu-scrollbar {
  height: 100%;
}

::deep(.el-scrollbar__view) {
  height: 100%;
}

::deep(.el-menu) {
  border: none;
  background: transparent !important;
}

::deep(.el-menu-item),
::deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  padding: 0 16px !important;
  border-radius: 10px;
  color: #a0aec0;
  transition: all 0.3s ease;
}

::deep(.el-menu-item:hover),
::deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff;
}

::deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

::deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 4px 4px 0;
}

::deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
  margin: 2px 12px;
  min-height: 42px;
  height: 42px;
  line-height: 42px;
  background: rgba(0, 0, 0, 0.1);
}

::deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(102, 126, 234, 0.2) !important;
}

::deep(.el-sub-menu__title) {
  margin: 4px 12px;
}

::deep(.el-sub-menu__icon-arrow) {
  color: #a0aec0;
}

::deep(.el-menu--collapse) {
  width: 72px;
}

/* 用户区域 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.03);
}

.user-section:hover {
  background: rgba(255, 255, 255, 0.08);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  white-space: nowrap;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
}

.user-role {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
}

.logout-icon {
  color: #e53e3e;
  font-size: 1.1rem;
}

/* 主内容区域 */
.main-wrapper {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.3s ease;
}

.main-wrapper.sidebar-collapsed {
  margin-left: 72px;
}

/* 顶部导航 */
.top-header {
  height: 64px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 0.95rem;
}

.breadcrumb-icon {
  color: #999;
}

.breadcrumb-separator {
  color: #ccc;
}

.breadcrumb-current {
  color: #333;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  background: #f7f8fa;
  border: none;
  border-radius: 8px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #e8e9eb;
  color: #333;
}

.action-btn .is-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-menu:hover {
  background: #f7f8fa;
}

.header-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 600;
}

.header-username {
  font-size: 0.9rem;
  font-weight: 500;
  color: #333;
}

/* 页面内容 */
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
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

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.2s ease;
}

.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar {
    width: 72px;
  }

  .main-wrapper {
    margin-left: 72px;
  }

  .logo-text,
  .user-info,
  .logout-icon {
    display: none;
  }

  ::deep(.el-menu-item span),
  ::deep(.el-sub-menu__title span) {
    display: none;
  }
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.collapsed {
    transform: translateX(-100%);
  }

  .main-wrapper,
  .main-wrapper.sidebar-collapsed {
    margin-left: 0;
  }

  .breadcrumb {
    display: none;
  }

  .header-username {
    display: none;
  }
}
</style>
