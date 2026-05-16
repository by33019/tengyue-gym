import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/pages/LoginPage.vue') },
    { path: '/', name: 'Home', component: () => import('@/pages/HomePage.vue') },
    { path: '/checkin', name: 'Checkin', component: () => import('@/pages/CheckinPage.vue') },
    { path: '/checkin/calendar', name: 'Calendar', component: () => import('@/pages/CalendarPage.vue') },
    { path: '/plan', name: 'Plan', component: () => import('@/pages/PlanPage.vue') },
    { path: '/plan/create', name: 'PlanCreate', component: () => import('@/pages/PlanCreatePage.vue') },
    { path: '/plan/:id', name: 'PlanDetail', component: () => import('@/pages/PlanDetailPage.vue') },
    { path: '/stats', name: 'Stats', component: () => import('@/pages/StatsPage.vue') },
    { path: '/ranking', name: 'Ranking', component: () => import('@/pages/RankingPage.vue') },
    { path: '/community', name: 'Community', component: () => import('@/pages/CommunityPage.vue') },
    { path: '/ai', name: 'Ai', component: () => import('@/pages/AiPage.vue') },
    { path: '/profile', name: 'Profile', component: () => import('@/pages/ProfilePage.vue') },
    { path: '/admin', name: 'Admin', component: () => import('@/pages/AdminPage.vue') }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
