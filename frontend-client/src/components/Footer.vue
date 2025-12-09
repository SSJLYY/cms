<!--
  网站页脚组件
  显示版权信息、运行统计、访问统计和性能指标
-->
<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-info">
        <!-- 版权信息 -->
        <p class="copyright">© 2025 Shaun.Sheng · <a href="https://shaun.top" target="_blank" class="link">个人简介</a></p>
        
        <!-- 使用声明 -->
        <p class="description">仅供个人备学习，请勿用于商业用途</p>
        
        <!-- 建站统计 -->
        <p class="stats">
          建站时间: {{ buildDate }} | 已运行: {{ runningDays }}天
        </p>
        
        <!-- 访问统计 -->
        <p class="visit-stats">
          <span class="stat-item">访问统计: {{ visitCount }}次</span>
          <span class="stat-separator">|</span>
          <span class="stat-item">访客人数: {{ visitorCount }}人</span>
          <span class="stat-separator">|</span>
          <span class="stat-item">今日访问: {{ todayVisit }}次</span>
        </p>
        
        <!-- 性能统计 -->
        <p class="performance-stats">
          页面加载: {{ pageLoadTime }}ms | 查询 {{ queryCount }} 次，耗时 {{ queryTime }} 秒
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 响应式数据
const buildDate = ref('2025年08月27日')  // 建站日期
const runningDays = ref(99)              // 运行天数
const visitCount = ref(0)                // 总访问次数
const visitorCount = ref(0)              // 访客人数
const todayVisit = ref(0)                // 今日访问次数
const pageLoadTime = ref(0)              // 页面加载时间
const queryCount = ref(0)                // 查询次数
const queryTime = ref('0.0000')          // 查询耗时

// 记录页面开始加载时间
const pageStartTime = performance.now()

/**
 * 组件挂载时初始化统计数据
 */
onMounted(() => {
  // 计算网站运行天数
  const startDate = new Date('2025-08-27')
  const today = new Date()
  const diffTime = Math.abs(today - startDate)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  runningDays.value = diffDays
  
  // 模拟访问统计数据（实际项目中应该从后端API获取）
  visitCount.value = 1024
  visitorCount.value = 256
  todayVisit.value = 42
  
  // 计算真实的页面加载时间
  const loadTime = performance.now() - pageStartTime
  pageLoadTime.value = loadTime.toFixed(2)
  
  // 从 Performance API 获取资源加载信息
  const resources = performance.getEntriesByType('resource')
  queryCount.value = resources.length
  
  // 计算所有资源加载总耗时
  let totalDuration = 0
  resources.forEach(resource => {
    totalDuration += resource.duration
  })
  queryTime.value = (totalDuration / 1000).toFixed(4)
})
</script>

<style scoped>
.site-footer {
  background-color: var(--footer-bg, var(--card-bg));
  border-top: 1px solid var(--footer-border, var(--border-color));
  padding: 30px 20px;
  margin-top: 60px;
  text-align: center;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-info {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.8;
}

.footer-info p {
  margin: 8px 0;
}

.copyright {
  font-size: 15px;
  color: var(--text-primary);
}

.link {
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s;
}

.link:hover {
  color: #2563eb;
  text-decoration: underline;
}

.description {
  color: var(--text-tertiary);
  font-size: 13px;
}

.stats {
  color: var(--text-secondary);
  font-size: 13px;
}

.visit-stats {
  color: #3b82f6;
  font-size: 13px;
}

.performance-stats {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 4px;
}

.stat-item {
  display: inline-block;
}

.stat-separator {
  margin: 0 8px;
  color: #d1d5db;
}

@media (max-width: 768px) {
  .site-footer {
    padding: 20px 15px;
    margin-top: 40px;
  }

  .footer-info {
    font-size: 12px;
  }

  .visit-stats {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .stat-separator {
    display: none;
  }
}
</style>
