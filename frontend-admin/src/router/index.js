import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: '/resources',
        name: 'Resources',
        component: () => import('../views/Resources.vue')
      },
      {
        path: '/categories',
        name: 'Categories',
        component: () => import('../views/Categories.vue')
      },
      {
        path: '/config',
        name: 'Config',
        component: () => import('../views/Config.vue')
      },
      {
        path: '/feedback',
        name: 'Feedback',
        component: () => import('../views/Feedback.vue')
      },
      {
        path: '/logs',
        name: 'Logs',
        component: () => import('../views/Logs.vue')
      },
      {
        path: '/files',
        name: 'Files',
        component: () => import('../views/Files.vue')
      },
      {
        path: '/seo',
        name: 'Seo',
        component: () => import('../views/Seo.vue')
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('../views/Statistics.vue')
      },
      {
        path: '/revenue',
        name: 'Revenue',
        component: () => import('../views/Revenue.vue')
      },
      {
        path: '/promotion',
        name: 'Promotion',
        component: () => import('../views/Promotion.vue')
      },
      {
        path: '/friendlink',
        name: 'FriendLink',
        component: () => import('../views/FriendLink.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
