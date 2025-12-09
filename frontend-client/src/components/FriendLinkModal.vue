<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click="handleClose"></div>
    </Transition>
    
    <Transition name="slide">
      <div v-if="visible" class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">关联链接</h3>
          <button class="close-btn" @click="handleClose">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="friendLinks.length === 0" class="empty">暂无友情链接</div>
          <div v-else class="link-list">
            <a 
              v-for="link in friendLinks" 
              :key="link.id"
              :href="link.url"
              target="_blank"
              rel="noopener noreferrer"
              class="link-item"
            >
              <div class="link-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M13.5 2c-5.629 0-10.212 4.436-10.475 10h-3.025l4.537 5.917 4.463-5.917h-2.975c.26-3.902 3.508-7 7.475-7 4.136 0 7.5 3.364 7.5 7.5s-3.364 7.5-7.5 7.5c-2.381 0-4.502-1.119-5.876-2.854l-1.847 2.449c1.919 2.088 4.664 3.405 7.723 3.405 5.798 0 10.5-4.702 10.5-10.5s-4.702-10.5-10.5-10.5z"/>
                </svg>
              </div>
              <span class="link-name">{{ link.name }}</span>
            </a>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getEnabledFriendLinks } from '../api/friendlink'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

const loading = ref(false)
const friendLinks = ref([])

const handleClose = () => {
  emit('update:visible', false)
}

const loadFriendLinks = async () => {
  loading.value = true
  try {
    console.log('开始加载友情链接...')
    const response = await getEnabledFriendLinks()
    console.log('友情链接响应:', response)
    if (response.code === 200) {
      friendLinks.value = response.data || []
      console.log('友情链接数据:', friendLinks.value)
    } else {
      console.error('响应码不是200:', response)
    }
  } catch (error) {
    console.error('加载友情链接失败:', error)
    console.error('错误详情:', error.response)
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadFriendLinks()
  }
})
</script>

<style scoped>
/* 遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 999;
}

/* 遮罩层过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 弹窗容器 - 左侧悬浮 */
.modal-container {
  position: fixed;
  left: 20px;
  bottom: 150px;
  width: 280px;
  max-height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
}

/* 弹窗滑入动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* 弹窗头部 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background-color: #e5e5e5;
  color: #333;
}

/* 弹窗主体 */
.modal-body {
  padding: 12px 0;
  overflow-y: auto;
  flex: 1;
  max-height: 440px;
}

.modal-body::-webkit-scrollbar {
  width: 6px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #b0b0b0;
}

.loading,
.empty {
  text-align: center;
  padding: 30px 20px;
  color: #999;
  font-size: 14px;
}

/* 链接列表 */
.link-list {
  display: flex;
  flex-direction: column;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  text-decoration: none;
  color: #333;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.link-item:hover {
  background-color: #f5f7fa;
  border-left-color: #3b82f6;
}

.link-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 6px;
  flex-shrink: 0;
  color: white;
}

.link-icon svg {
  width: 16px;
  height: 16px;
}

.link-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-container {
    left: 10px;
    bottom: 130px;
    width: 240px;
    max-height: 400px;
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .modal-title {
    font-size: 15px;
  }
  
  .modal-body {
    max-height: 340px;
  }
  
  .link-item {
    padding: 10px 16px;
  }
  
  .link-name {
    font-size: 13px;
  }
}
</style>
