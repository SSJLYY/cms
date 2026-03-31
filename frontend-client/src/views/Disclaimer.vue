<template>
  <div class="disclaimer-page">
    <!-- 背景装饰 -->
    <div class="page-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="container">
      <!-- 英雄区 -->
      <div class="hero-section">
        <div class="hero-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
          </svg>
        </div>
        <h1 class="hero-title">免责声明</h1>
        <p class="hero-subtitle">请仔细阅读以下条款，使用本站即表示您已同意</p>
      </div>

      <!-- 内容卡片 -->
      <div class="disclaimer-card">
        <div class="content">
          <section class="section">
            <div class="section-header">
              <div class="section-number">1</div>
              <div class="section-info">
                <h2>资源用途声明</h2>
                <p>本站所有资源均为「个人学习」用途。禁商用盈利违法。仅提供给用户进行个人学习和研究使用，下载后请在24小时内删除，不得用于商业、盈利或其他违法违规场景。</p>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <div class="section-number">2</div>
              <div class="section-info">
                <h2>版权归属声明</h2>
                <p>本站资源均来源于互联网及公开渠道，所有软件、模板、工具的版权归属于原作者或互联网公开渠道。本站仅做「资源整理备份」，不拥有任何资源的所有权，也不参与资源的开发、修改或传播。</p>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <div class="section-number">3</div>
              <div class="section-info">
                <h2>禁止商用声明</h2>
                <p>严禁将本站下载的资源用于商业活动（如销售、分发、捆绑盈利等），若因商用导致的版权纠纷、法律责任，均由使用者自行承担。本站不承担任何连带责任。</p>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <div class="section-number">4</div>
              <div class="section-info">
                <h2>用户责任声明</h2>
                <p>用户在下载和使用本站资源前，应确保符合当地法律法规及相关政策要求（如病毒检测、系统兼容等）。因使用本站资源造成的损失、数据丢失、系统故障等后果，由用户自行承担。</p>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <div class="section-number">5</div>
              <div class="section-info">
                <h2>链接有效性声明</h2>
                <p>本站资源链接来源于第三方网盘（夸克、迅雷、百度网盘等），因第三方平台政策变动或链接失效，导致的无法下载本站会尽力更新有效链接，但不保证所有链接长期有效。失效后可通过反馈功能告知。</p>
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <div class="section-number">6</div>
              <div class="section-info">
                <h2>免责范围声明</h2>
                <p>若因本站资源涉及侵权、违法等问题，请及时通过邮件联系本站删除相关资源。本站不承担因其他使用者违规使用资源而产生的任何责任。</p>
              </div>
            </div>
          </section>

          <!-- 重要提示框 -->
          <div class="warning-box">
            <div class="warning-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
              </svg>
              <strong>重要提示</strong>
            </div>
            <ul class="warning-list">
              <li>使用本站资源即表示您已阅读并同意本免责声明</li>
              <li>本站有权随时修改或删除任何内容，无需事先通知</li>
              <li>本免责声明的最终解释权归本站所有</li>
            </ul>
          </div>

          <!-- 联系区域 -->
          <div class="contact-section">
            <h3>联系我们</h3>
            <p>如有疑问或需要删除内容，请通过以下方式联系：</p>
            <div class="contact-methods">
              <div class="contact-item">
                <div class="contact-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                  </svg>
                </div>
                <span>admin@example.com</span>
              </div>
              <div class="contact-item">
                <div class="contact-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                  </svg>
                </div>
                <span>点击右下角「反馈」按钮</span>
              </div>
            </div>
          </div>
        </div>

        <div class="footer">
          <button class="back-btn" @click="goBack">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"/>
              <polyline points="12 19 5 12 12 5"/>
            </svg>
            返回首页
          </button>
        </div>
      </div>
    </div>

    <!-- 右下角操作按钮 -->
    <ActionButtons />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ActionButtons from '../components/ActionButtons.vue'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.disclaimer-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.page-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
  filter: blur(60px);
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: white;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  background: #ffd700;
  bottom: -80px;
  left: -80px;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.container {
  max-width: 820px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 英雄区 */
.hero-section {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInDown 0.6s ease;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  color: white;
  margin: 0 0 12px;
  letter-spacing: -1px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
}

/* 内容卡片 */
.disclaimer-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: fadeInUp 0.6s ease 0.1s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content {
  padding: 40px 48px;
}

.section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #f0f0f0;
}

.section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.section-number {
  width: 40px;
  height: 40px;
  min-width: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.section-info h2 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
}

.section-info p {
  margin: 0;
  font-size: 15px;
  color: #555;
  line-height: 1.8;
}

/* 警告框 */
.warning-box {
  background: linear-gradient(135deg, #fff8e1 0%, #fffde7 100%);
  border: 1px solid #ffe082;
  border-radius: 14px;
  padding: 24px;
  margin: 32px 0;
}

.warning-icon {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #f57c00;
}

.warning-icon strong {
  font-size: 16px;
}

.warning-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.warning-list li {
  color: #555;
  line-height: 1.8;
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}

.warning-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #667eea;
  font-weight: bold;
  font-size: 16px;
}

.warning-list li:last-child {
  margin-bottom: 0;
}

/* 联系区域 */
.contact-section {
  background: #f8f9fa;
  border-radius: 14px;
  padding: 24px;
  margin-top: 32px;
}

.contact-section h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.contact-section > p {
  margin: 0 0 16px;
  color: #666;
  font-size: 14px;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #444;
  font-size: 15px;
}

.contact-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

/* 底部按钮 */
.footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #e0e0e0;
}

.back-btn {
  padding: 14px 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.35);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.45);
}

/* 响应式 */
@media (max-width: 768px) {
  .disclaimer-page {
    padding: 40px 16px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-icon {
    width: 64px;
    height: 64px;
  }

  .content {
    padding: 28px 20px;
  }

  .section-info h2 {
    font-size: 16px;
  }

  .section-info p {
    font-size: 14px;
  }

  .section-number {
    width: 36px;
    height: 36px;
    min-width: 36px;
    font-size: 16px;
  }

  .back-btn {
    width: 100%;
    justify-content: center;
    padding: 14px 24px;
  }
}
</style>
