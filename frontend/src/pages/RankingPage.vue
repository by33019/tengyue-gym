<template>
  <div class="page">
    <div class="page-container">
      <h1 class="page-title">排行榜</h1>
      <div class="tab-row">
        <button :class="['tab', { on: tab === 'days' }]" @click="tab='days';load()">打卡天数</button>
        <button :class="['tab', { on: tab === 'calories' }]" @click="tab='calories';load()">消耗卡路里</button>
      </div>
      <div class="tab-row-sub">
        <button :class="['tab sm', { on: p === 'week' }]" @click="p='week';load()">本周</button>
        <button :class="['tab sm', { on: p === 'month' }]" @click="p='month';load()">本月</button>
      </div>

      <div v-if="list.length === 0" class="empty">暂无排行数据</div>
      <div v-for="(item, i) in list" :key="i" :class="['rank-card', i < 3 ? 'top-' + (i+1) : '']">
        <span class="rank-num">{{ i + 1 }}</span>
        <div class="rank-info">
          <span class="rank-name">{{ item.username || '匿名用户' }}</span>
        </div>
        <span class="rank-val">{{ item.value }} <small>{{ tab === 'days' ? '天' : 'kcal' }}</small></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { statsApi } from '@/api/stats'
const tab = ref('days'); const p = ref('week'); const list = ref<any[]>([])
import { onMounted } from 'vue'
onMounted(load)
async function load() {
  try { const r = await statsApi.ranking(tab.value, p.value); if (r.data.code===200) list.value = r.data.data } catch {}
}
</script>

<style scoped>
.page { @apply min-h-screen; background: #0a0a0f; font-family: 'Noto Sans SC', sans-serif; }
.page-title { font-family: 'Bebas Neue', sans-serif; @apply text-3xl tracking-wider mb-4; color: #fff; }
.tab-row, .tab-row-sub { @apply flex gap-2 mb-3; }
.tab { @apply px-4 py-2 rounded-lg text-sm; background: rgba(255,255,255,0.03); color: rgba(255,255,255,0.4); border: 1px solid rgba(255,255,255,0.05); cursor: pointer; transition: all 0.2s; }
.tab.sm { @apply px-3 py-1.5 text-xs; }
.tab.on { background: rgba(255,107,107,0.1); border-color: rgba(255,107,107,0.3); color: #FF6B6B; }
.empty { @apply text-sm text-center py-12; color: rgba(255,255,255,0.15); }
.rank-card { @apply flex items-center gap-4 p-4 rounded-xl mb-2; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.rank-card.top-1 { border-color: rgba(255,215,0,0.3); background: rgba(255,215,0,0.03); }
.rank-card.top-2 { border-color: rgba(192,192,192,0.2); }
.rank-card.top-3 { border-color: rgba(205,127,50,0.2); }
.rank-num { @apply w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold; color: rgba(255,255,255,0.3); }
.top-1 .rank-num { color: #FFD700; }
.top-2 .rank-num { color: #C0C0C0; }
.top-3 .rank-num { color: #CD7F32; }
.rank-info { @apply flex-1; }
.rank-name { @apply text-sm; color: #fff; }
.rank-val { @apply text-lg; font-family: 'Bebas Neue', sans-serif; color: #FF6B6B; }
.rank-val small { @apply text-xs; color: rgba(255,255,255,0.3); font-family: 'Noto Sans SC', sans-serif; }
</style>
