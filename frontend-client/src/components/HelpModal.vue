<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click="handleClose"></div>
    </Transition>
    
    <Transition name="slide-up">
      <div v-if="visible" class="help-modal-container">
        <div class="help-modal-header">
          <div class="help-title-wrapper">
            <i class="fas fa-life-ring help-icon"></i>
            <h3 class="help-title">帮助中心</h3>
          </div>
          <button class="close-btn" @click="handleClose">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        
        <div class="help-modal-body">
          <div class="help-description">在这里可以找到常见问题的解答</div>
          
          <!-- 游戏相关 -->
          <div class="help-section">
            <div class="help-section-header" @click="toggleSection('game')">
              <div class="section-title-wrapper">
                <div class="section-icon">
                  <i class="fas fa-gamepad"></i>
                </div>
                <span class="section-title">游戏相关问题</span>
              </div>
              <i class="fas fa-chevron-down" :class="{ 'rotated': openSections.game }"></i>
            </div>
            <Transition name="expand">
              <div v-show="openSections.game" class="help-section-content">
                <div class="help-item">
                  <h5 class="help-item-title">
                    <i class="fas fa-shield-alt"></i>
                    如何彻底关闭Windows防火墙？
                  </h5>
                  <div class="help-item-content">
                    1. 按Win+R打开运行，输入control打开控制面板<br>
                    2. 点击"系统和安全"<br>
                    3. 点击"Windows Defender防火墙"<br>
                    4. 点击左侧"启用或关闭Windows Defender防火墙"<br>
                    5. 在专用网络和公用网络位置下都选择"关闭Windows Defender防火墙"<br>
                    6. 点击确定保存设置
                  </div>
                </div>
                
                <div class="help-item">
                  <h5 class="help-item-title">
                    <i class="fas fa-exclamation-triangle"></i>
                    游戏运行没反应？游戏打开缺少DLL或报错？
                  </h5>
                  <div class="help-item-content">
                    <strong>可能的原因和解决方案：</strong><br>
                    1. 缺少运行库：安装游戏常用运行库<br>
                    2. 缺少 DirectX 或 DLL 文件：安装DirectX修复工具<br>
                    3. 缺少 .NET Framework：安装 .NET Framework 4.5 或更高版本<br>
                    4. Windows 防火墙拦截：彻底关闭 Windows 防火墙<br>
                    5. 杀毒软件删除文件：将游戏目录添加到杀毒软件白名单<br>
                    6. 路径问题：游戏路径不要包含中文，使用英文路径<br>
                    7. 管理员权限：右键游戏主程序选择"以管理员身份运行"
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <!-- 解压相关 -->
          <div class="help-section">
            <div class="help-section-header" @click="toggleSection('unzip')">
              <div class="section-title-wrapper">
                <div class="section-icon">
                  <i class="fas fa-file-archive"></i>
                </div>
                <span class="section-title">解压相关问题</span>
              </div>
              <i class="fas fa-chevron-down" :class="{ 'rotated': openSections.unzip }"></i>
            </div>
            <Transition name="expand">
              <div v-show="openSections.unzip" class="help-section-content">
                <div class="help-item">
                  <h5 class="help-item-title">
                    <i class="fas fa-key"></i>
                    解压密码是多少？
                  </h5>
                  <div class="help-item-content">
                    游戏资源的解压密码默认为：<strong>XDGAME</strong><br>
                    （纯大写字母，复制时注意不要多复制空格）
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <!-- 下载相关 -->
          <div class="help-section">
            <div class="help-section-header" @click="toggleSection('download')">
              <div class="section-title-wrapper">
                <div class="section-icon">
                  <i class="fas fa-cloud-download-alt"></i>
                </div>
                <span class="section-title">资源下载相关问题</span>
              </div>
              <i class="fas fa-chevron-down" :class="{ 'rotated': openSections.download }"></i>
            </div>
            <Transition name="expand">
              <div v-show="openSections.download" class="help-section-content">
                <div class="help-item">
                  <h5 class="help-item-title">
                    <i class="fas fa-download"></i>
                    如何下载资源？
                  </h5>
                  <div class="help-item-content">
                    点击资源卡片或资源标题进入详情页，在详情页可以看到下载按钮，选择任意网盘链接即可下载。
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <!-- 账号相关 -->
          <div class="help-section">
            <div class="help-section-header" @click="toggleSection('account')">
              <div class="section-title-wrapper">
                <div class="section-icon">
                  <i class="fas fa-user-circle"></i>
                </div>
                <span class="section-title">账号相关问题</span>
              </div>
              <i class="fas fa-chevron-down" :class="{ 'rotated': openSections.account }"></i>
            </div>
            <Transition name="expand">
              <div v-show="openSections.account" class="help-section-content">
                <div class="help-item">
                  <h5 class="help-item-title">
                    <i class="fas fa-shield-alt"></i>
                    关于VPN网络访问
                  </h5>
                  <div class="help-item-content">
                    <strong>请不要使用VPN网络访问本站</strong><br>
                    使用VPN可能导致无法正常访问站点，出现异常情况或连接错误。建议使用正常网络环境访问，以确保最佳的访问体验和功能正常使用。
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

const openSections = reactive({
  game: false,
  unzip: false,
  download: false,
  account: false
})

const handleClose = () => {
  emit('update:visible', false)
}

const toggleSection = (section) => {
  openSections[section] = !openSections[section]
}
</script>

<style scoped>
/* 遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 帮助弹窗容器 */
.help-modal-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 700px;
  max-height: 85vh;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  overflow: hidden;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  transform: translate(-50%, -40%);
  opacity: 0;
}

.slide-up-leave-to {
  transform: translate(-50%, -40%);
  opacity: 0;
}

/* 弹窗头部 */
.help-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.help-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.help-icon {
  font-size: 24px;
  color: white;
}

.help-title {
  font-size: 20px;
  font-weight: 600;
  color: white;
  margin: 0;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  cursor: pointer;
  color: white;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 弹窗主体 */
.help-modal-body {
  padding: 24px 28px;
  overflow-y: auto;
  flex: 1;
}

.help-modal-body::-webkit-scrollbar {
  width: 8px;
}

.help-modal-body::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.help-modal-body::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 4px;
}

.help-modal-body::-webkit-scrollbar-thumb:hover {
  background: #b0b0b0;
}

.help-description {
  text-align: center;
  color: #6b7280;
  font-size: 15px;
  margin-bottom: 24px;
}

/* 帮助分类 */
.help-section {
  margin-bottom: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: white;
}

.help-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  background: #fafafa;
  transition: all 0.2s;
}

.help-section-header:hover {
  background: #f3f4f6;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  color: white;
  font-size: 18px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.help-section-header i.fa-chevron-down {
  color: #9ca3af;
  transition: transform 0.3s ease;
  font-size: 14px;
}

.help-section-header i.fa-chevron-down.rotated {
  transform: rotate(180deg);
}

/* 展开动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.expand-enter-to,
.expand-leave-from {
  max-height: 1000px;
  opacity: 1;
}

/* 帮助内容 */
.help-section-content {
  padding: 20px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.help-item {
  margin-bottom: 20px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.help-item:last-child {
  margin-bottom: 0;
}

.help-item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.help-item-title i {
  color: #667eea;
  font-size: 14px;
}

.help-item-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
}

.help-item-content strong {
  color: #1f2937;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .help-modal-container {
    width: 95%;
    max-height: 90vh;
  }
  
  .help-modal-header {
    padding: 20px 20px;
  }
  
  .help-title {
    font-size: 18px;
  }
  
  .help-icon {
    font-size: 20px;
  }
  
  .help-modal-body {
    padding: 20px 16px;
  }
  
  .help-section-header {
    padding: 14px 16px;
  }
  
  .section-icon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .section-title {
    font-size: 15px;
  }
  
  .help-section-content {
    padding: 16px;
  }
  
  .help-item {
    padding: 14px;
  }
  
  .help-item-title {
    font-size: 14px;
  }
  
  .help-item-content {
    font-size: 13px;
  }
}
</style>
