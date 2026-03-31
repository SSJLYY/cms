<template>
  <div class="help-page">
    <!-- 顶部导航 -->
    <div class="help-nav">
      <router-link to="/" class="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
        返回首页
      </router-link>
    </div>

    <!-- 英雄区 -->
    <div class="help-hero">
      <div class="hero-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
        <div class="grid-bg"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          帮助文档
        </div>
        <h1 class="hero-title">
          <span class="title-icon">💡</span>
          帮助中心
        </h1>
        <p class="hero-sub">遇到问题？在这里可以找到所有常见问题的答案</p>
        <!-- 快捷入口 -->
        <div class="quick-nav">
          <button
            v-for="section in sections"
            :key="section.key"
            class="quick-btn"
            @click="scrollToSection(section.key)"
          >
            {{ section.icon }} {{ section.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- 帮助内容 -->
    <div class="help-container">
      <div
        v-for="section in sections"
        :key="section.key"
        :id="`section-${section.key}`"
        class="help-section"
      >
        <div
          class="section-header"
          @click="toggleSection(section.key)"
          :class="{ open: openSections[section.key] }"
        >
          <div class="section-left">
            <div class="section-icon-wrap" :style="{ background: section.bg }">
              <span class="section-emoji">{{ section.icon }}</span>
            </div>
            <div>
              <div class="section-title">{{ section.label }}</div>
              <div class="section-count">{{ section.items.length }} 个问题</div>
            </div>
          </div>
          <div class="chevron" :class="{ rotated: openSections[section.key] }">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="6 9 12 15 18 9"></polyline>
            </svg>
          </div>
        </div>

        <Transition name="expand">
          <div v-show="openSections[section.key]" class="section-content">
            <div
              v-for="(item, idx) in section.items"
              :key="idx"
              class="help-item"
            >
              <div class="item-header">
                <div class="item-q-icon">Q</div>
                <div class="item-title" v-html="sanitizeHtml(item.title)"></div>
              </div>
              <div class="item-content" v-html="sanitizeHtml(item.content)"></div>
            </div>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 联系支持 -->
    <div class="support-section">
      <div class="support-card">
        <div class="support-icon">🙋</div>
        <div class="support-text">
          <div class="support-title">没找到答案？</div>
          <div class="support-sub">通过反馈功能联系我们，我们会尽快回复</div>
        </div>
      </div>
    </div>

    <ActionButtons />
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import ActionButtons from '../components/ActionButtons.vue'

/**
 * HTML 安全过滤：仅保留安全的HTML标签和属性，防止XSS注入
 * 注意：当前帮助页面的 sections 数据均为硬编码静态内容，无用户输入风险。
 * 此函数作为防御性编程的最佳实践，防止未来数据源变更时引入XSS漏洞。
 */
const sanitizeHtml = (html) => {
  if (!html) return ''
  return html
    .replace(/<(?!\/?(strong|em|code|br|div|span|p|h[1-6]|ul|ol|li|a|img|table|tr|td|th|b|i)\b)[^>]*>/gi, '')
    .replace(/ on\w+\s*=\s*"[^"]*"/gi, '')
    .replace(/ on\w+\s*=\s*'[^']*'/gi, '')
    .replace(/javascript\s*:/gi, '')
    .replace(/<script[\s\S]*?<\/script>/gi, '')
    .replace(/<iframe[\s\S]*?<\/iframe>/gi, '')
    .replace(/<object[\s\S]*?<\/object>/gi, '')
    .replace(/<embed[^>]*>/gi, '')
}

const sections = [
  {
    key: 'game',
    label: '游戏相关',
    icon: '🎮',
    bg: 'linear-gradient(135deg, rgba(102,126,234,0.15), rgba(118,75,162,0.15))',
    items: [
      {
        title: '<strong style="color:#f5222d">【必看！】</strong> 如何彻底关闭 Windows 防火墙？',
        content: `1. 按 <code>Win+R</code> 打开运行，输入 <code>control</code> 打开控制面板<br>
2. 点击 <strong>系统和安全</strong><br>
3. 点击 <strong>Windows Defender 防火墙</strong><br>
4. 点击左侧 <strong>启用或关闭 Windows Defender 防火墙</strong><br>
5. 在专用网络和公用网络位置下都选择 <strong>关闭 Windows Defender 防火墙</strong><br>
6. 点击确定保存设置<br><br>
<em>注意：部分游戏必须关闭防火墙才能正常运行，这是正常现象。</em>`
      },
      {
        title: '游戏运行没反应？游戏打开缺少 DLL 或报错？',
        content: `<strong>可能的原因和解决方案：</strong><br><br>
1. <strong>缺少运行库</strong>：下载安装游戏常用运行库 + DirectX 修复增强版<br>
2. <strong>缺少 DirectX 或 DLL 文件</strong>：下载安装 DirectX 修复工具<br>
3. <strong>缺少 .NET Framework</strong>：安装 .NET Framework 4.5 或更高版本<br>
4. <strong>Windows 防火墙拦截</strong>：彻底关闭 Windows 防火墙<br>
5. <strong>杀毒软件删除文件</strong>：将游戏目录添加到杀毒软件白名单<br>
6. <strong>路径问题</strong>：游戏路径不要包含中文，使用英文路径<br>
7. <strong>管理员权限</strong>：右键游戏主程序选择「以管理员身份运行」<br><br>
如果以上都解决不了，建议重装纯净的 Windows 系统。`
      },
      {
        title: 'GOG 游戏怎么安装？Steam 版和 GOG 版怎么选？',
        content: `<strong>① GOG 游戏怎么安装？</strong><br>
解压完 GOG 游戏后，先安装游戏本体，再依次安装 DLC。本体未安装时 DLC 无法安装，全部安装完成后即可运行。<br><br>
<strong>② Steam 版和 GOG 版怎么选？</strong><br>
<strong>Steam 版特点：</strong>解压即玩，无需安装。但使用了学习补丁，可能会被杀毒软件报毒。<br>
<strong>GOG 版特点：</strong>无加密，无需学习补丁即可运行，更干净。<br><br>
<strong>建议：</strong>优先 GOG（干净无加密），需要快速体验可选 Steam（解压即玩）。`
      }
    ]
  },
  {
    key: 'unzip',
    label: '解压相关',
    icon: '📦',
    bg: 'linear-gradient(135deg, rgba(67,233,123,0.15), rgba(56,249,215,0.15))',
    items: [
      {
        title: '解压密码是多少？',
        content: `游戏资源的解压密码默认为：<br><br>
<div style="display:inline-flex;align-items:center;gap:10px;background:linear-gradient(135deg,rgba(102,126,234,0.12),rgba(118,75,162,0.12));padding:12px 24px;border-radius:12px;margin:8px 0;border:1px solid rgba(102,126,234,0.2);">
  <span style="font-size:20px;">🔑</span>
  <code style="font-size:18px;font-weight:700;color:#667eea;letter-spacing:2px;">XDGAME</code>
</div>
<br>（纯大写字母，复制时注意不要多复制空格）`
      }
    ]
  },
  {
    key: 'download',
    label: '下载相关',
    icon: '⬇️',
    bg: 'linear-gradient(135deg, rgba(250,112,154,0.15), rgba(254,225,64,0.15))',
    items: [
      {
        title: '如何下载资源？',
        content: '点击资源卡片或资源标题进入详情页，在详情页可以看到下载按钮，选择任意网盘链接即可下载。'
      },
      {
        title: '为什么我无法下载？',
        content: `可能的原因：<br>
1. 达到每日下载上限（次日凌晨自动重置）<br>
2. IP 被限制（请勿频繁下载，合理使用）`
      }
    ]
  },
  {
    key: 'browser',
    label: '浏览器兼容',
    icon: '🌐',
    bg: 'linear-gradient(135deg, rgba(79,172,254,0.15), rgba(0,242,254,0.15))',
    items: [
      {
        title: '支持哪些浏览器？',
        content: `建议使用以下现代浏览器访问：<br><br>
<div style="display:flex;gap:12px;flex-wrap:wrap;margin-top:4px;">
  <span style="background:#f5f7fa;padding:6px 14px;border-radius:8px;font-size:13px;">🟡 Chrome 最新版</span>
  <span style="background:#f5f7fa;padding:6px 14px;border-radius:8px;font-size:13px;">🔵 Edge 最新版</span>
  <span style="background:#f5f7fa;padding:6px 14px;border-radius:8px;font-size:13px;">🟠 Firefox 最新版</span>
</div>`
      },
      {
        title: '移动端访问注意事项',
        content: '移动端推荐使用系统自带浏览器或 Chrome 浏览器访问，部分第三方浏览器可能存在兼容性问题。'
      }
    ]
  },
  {
    key: 'account',
    label: '账号限制',
    icon: '👤',
    bg: 'linear-gradient(135deg, rgba(118,75,162,0.15), rgba(102,126,234,0.15))',
    items: [
      {
        title: '下载次数说明',
        content: '每个账号每天有固定的下载次数限制，次日凌晨会自动重置下载次数，请合理安排下载计划。'
      },
      {
        title: 'IP 限制说明',
        content: '为了防止滥用，同一 IP 地址短时间内的下载次数也会受到限制，请合理下载使用，避免频繁操作。'
      },
      {
        title: '关于 VPN 网络访问',
        content: `<strong style="color:#f5222d;">请不要使用 VPN 网络访问本站！</strong><br><br>
使用 VPN 可能导致无法正常访问站点，出现异常情况或连接错误。建议使用正常网络环境访问，以确保最佳的访问体验和功能正常使用。`
      }
    ]
  }
]

const openSections = reactive({
  game: true,
  unzip: false,
  download: false,
  browser: false,
  account: false
})

const toggleSection = (key) => {
  openSections[key] = !openSections[key]
}

const scrollToSection = (key) => {
  openSections[key] = true
  setTimeout(() => {
    const el = document.getElementById(`section-${key}`)
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, 100)
}
</script>

<style scoped>
.help-page {
  min-height: 100vh;
  background: var(--bg-primary, #f0f2f5);
  padding-bottom: 80px;
}

/* 导航栏 */
.help-nav {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 24px 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  padding: 6px 12px;
  border-radius: 8px;
}

.back-link:hover {
  background: rgba(102,126,234,0.1);
  color: #764ba2;
}

/* 英雄区 */
.help-hero {
  position: relative;
  text-align: center;
  padding: 60px 24px 50px;
  overflow: hidden;
}

.hero-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.c1 {
  width: 300px; height: 300px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: -100px; right: -50px;
  animation: float1 8s ease-in-out infinite;
}

.c2 {
  width: 200px; height: 200px;
  background: linear-gradient(135deg, #43e97b, #38f9d7);
  bottom: -80px; left: -30px;
  animation: float2 6s ease-in-out infinite;
}

.c3 {
  width: 120px; height: 120px;
  background: linear-gradient(135deg, #fa709a, #fee140);
  top: 40%; left: 20%;
  animation: float1 10s ease-in-out infinite reverse;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(102,126,234,0.08) 1px, transparent 1px);
  background-size: 32px 32px;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-20px, -30px) rotate(15deg); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(20px, -20px) rotate(-10deg); }
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(102,126,234,0.12);
  color: #667eea;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 16px;
}

.badge-dot {
  width: 6px; height: 6px;
  background: #667eea;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.hero-title {
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: 800;
  color: var(--text-primary, #1a1a2e);
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  letter-spacing: -0.02em;
}

.title-icon { font-size: 0.9em; }

.hero-sub {
  font-size: 1.1rem;
  color: var(--text-secondary, #606266);
  margin: 0 0 28px;
}

/* 快捷入口 */
.quick-nav {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-btn {
  padding: 8px 18px;
  border-radius: 20px;
  border: 1.5px solid rgba(102,126,234,0.3);
  background: rgba(102,126,234,0.08);
  color: #667eea;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102,126,234,0.3);
}

/* 帮助内容 */
.help-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.help-section {
  background: var(--card-bg, white);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: 1px solid var(--border-color, #f0f0f0);
  transition: box-shadow 0.3s;
}

.help-section:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  cursor: pointer;
  transition: background 0.2s;
}

.section-header:hover {
  background: var(--bg-tertiary, rgba(102,126,234,0.04));
}

.section-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.section-icon-wrap {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-emoji { font-size: 22px; }

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #303133);
}

.section-count {
  font-size: 12px;
  color: var(--text-tertiary, #c0c4cc);
  margin-top: 2px;
}

.chevron {
  color: var(--text-tertiary, #c0c4cc);
  transition: transform 0.3s;
}

.chevron.rotated {
  transform: rotate(180deg);
}

/* 展开动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-8px);
}

.expand-enter-to,
.expand-leave-from {
  max-height: 3000px;
  opacity: 1;
  transform: translateY(0);
}

/* 内容区 */
.section-content {
  padding: 8px 24px 24px;
  border-top: 1px solid var(--border-color, #f0f0f0);
}

.help-item {
  margin-top: 16px;
  padding: 20px;
  background: var(--bg-tertiary, #f8f9fa);
  border-radius: 12px;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}

.help-item:hover {
  border-left-color: #667eea;
  background: rgba(102,126,234,0.04);
}

.item-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}

.item-q-icon {
  flex-shrink: 0;
  width: 24px; height: 24px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary, #303133);
  line-height: 1.5;
  flex: 1;
}

.item-content {
  font-size: 14px;
  color: var(--text-secondary, #606266);
  line-height: 1.8;
  padding-left: 34px;
}

.item-content :deep(code) {
  background: rgba(102,126,234,0.1);
  color: #667eea;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.item-content :deep(strong) {
  color: var(--text-primary, #303133);
  font-weight: 600;
}

/* 联系支持 */
.support-section {
  max-width: 900px;
  margin: 24px auto 0;
  padding: 0 24px;
}

.support-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(102,126,234,0.3);
}

.support-icon { font-size: 36px; }

.support-title {
  font-size: 18px;
  font-weight: 700;
  color: white;
  margin-bottom: 4px;
}

.support-sub {
  font-size: 14px;
  color: rgba(255,255,255,0.8);
}

@media (max-width: 768px) {
  .help-hero { padding: 40px 16px 30px; }
  .hero-title { font-size: 2rem; }
  .help-container { padding: 0 16px; }
  .section-header { padding: 16px 20px; }
  .section-content { padding: 8px 16px 16px; }
  .help-item { padding: 14px; }
  .item-content { padding-left: 0; }
  .support-card { flex-direction: column; text-align: center; }
  .quick-nav { gap: 8px; }
  .quick-btn { font-size: 12px; padding: 6px 14px; }
}
</style>
