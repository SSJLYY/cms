<template>
  <div class="help-page">
    <!-- 返回首页 -->
    <div class="back-home">
      <router-link to="/" class="back-link">
        <i class="fas fa-arrow-left"></i>
        返回首页
      </router-link>
    </div>

    <!-- 头部 -->
    <div class="help-header">
      <div class="help-content">
        <h1 class="help-title">
          <i class="fas fa-life-ring"></i>
          帮助中心
        </h1>
        <p class="help-description">在这里可以找到常见问题的解答</p>
        
        <!-- 装饰性元素 -->
        <div class="help-decoration">
          <div class="help-decoration-circle help-circle-1"></div>
          <div class="help-decoration-circle help-circle-2"></div>
          <div class="help-decoration-circle help-circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 帮助内容 -->
    <div class="container">
      <div class="help-container">
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
                  如何彻底关闭Windows防火墙？【必看！不看任何游戏都无法运行！】
                </h5>
                <div class="help-item-content">
                  1. 按Win+R打开运行，输入control打开控制面板<br>
                  2. 点击"系统和安全"<br>
                  3. 点击"Windows Defender防火墙"<br>
                  4. 点击左侧"启用或关闭Windows Defender防火墙"<br>
                  5. 在专用网络和公用网络位置下都选择"关闭Windows Defender防火墙"<br>
                  6. 点击确定保存设置<br><br>
                  注意：部分游戏必须关闭防火墙才能正常运行，这是正常现象。
                </div>
              </div>
              
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-exclamation-triangle"></i>
                  游戏运行没反应？游戏打开缺少DLL或报错？
                </h5>
                <div class="help-item-content">
                  <strong>可能的原因和解决方案：</strong><br><br>
                  1. <strong>缺少运行库</strong>：下载安装游戏常用运行库+DirectX修复增强版<br>
                  2. <strong>缺少 DirectX 或 DLL 文件</strong>：下载安装DirectX修复工具<br>
                  3. <strong>缺少 .NET Framework</strong>：安装 .NET Framework 4.5 或更高版本<br>
                  4. <strong>Windows 防火墙拦截</strong>：彻底关闭 Windows 防火墙<br>
                  5. <strong>杀毒软件删除文件</strong>：将游戏目录添加到杀毒软件白名单<br>
                  6. <strong>路径问题</strong>：游戏路径不要包含中文，使用英文路径<br>
                  7. <strong>管理员权限</strong>：右键游戏主程序选择"以管理员身份运行"<br><br>
                  如果以上都解决不了，建议重装纯净的 Windows 系统。
                </div>
              </div>
              
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-question-circle"></i>
                  GOG游戏怎么安装？Steam版和GOG版怎么选？
                </h5>
                <div class="help-item-content">
                  <strong>① GOG游戏怎么安装？</strong><br>
                  解压完GOG游戏后，先安装游戏本体，再依次安装DLC（部分带Bonus，包含壁纸、原生音乐、小说艺术集等）。本体未安装时DLC无法安装，全部安装完成后即可运行。<br><br>
                  <strong>② Steam版和GOG版怎么选？</strong><br>
                  <strong>Steam版特点：</strong>解压即玩，无需安装。但使用了学习补丁，可能会被杀毒软件报毒。<br><br>
                  <strong>GOG版特点：</strong>Steam采用加密防盗版，GOG无加密，玩家可自由拷贝游戏。GOG资源无加密，无需学习补丁即可运行。<br><br>
                  <strong>建议：</strong>优先GOG（干净无加密），需要快速体验可选Steam（解压即玩）。
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
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-exclamation-circle"></i>
                  为什么我无法下载？
                </h5>
                <div class="help-item-content">
                  可能的原因：<br>
                  1. 达到每日下载上限<br>
                  2. IP被限制（请勿频繁下载）
                </div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 浏览器相关 -->
        <div class="help-section">
          <div class="help-section-header" @click="toggleSection('browser')">
            <div class="section-title-wrapper">
              <div class="section-icon">
                <i class="fas fa-globe"></i>
              </div>
              <span class="section-title">浏览器兼容性问题</span>
            </div>
            <i class="fas fa-chevron-down" :class="{ 'rotated': openSections.browser }"></i>
          </div>
          <Transition name="expand">
            <div v-show="openSections.browser" class="help-section-content">
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fab fa-chrome"></i>
                  支持哪些浏览器？
                </h5>
                <div class="help-item-content">
                  建议使用以下浏览器访问：<br>
                  1. Chrome 最新版<br>
                  2. Edge 最新版<br>
                  3. Firefox 最新版
                </div>
              </div>
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-mobile-alt"></i>
                  移动端访问注意事项
                </h5>
                <div class="help-item-content">
                  移动端推荐使用系统自带浏览器或Chrome浏览器访问，部分第三方浏览器可能存在兼容性问题。
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
                  <i class="fas fa-clock"></i>
                  下载次数说明
                </h5>
                <div class="help-item-content">
                  每个账号每天有固定的下载次数限制，次日凌晨会自动重置下载次数。
                </div>
              </div>
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-network-wired"></i>
                  IP限制说明
                </h5>
                <div class="help-item-content">
                  为了防止滥用，同一IP地址短时间内的下载次数也会受到限制，请合理下载使用。
                </div>
              </div>
              <div class="help-item">
                <h5 class="help-item-title">
                  <i class="fas fa-shield-alt"></i>
                  关于VPN网络访问
                </h5>
                <div class="help-item-content">
                  <strong>请不要使用VPN网络访问本站</strong><br><br>
                  使用VPN可能导致无法正常访问站点，出现异常情况或连接错误。建议使用正常网络环境访问，以确保最佳的访问体验和功能正常使用。
                </div>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>

    <!-- 右下角操作按钮 -->
    <ActionButtons />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import ActionButtons from '../components/ActionButtons.vue'

const openSections = reactive({
  game: true,
  unzip: false,
  download: false,
  browser: false,
  account: false
})

const toggleSection = (section) => {
  openSections[section] = !openSections[section]
}
</script>

<style scoped>
.help-page {
  min-height: 100vh;
  background-color: var(--bg-primary);
  padding-bottom: 60px;
  transition: background-color 0.3s ease;
}

.back-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 20px 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  font-size: 15px;
}

.back-link:hover {
  color: #2563eb;
}

/* 头部 */
.help-header {
  position: relative;
  padding: 60px 0 40px;
  margin: 0 0 30px;
  text-align: center;
  overflow: hidden;
}

.help-content {
  position: relative;
  z-index: 2;
}

.help-title {
  margin: 0 0 16px;
  font-size: clamp(1.8rem, 5vw, 2.8rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.1;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.help-title i {
  font-size: 0.8em;
  color: #3b82f6;
}

.help-description {
  font-size: 1.2rem;
  color: var(--text-secondary);
  font-weight: 400;
  margin: 0;
  opacity: 0.9;
}

/* 装饰性元素 */
.help-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.help-decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.help-circle-1 {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  top: 20%;
  right: 10%;
  animation-delay: 0s;
}

.help-circle-2 {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  top: 60%;
  left: 15%;
  animation-delay: 2s;
}

.help-circle-3 {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  top: 30%;
  left: 70%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.help-container {
  background: var(--card-bg);
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

/* 帮助分类 */
.help-section {
  border-bottom: 1px solid var(--border-color);
}

.help-section:last-child {
  border-bottom: none;
}

.help-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  cursor: pointer;
  background: var(--card-bg);
  transition: all 0.3s ease;
}

.help-section-header:hover {
  background: var(--bg-tertiary);
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border-radius: 12px;
  color: #3b82f6;
  font-size: 1.2rem;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.help-section-header:hover .section-icon {
  background: #e0f2fe;
  color: #0284c7;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.help-section-header i.fa-chevron-down {
  color: var(--text-tertiary);
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
  max-height: 2000px;
  opacity: 1;
}

/* 帮助内容 */
.help-section-content {
  padding: 16px 32px 32px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.help-item {
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  transition: background-color 0.3s ease;
}

.help-item:last-child {
  margin-bottom: 0;
}

.help-item-title {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.help-item-title i {
  color: #3b82f6;
  font-size: 1rem;
}

.help-item-content {
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 1rem;
  margin: 0;
}

.help-item-content strong {
  color: var(--text-primary);
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .help-header {
    padding: 30px 0 20px;
  }
  
  .help-title {
    font-size: clamp(1.6rem, 7vw, 2.2rem);
    margin-bottom: 12px;
  }
  
  .help-description {
    font-size: 0.95rem;
  }
  
  .help-decoration-circle {
    display: none;
  }
  
  .help-section-header {
    padding: 20px 20px;
  }
  
  .section-icon {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .section-title {
    font-size: 1rem;
  }
  
  .help-section-content {
    padding: 0 20px 20px;
  }
  
  .help-item {
    padding: 16px;
    margin-bottom: 16px;
  }
  
  .help-item-title {
    font-size: 1rem;
  }
  
  .help-item-content {
    font-size: 0.95rem;
  }
}
</style>
