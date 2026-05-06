<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="bg-particles">
        <span v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></span>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-container">
      <div class="login-card">
        <!-- 顶部品牌区域 -->
        <div class="card-header">
          <div class="logo-wrapper">
            <div class="logo-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7l10 5 10-5-10-5z" fill="currentColor" opacity="0.9"/>
                <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="logo-text">
              <h1 class="brand-name">资源管理平台</h1>
              <span class="brand-subtitle">Resource Platform</span>
            </div>
          </div>
        </div>

        <!-- 欢迎信息 -->
        <div class="welcome-section">
          <h2 class="welcome-title">欢迎回来</h2>
          <p class="welcome-desc">请登录您的管理员账户</p>
        </div>

        <!-- 登录表单 -->
        <el-form 
          :model="loginForm" 
          :rules="rules" 
          ref="formRef"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <label class="input-label">
                <el-icon class="input-icon"><User /></el-icon>
                <span>用户名</span>
              </label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
                autocomplete="username"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <label class="input-label">
                <el-icon class="input-icon"><Lock /></el-icon>
                <span>密码</span>
              </label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                autocomplete="current-password"
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              :disabled="loading"
              @click="handleLogin"
            >
              <span v-if="!loading" class="btn-content">
                <el-icon class="btn-icon"><Promotion /></el-icon>
                <span>登录</span>
              </span>
              <span v-else class="btn-loading">
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
                <span class="loading-dot"></span>
              </span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部信息 -->
        <div class="card-footer">
          <div class="security-tip">
            <el-icon><Key /></el-icon>
            <span>安全加密登录 · 保护您的账户</span>
          </div>
        </div>
      </div>

      <!-- 右侧装饰 -->
      <div class="decoration-panel">
        <div class="deco-content">
          <div class="deco-icon">
            <svg width="120" height="120" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="currentColor" opacity="0.3"/>
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h3 class="deco-title">高效管理</h3>
          <p class="deco-desc">一站式资源管理平台，轻松管理您的所有资源</p>
          <div class="deco-features">
            <div class="feature-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据可视化</span>
            </div>
            <div class="feature-item">
              <el-icon><Setting /></el-icon>
              <span>灵活配置</span>
            </div>
            <div class="feature-item">
              <el-icon><User /></el-icon>
              <span>权限管理</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Promotion, DataAnalysis, Setting } from '@element-plus/icons-vue'
import { login } from '../api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 生成随机粒子样式
const getParticleStyle = (index) => {
  const size = Math.random() * 6 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = Math.random() * 10 + 10
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const res = await login(loginForm.value)
    localStorage.setItem('token', res.data.token)
    
    // 成功动画
    ElMessage.success({
      message: '登录成功，欢迎回来！',
      duration: 1500
    })
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/')
    }, 500)
  } catch (error) {
    if (error !== false) {
      ElMessage.error({
        message: '用户名或密码错误',
        duration: 2000
      })
    }
  } finally {
    setTimeout(() => {
      loading.value = false
    }, 500)
  }
}
</script>

<style scoped>
/* 页面布局 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -100px;
  left: -100px;
  animation-delay: -5s;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(20px, 30px) scale(1.02); }
}

/* 粒子动画 */
.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particle-float linear infinite;
}

@keyframes particle-float {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(720deg);
    opacity: 0;
  }
}

/* 登录容器 */
.login-container {
  display: flex;
  width: 950px;
  max-width: 95vw;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 10;
  backdrop-filter: blur(10px);
}

/* 登录卡片 */
.login-card {
  flex: 1;
  padding: 48px 40px;
}

/* 品牌区域 */
.card-header {
  margin-bottom: 32px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 32px;
}

.welcome-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.welcome-desc {
  font-size: 0.95rem;
  color: #666;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 24px;
}

.input-wrapper {
  width: 100%;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #444;
}

.input-icon {
  color: #667eea;
  font-size: 1rem;
}

::deep(.el-input__wrapper) {
  padding: 4px 15px;
  border-radius: 12px;
  box-shadow: 0 0 0 1px #e0e6ed;
  transition: all 0.3s ease;
}

::deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea;
}

::deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2), 0 0 0 1px #667eea;
}

::deep(.el-input__inner) {
  font-size: 1rem;
  height: 44px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-icon {
  font-size: 1.2rem;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.loading-dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: bounce 1.4s ease-in-out infinite both;
}

.loading-dot:nth-child(1) { animation-delay: -0.32s; }
.loading-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 底部信息 */
.card-footer {
  text-align: center;
}

.security-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #888;
  padding: 8px 16px;
  background: #f8f9fa;
  border-radius: 20px;
}

.security-tip .el-icon {
  color: #667eea;
}

/* 右侧装饰面板 */
.decoration-panel {
  width: 320px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.decoration-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.deco-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.deco-icon {
  margin-bottom: 24px;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.deco-title {
  font-size: 1.6rem;
  font-weight: 700;
  margin: 0 0 12px;
}

.deco-desc {
  font-size: 0.95rem;
  opacity: 0.9;
  margin: 0 0 32px;
  line-height: 1.6;
}

.deco-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 0.95rem;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.feature-item .el-icon {
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: 100%;
    max-width: 100vw;
    border-radius: 0;
    min-height: 100vh;
  }

  .login-card {
    padding: 32px 24px;
  }

  .decoration-panel {
    display: none;
  }

  .card-header {
    margin-bottom: 24px;
  }

  .logo-icon {
    width: 48px;
    height: 48px;
  }

  .brand-name {
    font-size: 1.2rem;
  }

  .welcome-title {
    font-size: 1.5rem;
  }

  .bg-shape {
    opacity: 0.2;
  }
}
</style>
