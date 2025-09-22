import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '../layouts/DefaultLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import DocumentView from '../views/DocumentView.vue'
import SearchView from '../views/SearchView.vue'
import AskView from '../views/AskView.vue'
import VectorDataView from '../views/VectorDataView.vue'
import SystemMonitorView from '../views/SystemMonitorView.vue'
import ProfileView from '../views/ProfileView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      component: DefaultLayout,
      redirect: '/dashboard', // 添加重定向到仪表板
      children: [
        {
          path: '/dashboard',
          name: 'dashboard',
          component: DashboardView
        },
        {
          path: '/documents',
          name: 'documents',
          component: DocumentView
        },
        {
          path: '/search',
          name: 'search',
          component: SearchView
        },
        {
          path: '/ask',
          name: 'ask',
          component: AskView
        },
        {
          path: '/vector-data',
          name: 'vector-data',
          component: VectorDataView
        },
        {
          path: '/system-monitor',
          name: 'system-monitor',
          component: SystemMonitorView
        },
        {
          path: '/profile',
          name: 'profile',
          component: ProfileView
        },
        {
          path: '/health',
          name: 'health',
          component: SystemMonitorView
        }
      ]
    }
  ]
})

// 路由守卫，检查用户是否已登录
router.beforeEach((to, from, next) => {
  console.log('路由守卫执行:', to, from)
  const token = localStorage.getItem('token')
  console.log('当前Token:', token)
  
  // 如果访问登录页且已登录，重定向到首页
  if (to.name === 'login' && token) {
    console.log('已登录用户访问登录页，重定向到仪表板')
    next({ name: 'dashboard' })
    return
  }
  
  // 如果访问其他页面但未登录，重定向到登录页
  if (to.name !== 'login' && !token) {
    console.log('未登录用户访问受保护页面，重定向到登录页')
    next({ name: 'login' })
    return
  }
  
  console.log('路由通过，允许访问:', to.path)
  next()
})

export default router