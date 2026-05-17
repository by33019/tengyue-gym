<template>
  <div class="page-container">
    <!-- 顶部运动风横幅 -->
    <div class="hero-banner">
      <div class="hero-text">
        <h1 class="hero-greeting">HI, {{ username }} 👋</h1>
        <p class="hero-subtitle">{{ greetingText }}</p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="hero-stat-value">{{ todayCount !== null ? todayCount : '...' }}</span>
          <span class="hero-stat-label">今日打卡</span>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ activePlans }}</span>
          <span class="hero-stat-label">进行中计划</span>
        </div>
      </div>
      <div class="hero-decoration">
        <span class="hero-emoji">💪</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section-header">
      <h2 class="section-title">快捷入口</h2>
    </div>
    <div class="quick-grid">
      <router-link v-for="a in actions" :key="a.path" :to="a.path" :class="['quick-card', a.accent]">
        <span class="quick-icon">{{ a.icon }}</span>
        <div class="quick-info">
          <span class="quick-label">{{ a.label }}</span>
          <span class="quick-desc">{{ a.desc }}</span>
        </div>
        <span class="quick-arrow">→</span>
      </router-link>
    </div>

    <!-- 数据与社区 -->
    <div class="section-header">
      <h2 class="section-title">数据与社区</h2>
    </div>
    <div class="quick-grid">
      <router-link v-for="a in secondaryActions" :key="a.path" :to="a.path" :class="['quick-card', a.accent]">
        <span class="quick-icon">{{ a.icon }}</span>
        <div class="quick-info">
          <span class="quick-label">{{ a.label }}</span>
          <span class="quick-desc">{{ a.desc }}</span>
        </div>
        <span class="quick-arrow">→</span>
      </router-link>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { checkinApi } from '@/api/checkin'
import { planApi } from '@/api/plan'

const userStore = useUserStore()
const username = computed(() => userStore.userInfo?.username || '')
const role = computed(() => userStore.userInfo?.role ?? 0)

const todayCount = ref<number | null>(null)
const activePlans = ref(0)

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '晨练好时光，今天也要加油！'
  if (hour < 18) return '坚持就是胜利，继续冲刺！'
  return '晚上来组拉伸，放松一下吧'
})

const actions = [
  { path: '/checkin', icon: '🏃', label: '去打卡', desc: '记录今日训练', accent: 'accent-red' },
  { path: '/plan', icon: '📋', label: '训练计划', desc: '查看/创建计划', accent: 'accent-green' },
  { path: '/ai', icon: '🤖', label: 'AI 助手', desc: '智能健身指导', accent: 'accent-blue' },
  { path: '/profile', icon: '👤', label: '个人中心', desc: '身体数据与成就', accent: 'accent-purple' },
]

const secondaryActions = [
  { path: '/stats', icon: '📊', label: '数据统计', desc: '运动趋势分析', accent: 'accent-green' },
  { path: '/ranking', icon: '🏆', label: '排行榜', desc: '周/月 Top20', accent: 'accent-red' },
  { path: '/community', icon: '💬', label: '社区', desc: '动态分享互动', accent: 'accent-blue' },
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
.page-container { padding: 2rem; max-width: 1400px; }
@media (max-width: 767px) { .page-container { padding: 1rem; } }

.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: 1rem;
  padding: 2rem;
  margin-bottom: 2rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  background: linear-gradient(135deg, #1A0A2E 0%, #16213E 40%, #0F3460 100%);
  border: 1px solid rgba(255, 59, 92, 0.1);
}
@media (min-width: 768px) { .hero-banner { padding: 2.5rem; } }

.hero-text { flex: 1; min-width: 200px; }
.hero-greeting { font-size: 2rem; font-weight: 700; color: #fff; margin: 0; }
@media (min-width: 768px) { .hero-greeting { font-size: 2.5rem; } }
.hero-subtitle { margin-top: 0.5rem; color: rgba(255, 255, 255, 0.5); font-size: 1rem; }

.hero-stats { display: flex; gap: 1.5rem; }
.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  min-width: 80px;
}
.hero-stat-value {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 2rem;
  color: #FF3B5C;
}
.hero-stat-label { font-size: 0.75rem; color: rgba(255, 255, 255, 0.3); }

.hero-decoration { position: absolute; right: 1rem; bottom: -0.5rem; opacity: 0.15; }
.hero-emoji { font-size: 120px; }

.section-header { margin: 2rem 0 1rem; }
.section-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 1.5rem;
  letter-spacing: 0.05em;
  color: #fff;
  margin: 0;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}
@media (min-width: 1024px) {
  .quick-grid { grid-template-columns: repeat(4, 1fr); }
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 0.75rem;
  transition: all 0.2s ease;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.quick-card:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.1);
}
.quick-icon { font-size: 28px; flex-shrink: 0; }
.quick-info { flex: 1; min-width: 0; }
.quick-label { display: block; font-size: 0.9rem; font-weight: 600; color: #fff; }
.quick-desc { display: block; font-size: 0.75rem; color: rgba(255, 255, 255, 0.3); margin-top: 2px; }
.quick-arrow { color: rgba(255, 255, 255, 0.15); font-size: 1.2rem; flex-shrink: 0; }
.quick-card:hover .quick-arrow { color: #FF3B5C; }

.accent-red { border-left: 3px solid #FF3B5C; }
.accent-green { border-left: 3px solid #00F5A0; }
.accent-blue { border-left: 3px solid #00D2FF; }
.accent-purple { border-left: 3px solid #A78BFA; }
.admin-accent { border-left: 3px solid #FF8C00; }
</style>
