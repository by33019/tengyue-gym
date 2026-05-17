<template>
  <div class="app-shell">
    <!-- 桌面端：侧边栏 -->
    <Sidebar class="sidebar-desktop" />

    <!-- 内容区 -->
    <main class="main-area">
      <router-view />
    </main>

    <!-- 移动端：底部导航 -->
    <nav class="bottom-nav-mobile">
      <router-link v-for="item in navItems" :key="item.path" :to="item.path"
        class="nav-item" :class="{ active: isActive(item.path) }">
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Sidebar from '@/components/Sidebar.vue'

const route = useRoute()
const userStore = useUserStore()
const role = computed(() => userStore.userInfo?.role ?? 0)

const userNav = [
  { path: '/', icon: '🏠', label: '首页' },
  { path: '/checkin', icon: '🏃', label: '打卡' },
  { path: '/plan', icon: '📋', label: '计划' },
  { path: '/ai', icon: '🤖', label: 'AI' },
  { path: '/profile', icon: '👤', label: '我的' },
]
const coachNav = [
  { path: '/admin', icon: '🏠', label: '工作台' },
  { path: '/checkin', icon: '🏃', label: '打卡' },
  { path: '/plan', icon: '📋', label: '计划' },
  { path: '/ai', icon: '🤖', label: 'AI' },
  { path: '/profile', icon: '👤', label: '我的' },
]
const adminNav = [
  { path: '/admin', icon: '🏠', label: '管理' },
  { path: '/profile', icon: '👤', label: '我的' },
]
const navItems = computed(() => {
  if (role.value === 2) return adminNav
  if (role.value === 1) return coachNav
  return userNav
})

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<style>
/* 全局样式覆盖 */
.app-shell {
  min-height: 100vh;
  background: #0B0B14;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}

/* 桌面端 */
@media (min-width: 1024px) {
  .app-shell { padding-left: 240px; }
  .bottom-nav-mobile { display: none; }
}

/* 移动端 */
@media (max-width: 1023px) {
  .sidebar-desktop { display: none; }
  .app-shell { padding-bottom: 72px; }
  .bottom-nav-mobile {
    @apply fixed bottom-0 left-0 right-0 z-50 flex justify-around items-center;
    height: 64px;
    padding-bottom: env(safe-area-inset-bottom);
    background: rgba(15, 15, 20, 0.85);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-top: 1px solid rgba(255, 255, 255, 0.06);
  }
}

/* 底部导航项 */
.nav-item {
  @apply flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all duration-200;
  text-decoration: none;
  min-width: 56px;
}
.nav-icon { font-size: 20px; }
.nav-label { font-size: 10px; color: rgba(255, 255, 255, 0.3); transition: color 0.2s; }
.nav-item.active .nav-label { color: #FF3B5C; }
.nav-item:not(.active):hover .nav-label { color: rgba(255, 255, 255, 0.5); }
</style>
