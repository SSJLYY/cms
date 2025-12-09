<!--
  主题切换器组件
  提供用户主题切换功能，采用中国风流苏设计
  支持浅色/深色主题切换，带有切换提示动画
-->
<template>
  <div class="theme-switcher">
    <!-- 中国式流苏切换按钮 -->
    <div class="chinese-tassel" @click="toggleTheme">
      <div class="tassel-knot">
        <div class="knot-pattern"></div>
      </div>
      <div class="tassel-cord"></div>
      <div class="tassel-fringe">
        <div class="fringe-strand" v-for="i in 8" :key="i"></div>
      </div>
    </div>

    <!-- 主题切换提示闪现 -->
    <transition name="flash">
      <div v-if="showFlash" class="theme-flash">
        <div class="flash-content">
          <i :class="['flash-icon', currentTheme === 'dark' ? 'fas fa-moon' : 'fas fa-sun']"></i>
          <span class="flash-text">{{ currentTheme === 'dark' ? '你已切换为深色模式' : '你已切换为浅色模式' }}</span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 响应式数据
const currentTheme = ref('light')  // 当前主题
const showFlash = ref(false)       // 是否显示切换提示

/**
 * 从localStorage读取用户主题偏好
 * 在组件初始化时恢复用户之前选择的主题
 */
const loadTheme = () => {
  const savedTheme = localStorage.getItem('userTheme')
  if (savedTheme) {
    currentTheme.value = savedTheme
    applyTheme(savedTheme)
  }
}

/**
 * 应用主题到DOM
 * @param {string} theme - 主题名称（light/dark）
 */
const applyTheme = (theme) => {
  document.documentElement.setAttribute('data-theme', theme)
}

/**
 * 切换主题
 * 在浅色和深色主题之间切换，并显示切换提示
 */
const toggleTheme = () => {
  const newTheme = currentTheme.value === 'light' ? 'dark' : 'light'
  currentTheme.value = newTheme
  localStorage.setItem('userTheme', newTheme)
  applyTheme(newTheme)
  
  // 显示切换提示，2秒后自动隐藏
  showFlash.value = true
  setTimeout(() => {
    showFlash.value = false
  }, 2000)
}

// 组件挂载时加载主题
onMounted(() => {
  loadTheme()
})
</script>

<style scoped>
.theme-switcher {
  position: fixed;
  top: 20px;
  right: 30px;
  z-index: 1000;
}

/* 中国式流苏样式 */
.chinese-tassel {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: transform 0.3s ease;
  filter: drop-shadow(0 2px 8px rgba(220, 20, 60, 0.3));
}

.chinese-tassel:hover {
  transform: translateY(5px);
  filter: drop-shadow(0 4px 12px rgba(220, 20, 60, 0.5));
}

.chinese-tassel:active {
  transform: translateY(12px);
}

/* 中国结顶部 */
.tassel-knot {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #dc143c 0%, #ff6347 50%, #dc143c 100%);
  border-radius: 8px;
  transform: rotate(45deg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(220, 20, 60, 0.4);
  border: 2px solid #ffd700;
  position: relative;
  animation: knotGlow 2s ease-in-out infinite;
}

.knot-pattern {
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, #ffd700 30%, transparent 30%);
  background-size: 8px 8px;
  border-radius: 2px;
}

@keyframes knotGlow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(220, 20, 60, 0.4);
  }
  50% {
    box-shadow: 0 2px 12px rgba(255, 215, 0, 0.6);
  }
}

/* 流苏绳 */
.tassel-cord {
  width: 3px;
  height: 25px;
  background: linear-gradient(to bottom, #dc143c, #b22222);
  border-radius: 2px;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 流苏穗 */
.tassel-fringe {
  display: flex;
  gap: 2px;
  padding-top: 2px;
}

.fringe-strand {
  width: 2px;
  height: 20px;
  background: linear-gradient(to bottom, #dc143c 0%, #ff6347 50%, #ffd700 100%);
  border-radius: 0 0 2px 2px;
  animation: sway 2s ease-in-out infinite;
}

.fringe-strand:nth-child(1) { animation-delay: 0s; }
.fringe-strand:nth-child(2) { animation-delay: 0.1s; }
.fringe-strand:nth-child(3) { animation-delay: 0.2s; }
.fringe-strand:nth-child(4) { animation-delay: 0.3s; }
.fringe-strand:nth-child(5) { animation-delay: 0.4s; }
.fringe-strand:nth-child(6) { animation-delay: 0.3s; }
.fringe-strand:nth-child(7) { animation-delay: 0.2s; }
.fringe-strand:nth-child(8) { animation-delay: 0.1s; }

@keyframes sway {
  0%, 100% {
    transform: translateX(0) rotate(0deg);
  }
  25% {
    transform: translateX(1px) rotate(2deg);
  }
  75% {
    transform: translateX(-1px) rotate(-2deg);
  }
}

/* 主题切换提示闪现 */
.theme-flash {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px 0;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.flash-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: white;
}

.flash-icon {
  font-size: 24px;
  animation: pulse 0.6s ease-in-out;
}

.flash-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

/* 过渡动画 */
.flash-enter-active {
  animation: slideDown 0.4s ease-out;
}

.flash-leave-active {
  animation: slideUp 0.4s ease-in;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(0);
    opacity: 1;
  }
  to {
    transform: translateY(-100%);
    opacity: 0;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .theme-switcher {
    top: 15px;
    right: 20px;
  }

  .tassel-knot {
    width: 32px;
    height: 32px;
  }

  .knot-pattern {
    width: 16px;
    height: 16px;
    background-size: 6px 6px;
  }

  .tassel-cord {
    height: 20px;
  }

  .fringe-strand {
    height: 16px;
  }

  .flash-text {
    font-size: 14px;
  }

  .flash-icon {
    font-size: 20px;
  }
}
</style>
