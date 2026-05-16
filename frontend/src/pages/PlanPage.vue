<template>
  <div class="plan-page">
    <div class="max-w-lg mx-auto px-4 py-8 relative z-10">
      <!-- 头部 -->
      <div class="header-row">
        <h1 class="page-title">我的计划</h1>
        <router-link to="/plan/create" class="add-btn">+ 新建</router-link>
      </div>

      <!-- 状态筛选 -->
      <div class="filter-row">
        <button :class="['filter-chip', { on: filterStatus === null }]" @click="filterStatus = null; loadPlans()">全部</button>
        <button :class="['filter-chip', { on: filterStatus === 1 }]" @click="filterStatus = 1; loadPlans()">进行中</button>
        <button :class="['filter-chip', { on: filterStatus === 0 }]" @click="filterStatus = 0; loadPlans()">已停用</button>
      </div>

      <!-- 模板市场入口 -->
      <div class="template-banner" @click="showTemplates = !showTemplates">
        <span>📋 计划模板市场</span>
        <span class="arrow" :class="{ open: showTemplates }">▾</span>
      </div>

      <!-- 模板列表 -->
      <div v-if="showTemplates" class="template-list">
        <div v-if="templates.length === 0" class="empty-text">暂无公共模板</div>
        <div v-for="t in templates" :key="t.id" class="template-card">
          <div class="tpl-left">
            <span class="tpl-name">{{ t.planName }}</span>
            <span class="tpl-meta">{{ t.goal }} · {{ t.difficulty }}</span>
          </div>
          <button class="apply-btn" @click="applyTemplate(t)">套用</button>
        </div>
      </div>

      <!-- 计划列表 -->
      <div v-if="plans.length === 0" class="empty-text mt-8">暂无计划，点击右上角新建</div>
      <div v-for="p in plans" :key="p.id" :class="['plan-card', { inactive: p.status === 0 }]">
        <div class="card-top">
          <div class="card-left">
            <h3 class="plan-name">{{ p.planName }}</h3>
            <div class="plan-tags">
              <span class="tag source">{{ p.source }}</span>
              <span class="tag goal">{{ p.goal }}</span>
              <span class="tag diff" :class="'diff-' + p.difficulty">{{ p.difficulty }}</span>
            </div>
          </div>
          <div class="card-right">
            <span :class="['status-badge', p.status === 1 ? 'active' : 'paused']">
              {{ p.status === 1 ? '进行中' : '已停用' }}
            </span>
          </div>
        </div>
        <div class="card-meta">
          {{ p.startDate }} → {{ p.endDate }}
        </div>
        <div class="card-actions">
          <button class="action-btn" @click="router.push('/plan/' + p.id)">详情</button>
          <button class="action-btn" @click="togglePlan(p)">{{ p.status === 1 ? '停用' : '启用' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { planApi } from '@/api/plan'

const router = useRouter()
const plans = ref<any[]>([])
const templates = ref<any[]>([])
const filterStatus = ref<number | null>(1)
const showTemplates = ref(false)

onMounted(() => { loadPlans(); loadTemplates() })

async function loadPlans() {
  try {
    const { data: res } = await planApi.list({ status: filterStatus.value ?? undefined })
    if (res.code === 200) plans.value = res.data.records || []
  } catch { /* */ }
}

async function loadTemplates() {
  try {
    const { data: res } = await planApi.templates({})
    if (res.code === 200) templates.value = res.data.records || []
  } catch { /* */ }
}

async function togglePlan(p: any) {
  try {
    await planApi.toggleStatus(p.id)
    loadPlans()
  } catch { /* */ }
}

async function applyTemplate(t: any) {
  try {
    const { data: detail } = await planApi.getById(t.id)
    if (detail.code === 200) {
      await planApi.create({
        planName: t.planName + '（我的）',
        goal: t.goal,
        difficulty: t.difficulty,
        startDate: new Date().toISOString().slice(0, 10),
        endDate: new Date(Date.now() + 30*86400000).toISOString().slice(0, 10),
        source: '模板',
        details: detail.data.details || []
      })
      loadPlans()
    }
  } catch { /* */ }
}
</script>

<style scoped>
.plan-page {
  @apply min-h-screen;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
.header-row {
  @apply flex items-center justify-between mb-6;
  animation: fadeInUp 0.5s ease both;
}
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  @apply text-4xl tracking-wider;
  color: #fff;
}
.add-btn {
  @apply px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  text-decoration: none;
}
.add-btn:hover { transform: translateY(-1px); }

.filter-row { @apply flex gap-2 mb-6; }
.filter-chip {
  @apply px-4 py-2 rounded-lg text-sm transition-all duration-200;
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;
}
.filter-chip.on { background: rgba(255, 107, 107, 0.1); border-color: rgba(255, 107, 107, 0.3); color: #FF6B6B; }

.template-banner {
  @apply flex justify-between items-center p-4 rounded-xl mb-4 cursor-pointer transition-all duration-200;
  background: rgba(0, 245, 160, 0.04);
  border: 1px solid rgba(0, 245, 160, 0.1);
  color: #00F5A0;
  font-size: 14px;
}
.template-banner:hover { background: rgba(0, 245, 160, 0.08); }
.arrow { transition: transform 0.3s; }
.arrow.open { transform: rotate(180deg); }

.template-list { @apply mb-4; }
.template-card {
  @apply flex justify-between items-center p-3 rounded-xl mb-2;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.tpl-name { @apply block text-sm; color: #fff; }
.tpl-meta { @apply text-xs; color: rgba(255, 255, 255, 0.3); }
.apply-btn {
  @apply px-3 py-1 rounded-lg text-xs transition-all;
  background: rgba(0, 245, 160, 0.1);
  color: #00F5A0;
  border: 1px solid rgba(0, 245, 160, 0.2);
  cursor: pointer;
}
.apply-btn:hover { background: rgba(0, 245, 160, 0.2); }

.empty-text { @apply text-sm text-center py-8; color: rgba(255, 255, 255, 0.15); }

.plan-card {
  @apply rounded-2xl p-5 mb-3 transition-all duration-300;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  animation: fadeInUp 0.4s ease both;
}
.plan-card.inactive { opacity: 0.5; }
.card-top { @apply flex justify-between items-start mb-3; }
.plan-name { @apply text-base font-semibold; color: #fff; }
.plan-tags { @apply flex gap-1.5 mt-1.5; }
.tag {
  @apply px-2 py-0.5 rounded text-[10px];
}
.tag.source { background: rgba(0, 210, 255, 0.1); color: #00D2FF; }
.tag.goal { background: rgba(255, 255, 255, 0.05); color: rgba(255, 255, 255, 0.5); }
.tag.diff { color: #FF6B6B; }
.tag.diff-入门 { background: rgba(0, 245, 160, 0.1); color: #00F5A0; }
.tag.diff-进阶 { background: rgba(255, 107, 107, 0.1); color: #FF6B6B; }
.tag.diff-高级 { background: rgba(255, 140, 0, 0.1); color: #FF8C00; }

.status-badge { @apply px-3 py-1 rounded-full text-xs; }
.status-badge.active { background: rgba(0, 245, 160, 0.1); color: #00F5A0; }
.status-badge.paused { background: rgba(255, 255, 255, 0.05); color: rgba(255, 255, 255, 0.3); }

.card-meta { @apply text-xs mb-3; color: rgba(255, 255, 255, 0.2); }
.card-actions { @apply flex gap-2; }
.action-btn {
  @apply px-4 py-2 rounded-lg text-xs transition-all duration-200;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}
.action-btn:hover { background: rgba(255, 255, 255, 0.08); color: #fff; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
