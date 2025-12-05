<template>
  <el-container class="layout-container">
    <el-aside width="220px">
      <div class="logo">
        <h2>📦 资源管理</h2>
        <p class="logo-subtitle">Resource Platform</p>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="$route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          :default-openeds="defaultOpeneds"
        >
          <!-- 控制面板 -->
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>控制面板</span>
          </el-menu-item>

          <!-- 统计管理 -->
          <el-menu-item index="/statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>统计管理</span>
          </el-menu-item>

          <!-- 收益概况 -->
          <el-menu-item index="/revenue">
            <el-icon><Money /></el-icon>
            <span>收益概况</span>
          </el-menu-item>

          <!-- 内容管理 -->
          <el-sub-menu index="content-group">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/resources">
              <el-icon><Document /></el-icon>
              <span>资源管理</span>
            </el-menu-item>
            <el-menu-item index="/categories">
              <el-icon><Folder /></el-icon>
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
              <el-icon><ChatDotRound /></el-icon>
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

          <!-- 系统与配置 -->
          <el-sub-menu index="system-group">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统与配置</span>
            </template>
            <el-menu-item index="/config">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
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
        
        <!-- 底部退出登录 -->
        <div class="menu-footer">
          <el-button 
            class="logout-btn" 
            @click="handleLogout"
            text
          >
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ getBreadcrumb() }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Document, Folder, Setting, User, ArrowDown, SwitchButton, ChatDotRound, Tickets, Picture, Search, TrendCharts, Connection, Money, Promotion } from '@element-plus/icons-vue'
import { getCurrentUser, logout } from '../api/user'

const router = useRouter()
const route = useRoute()
const username = ref('')

// 默认展开的菜单组
const defaultOpeneds = ref(['content-group', 'operation-group', 'system-group'])

const breadcrumbMap = {
  '/dashboard': '控制面板',
  '/resources': '资源管理',
  '/categories': '分类管理',
  '/config': '系统设置',
  '/feedback': '用户反馈',
  '/logs': '日志管理',
  '/files': '图片管理',
  '/seo': 'SEO管理',
  '/statistics': '统计管理',
  '/revenue': '收益概况',
  '/promotion': '推广管理',
  '/friendlink': '友联管理'
}

const getBreadcrumb = () => {
  return breadcrumbMap[route.path] || '控制面板'
}

const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    username.value = res.data.username
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await handleLogout()
  }
}

const handleLogout = async () => {
  try {
    await logout()
    localStorage.removeItem('token')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    console.error('退出失败', error)
    // 即使退出接口失败，也清除本地token并跳转到登录页
    localStorage.removeItem('token')
    router.push('/login')
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background: #304156;
  color: #fff;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  background: #263445;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
  color: #fff;
}

.logo-subtitle {
  margin: 5px 0 0 0;
  font-size: 0.75rem;
  color: #bfcbd9;
  opacity: 0.8;
}

.menu-scrollbar {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.menu-scrollbar .el-scrollbar__wrap) {
  overflow-x: hidden;
}

:deep(.menu-scrollbar .el-scrollbar__view) {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.el-menu {
  border: none;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.2) !important;
}

:deep(.el-menu-item.is-active) {
  background-color: rgba(64, 158, 255, 0.2) !important;
}

:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(0, 0, 0, 0.2) !important;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 50px !important;
  background-color: rgba(0, 0, 0, 0.1);
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: rgba(0, 0, 0, 0.3) !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background-color: rgba(64, 158, 255, 0.3) !important;
}

.menu-footer {
  padding: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: #263445;
}

.logout-btn {
  width: 100%;
  color: #bfcbd9;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background-color: rgba(0, 0, 0, 0.2);
  color: #409eff;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.el-main {
  background: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}
</style>
