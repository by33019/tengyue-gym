<template>
  <div class="page">
    <div class="max-w-lg mx-auto px-4 py-6">
      <h1 class="page-title">{{ roleText }}工作台</h1>

      <!-- 概览 -->
      <div class="summary-grid">
        <div class="s-card"><span class="s-val">{{ dash.totalUsers }}</span><span class="s-lbl">健身用户</span></div>
        <div class="s-card"><span class="s-val">{{ dash.todayCheckIns }}</span><span class="s-lbl">今日打卡</span></div>
        <div class="s-card"><span class="s-val">{{ dash.activePlans }}</span><span class="s-lbl">进行中计划</span></div>
      </div>

      <!-- 用户管理 -->
      <div class="section-title">用户打卡情况</div>
      <div v-if="!dash.userRates || dash.userRates.length === 0" class="empty">暂无数据</div>
      <div v-for="u in dash.userRates" :key="u.userId" :class="['user-card', { warn: u.needsRemind }]">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">{{ u.fitnessGoal || '未设置目标' }} · 连续{{ u.streak }}天</span>
        </div>
        <div class="user-stats">
          <span class="user-stat">打卡{{ u.checkInDays }}天</span>
          <span :class="u.hasPlan ? 'tag-green' : 'tag-red'">{{ u.hasPlan ? '有计划' : '无计划' }}</span>
        </div>
        <button v-if="u.needsRemind" class="remind-btn" @click="remind(u)">发送提醒</button>
      </div>

      <!-- 管理操作 -->
      <div v-if="userRole >= 2" class="section-title mt-4">用户管理</div>
      <div v-if="userRole >= 2" v-for="u in userList" :key="u.id" class="user-card">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">角色: {{ ['普通','督导','管理员'][u.role] }}</span>
        </div>
        <button :class="['toggle-btn', u.status ? '' : 'disabled']" @click="toggleUser(u)">
          {{ u.status ? '禁用' : '启用' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role ?? 0)
const roleText = computed(() => ['普通用户', '督导', '管理员'][userRole.value] || '')
const dash = ref<any>({})
const userList = ref<any[]>([])

onMounted(async () => {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code===200) dash.value = r.data.data } catch {}
  if (userRole.value >= 2) {
    try { const r = await request.get('/admin/users?page=1&size=50'); if (r.data.code===200) userList.value = r.data.data.records } catch {}
  }
})

async function remind(u: any) {
  await request.post('/admin/remind')
  alert(`已向 ${u.username} 发送提醒`)
}

async function toggleUser(u: any) {
  await request.put(`/admin/user/${u.id}/status`)
  u.status = u.status ? 0 : 1
}
</script>

<style scoped>
.page { @apply min-h-screen; background: #0a0a0f; font-family: 'Noto Sans SC', sans-serif; }
.page-title { font-family: 'Bebas Neue', sans-serif; @apply text-3xl tracking-wider mb-4; color: #fff; }
.summary-grid { @apply grid grid-cols-3 gap-3 mb-6; }
.s-card { @apply p-4 rounded-xl; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { @apply block text-2xl; font-family: 'Bebas Neue', sans-serif; color: #FF6B6B; }
.s-lbl { @apply text-xs; color: rgba(255,255,255,0.3); }
.section-title { @apply text-xs uppercase tracking-wider mb-3; color: rgba(255,255,255,0.2); }
.empty { @apply text-sm text-center py-8; color: rgba(255,255,255,0.15); }
.user-card { @apply flex flex-wrap items-center gap-3 p-4 rounded-xl mb-2; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.user-card.warn { border-color: rgba(255,107,107,0.2); }
.user-info { @apply flex-1; }
.user-name { @apply block text-sm; color: #fff; }
.user-meta { @apply text-xs; color: rgba(255,255,255,0.3); }
.user-stats { @apply flex items-center gap-2; }
.user-stat { @apply text-xs; color: rgba(255,255,255,0.5); }
.tag-green { @apply px-2 py-0.5 rounded text-xs; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { @apply px-2 py-0.5 rounded text-xs; background: rgba(255,107,107,0.1); color: #FF6B6B; }
.remind-btn { @apply px-3 py-1 rounded-lg text-xs; background: rgba(255,107,107,0.1); border: 1px solid rgba(255,107,107,0.2); color: #FF6B6B; cursor: pointer; }
.remind-btn:hover { background: rgba(255,107,107,0.2); }
.toggle-btn { @apply px-3 py-1 rounded-lg text-xs cursor-pointer; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); }
.toggle-btn.disabled { background: rgba(0,245,160,0.1); border-color: rgba(0,245,160,0.2); color: #00F5A0; }
.toggle-btn:hover { color: #fff; }
</style>
