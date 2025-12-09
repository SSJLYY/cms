<template>
  <div class="theme-settings-container">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>主题设置</span>
          <el-button type="primary" @click="saveSettings">
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
        </div>
      </template>

      <el-alert
        title="主题说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>管理员设置的主题优先级高于用户选择，将覆盖所有用户的主题设置</p>
        <p>选择"用户自选"时，用户可以通过首页右上角的拉绳切换深色/浅色主题</p>
      </el-alert>

      <el-form :model="form" label-width="120px">
        <el-form-item label="当前主题">
          <el-radio-group v-model="form.theme" size="large">
            <el-radio-button label="user-choice">
              <el-icon><User /></el-icon>
              用户自选
            </el-radio-button>
            <el-radio-button label="light">
              <el-icon><Sunny /></el-icon>
              浅色主题
            </el-radio-button>
            <el-radio-button label="dark">
              <el-icon><Moon /></el-icon>
              深色主题
            </el-radio-button>
            <el-radio-button label="gray">
              <el-icon><Warning /></el-icon>
              灰色主题
            </el-radio-button>
            <el-radio-button label="christmas">
              <el-icon><Present /></el-icon>
              圣诞主题
            </el-radio-button>
            <el-radio-button label="spring-festival">
              <el-icon><Coin /></el-icon>
              春节主题
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
                <el-icon><User /></el-icon>
                <span>用户自选</span>
              </div>
              <div class="preview-content user-choice">
                <div class="preview-section light-section">浅色</div>
                <div class="preview-section dark-section">深色</div>
              </div>
              <div class="preview-desc">用户可自由切换深色/浅色</div>
            </div>

            <!-- 浅色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'light' }]"
              @click="form.theme = 'light'"
            >
              <div class="preview-header">
                <el-icon><Sunny /></el-icon>
                <span>浅色主题</span>
              </div>
              <div class="preview-content light-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">明亮清爽的白色主题</div>
            </div>

            <!-- 深色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'dark' }]"
              @click="form.theme = 'dark'"
            >
              <div class="preview-header">
                <el-icon><Moon /></el-icon>
                <span>深色主题</span>
              </div>
              <div class="preview-content dark-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">护眼舒适的黑色主题</div>
            </div>

            <!-- 灰色主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'gray' }]"
              @click="form.theme = 'gray'"
            >
              <div class="preview-header">
                <el-icon><Warning /></el-icon>
                <span>灰色主题</span>
              </div>
              <div class="preview-content gray-theme">
                <div class="preview-box"></div>
                <div class="preview-box"></div>
                <div class="preview-box"></div>
              </div>
              <div class="preview-desc">默哀模式，所有色彩失效</div>
            </div>

            <!-- 圣诞主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'christmas' }]"
              @click="form.theme = 'christmas'"
            >
              <div class="preview-header">
                <el-icon><Present /></el-icon>
                <span>圣诞主题</span>
              </div>
              <div class="preview-content christmas-theme">
                <div class="christmas-hat">🎅</div>
                <div class="christmas-gift">🎁</div>
                <div class="christmas-tree">🎄</div>
              </div>
              <div class="preview-desc">圣诞帽+礼物飘落特效</div>
            </div>

            <!-- 春节主题 -->
            <div 
              :class="['theme-preview-card', { active: form.theme === 'spring-festival' }]"
              @click="form.theme = 'spring-festival'"
            >
              <div class="preview-header">
                <el-icon><Coin /></el-icon>
                <span>春节主题</span>
              </div>
              <div class="preview-content spring-festival-theme">
                <div class="spring-item">🧧</div>
                <div class="spring-item">🏮</div>
                <div class="spring-item">🧨</div>
              </div>
              <div class="preview-desc">红色主题+红包飘落+鞭炮</div>
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
import { Check, User, Sunny, Moon, Warning, Present, Coin } from '@element-plus/icons-vue'
import { getConfig, updateConfig } from '../api/config'

const form = ref({
  theme: 'user-choice'
})

const loadSettings = async () => {
  try {
    const res = await getConfig()
    if (res.data) {
      // 遍历所有分类查找site.theme配置
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
    console.error('加载主题设置失败', error)
  }
}

const saveSettings = async () => {
  try {
    await updateConfig({
      'site.theme': form.value.theme
    })
    ElMessage.success('主题设置已保存')
  } catch (error) {
    console.error('保存主题设置失败', error)
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.theme-settings-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
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
}

.theme-preview-card {
  border: 2px solid #e0e6ed;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.theme-preview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.theme-preview-card.active {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
}

.preview-content {
  height: 100px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 12px;
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
  font-size: 14px;
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
  font-size: 28px;
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
    transform: translateY(-10px);
  }
}

.preview-desc {
  font-size: 12px;
  color: #999;
  text-align: center;
}
</style>
