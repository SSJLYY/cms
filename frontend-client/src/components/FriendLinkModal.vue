<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click="handleClose"></div>
    </Transition>

    <Transition name="slide">
      <div v-if="visible" class="modal-container">
        <!-- 头部 -->
        <div class="modal-header">
          <div class="modal-title-wrap">
            <div class="header-icon">🔗</div>
            <div>
              <div class="modal-title">关联链接</div>
              <div class="modal-sub" v-if="friendLinks.length > 0">{{ friendLinks.length }} 个友情链接</div>
            </div>
          </div>
          <button class="close-btn" @click="handleClose">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        <!-- 主体 -->
        <div class="modal-body">
          <div v-if="loading" class="loading-state">
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
            <div class="loading-dot"></div>
          </div>
          <div v-else-if="friendLinks.length === 0" class="empty-state">
            <div class="empty-icon">🌐</div>
            <div class="empty-text">暂无友情链接</div>
          </div>
          <div v-else class="link-list">
            <a
              v-for="link in friendLinks"
              :key="link.id"
              :href="link.url"
              target="_blank"
              rel="noopener noreferrer"
              class="link-item"
            >
              <div class="link-avatar">
                <img
                  v-if="link.logo"
                  :src="link.logo"
                  :alt="link.name"
                  class="link-logo"
                  @error="(e) => e.target.style.display='none'"
                />
                <span class="link-letter">{{ link.name?.charAt(0) }}</span>
              </div>
              <div class="link-info">
                <div class="link-name">{{ link.name }}</div>
                <div class="link-url">{{ link.url }}</div>
              </div>
              <div class="link-arrow">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </div>
            </a>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnabledFriendLinks } from '../api/friendlink'

const props = defineProps({
  visible: { type: Boolean, default: false }
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
    const response = await getEnabledFriendLinks()
    if (response.code === 200) {
      friendLinks.value = response.data || []
    }
  } catch (error) {
    ElMessage.error('加载友情链接失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) loadFriendLinks()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  z-index: 999;
  backdrop-filter: blur(2px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 弹窗容器 */
.modal-container {
  position: fixed;
  left: 20px;
  bottom: 160px;
  width: 300px;
  max-height: 480px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18), 0 0 0 1px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slide-enter-from {
  transform: translateX(-110%) scale(0.9);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-110%) scale(0.9);
  opacity: 0;
}

/* 头部 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.modal-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  filter: brightness(1.2);
}

.modal-title {
  font-size: 15px;
  font-weight: 700;
  color: white;
}

.modal-sub {
  font-size: 11px;
  color: rgba(255,255,255,0.75);
  margin-top: 1px;
}

.close-btn {
  background: rgba(255,255,255,0.2);
  border: none;
  cursor: pointer;
  color: white;
  padding: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255,255,255,0.35);
}

/* 主体 */
.modal-body {
  overflow-y: auto;
  flex: 1;
  max-height: 420px;
}

.modal-body::-webkit-scrollbar { width: 5px; }
.modal-body::-webkit-scrollbar-track { background: transparent; }
.modal-body::-webkit-scrollbar-thumb { background: #ddd; border-radius: 3px; }
.modal-body::-webkit-scrollbar-thumb:hover { background: #bbb; }

/* 加载状态 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 40px;
}

.loading-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #667eea;
  animation: bounce 1.4s infinite ease-in-out;
}

.loading-dot:nth-child(2) { animation-delay: 0.16s; }
.loading-dot:nth-child(3) { animation-delay: 0.32s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon { font-size: 36px; margin-bottom: 10px; }
.empty-text { font-size: 14px; color: #c0c4cc; }

/* 链接列表 */
.link-list {
  padding: 8px 0;
}

.link-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  text-decoration: none;
  color: #333;
  transition: all 0.2s;
  border-radius: 0;
  position: relative;
}

.link-item::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 0 2px 2px 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.link-item:hover {
  background: rgba(102,126,234,0.05);
}

.link-item:hover::before {
  opacity: 1;
}

.link-avatar {
  position: relative;
  width: 36px; height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.link-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: absolute;
  inset: 0;
  border-radius: 10px;
}

.link-letter {
  font-size: 16px;
  font-weight: 700;
  color: white;
}

.link-info {
  flex: 1;
  min-width: 0;
}

.link-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-url {
  font-size: 11px;
  color: #c0c4cc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-top: 2px;
}

.link-arrow {
  color: #c0c4cc;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.link-item:hover .link-arrow {
  color: #667eea;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .modal-container {
    left: 10px;
    bottom: 140px;
    width: 260px;
    max-height: 400px;
  }
}
</style>
