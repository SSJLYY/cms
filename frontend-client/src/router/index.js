/**
 * 客户端路由配置
 * 定义用户端的页面路由结构
 */
import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置数组
 * 包含首页、资源详情、免责声明和帮助页面
 */
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/resource/:id',
    name: 'ResourceDetail',
    component: () => import('../views/ResourceDetail.vue')
  },
  {
    path: '/disclaimer',
    name: 'Disclaimer',
    component: () => import('../views/Disclaimer.vue')
  },
  {
    path: '/help',
    name: 'Help',
    component: () => import('../views/Help.vue')
  }
]

/**
 * 创建路由实例
 * 使用HTML5 History模式
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
