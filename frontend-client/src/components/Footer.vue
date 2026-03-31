<!--
  网站页脚组件
  显示版权信息、运行统计、访问统计和性能指标
-->
<template>
  <footer class="site-footer">
    <div class="footer-wave">
      <svg viewBox="0 0 1440 100" preserveAspectRatio="none">
        <path d="M0,50 C360,100 720,0 1080,50 C1260,75 1350,75 1440,50 L1440,100 L0,100 Z" fill="var(--bg-primary)"></path>
      </svg>
    </div>
    <div class="footer-content">
      <!-- 主要信息 -->
      <div class="footer-main">
        <div class="footer-brand">
          <div class="brand-icon">📦</div>
          <div class="brand-info">
            <span class="brand-name">shaun・个人备份库</span>
            <span class="brand-slogan">资源共享 · 互利共赢</span>
          </div>
        </div>
        
        <div class="footer-links">
          <a href="/" class="footer-link">
            <i class="fas fa-home"></i>
            <span>首页</span>
          </a>
          <span class="link-separator">|</span>
          <a href="/help" class="footer-link">
            <i class="fas fa-question-circle"></i>
            <span>帮助</span>
          </a>
          <span class="link-separator">|</span>
          <a href="/disclaimer" class="footer-link">
            <i class="fas fa-file-alt"></i>
            <span>免责</span>
          </a>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="footer-stats">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-calendar-check"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ runningDays }}</span>
            <span class="stat-label">运行天数</span>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-eye"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(visitCount) }}</span>
            <span class="stat-label">总访问量</span>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-users"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(visitorCount) }}</span>
            <span class="stat-label">访客人数</span>
          </div>
        </div>
        
        <div class="stat-card highlight">
          <div class="stat-icon">
            <i class="fas fa-chart-line"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ todayVisit }}</span>
            <span class="stat-label">今日访问</span>
          </div>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="footer-divider"></div>

      <!-- 版权和性能信息 -->
      <div class="footer-bottom">
        <div class="copyright-section">
          <p class="copyright">
            © {{ currentYear }} Shaun.Sheng · 
            <a href="https://shaun.top" target="_blank" class="link">个人简介</a>
          </p>
          <p class="description">仅供个人备学习，请勿用于商业用途</p>
        </div>
        
        <div class="performance-section">
          <div class="performance-item">
            <i class="fas fa-stopwatch"></i>
            <span>加载 {{ pageLoadTime }}ms</span>
          </div>
          <div class="performance-item">
            <i class="fas fa-database"></i>
            <span>查询 {{ queryCount }} 次</span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'

const buildDate = ref('2025年08月27日')
const runningDays = ref(0)
const visitCount = ref(0)
const visitorCount = ref(0)
const todayVisit = ref(0)
const pageLoadTime = ref(0)
const queryCount = ref(0)
const queryTime = ref('0.0000')

const currentYear = computed(() => new Date().getFullYear())

const pageStartTime = performance.now()

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

onMounted(() => {
  const startDate = new Date('2025-08-27')
  const today = new Date()
  const diffTime = Math.abs(today - startDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  runningDays.value = diffDays
  
  // TODO: 接入后端统计 API 获取真实数据，当前为占位数据
  visitCount.value = 0
  visitorCount.value = 0
  todayVisit.value = 0
  
  const loadTime = performance.now() - pageStartTime
  pageLoadTime.value = loadTime.toFixed(2)
  
  const resources = performance.getEntriesByType('resource')
  queryCount.value = resources.length
  
  let totalDuration = 0
  resources.forEach(resource => {
    totalDuration += resource.duration
  })
  queryTime.value = (totalDuration / 1000).toFixed(4)
})
</script>

<style scoped>
.site-footer {
  background: var(--footer-bg, var(--card-bg));
  border-top: 1px solid var(--footer-border, var(--border-color));
  padding: 60px 0 40px;
  margin-top: 80px;
  position: relative;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.footer-wave {
  position: absolute;
  top: -50px;
  left: 0;
  right: 0;
  height: 50px;
  overflow: hidden;
}

.footer-wave svg {
  width: 100%;
  height: 100%;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
  margin-bottom: 40px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 16px;
}

.brand-icon {
  font-size: 2.5rem;
}

.brand-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
}

.brand-slogan {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.9rem;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.footer-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.link-separator {
  color: var(--border-color);
}

.footer-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 40px;
}

.stat-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card.highlight {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: rgba(102, 126, 234, 0.2);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.2rem;
}

.stat-card.highlight .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

.footer-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color), transparent);
  margin-bottom: 24px;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.copyright-section {
  text-align: left;
}

.copyright {
  font-size: 0.9rem;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.description {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin: 0;
}

.performance-section {
  display: flex;
  gap: 20px;
}

.performance-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  color: var(--text-tertiary);
  padding: 6px 12px;
  background: var(--bg-primary);
  border-radius: 6px;
}

.performance-item i {
  color: #667eea;
}

@media (max-width: 1024px) {
  .footer-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .site-footer {
    padding: 40px 0 30px;
    margin-top: 60px;
  }

  .footer-wave {
    display: none;
  }

  .footer-main {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .footer-brand {
    flex-direction: column;
  }

  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
  }

  .footer-stats {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }

  .stat-value {
    font-size: 1.2rem;
  }

  .footer-bottom {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .copyright-section {
    text-align: center;
  }

  .performance-section {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .footer-stats {
    grid-template-columns: 1fr 1fr;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 16px 12px;
  }

  .stat-info {
    align-items: center;
  }
}
</style>
