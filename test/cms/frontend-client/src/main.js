/**
 * 客户端应用入口文件
 * 初始化Vue应用，配置状态管理和路由
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 创建Vue应用实例
const app = createApp(App)

// 配置状态管理
app.use(createPinia())

// 配置路由
app.use(router)

// 挂载应用到DOM
app.mount('#app')
