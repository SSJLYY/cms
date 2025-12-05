<template>
  <div class="disclaimer-page">
    <div class="container">
      <div class="disclaimer-card">
        <h1>免责声明页面</h1>
        <p class="subtitle">请仔细阅读以下条款</p>

        <div class="content">
          <section class="section">
            <div class="section-header">
              <span class="number">1</span>
              <h2>资源用途声明</h2>
            </div>
            <p>本站所有资源均为「个人学习」用途。禁商用盈利违法。仅提供给用户进行个人学习和研究使用，下载后请在24小时内删除，不得用于商业、盈利或其他违法违规场景。</p>
          </section>

          <section class="section">
            <div class="section-header">
              <span class="number">2</span>
              <h2>版权归属声明</h2>
            </div>
            <p>本站资源均来源于互联网及公开渠道，所有软件、模板、工具的版权归属于原作者或互联网公开渠道。本站仅做「资源整理备份」，不拥有任何资源的所有权，也不参与资源的开发、修改或传播。</p>
          </section>

          <section class="section">
            <div class="section-header">
              <span class="number">3</span>
              <h2>禁止商用声明</h2>
            </div>
            <p>严禁将本站下载的资源用于商业活动（如销售、分发、捆绑盈利等），若因商用导致的版权纠纷、法律责任，均由使用者自行承担。本站不承担任何连带责任。</p>
          </section>

          <section class="section">
            <div class="section-header">
              <span class="number">4</span>
              <h2>用户责任声明</h2>
            </div>
            <p>用户在下载和使用本站资源前，应确保符合当地法律法规及相关政策要求（如病毒检测、系统兼容等）。造成的损失、数据丢失、系统故障、数据泄露等）造成的损失，本站不承担任何责任。</p>
          </section>

          <section class="section">
            <div class="section-header">
              <span class="number">5</span>
              <h2>链接有效性声明</h2>
            </div>
            <p>本站资源链接来源于第三方网盘（夸克、迅雷、百度网盘等），因第三方平台政策变动或链接失效，导致的无法下载，本站会尽力更新有效链接，但不保证所有链接长期有效。失效后可通过反馈功能告知，本站会尽快更新。</p>
          </section>

          <section class="section">
            <div class="section-header">
              <span class="number">6</span>
              <h2>免责范围声明</h2>
            </div>
            <p>若因本站资源涉及侵权、违法等问题，请及时通过邮件联系本站，本站将在确认后24小时内删除相关资源。不承担因其他使用者违规使用资源而产生的任何责任。</p>
          </section>

          <div class="warning-box">
            <div class="warning-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
              </svg>
              <strong>重要提示</strong>
            </div>
            <ul>
              <li>使用本站资源即表示您已阅读并同意本免责声明</li>
              <li>本站有权随时修改或删除任何内容，无需事先通知</li>
              <li>本免责声明的最终解释权归本站所有</li>
            </ul>
          </div>

          <div class="contact-section">
            <h3>联系我们</h3>
            <p>如有疑问或需要删除内容，请通过以下方式联系：</p>
            <div class="contact-methods">
              <div class="contact-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                </svg>
                <span>admin@example.com</span>
              </div>
              <div class="contact-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                </svg>
                <span>点击右下角"反馈"按钮</span>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.disclaimer-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

.disclaimer-card {
  background: white;
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

h1 {
  font-size: 32px;
  color: #1a1a1a;
  margin: 0 0 8px;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin: 0 0 40px;
}

.content {
  margin-top: 32px;
}

.section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.section:last-of-type {
  border-bottom: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.section h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.section p {
  margin: 0;
  line-height: 1.8;
  color: #666;
  font-size: 15px;
}

.warning-box {
  background: #fff8e1;
  border: 2px solid #ffe082;
  border-radius: 12px;
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

.warning-box ul {
  margin: 0;
  padding-left: 20px;
}

.warning-box li {
  color: #666;
  line-height: 1.8;
  margin-bottom: 8px;
}

.contact-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  margin-top: 32px;
}

.contact-section h3 {
  margin: 0 0 12px;
  font-size: 18px;
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
  gap: 8px;
  color: #555;
  font-size: 15px;
}

.contact-item svg {
  color: #667eea;
}

.footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #e0e0e0;
}

.back-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .disclaimer-page {
    padding: 20px 10px;
  }

  .disclaimer-card {
    padding: 24px 20px;
  }

  h1 {
    font-size: 24px;
  }

  .section h2 {
    font-size: 18px;
  }

  .number {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}

.section:last-of-type {
  margin-bottom: 32px;
}

h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #667eea;
}

.section > p {
  line-height: 1.8;
  color: #555;
  margin: 0 0 16px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.list-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.text {
  flex: 1;
}

.text strong {
  display: block;
  color: #333;
  font-size: 16px;
  margin-bottom: 8px;
}

.text p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.obligation-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.obligation-list li {
  padding: 12px 0 12px 32px;
  position: relative;
  color: #555;
  line-height: 1.6;
}

.obligation-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #667eea;
  font-weight: bold;
  font-size: 18px;
}

.warning-box {
  background: #fff8e1;
  border: 2px solid #ffe082;
  border-radius: 12px;
  padding: 24px;
}

.warning-box p {
  margin: 0 0 12px;
  color: #333;
}

.warning-box ul {
  margin: 0;
  padding-left: 20px;
}

.warning-box li {
  color: #666;
  line-height: 1.8;
  margin-bottom: 8px;
}

.contact-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-top: 16px;
}

.contact-info p {
  margin: 0 0 12px;
  color: #555;
  font-size: 15px;
}

.contact-info p:last-child {
  margin-bottom: 0;
}

.back-button {
  text-align: center;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid #e0e0e0;
}

.back-button button {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.back-button button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .disclaimer-page {
    padding: 20px 10px;
  }

  .disclaimer-card {
    padding: 24px 20px;
  }

  h1 {
    font-size: 24px;
  }

  h2 {
    font-size: 20px;
  }

  .list-item {
    flex-direction: column;
    gap: 12px;
  }

  .number {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}
</style>
