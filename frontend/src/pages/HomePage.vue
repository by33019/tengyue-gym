<template>
  <div class="home-page">
    <!-- 顶部 -->
    <div class="header">
      <div class="greeting">
        <h1 class="hi">你好，{{ username }}</h1>
        <span :class="['role-tag', roleClass]">{{ roleText }}</span>
      </div>
      <div class="header-actions">
        <router-link to="/checkin/calendar" class="cal-link">📅</router-link>
      </div>
    </div>

    <!-- 快捷统计 -->
    <div class="stats-row">
      <div class="stat-card" v-for="s in stats" :key="s.label">
        <span class="stat-num">{{ s.value }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section-title">快捷入口</div>
    <div class="action-grid">
      <router-link v-for="a in actions" :key="a.path" :to="a.path"
        :class="['action-card', a.accent]">
        <span class="action-icon">{{ a.icon }}</span>
        <span class="action-label">{{ a.label }}</span>
        <span class="action-desc">{{ a.desc }}</span>
      </router-link>
    </div>

    <!-- 数据与社区入口 -->
    <div class="section-title mt-2">数据与社区</div>
    <div class="action-grid">
      <router-link v-for="a in secondaryActions" :key="a.path" :to="a.path"
        :class="['action-card', a.accent]">
        <span class="action-icon">{{ a.icon }}</span>
        <span class="action-label">{{ a.label }}</span>
        <span class="action-desc">{{ a.desc }}</span>
      </router-link>
    </div>

    <!-- 最近动态 -->
    <div class="feed-card" v-if="todayCount !== null">
      <div class="feed-row">
        <span>今日打卡</span>
        <span :class="todayCount > 0 ? 'text-green' : 'text-dim'">{{ todayCount > 0 ? `${todayCount} 次` : '还未打卡' }}</span>
      </div>
      <div class="feed-row">
        <span>进行中计划</span>
        <span class="text-green">{{ activePlans }} 个</span>
      </div>
    </div>
    <div v-else class="feed-card">
      <span class="text-dim text-sm">加载中...</span>
    </div>

    <!-- 督导/管理员专用区域 -->
    <div v-if="role >= 1" class="section-title mt-4">管理入口</div>
    <div v-if="role >= 1" class="action-grid">
      <router-link to="/admin" class="action-card admin-accent">
        <span class="action-icon">⚙️</span>
        <span class="action-label">管理后台</span>
        <span class="action-desc">用户管理 · 内容审核</span>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { checkinApi } from '@/api/checkin'
import { planApi } from '@/api/plan'

const userStore = useUserStore()
const username = computed(() => userStore.userInfo?.username || '')
const role = computed(() => userStore.userInfo?.role ?? 0)

const roleText = computed(() => ['普通用户', '健身督导', '管理员'][role.value] || '')
const roleClass = computed(() => ['role-user', 'role-coach', 'role-admin'][role.value] || '')

const todayCount = ref<number | null>(null)
const activePlans = ref(0)

const stats = computed(() => [
  { label: '今日打卡', value: todayCount.value !== null ? `${todayCount.value}/3` : '...' },
  { label: '进行中计划', value: `${activePlans.value}` },
  { label: '角色', value: roleText.value },
])

const actions = [
  { path: '/checkin', icon: '🏃', label: '去打卡', desc: '记录今日训练', accent: 'accent-red' },
  { path: '/plan', icon: '📋', label: '我的计划', desc: '查看/创建训练计划', accent: 'accent-green' },
  { path: '/ai', icon: '🤖', label: 'AI 助手', desc: '智能健身指导', accent: 'accent-blue' },
  { path: '/profile', icon: '👤', label: '个人中心', desc: '身体数据与成就', accent: 'accent-purple' },
]

const secondaryActions = [
  { path: '/stats', icon: '📊', label: '数据统计', desc: '运动趋势与总览', accent: 'accent-green' },
  { path: '/ranking', icon: '🏆', label: '排行榜', desc: '周/月 Top20', accent: 'accent-red' },
  { path: '/community', icon: '💬', label: '社区', desc: '动态分享与互动', accent: 'accent-blue' },
]

onMounted(async () => {
  try {
    const [tc, pl] = await Promise.all([
      checkinApi.todayCount(),
      planApi.list({ status: 1 })
    ])
    if (tc.data.code === 200) todayCount.value = tc.data.data
    if (pl.data.code === 200) activePlans.value = pl.data.data.total || 0
  } catch { /* */ }
})
</script>

<style scoped>
.home-page {
  @apply px-4 py-6 max-w-lg mx-auto;
}

.header {
  @apply flex justify-between items-start mb-6;
  animation: fadeInUp 0.5s ease both;
}
.greeting { @apply flex flex-col gap-1; }
.hi {
  @apply text-2xl font-bold;
  font-family: 'Bebas Neue', sans-serif;
  color: #fff;
  letter-spacing: 0.05em;
}
.role-tag {
  @apply inline-block px-2 py-0.5 rounded text-xs w-fit;
}
.role-user { background: rgba(0, 245, 160, 0.1); color: #00F5A0; }
.role-coach { background: rgba(0, 210, 255, 0.1); color: #00D2FF; }
.role-admin { background: rgba(255, 107, 107, 0.1); color: #FF6B6B; }
.cal-link {
  @apply w-10 h-10 rounded-xl flex items-center justify-center text-lg;
  background: rgba(255, 255, 255, 0.04);
  text-decoration: none;
}

.stats-row {
  @apply grid grid-cols-3 gap-3 mb-6;
  animation: fadeInUp 0.5s 0.1s ease both;
}
.stat-card {
  @apply p-4 rounded-2xl text-center;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.stat-num {
  font-family: 'Bebas Neue', sans-serif;
  @apply block text-xl font-bold;
  color: #FF6B6B;
}
.stat-label { @apply text-xs mt-0.5; color: rgba(255, 255, 255, 0.3); }

.section-title {
  @apply text-xs uppercase tracking-wider mb-3;
  color: rgba(255, 255, 255, 0.2);
}

.action-grid {
  @apply grid grid-cols-2 gap-3 mb-6;
  animation: fadeInUp 0.5s 0.15s ease both;
}
.action-card {
  @apply p-4 rounded-2xl flex flex-col gap-1 transition-all duration-200 border;
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.04);
  text-decoration: none;
}
.action-card:hover { transform: translateY(-2px); }
.action-icon { font-size: 24px; }
.action-label { @apply text-sm font-semibold mt-1; color: #fff; }
.action-desc { @apply text-xs; color: rgba(255, 255, 255, 0.25); }

.accent-red { border-left: 3px solid #FF6B6B; }
.accent-green { border-left: 3px solid #00F5A0; }
.accent-blue { border-left: 3px solid #00D2FF; }
.accent-purple { border-left: 3px solid #A78BFA; }
.admin-accent { border-left: 3px solid #FF8C00; }

.feed-card {
  @apply p-4 rounded-2xl;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  animation: fadeInUp 0.5s 0.2s ease both;
}
.feed-row { @apply flex justify-between py-2 text-sm; color: rgba(255, 255, 255, 0.5); }
.text-green { color: #00F5A0; }
.text-dim { color: rgba(255, 255, 255, 0.2); }
.text-sm { font-size: 14px; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
