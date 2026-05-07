<template>
  <div class="config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
          系统配置
        </h2>
        <p class="subtitle">管理网站各项系统参数和设置</p>
      </div>
    </div>

    <!-- 配置卡片 -->
    <div class="config-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="modern-tabs">
        <el-tab-pane name="basic">
          <template #label>
            <span class="tab-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 20h9"/>
                <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>
              </svg>
              基本设置
            </span>
          </template>
          <el-form :model="basicConfig" label-width="150px" class="config-form">
            <el-form-item label="网站名称">
              <el-input v-model="basicConfig['site.name']" placeholder="请输入网站名称" />
            </el-form-item>
            <el-form-item label="网站标题">
              <el-input v-model="basicConfig['site.title']" placeholder="请输入网站标题" />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input v-model="basicConfig['site.description']" type="textarea" :rows="3" placeholder="请输入网站描述" />
            </el-form-item>
            <el-form-item label="网站关键词">
              <el-input v-model="basicConfig['site.keywords']" placeholder="请输入网站关键词，逗号分隔" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="basicConfig['site.icp']" placeholder="请输入ICP备案号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('basic')" class="save-btn">保存设置</el-button>
              <el-button @click="handleReset('basic')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="seo">
          <template #label>
            <span class="tab-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              SEO设置
            </span>
          </template>
          <el-form :model="seoConfig" label-width="150px" class="config-form">
            <el-form-item label="SEO标题">
              <el-input v-model="seoConfig['seo.title']" placeholder="请输入SEO标题" />
            </el-form-item>
            <el-form-item label="SEO描述">
              <el-input v-model="seoConfig['seo.description']" type="textarea" :rows="3" placeholder="请输入SEO描述" />
            </el-form-item>
            <el-form-item label="SEO关键词">
              <el-input v-model="seoConfig['seo.keywords']" placeholder="请输入SEO关键词，逗号分隔" />
            </el-form-item>
            <el-divider content-position="left">搜索引擎验证</el-divider>
            <el-form-item label="百度站长Token">
              <el-input v-model="seoConfig['seo.baidu.token']" placeholder="请输入百度站长Token" />
            </el-form-item>
            <el-form-item label="必应站长Key">
              <el-input v-model="seoConfig['seo.bing.key']" placeholder="请输入必应站长Key" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('seo')" class="save-btn">保存设置</el-button>
              <el-button @click="handleReset('seo')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="storage">
          <template #label>
            <span class="tab-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
              </svg>
              存储设置
            </span>
          </template>
          <el-form :model="storageConfig" label-width="150px" class="config-form">
            <el-form-item label="存储类型">
              <el-select v-model="storageConfig['storage.type']" class="full-width">
                <el-option label="本地存储" value="local" />
                <el-option label="阿里云OSS" value="oss" />
                <el-option label="腾讯云COS" value="cos" />
                <el-option label="七牛云" value="qiniu" />
              </el-select>
            </el-form-item>
            <el-form-item label="本地存储路径">
              <el-input v-model="storageConfig['storage.local.path']" placeholder="/data/uploads" />
            </el-form-item>
            <el-divider content-position="left">OSS配置</el-divider>
            <el-form-item label="OSS Endpoint">
              <el-input v-model="storageConfig['storage.oss.endpoint']" placeholder="oss-cn-hangzhou.aliyuncs.com" />
            </el-form-item>
            <el-form-item label="OSS AccessKey">
              <el-input v-model="storageConfig['storage.oss.accessKey']" placeholder="请输入AccessKey" />
            </el-form-item>
            <el-form-item label="OSS SecretKey">
              <el-input v-model="storageConfig['storage.oss.secretKey']" type="password" show-password placeholder="请输入SecretKey" />
            </el-form-item>
            <el-form-item label="OSS Bucket">
              <el-input v-model="storageConfig['storage.oss.bucket']" placeholder="请输入Bucket名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('storage')" class="save-btn">保存设置</el-button>
              <el-button type="success" @click="handleTest('storage')">测试连接</el-button>
              <el-button @click="handleReset('storage')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="email">
          <template #label>
            <span class="tab-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              邮件设置
            </span>
          </template>
          <el-form :model="emailConfig" label-width="150px" class="config-form">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailConfig['email.smtp.host']" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailConfig['email.smtp.port']" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailConfig['email.from']" placeholder="noreply@example.com" />
            </el-form-item>
            <el-form-item label="发件人名称">
              <el-input v-model="emailConfig['email.from.name']" placeholder="资源下载平台" />
            </el-form-item>
            <el-form-item label="邮箱用户名">
              <el-input v-model="emailConfig['email.username']" placeholder="请输入邮箱用户名" />
            </el-form-item>
            <el-form-item label="邮箱密码">
              <el-input v-model="emailConfig['email.password']" type="password" show-password placeholder="请输入邮箱密码" />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="emailConfig['email.ssl.enable']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('email')" class="save-btn">保存设置</el-button>
              <el-button type="success" @click="handleTest('email')">测试连接</el-button>
              <el-button @click="handleReset('email')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="security">
          <template #label>
            <span class="tab-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              安全设置
            </span>
          </template>
          <el-form :model="securityConfig" label-width="150px" class="config-form">
            <el-form-item label="JWT密钥">
              <el-input v-model="securityConfig['jwt.secret']" type="password" show-password placeholder="请输入JWT密钥" />
            </el-form-item>
            <el-form-item label="JWT过期时间">
              <el-input-number v-model="securityConfig['jwt.expiration']" :min="1" :max="720" />
              <span class="form-tip">单位：小时</span>
            </el-form-item>
            <el-divider content-position="left">密码策略</el-divider>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityConfig['password.min.length']" :min="6" :max="20" />
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-input-number v-model="securityConfig['login.max.attempts']" :min="3" :max="10" />
              <span class="form-tip">次后锁定</span>
            </el-form-item>
            <el-form-item label="启用验证码">
              <el-switch />
              <span class="form-tip">登录时显示验证码</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('security')" class="save-btn">保存设置</el-button>
              <el-button @click="handleReset('security')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const activeTab = ref('basic')
const basicConfig = reactive({})
const seoConfig = reactive({})
const storageConfig = reactive({})
const emailConfig = reactive({})
const securityConfig = reactive({})

const configMap = {
  basic: basicConfig,
  seo: seoConfig,
  storage: storageConfig,
  email: emailConfig,
  security: securityConfig
}

const buildEmailTestPayload = () => ({
  host: emailConfig['email.smtp.host'] || '',
  port: String(emailConfig['email.smtp.port'] || ''),
  username: emailConfig['email.username'] || '',
  password: emailConfig['email.password'] || '',
  from: emailConfig['email.from'] || '',
  fromName: emailConfig['email.from.name'] || '',
  sslEnable: String(Boolean(emailConfig['email.ssl.enable']))
})

const buildStorageTestPayload = () => ({
  type: storageConfig['storage.type'] || '',
  path: storageConfig['storage.local.path'] || '',
  endpoint: storageConfig['storage.oss.endpoint'] || '',
  accessKey: storageConfig['storage.oss.accessKey'] || '',
  secretKey: storageConfig['storage.oss.secretKey'] || '',
  bucketName: storageConfig['storage.oss.bucket'] || ''
})

const loadConfigs = async (category) => {
  try {
    const { data } = await request({
      url: `/api/config/category/${category}`,
      method: 'get',
      skipBusinessErrorMessage: true
    })
    const configs = data
    const configObj = configMap[category]
    
    Object.keys(configObj).forEach(key => delete configObj[key])
    
    configs.forEach(config => {
      configObj[config.configKey] = config.configValue
    })
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const handleTabChange = (tabName) => {
  loadConfigs(tabName)
}

const handleSave = async (category) => {
  try {
    const configs = configMap[category]
    await request({
      url: '/api/config/batch',
      method: 'put',
      data: configs,
      skipBusinessErrorMessage: true
    })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

const handleReset = async (category) => {
  try {
    const configs = configMap[category]
    for (const key of Object.keys(configs)) {
      await request({
        url: `/api/config/${key}/reset`,
        method: 'post',
        skipBusinessErrorMessage: true
      })
    }
    ElMessage.success('重置成功')
    loadConfigs(category)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '重置失败')
  }
}

const handleTest = async (type) => {
  try {
    const configs = type === 'email' ? buildEmailTestPayload() : buildStorageTestPayload()
    const endpoint = type === 'email' ? '/api/config/test/email' : '/api/config/test/storage'
    const { data } = await request({
      url: endpoint,
      method: 'post',
      data: configs,
      skipBusinessErrorMessage: true
    })
    
    if (data) {
      ElMessage.success('测试成功')
    } else {
      ElMessage.error('测试失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '测试失败')
  }
}

onMounted(() => {
  loadConfigs('basic')
})
</script>

<style scoped>
.config-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
}

.header-content h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.config-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.modern-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.modern-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.modern-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  height: 50px;
  line-height: 50px;
}

.modern-tabs :deep(.el-tabs__item.is-active) {
  color: #667eea;
}

.modern-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-form {
  max-width: 700px;
}

.config-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.config-form :deep(.el-input__wrapper),
.config-form :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.full-width {
  width: 100%;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .header-content {
    padding: 20px;
  }
  
  .config-form {
    max-width: 100%;
  }
}
</style>
