// 路由配置放这里，页面跳转规则统一管理
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/',        redirect: '/dashboard' },
  { path: '/login',   component: () => import('@/views/Login.vue'),   meta: { public: true } },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      { path: 'dashboard',  component: () => import('@/views/Dashboard.vue'),          meta: { title: '首页总览' } },
      { path: 'health',     component: () => import('@/views/health/HealthRecord.vue'),meta: { title: '健康档案' } },
      { path: 'exercise',   component: () => import('@/views/exercise/Exercise.vue'),  meta: { title: '运动管理' } },
      { path: 'diet',       component: () => import('@/views/diet/Diet.vue'),          meta: { title: '饮食记录' } },
      { path: 'charts',     component: () => import('@/views/statistics/Charts.vue'),  meta: { title: '数据图表' } },
      { path: 'ai',        component: () => import('@/views/ai/AiAssistant.vue'),   meta: { title: 'AI 健康助手' } },
      { path: 'admin/users',    component: () => import('@/views/admin/UserManage.vue'),  meta: { title: '用户管理', admin: true } },
      { path: 'admin/dict',     component: () => import('@/views/admin/DictManage.vue'),  meta: { title: '字典管理', admin: true } },
    ]
  },
  { path: '/403', component: () => import('@/views/403.vue'), meta: { public: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const store = useUserStore()
  if (!to.meta.public && !store.isLoggedIn) return '/login'
  // 这里判断一下边界，避免页面逻辑走偏
  if (to.meta.admin && !store.isAdmin) return '/403'
})

export default router
