<!--
  客户端根组件
  提供应用的基础布局、主题管理和特效组件
-->
<template>
  <div id="app">
    <!-- 主题切换器（仅在非管理员强制主题时显示） -->
    <ThemeSwitcher v-if="!isAdminTheme" />
    
    <!-- 节日特效组件 -->
    <ParticleEffect v-if="currentTheme === 'christmas'" type="christmas" />
    <ParticleEffect v-if="currentTheme === 'spring-festival'" type="spring-festival" />
    <FirecrackerAnimation v-if="currentTheme === 'spring-festival'" />
    
    <!-- 路由视图容器 -->
    <router-view />
    
    <!-- 页脚组件 -->
    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Footer from './components/Footer.vue'
import ThemeSwitcher from './components/ThemeSwitcher.vue'
import ParticleEffect from './components/ParticleEffect.vue'
import FirecrackerAnimation from './components/FirecrackerAnimation.vue'
import { getConfig } from './api/resource'

// 响应式数据
const currentTheme = ref('light')  // 当前主题
const isAdminTheme = ref(false)    // 是否为管理员强制主题

/**
 * 加载管理员设置的主题配置
 * 管理员主题优先级高于用户选择的主题
 */
const loadAdminTheme = async () => {
  try {
    const res = await getConfig()
    const adminTheme = res.data?.configs?.['site.theme']
    
    if (adminTheme && adminTheme !== 'user-choice') {
      // 管理员设置了强制主题
      currentTheme.value = adminTheme
      isAdminTheme.value = true
      document.documentElement.setAttribute('data-theme', adminTheme)
      localStorage.setItem('adminTheme', adminTheme)
    } else {
      // 使用用户选择的主题
      isAdminTheme.value = false
      const userTheme = localStorage.getItem('userTheme') || 'light'
      currentTheme.value = userTheme
      document.documentElement.setAttribute('data-theme', userTheme)
    }
  } catch (error) {
    console.error('加载主题配置失败', error)
  }
}

/**
 * 监听DOM主题属性变化
 * 当主题切换时同步更新组件状态
 */
const observeThemeChange = () => {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === 'data-theme') {
        const theme = document.documentElement.getAttribute('data-theme')
        currentTheme.value = theme
      }
    })
  })
  
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
}

// 组件挂载时初始化
onMounted(() => {
  loadAdminTheme()
  observeThemeChange()
})
</script>

<style>
/* 导入主题样式 */
@import './styles/theme.css';

/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 全局字体和主题样式 */
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* 应用容器样式 */
#app {
  min-height: 100vh;
}
</style>
