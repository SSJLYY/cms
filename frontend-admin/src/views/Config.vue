<template>
  <div class="config-container">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="basicConfig" label-width="150px">
            <el-form-item label="网站名称">
              <el-input v-model="basicConfig['site.name']" />
            </el-form-item>
            <el-form-item label="网站标题">
              <el-input v-model="basicConfig['site.title']" />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input v-model="basicConfig['site.description']" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="网站关键词">
              <el-input v-model="basicConfig['site.keywords']" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="basicConfig['site.icp']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('basic')">保存</el-button>
              <el-button @click="handleReset('basic')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="SEO设置" name="seo">
          <el-form :model="seoConfig" label-width="150px">
            <el-form-item label="SEO标题">
              <el-input v-model="seoConfig['seo.title']" />
            </el-form-item>
            <el-form-item label="SEO描述">
              <el-input v-model="seoConfig['seo.description']" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="SEO关键词">
              <el-input v-model="seoConfig['seo.keywords']" />
            </el-form-item>
            <el-form-item label="百度站长Token">
              <el-input v-model="seoConfig['seo.baidu.token']" />
            </el-form-item>
            <el-form-item label="必应站长Key">
              <el-input v-model="seoConfig['seo.bing.key']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('seo')">保存</el-button>
              <el-button @click="handleReset('seo')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="存储设置" name="storage">
          <el-form :model="storageConfig" label-width="150px">
            <el-form-item label="存储类型">
              <el-select v-model="storageConfig['storage.type']">
                <el-option label="本地存储" value="local" />
                <el-option label="阿里云OSS" value="oss" />
                <el-option label="腾讯云COS" value="cos" />
                <el-option label="七牛云" value="qiniu" />
              </el-select>
            </el-form-item>
            <el-form-item label="本地存储路径">
              <el-input v-model="storageConfig['storage.local.path']" />
            </el-form-item>
            <el-form-item label="OSS Endpoint">
              <el-input v-model="storageConfig['storage.oss.endpoint']" />
            </el-form-item>
            <el-form-item label="OSS AccessKey">
              <el-input v-model="storageConfig['storage.oss.accessKey']" />
            </el-form-item>
            <el-form-item label="OSS SecretKey">
              <el-input v-model="storageConfig['storage.oss.secretKey']" type="password" show-password />
            </el-form-item>
            <el-form-item label="OSS Bucket">
              <el-input v-model="storageConfig['storage.oss.bucket']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('storage')">保存</el-button>
              <el-button @click="handleTest('storage')">测试连接</el-button>
              <el-button @click="handleReset('storage')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮件设置" name="email">
          <el-form :model="emailConfig" label-width="150px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailConfig['email.smtp.host']" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailConfig['email.smtp.port']" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailConfig['email.from']" />
            </el-form-item>
            <el-form-item label="发件人名称">
              <el-input v-model="emailConfig['email.from.name']" />
            </el-form-item>
            <el-form-item label="邮箱用户名">
              <el-input v-model="emailConfig['email.username']" />
            </el-form-item>
            <el-form-item label="邮箱密码">
              <el-input v-model="emailConfig['email.password']" type="password" show-password />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="emailConfig['email.ssl.enable']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('email')">保存</el-button>
              <el-button @click="handleTest('email')">测试连接</el-button>
              <el-button @click="handleReset('email')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securityConfig" label-width="150px">
            <el-form-item label="JWT密钥">
              <el-input v-model="securityConfig['jwt.secret']" type="password" show-password />
            </el-form-item>
            <el-form-item label="JWT过期时间(小时)">
              <el-input-number v-model="securityConfig['jwt.expiration']" :min="1" :max="720" />
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityConfig['password.min.length']" :min="6" :max="20" />
            </el-form-item>
            <el-form-item label="登录失败锁定次数">
              <el-input-number v-model="securityConfig['login.max.attempts']" :min="3" :max="10" />
            </el-form-item>
            <el-form-item label="启用验证码">
              <el-switch v-model="securityConfig['captcha.enable']" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave('security')">保存</el-button>
              <el-button @click="handleReset('security')">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/index'

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

const loadConfigs = async (category) => {
  try {
    const { data } = await request({
      url: `/api/config/category/${category}`,
      method: 'get'
    })
    const configs = data
    const configObj = configMap[category]
    
    // 清空现有配置
    Object.keys(configObj).forEach(key => delete configObj[key])
    
    // 填充新配置
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
      data: configs
    })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleReset = async (category) => {
  try {
    const configs = configMap[category]
    for (const key of Object.keys(configs)) {
      await request({
        url: `/api/config/${key}/reset`,
        method: 'post'
      })
    }
    ElMessage.success('重置成功')
    loadConfigs(category)
  } catch (error) {
    ElMessage.error('重置失败')
  }
}

const handleTest = async (type) => {
  try {
    const configs = type === 'email' ? emailConfig : storageConfig
    const endpoint = type === 'email' ? '/api/config/test/email' : '/api/config/test/storage'
    const { data } = await request({
      url: endpoint,
      method: 'post',
      data: configs
    })
    
    if (data) {
      ElMessage.success('测试成功')
    } else {
      ElMessage.error('测试失败')
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

onMounted(() => {
  loadConfigs('basic')
})
</script>

<style scoped>
.config-container {
  padding: 20px;
}

.el-form {
  max-width: 800px;
}
</style>
