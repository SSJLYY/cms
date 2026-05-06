<template>
  <div class="action-buttons-container">
    <!-- 反馈按钮 -->
    <button class="action-btn feedback-btn" @click="showFeedbackModal = true" title="意见反馈">
      <div class="btn-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <span class="btn-text">一键反馈</span>
    </button>

    <!-- 帮助按钮 -->
    <a href="/help" target="_blank" class="action-btn help-btn" title="帮助中心">
      <div class="btn-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
          <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
      </div>
      <span class="btn-text">帮助中心</span>
    </a>

    <!-- 反馈弹窗 -->
    <div v-if="showFeedbackModal" class="modal-overlay" @click="closeFeedbackModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="header-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <h3>意见反馈</h3>
          <button class="close-btn" @click="closeFeedbackModal">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                  <polyline points="22,6 12,13 2,6"/>
                </svg>
                反馈类型 <span class="required">*</span>
              </label>
              <select v-model="form.type" required class="modern-select">
                <option value="">请选择</option>
                <option value="SUGGESTION">💡 建议</option>
                <option value="ISSUE">🐛 问题</option>
                <option value="COMPLAINT">📢 投诉</option>
                <option value="OTHER">📝 其他</option>
              </select>
            </div>

            <div class="form-group">
              <label>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 20h9"/>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
                </svg>
                标题
              </label>
              <input 
                v-model="form.title" 
                type="text" 
                placeholder="请输入标题（选填）"
                maxlength="100"
                class="modern-input"
              />
            </div>

            <div class="form-group">
              <label>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="17" y1="10" x2="3" y2="10"/>
                  <line x1="21" y1="6" x2="3" y2="6"/>
                  <line x1="21" y1="14" x2="3" y2="14"/>
                  <line x1="17" y1="18" x2="3" y2="18"/>
                </svg>
                反馈内容 <span class="required">*</span>
              </label>
              <textarea 
                v-model="form.content" 
                placeholder="请详细描述您的问题或建议..."
                rows="6"
                required
                maxlength="1000"
                class="modern-textarea"
              ></textarea>
              <div class="char-count">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (form.content.length / 1000) * 100 + '%' }"></div>
                </div>
                {{ form.content.length }}/1000
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  称呼
                </label>
                <input 
                  v-model="form.contactName" 
                  type="text" 
                  placeholder="您的称呼（选填）"
                  maxlength="50"
                  class="modern-input"
                />
              </div>

              <div class="form-group">
                <label>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                  邮箱
                </label>
                <input 
                  v-model="form.contactEmail" 
                  type="email" 
                  placeholder="您的邮箱（选填）"
                  maxlength="100"
                  class="modern-input"
                />
              </div>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeFeedbackModal">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                取消
              </button>
              <button type="submit" class="btn-submit" :disabled="submitting">
                <svg v-if="!submitting" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="22" y1="2" x2="11" y2="13"/>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
                <span v-if="submitting" class="spinner"></span>
                {{ submitting ? '提交中...' : '提交反馈' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 成功提示 -->
    <Transition name="toast">
      <div v-if="showSuccess" class="success-toast">
        <div class="toast-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <span>反馈提交成功！感谢您的反馈</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback } from '../api/feedback'

const showFeedbackModal = ref(false)
const submitting = ref(false)
const showSuccess = ref(false)

const form = reactive({
  type: '',
  title: '',
  content: '',
  contactName: '',
  contactEmail: ''
})

const closeFeedbackModal = () => {
  showFeedbackModal.value = false
}

const resetForm = () => {
  form.type = ''
  form.title = ''
  form.content = ''
  form.contactName = ''
  form.contactEmail = ''
}

const handleSubmit = async () => {
  if (!form.type || !form.content.trim()) {
    return
  }

  try {
    submitting.value = true
    await submitFeedback(form)
    
    showSuccess.value = true
    setTimeout(() => {
      showSuccess.value = false
    }, 3000)
    
    closeFeedbackModal()
    resetForm()
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.action-buttons-container {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.action-btn {
  min-width: 140px;
  height: 56px;
  padding: 0 24px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  border: none;
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25);
}

.action-btn:active {
  transform: translateY(-2px) scale(1.01);
}

.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  flex-shrink: 0;
}

.btn-text {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.feedback-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.feedback-btn:hover {
  background: linear-gradient(135deg, #ec4899 0%, #ef4444 100%);
}

.help-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.help-btn:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6b3fa3 100%);
}

/* 弹窗遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 弹窗内容 */
.modal-content {
  background: white;
  border-radius: 24px;
  width: 90%;
  max-width: 520px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.25);
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from {
    transform: translateY(40px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 弹窗头部 */
.modal-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-header h3 {
  flex: 1;
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  cursor: pointer;
  color: white;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

/* 弹窗主体 */
.modal-body {
  padding: 28px;
  max-height: calc(90vh - 100px);
  overflow-y: auto;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 0.95rem;
  color: #475569;
  font-weight: 500;
}

.required {
  color: #ef4444;
}

.modern-input,
.modern-select,
.modern-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  transition: all 0.3s;
  background: white;
  font-family: inherit;
}

.modern-input:focus,
.modern-select:focus,
.modern-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.modern-textarea {
  resize: vertical;
  min-height: 140px;
}

.char-count {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  font-size: 0.85rem;
  color: #94a3b8;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
  transition: width 0.3s;
}

/* 表单按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 14px 24px;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 16px 28px;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(16, 185, 129, 0.4);
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 10001;
}

.toast-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.toast-enter-from {
  transform: translateX(-50%) translateY(-100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(-50%) translateY(-100%);
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .action-buttons-container {
    right: 16px;
    bottom: 16px;
    gap: 12px;
  }
  
  .action-btn {
    min-width: 120px;
    height: 48px;
    padding: 0 18px;
    border-radius: 24px;
  }

  .btn-icon {
    width: 28px;
    height: 28px;
  }

  .btn-text {
    font-size: 13px;
  }

  .modal-content {
    width: 95%;
    margin: 16px;
  }

  .modal-header {
    padding: 20px;
  }

  .modal-body {
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .action-buttons-container {
    right: 12px;
    bottom: 12px;
    gap: 10px;
  }
  
  .action-btn {
    min-width: 50px;
    height: 50px;
    padding: 0;
    border-radius: 25px;
  }

  .btn-text {
    display: none;
  }
}
</style>
