import { createRouter, createWebHistory } from 'vue-router'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
