/**
 * 管理后台应用入口文件
 * 初始化Vue应用，配置全局插件和路由
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 创建Vue应用实例
const app = createApp(App)

// 配置状态管理
app.use(createPinia())

// 配置UI组件库
app.use(ElementPlus)

// 配置路由
app.use(router)

// 挂载应用到DOM
app.mount('#app')
