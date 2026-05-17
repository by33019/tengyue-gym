# 全局布局重设计 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将全站窄列居中布局改为桌面端侧边栏+宽内容区，运动风配色升级

**Architecture:** 新建 Sidebar.vue 组件 + 重写 DefaultLayout.vue 响应式切换（≥1024px 侧边栏，<1024px 底部导航），13 个页面去掉 max-w 限制，全部改为宽屏友好布局

**Tech Stack:** Vue 3 + Tailwind CSS + Vite，纯前端改造

---

## File Map

| 操作 | 路径 | 说明 |
|:---|:---|:---|
| Create | `components/Sidebar.vue` | 侧边栏组件 |
| Modify | `layouts/DefaultLayout.vue` | 响应式布局切换 |
| Modify | `style.css` | 全局配色变量 + 运动风字体 |
| Modify | `pages/HomePage.vue` | 全宽仪表盘 |
| Modify | `pages/CheckinPage.vue` | 左右分栏 |
| Modify | `pages/CalendarPage.vue` | 全宽日历 |
| Modify | `pages/PlanPage.vue` | 多列卡片 |
| Modify | `pages/PlanCreatePage.vue` | 宽表单 |
| Modify | `pages/PlanDetailPage.vue` | 宽详情 |
| Modify | `pages/AiPage.vue` | 宽对话 |
| Modify | `pages/ProfilePage.vue` | 全宽分段 |
| Modify | `pages/AdminPage.vue` | 全宽数据表 |
| Modify | `pages/StatsPage.vue` | 全宽图表 |
| Modify | `pages/RankingPage.vue` | 全宽排名 |
| Modify | `pages/CommunityPage.vue` | 宽内容流 |

---

### Task 1: Sidebar.vue — 侧边栏组件

**Files:**
- Create: `frontend/src/components/Sidebar.vue`

- [ ] **Step 1: 创建侧边栏组件**

```vue
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
      <div class="user-avatar">{{ username.charAt(0) }}</div>
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
const role = computed(() => userStore.userInfo?.role ?? 0)

const roleText = computed(() => ['普通用户', '健身督导', '管理员'][role.value] || '')

const baseItems = [
  { path: '/', icon: '🏠', label: '首页' },
  { path: '/checkin', icon: '🏃', label: '健身打卡' },
  { path: '/plan', icon: '📋', label: '训练计划' },
  { path: '/ai', icon: '🤖', label: 'AI 助手' },
  { path: '/profile', icon: '👤', label: '个人中心' },
]

const adminItem = { path: '/admin', icon: '⚙️', label: '管理后台' }

const items = computed(() => {
  if (role.value >= 1) return [...baseItems, adminItem]
  return baseItems
})

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
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
  @apply flex items-center gap-2 px-5 py-6;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}
.logo-icon { font-size: 28px; }
.logo-text {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 20px;
  letter-spacing: 0.08em;
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
  @apply w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold shrink-0;
  background: linear-gradient(135deg, #FF3B5C, #FF8C00);
  color: #fff;
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
```

- [ ] **Step 2: 前端构建验证**

```bash
cd frontend && npx vue-tsc --noEmit 2>&1 | head -5; npm run build 2>&1 | tail -3
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/Sidebar.vue
git commit -m "feat: add Sidebar component with sporty gradient design"
```

---

### Task 2: DefaultLayout — 响应式布局切换

**Files:**
- Modify: `frontend/src/layouts/DefaultLayout.vue`

- [ ] **Step 1: 重写布局，加入响应式侧边栏**

```vue
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

const baseNav = [
  { path: '/', icon: '🏠', label: '首页' },
  { path: '/checkin', icon: '🏃', label: '打卡' },
  { path: '/plan', icon: '📋', label: '计划' },
  { path: '/ai', icon: '🤖', label: 'AI' },
  { path: '/profile', icon: '👤', label: '我的' },
]
const adminNav = { path: '/admin', icon: '⚙️', label: '管理' }
const navItems = computed(() => role.value >= 1 ? [...baseNav, adminNav] : baseNav)

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
```

- [ ] **Step 2: 构建验证**

```bash
cd frontend && npm run build 2>&1 | tail -3
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/layouts/DefaultLayout.vue
git commit -m "feat: responsive layout - sidebar on desktop, bottom nav on mobile"
```

---

### Task 3: 全局样式 + HomePage

**Files:**
- Modify: `frontend/src/pages/HomePage.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: 更新全局 CSS 变量**

在 `frontend/src/style.css` 末尾追加运动风全局样式：

```css
/* 运动风全局变量 */
:root {
  --primary: #FF3B5C;
  --secondary: #00F5A0;
  --accent: #00D2FF;
  --bg: #0B0B14;
  --card: rgba(255, 255, 255, 0.03);
  --border: rgba(255, 255, 255, 0.06);
}

/* 页面通用容器 */
.page-container {
  padding: 2rem 2rem;
  max-width: 1400px;
}
@media (max-width: 767px) {
  .page-container { padding: 1rem 1rem; }
}

/* 运动风卡片 */
.sport-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 1rem;
  transition: all 0.2s ease;
}
.sport-card:hover {
  border-color: rgba(255, 59, 92, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

/* 渐变文字 */
.gradient-text {
  background: linear-gradient(135deg, #FF3B5C, #FF8C00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 荧光绿强调 */
.neon-green { color: #00F5A0; }

/* 页面标题 */
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: 2rem;
  letter-spacing: 0.05em;
  color: #fff;
  margin-bottom: 1.5rem;
}
```

- [ ] **Step 2: 重写 HomePage 为全宽仪表盘**

```vue
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

    <!-- 快捷入口 2 列网格 -->
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

    <!-- 管理入口 -->
    <div v-if="role >= 1" class="section-header">
      <h2 class="section-title">管理</h2>
    </div>
    <div v-if="role >= 1" class="quick-grid">
      <router-link to="/admin" class="quick-card admin-accent">
        <span class="quick-icon">⚙️</span>
        <div class="quick-info">
          <span class="quick-label">管理后台</span>
          <span class="quick-desc">用户管理 · 内容审核</span>
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
  @apply relative overflow-hidden rounded-2xl p-6 md:p-8 mb-8 flex flex-wrap gap-6;
  background: linear-gradient(135deg, #1A0A2E 0%, #16213E 40%, #0F3460 100%);
  border: 1px solid rgba(255, 59, 92, 0.1);
}
.hero-text { flex: 1; min-width: 200px; }
.hero-greeting { font-size: 2rem; font-weight: 700; color: #fff; margin: 0; }
@media (min-width: 768px) { .hero-greeting { font-size: 2.5rem; } }
.hero-subtitle { margin-top: 0.5rem; color: rgba(255, 255, 255, 0.5); font-size: 1rem; }
.hero-stats { display: flex; gap: 1.5rem; }
.hero-stat {
  @apply flex flex-col items-center justify-center px-4 py-3 rounded-xl;
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
@media (max-width: 767px) {
  .quick-grid { grid-template-columns: repeat(2, 1fr); }
}

.quick-card {
  @apply flex items-center gap-3 p-4 rounded-xl transition-all duration-200;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.quick-card:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.1);
}
.quick-icon { font-size: 28px; }
.quick-info { flex: 1; }
.quick-label { display: block; font-size: 0.9rem; font-weight: 600; color: #fff; }
.quick-desc { display: block; font-size: 0.75rem; color: rgba(255, 255, 255, 0.3); margin-top: 2px; }
.quick-arrow { color: rgba(255, 255, 255, 0.15); font-size: 1.2rem; }
.quick-card:hover .quick-arrow { color: #FF3B5C; }

.accent-red { border-left: 3px solid #FF3B5C; }
.accent-green { border-left: 3px solid #00F5A0; }
.accent-blue { border-left: 3px solid #00D2FF; }
.accent-purple { border-left: 3px solid #A78BFA; }
.admin-accent { border-left: 3px solid #FF8C00; }
</style>
```

- [ ] **Step 3: 构建验证**

```bash
cd frontend && npm run build 2>&1 | tail -3
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/pages/HomePage.vue frontend/src/style.css
git commit -m "feat: redesign homepage with hero banner, sporty colors, full-width dashboard"
```

---

### Task 4: CheckinPage + CalendarPage — 打卡模块

**Files:**
- Modify: `frontend/src/pages/CheckinPage.vue`
- Modify: `frontend/src/pages/CalendarPage.vue`

- [ ] **Step 1: CheckinPage 改为左右分栏布局**

关键改动：去掉 `max-w-lg mx-auto`，改为 `page-container` + 桌面端 `md:grid grid-cols-2` 左右分栏（左侧打卡表单，右侧今日记录/日历入口）。

```vue
<!-- 外层改为 -->
<div class="page-container">
  <h1 class="page-title">健身打卡</h1>
  <div class="checkin-layout">
    <!-- 左栏：打卡表单 -->
    <div class="checkin-form-panel">
      <!-- 保持现有表单，调整样式 -->
    </div>
    <!-- 右栏：今日记录 + 日历入口 -->
    <div class="checkin-info-panel">
      <!-- 今日打卡列表 + 日历链接卡片 -->
    </div>
  </div>
</div>

<style scoped>
.checkin-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}
@media (min-width: 768px) {
  .checkin-layout { grid-template-columns: 1fr 1fr; }
}
</style>
```

- [ ] **Step 2: CalendarPage 全宽日历**

关键改动：去掉 `max-w-lg`，日历宽度占满，月份切换栏居中。

- [ ] **Step 3: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/CheckinPage.vue frontend/src/pages/CalendarPage.vue
git commit -m "feat: full-width checkin page with two-column layout, wide calendar"
```

---

### Task 5: PlanPages — 计划模块（3 页）

**Files:**
- Modify: `frontend/src/pages/PlanPage.vue`
- Modify: `frontend/src/pages/PlanCreatePage.vue`
- Modify: `frontend/src/pages/PlanDetailPage.vue`

- [ ] **Step 1: 三页统一去掉 `max-w-lg`，改为 `page-container`**

PlanPage 改造要点：
- 2-3 列卡片网格（`grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3`）
- 页面标题 + 新建按钮在顶部行

PlanCreatePage 改造要点：
- 表单项使用 `max-w-3xl`（768px 仍比之前宽）
- 表单字段横排（如开始/结束日期同行）

PlanDetailPage 改造要点：
- 详情内容使用宽卡片布局

- [ ] **Step 2: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/PlanPage.vue frontend/src/pages/PlanCreatePage.vue frontend/src/pages/PlanDetailPage.vue
git commit -m "feat: full-width plan pages with multi-column card grids"
```

---

### Task 6: AiPage — AI 助手宽屏

**Files:**
- Modify: `frontend/src/pages/AiPage.vue`

- [ ] **Step 1: 扩宽对话区，功能卡片改为横排胶囊**

关键改动：
- 去掉 `max-w-2xl`，改为 `max-w-4xl`（~896px）
- 功能卡片改为横排胶囊形按钮
- 输入区保持适中宽度便于阅读

- [ ] **Step 2: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/AiPage.vue
git commit -m "feat: wider AI chat area, horizontal pill-style function buttons"
```

---

### Task 7: ProfilePage — 全宽个人中心

**Files:**
- Modify: `frontend/src/pages/ProfilePage.vue`

- [ ] **Step 1: 改为全宽分段布局**

关键改动：
- 去掉 `max-w-lg`，使用 `page-container` + 分段宽卡片
- 顶部：头像 + 基本信息横幅（全宽）
- 中部：身体数据 + 运动等级（2 列网格）
- 底部：成就墙（网格展示）+ 设置项

- [ ] **Step 2: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/ProfilePage.vue
git commit -m "feat: full-width profile page with sectioned layout"
```

---

### Task 8: 数据页面（Stats + Ranking + Community）

**Files:**
- Modify: `frontend/src/pages/StatsPage.vue`
- Modify: `frontend/src/pages/RankingPage.vue`
- Modify: `frontend/src/pages/CommunityPage.vue`

- [ ] **Step 1: 三页统一去 `max-w-lg`**

StatsPage：全宽 ECharts 图表 + 顶部指标卡行
RankingPage：全宽排名表 + 筛选项横排
CommunityPage：宽内容流

- [ ] **Step 2: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/StatsPage.vue frontend/src/pages/RankingPage.vue frontend/src/pages/CommunityPage.vue
git commit -m "feat: full-width stats, ranking, and community pages"
```

---

### Task 9: AdminPage — 管理后台全宽

**Files:**
- Modify: `frontend/src/pages/AdminPage.vue`

- [ ] **Step 1: 改为全宽数据表**

关键改动：
- 去掉 `max-w-lg`，使用 `page-container`
- 统计卡片行（打卡率、异常数等）
- 数据表全宽滚动

- [ ] **Step 2: 构建 + Commit**

```bash
cd frontend && npm run build 2>&1 | tail -3
git add frontend/src/pages/AdminPage.vue
git commit -m "feat: full-width admin dashboard with data tables"
```

---

### Task 10: 整体验证

- [ ] **Step 1: 最终构建验证**

```bash
cd frontend && npm run build
```
预期：构建成功，无错误。

- [ ] **Step 2: 最终 Commit**

```bash
git add -A
git commit -m "feat: complete layout redesign - sidebar + sporty theme, full-width pages"
```
