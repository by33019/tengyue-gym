import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/pages/LoginPage.vue')
    },
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      children: [
        { path: '', name: 'Home', component: () => import('@/pages/HomePage.vue') },
        { path: 'checkin', name: 'Checkin', component: () => import('@/pages/CheckinPage.vue') },
        { path: 'checkin/calendar', name: 'Calendar', component: () => import('@/pages/CalendarPage.vue') },
        { path: 'plan', name: 'Plan', component: () => import('@/pages/PlanPage.vue') },
        { path: 'plan/create', name: 'PlanCreate', component: () => import('@/pages/PlanCreatePage.vue') },
        { path: 'plan/:id', name: 'PlanDetail', component: () => import('@/pages/PlanDetailPage.vue') },
        { path: 'stats', name: 'Stats', component: () => import('@/pages/StatsPage.vue') },
        { path: 'ranking', name: 'Ranking', component: () => import('@/pages/RankingPage.vue') },
        { path: 'community', name: 'Community', component: () => import('@/pages/CommunityPage.vue') },
        { path: 'ai', name: 'Ai', component: () => import('@/pages/AiPage.vue') },
        { path: 'profile', name: 'Profile', component: () => import('@/pages/ProfilePage.vue') },
        { path: 'admin', name: 'Admin', component: () => import('@/pages/AdminPage.vue') }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const role = userStore.userInfo?.role ?? 0

  // 未登录 → 登录页
  if (to.path !== '/login' && !userStore.token) return next('/login')

  // 已登录访问登录页 → 首页/工作台
  if (to.path === '/login' && userStore.token) return next(role >= 1 ? '/admin' : '/')

  // 首页分流：教练/管理员 → 工作台
  if (to.path === '/' && role >= 1) return next('/admin')

  // stats, ranking, community 仅学员
  if (['/stats', '/ranking', '/community'].some(r => to.path.startsWith(r)) && role !== 0)
    return next('/')

  // AI 仅学员和教练
  if (to.path.startsWith('/ai') && role === 2) return next('/admin')

  // Admin 仅教练和管理员
  if (to.path.startsWith('/admin') && role === 0) return next('/')

  next()
})

export default router
