<template>
  <div class="feedback-container">
    <!-- 浮动按钮 -->
    <button class="feedback-btn" @click="showModal = true" title="意见反馈">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
      </svg>
      <span>反馈</span>
    </button>

    <!-- 反馈弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>意见反馈</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label>反馈类型 <span class="required">*</span></label>
              <select v-model="form.type" required>
                <option value="">请选择</option>
                <option value="SUGGESTION">建议</option>
                <option value="ISSUE">问题</option>
                <option value="COMPLAINT">投诉</option>
                <option value="OTHER">其他</option>
              </select>
            </div>

            <div class="form-group">
              <label>标题</label>
              <input 
                v-model="form.title" 
                type="text" 
                placeholder="请输入标题（选填）"
                maxlength="100"
              />
            </div>

            <div class="form-group">
              <label>反馈内容 <span class="required">*</span></label>
              <textarea 
                v-model="form.content" 
                placeholder="请详细描述您的问题或建议..."
                rows="6"
                required
                maxlength="1000"
              ></textarea>
              <div class="char-count">{{ form.content.length }}/1000</div>
            </div>

            <div class="form-group">
              <label>联系方式</label>
              <input 
                v-model="form.contactName" 
                type="text" 
                placeholder="您的称呼（选填）"
                maxlength="50"
              />
            </div>

            <div class="form-group">
              <label>联系邮箱</label>
              <input 
                v-model="form.contactEmail" 
                type="email" 
                placeholder="您的邮箱（选填）"
                maxlength="100"
              />
            </div>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeModal">取消</button>
              <button type="submit" class="btn-submit" :disabled="submitting">
                {{ submitting ? '提交中...' : '提交反馈' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccess" class="success-toast">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"/>
      </svg>
      <span>反馈提交成功！感谢您的反馈</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { submitFeedback } from '../api/feedback'

const showModal = ref(false)
const submitting = ref(false)
const showSuccess = ref(false)

const form = reactive({
  type: '',
  title: '',
  content: '',
  contactName: '',
  contactEmail: ''
})

const closeModal = () => {
  showModal.value = false
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
    
    // 显示成功提示
    showSuccess.value = true
    setTimeout(() => {
      showSuccess.value = false
    }, 3000)
    
    // 关闭弹窗并重置表单
    closeModal()
    resetForm()
  } catch (error) {
    console.error('提交反馈失败', error)
    alert('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.feedback-container {
  position: fixed;
  z-index: 9999;
}

/* 浮动按钮 */
.feedback-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.feedback-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.feedback-btn svg {
  width: 24px;
  height: 24px;
}

.feedback-btn span {
  font-size: 11px;
  font-weight: 500;
}

/* 弹窗遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
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

/* 弹窗内容 */
.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
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
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e0e6ed;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

/* 弹窗主体 */
.modal-body {
  padding: 24px;
  max-height: calc(90vh - 80px);
  overflow-y: auto;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 0.95rem;
  color: #555;
  font-weight: 500;
}

.required {
  color: #ff4757;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e6ed;
  border-radius: 6px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.char-count {
  text-align: right;
  font-size: 0.85rem;
  color: #999;
  margin-top: 4px;
}

/* 表单按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e6ed;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #10b981;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  display: flex;
  align-items: center;
  gap: 8px;
  animation: slideDown 0.3s ease;
  z-index: 10001;
}

@keyframes slideDown {
  from {
    transform: translateX(-50%) translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) translateY(0);
    opacity: 1;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .feedback-btn {
    right: 20px;
    bottom: 20px;
    width: 56px;
    height: 56px;
  }

  .feedback-btn svg {
    width: 22px;
    height: 22px;
  }

  .feedback-btn span {
    font-size: 10px;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .modal-header {
    padding: 16px 20px;
  }

  .modal-body {
    padding: 20px;
  }
}
</style>
