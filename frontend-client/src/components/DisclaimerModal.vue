<template>
  <transition name="modal-fade">
    <div v-if="showModal" class="disclaimer-overlay" @click="handleOverlayClick">
      <div class="disclaimer-modal" @click.stop>
        <button class="close-btn" @click="closeModal">×</button>
        
        <div class="modal-header">
          <div class="icon-wrapper">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <h2>免责声明</h2>
          <p class="subtitle">请仔细阅读以下条款</p>
        </div>

        <div class="modal-body">
          <div class="disclaimer-content">
            <p class="disclaimer-item">
              <span class="bullet">•</span>
              <span>个人学习用，禁商用盈利违法。</span>
            </p>
            <p class="disclaimer-item">
              <span class="bullet">•</span>
              <span>网源/原主，本站仅备份。</span>
            </p>
            <p class="disclaimer-item">
              <span class="bullet">•</span>
              <span>纠纷责任自担，本站无责。</span>
            </p>
          </div>

          <div class="agreement-section">
            <label class="checkbox-wrapper">
              <input 
                type="checkbox" 
                v-model="agreed" 
                class="checkbox-input"
              />
              <span class="checkbox-custom"></span>
              <span class="checkbox-label">
                点击「我已阅读并同意」，即表示您已了解并同意以上条款。
              </span>
            </label>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-secondary" @click="viewDetails">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            查看详细
          </button>
          <button 
            class="btn-primary" 
            :disabled="!agreed"
            @click="confirmAgreement"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            我已阅读并同意
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const showModal = ref(false)
const agreed = ref(false)

const DISCLAIMER_KEY = 'disclaimer_agreed'

onMounted(() => {
  // 检查用户是否已经同意过
  const hasAgreed = localStorage.getItem(DISCLAIMER_KEY)
  
  if (!hasAgreed) {
    // 3秒后显示弹窗
    setTimeout(() => {
      showModal.value = true
    }, 3000)
  }
})

const handleOverlayClick = () => {
  // 点击遮罩层不关闭，必须同意才能关闭
}

const closeModal = () => {
  if (agreed.value) {
    showModal.value = false
    localStorage.setItem(DISCLAIMER_KEY, 'true')
  }
}

const viewDetails = () => {
  // 可以跳转到详细的免责声明页面
  window.open('/disclaimer', '_blank')
}

const confirmAgreement = () => {
  if (agreed.value) {
    showModal.value = false
    localStorage.setItem(DISCLAIMER_KEY, 'true')
  }
}
</script>

<style scoped>
/* 过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .disclaimer-modal,
.modal-fade-leave-active .disclaimer-modal {
  transition: transform 0.3s ease;
}

.modal-fade-enter-from .disclaimer-modal,
.modal-fade-leave-to .disclaimer-modal {
  transform: scale(0.9);
}

/* 遮罩层 - 背景模糊 */
.disclaimer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

/* 弹窗主体 */
.disclaimer-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 24px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  line-height: 1;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  color: #333;
  transform: rotate(90deg);
}

/* 头部 */
.modal-header {
  padding: 32px 32px 24px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.modal-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #666;
}

/* 内容区 */
.modal-body {
  padding: 24px 32px;
}

.disclaimer-content {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.disclaimer-item {
  display: flex;
  align-items: flex-start;
  margin: 0 0 12px;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
}

.disclaimer-item:last-child {
  margin-bottom: 0;
}

.bullet {
  color: #667eea;
  font-weight: bold;
  margin-right: 8px;
  flex-shrink: 0;
}

/* 同意区域 */
.agreement-section {
  background: #fff8e1;
  border: 1px solid #ffe082;
  border-radius: 8px;
  padding: 16px;
}

.checkbox-wrapper {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #667eea;
  border-radius: 4px;
  margin-right: 12px;
  flex-shrink: 0;
  position: relative;
  transition: all 0.2s;
}

.checkbox-input:checked + .checkbox-custom {
  background: #667eea;
  border-color: #667eea;
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 5px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 底部按钮 */
.modal-footer {
  padding: 20px 32px 32px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-secondary,
.btn-primary {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 768px) {
  .disclaimer-overlay {
    padding: 10px;
  }

  .disclaimer-modal {
    max-width: 100%;
  }

  .modal-header {
    padding: 24px 20px 20px;
  }

  .modal-header h2 {
    font-size: 20px;
  }

  .icon-wrapper {
    width: 56px;
    height: 56px;
  }

  .modal-body {
    padding: 20px;
  }

  .disclaimer-content {
    padding: 16px;
  }

  .disclaimer-item {
    font-size: 14px;
  }

  .modal-footer {
    padding: 16px 20px 24px;
    flex-direction: column;
  }

  .btn-secondary,
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
