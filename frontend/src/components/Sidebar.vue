<template>
  <aside class="sidebar">
    <!-- Logo -->
    <div class="logo-area">
      <span class="logo-icon">🏋️</span>
      <span class="logo-text">腾跃健身</span>
    </div>

    <!-- 导航 -->
    <nav class="nav-list">
      <router-link
        v-for="item in items"
        :key="item.path"
        :to="item.path"
        :class="['nav-link', { active: isActive(item.path) }]"
      >
        <span class="nav-link-icon">{{ item.icon }}</span>
        <span class="nav-link-label">{{ item.label }}</span>
        <span v-if="isActive(item.path)" class="active-dot"></span>
      </router-link>
    </nav>

    <!-- 用户区 -->
    <div class="user-area">
      <div class="user-avatar">
        <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
        <span v-else>{{ username.charAt(0) }}</span>
      </div>
      <div class="user-info">
        <span class="user-name">{{ username }}</span>
        <span class="user-role">{{ roleText }}</span>
      </div>
      <button @click="logout" class="logout-btn" title="退出登录">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16,17 21,12 16,7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.userInfo?.username || '用户')
const avatarUrl = computed(() => userStore.userInfo?.avatar || '')
const role = computed(() => userStore.userInfo?.role ?? 0)

const roleText = computed(() => ['普通用户', '健身督导', '管理员'][role.value] || '')

const userItems = [
  { path: '/', icon: '🏠', label: '首页' },
  { path: '/checkin', icon: '🏃', label: '健身打卡' },
  { path: '/plan', icon: '📋', label: '训练计划' },
  { path: '/stats', icon: '📊', label: '数据统计' },
  { path: '/ranking', icon: '🏆', label: '排行榜' },
  { path: '/community', icon: '💬', label: '社区' },
  { path: '/ai', icon: '🤖', label: 'AI 助手' },
  { path: '/profile', icon: '👤', label: '个人中心' },
]

const coachItems = [
  { path: '/admin', icon: '🏠', label: '工作台' },
  { path: '/coach/students', icon: '👥', label: '学员管理' },
  { path: '/checkin', icon: '🏃', label: '健身打卡' },
  { path: '/plan', icon: '📋', label: '训练计划' },
  { path: '/coach/templates', icon: '📌', label: '模板管理' },
  { path: '/coach/alerts', icon: '🔔', label: '提醒日志' },
  { path: '/ai', icon: '🤖', label: 'AI 助手' },
  { path: '/profile', icon: '👤', label: '个人中心' },
]

const adminItems = [
  { path: '/admin', icon: '🏠', label: '管理台' },
  { path: '/admin/users', icon: '👥', label: '用户管理' },
  { path: '/admin/coaches', icon: '👨‍🏫', label: '教练管理' },
  { path: '/audit', icon: '📝', label: '内容审核' },
  { path: '/admin/analytics', icon: '📊', label: '数据看板' },
  { path: '/profile', icon: '👤', label: '个人中心' },
]

const items = computed(() => {
  if (role.value === 2) return adminItems
  if (role.value === 1) return coachItems
  return userItems
})

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  if (path === '/admin') return route.path === '/admin'
  if (path.includes('?')) {
    const [base, qs] = path.split('?')
    const params = new URLSearchParams(qs)
    const tab = params.get('tab')
    return route.path.startsWith(base) && route.query.tab === tab
  }
  return route.path.startsWith(path) && !route.query.tab
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  @apply fixed left-0 top-0 h-full flex flex-col;
  width: 240px;
  background: linear-gradient(180deg, #0F0F1A 0%, #0B0B14 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  z-index: 40;
}

.logo-area {
  @apply flex items-center gap-2.5 px-5 py-6;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}
.logo-icon {
  font-size: 26px;
  line-height: 1;
  display: flex;
  align-items: center;
}
.logo-text {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 20px;
  line-height: 1;
  letter-spacing: 0.08em;
  padding-top: 2px;
  background: linear-gradient(135deg, #FF3B5C, #FF8C00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-list {
  @apply flex-1 flex flex-col gap-1 px-3 py-4;
}
.nav-link {
  @apply flex items-center gap-3 px-3 py-2.5 rounded-xl relative transition-all duration-200;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.45);
}
.nav-link:hover {
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.03);
}
.nav-link.active {
  color: #fff;
  background: linear-gradient(135deg, rgba(255, 59, 92, 0.15), rgba(255, 140, 0, 0.1));
}
.nav-link-icon { font-size: 18px; width: 24px; text-align: center; }
.nav-link-label { font-size: 14px; font-weight: 500; }
.active-dot {
  @apply absolute right-3 w-1.5 h-1.5 rounded-full;
  background: #FF3B5C;
  box-shadow: 0 0 8px rgba(255, 59, 92, 0.6);
}

.user-area {
  @apply flex items-center gap-2.5 px-4 py-4 mx-2 mb-2 rounded-2xl;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.user-avatar {
  @apply w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0 overflow-hidden;
  background: linear-gradient(135deg, #FF3B5C, #FF8C00);
  color: #fff;
}
.avatar-img {
  @apply w-full h-full object-cover;
}
.user-info {
  @apply flex-1 min-w-0;
}
.user-name {
  @apply block text-sm font-medium truncate;
  color: #fff;
}
.user-role {
  @apply block text-xs;
  color: rgba(255, 255, 255, 0.3);
}
.logout-btn {
  @apply p-1.5 rounded-lg transition-colors shrink-0;
  color: rgba(255, 255, 255, 0.2);
  background: none;
  border: none;
  cursor: pointer;
}
.logout-btn:hover { color: #FF3B5C; background: rgba(255, 59, 92, 0.1); }
</style>
