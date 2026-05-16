<template>
  <div class="layout">
    <main class="main-content">
      <router-view />
    </main>
    <nav class="bottom-nav">
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

const route = useRoute()
const userStore = useUserStore()
const role = computed(() => userStore.userInfo?.role ?? 0)

const baseNav = [
  { path: '/', icon: '🏠', label: '首页' },
  { path: '/checkin', icon: '🏃', label: '打卡' },
  { path: '/plan', icon: '📋', label: '计划' },
  { path: '/ai', icon: '🤖', label: 'AI' },
  { path: '/profile', icon: '👤', label: '我的' },
]

const adminNav = { path: '/admin', icon: '⚙️', label: '管理' }

const navItems = computed(() => {
  if (role.value >= 1) return [...baseNav, adminNav]
  return baseNav
})

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<style scoped>
.layout {
  @apply min-h-screen relative;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
  padding-bottom: 72px;
}
.main-content { @apply min-h-screen; }

.bottom-nav {
  @apply fixed bottom-0 left-0 right-0 z-50 flex justify-around items-center;
  height: 64px;
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(15, 15, 20, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}
.nav-item {
  @apply flex flex-col items-center gap-0.5 py-1 px-3 rounded-xl transition-all duration-200;
  text-decoration: none;
  min-width: 56px;
}
.nav-icon { font-size: 20px; }
.nav-label { font-size: 10px; color: rgba(255, 255, 255, 0.3); transition: color 0.2s; }
.nav-item.active .nav-label { color: #FF6B6B; }
.nav-item:not(.active):hover .nav-label { color: rgba(255, 255, 255, 0.5); }
</style>
