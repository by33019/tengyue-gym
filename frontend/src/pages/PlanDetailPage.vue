<template>
  <div class="detail-page">
    <div class="page-container relative z-10">
      <div class="header-row">
        <button class="back-btn" @click="router.back()">← 返回</button>
        <h1 class="page-title">{{ plan.planName }}</h1>
        <div></div>
      </div>

      <div class="info-card">
        <div class="info-row">
          <span class="info-label">目标</span><span class="info-val">{{ plan.goal }}</span>
          <span class="info-label">难度</span><span class="info-val">{{ plan.difficulty }}</span>
          <span class="info-label">来源</span><span class="info-val">{{ plan.source }}</span>
        </div>
        <div class="info-row mt-2">
          <span class="info-label">周期</span><span class="info-val">{{ plan.startDate }} → {{ plan.endDate }}</span>
        </div>
      </div>

      <div class="section-title mt-6">训练安排</div>
      <div v-for="day in weekDays" :key="day" class="day-block">
        <div class="day-label">{{ day }}</div>
        <div v-if="getDayDetails(day).length === 0" class="rest-text">休息日</div>
        <div v-for="ex in getDayDetails(day)" :key="ex.id" class="ex-item">
          <span class="ex-name">{{ ex.exerciseType }}</span>
          <span class="ex-detail" v-if="ex.sets > 0">{{ ex.sets }}组 × {{ ex.reps }}次</span>
          <span class="ex-detail" v-if="ex.duration > 0">{{ ex.duration }}分钟</span>
          <span class="ex-note" v-if="ex.note">{{ ex.note }}</span>
        </div>
      </div>

      <div class="action-row">
        <button class="edit-btn" @click="router.push('/plan/create?edit=' + plan.id)">编辑计划</button>
        <button class="toggle-btn" @click="toggleStatus">{{ plan.status === 1 ? '停用计划' : '启用计划' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { planApi } from '@/api/plan'

const router = useRouter()
const route = useRoute()
const plan = ref<any>({})

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const dayMap: Record<string, number> = { '周一': 1, '周二': 2, '周三': 3, '周四': 4, '周五': 5, '周六': 6, '周日': 7 }

onMounted(async () => {
  try {
    const { data: res } = await planApi.getById(Number(route.params.id))
    if (res.code === 200) plan.value = res.data
  } catch { /* */ }
})

function getDayDetails(day: string) {
  const dayNum = dayMap[day]
  return (plan.value.details || []).filter((d: any) => d.dayOfWeek === dayNum)
}

async function toggleStatus() {
  try {
    await planApi.toggleStatus(plan.value.id)
    plan.value.status = plan.value.status === 1 ? 0 : 1
  } catch { /* */ }
}
</script>

<style scoped>
.detail-page {
  @apply min-h-screen;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
.header-row { @apply flex items-center justify-between mb-6; }
.back-btn { background: none; border: none; color: rgba(255,255,255,0.4); cursor: pointer; font-size: 14px; }
.back-btn:hover { color: #fff; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; color: #fff; letter-spacing: 0.05em; }

.info-card {
  @apply rounded-2xl p-5;
  background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05);
}
.info-row { @apply flex flex-wrap gap-x-6 gap-y-2 text-sm; }
.info-label { color: rgba(255,255,255,0.3); }
.info-val { color: #fff; margin-right: 12px; }

.section-title { @apply text-sm mb-4 mt-6; color: rgba(255,255,255,0.4); }

.day-block { @apply mb-4; }
.day-label { @apply text-sm font-medium mb-2; color: #FF6B6B; }
.rest-text { @apply text-xs py-2; color: rgba(255,255,255,0.15); }
.ex-item {
  @apply flex items-center gap-3 py-2 px-3 rounded-lg mb-1;
  background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.03);
}
.ex-name { @apply text-sm; color: #fff; min-width: 80px; }
.ex-detail { @apply text-xs; color: rgba(255,255,255,0.3); }
.ex-note { @apply text-xs; color: rgba(255,255,255,0.15); }

.action-row { @apply flex gap-3 mt-8; }
.edit-btn, .toggle-btn {
  @apply flex-1 py-3 rounded-xl text-sm font-semibold transition-all cursor-pointer;
  border: none;
}
.edit-btn { background: linear-gradient(135deg, #FF6B6B, #FF8E53); color: #fff; }
.toggle-btn { background: rgba(255,255,255,0.04); color: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.08); }
.edit-btn:hover { transform: translateY(-1px); }
.toggle-btn:hover { color: #fff; }
</style>
