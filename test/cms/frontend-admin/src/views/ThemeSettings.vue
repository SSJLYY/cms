<template>
  <div class="theme-settings-container">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon">
              <el-icon><Brush /></el-icon>
            </div>
            <div class="header-text">
              <span class="header-title">主题设置</span>
              <span class="header-subtitle">Theme Settings</span>
            </div>
          </div>
          <el-button type="primary" class="save-btn" @click="saveSettings">
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
        </div>
      </template>

      <div class="alert-wrapper">
        <el-alert
          title="主题说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="alert-content">
              <p>管理员设置的主题优先级高于用户选择，将覆盖所有用户的主题设置</p>
              <p>选择「用户自选」时，用户可以通过首页右上角的拉绳切换深色/浅色主题</p>
            </div>
          </template>
        </el-alert>
      </div>

      <el-form :model="form" label-width="120px" class="theme-form">
        <el-form-item label="当前主题">
          <el-radio-group v-model="form.theme" size="large" class="theme-radio-group">
            <el-radio-button label="user-choice">
              <el-icon><User /></el-icon>
              <span>用户自选</span>
            </el-radio-button>
            <el-radio-button label="light">
              <el-icon><Sunny /></el-icon>
              <span>浅色主题</span>
            </el-radio-button>
            <el-radio-button label="dark">
              <el-icon><Moon /></el-icon>
              <span>深色主题</span>
            </el-radio-button>
            <el-radio-button label="gray">
              <el-icon><Warning /></el-icon>
              <span>灰色主题</span>
            </el-radio-button>
            <el-radio-button label="christmas">
              <el-icon><Present /></el-icon>
              <span>圣诞主题</span>
            </el-radio-button>
            <el-radio-button label="spring-festival">
              <el-icon><Coin /></el-icon>
              <span>春节主题</span>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="主题预览">
          <div class="theme-preview-grid">
            <!-- 用户自选 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'user-choice' }]"
              @click="form.theme = 'user-choice'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><User /></el-icon>
                <span>用户自选</span>
              </div>
              <div class="preview-content user-choice">
                <div class="preview-section light-section">浅色</div>
                <div class="preview-section dark-section">深色</div>
              </div>
              <div class="preview-desc">用户可自由切换深色/浅色</div>
              <div v-if="form.theme === 'user-choice'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <!-- 浅色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'light' }]"
              @click="form.theme = 'light'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><Sunny /></el-icon>
                <span>浅色主题</span>
              </div>
              <div class="preview-content light-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">明亮清爽的白色主题</div>
              <div v-if="form.theme === 'light'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <!-- 深色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'dark' }]"
              @click="form.theme = 'dark'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><Moon /></el-icon>
                <span>深色主题</span>
              </div>
              <div class="preview-content dark-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">护眼舒适的黑色主题</div>
              <div v-if="form.theme === 'dark'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <!-- 灰色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'gray' }]"
              @click="form.theme = 'gray'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><Warning /></el-icon>
                <span>灰色主题</span>
              </div>
              <div class="preview-content gray-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">默哀模式，所有色彩失效</div>
              <div v-if="form.theme === 'gray'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <!-- 圣诞主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'christmas' }]"
              @click="form.theme = 'christmas'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><Present /></el-icon>
                <span>圣诞主题</span>
              </div>
              <div class="preview-content christmas-theme">
                <div class="christmas-hat">🎅</div>
                <div class="christmas-gift">🎁</div>
                <div class="christmas-tree">🎄</div>
              </div>
              <div class="preview-desc">圣诞帽+礼物飘落特效</div>
              <div v-if="form.theme === 'christmas'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>

            <!-- 春节主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'spring-festival' }]"
              @click="form.theme = 'spring-festival'"
            >
              <div class="preview-header">
                <el-icon class="header-icon-inner"><Coin /></el-icon>
                <span>春节主题</span>
              </div>
              <div class="preview-content spring-festival-theme">
                <div class="spring-item">🧧</div>
                <div class="spring-item">🏮</div>
                <div class="spring-item">🧨</div>
              </div>
              <div class="preview-desc">红色主题+红包飘落+鞭炮</div>
              <div v-if="form.theme === 'spring-festival'" class="active-badge">
                <el-icon><Check /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, User, Sunny, Moon, Warning, Present, Coin, Brush } from '@element-plus/icons-vue'
import { getConfig, updateConfig } from '../api/config'

const form = ref({
  theme: 'user-choice'
})

const loadSettings = async () => {
  try {
    const res = await getConfig()
    if (res.data) {
      for (const category in res.data) {
        const configs = res.data[category]
        const themeConfig = configs.find(c => c.configKey === 'site.theme')
        if (themeConfig) {
          form.value.theme = themeConfig.configValue || 'user-choice'
          break
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载主题设置失败')
  }
}

const saveSettings = async () => {
  try {
    await updateConfig({
      'site.theme': form.value.theme
    })
    ElMessage.success('主题设置已保存')
  } catch (error) {
    ElMessage.error('保存主题设置失败')
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.theme-settings-container {
  padding: 24px;
}

.header-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  padding: 0;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.header-text {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.header-subtitle {
  font-size: 12px;
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.save-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.save-btn:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: translateY(-1px);
}

:deep(.el-card__body) {
  padding: 24px;
}

.alert-wrapper {
  margin-bottom: 24px;
}

:deep(.el-alert) {
  border-radius: 12px;
  border: 1px solid #e0e6ed;
}

:deep(.el-alert__icon) {
  font-size: 18px;
}

.alert-content p {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.alert-content p:first-child {
  margin-top: 0;
}

.alert-content p:last-child {
  margin-bottom: 0;
}

.theme-form {
  margin-top: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

.theme-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.el-radio-button__inner) {
  border-radius: 10px !important;
  border: 1px solid #e0e6ed;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border-color: #667eea !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.theme-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
  width: 100%;
}

@media (max-width: 1200px) {
  .theme-preview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .theme-preview-grid {
    grid-template-columns: 1fr;
  }
  
  .theme-settings-container {
    padding: 12px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px 20px;
  }
  
  .save-btn {
    width: 100%;
    justify-content: center;
  }
}

.theme-preview-card {
  border: 2px solid #e0e6ed;
  border-radius: 14px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  position: relative;
  overflow: hidden;
}

.theme-preview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #c0c8e0;
}

.theme-preview-card.active {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
  background: linear-gradient(to bottom, rgba(102, 126, 234, 0.03), white);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.header-icon-inner {
  color: #667eea;
  font-size: 16px;
}

.preview-content {
  height: 100px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
  overflow: hidden;
}

.user-choice {
  background: linear-gradient(90deg, #f5f5f5 50%, #1a1a1a 50%);
}

.light-section,
.dark-section {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.light-section {
  color: #333;
}

.dark-section {
  color: #e0e0e0;
}

.light-theme {
  background: #f5f5f5;
}

.dark-theme {
  background: #1a1a1a;
}

.gray-theme {
  background: #808080;
  filter: grayscale(100%);
}

.christmas-theme {
  background: linear-gradient(135deg, #c41e3a 0%, #165b33 100%);
}

.spring-festival-theme {
  background: linear-gradient(135deg, #ff0000 0%, #ffd700 100%);
}

.preview-box {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.light-theme .preview-box {
  background: white;
  border: 1px solid #e0e6ed;
}

.dark-theme .preview-box {
  background: #2d2d2d;
  border: 1px solid #404040;
}

.gray-theme .preview-box {
  background: #666;
}

.christmas-hat,
.christmas-gift,
.christmas-tree,
.spring-item {
  font-size: 26px;
  animation: float 2s ease-in-out infinite;
}

.christmas-gift {
  animation-delay: 0.3s;
}

.christmas-tree {
  animation-delay: 0.6s;
}

.spring-item:nth-child(2) {
  animation-delay: 0.3s;
}

.spring-item:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.preview-desc {
  font-size: 12px;
  color: #999;
  text-align: center;
}

.active-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}
</style>
