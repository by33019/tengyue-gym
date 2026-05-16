<template>
  <div class="page">
    <div class="max-w-lg mx-auto px-4 py-6">
      <h1 class="page-title">数据统计</h1>
      <div class="tab-row">
        <button :class="['tab', { on: period === 'week' }]" @click="period='week';loadTrend()">周</button>
        <button :class="['tab', { on: period === 'month' }]" @click="period='month';loadTrend()">月</button>
      </div>

      <div class="summary-grid">
        <div class="s-card" v-for="s in summaryCards" :key="s.label">
          <span class="s-val">{{ s.value }}</span>
          <span class="s-lbl">{{ s.label }}</span>
        </div>
      </div>

      <div class="chart-card">
        <div ref="chartRef" class="chart-box"></div>
      </div>

      <div class="section-title">运动类型分布</div>
      <div ref="pieRef" class="chart-box chart-pie"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { statsApi } from '@/api/stats'

const period = ref('week')
const chartRef = ref<HTMLDivElement>()
const pieRef = ref<HTMLDivElement>()
const summary = ref<any>({})
const trend = ref<any>({ labels: [], values: [] })

const summaryCards = computed(() => [
  { label: '总时长(分)', value: summary.value.totalMinutes ?? '...' },
  { label: '总消耗(kcal)', value: summary.value.totalCalories ?? '...' },
  { label: '打卡天数', value: summary.value.totalDays ?? '...' },
  { label: '周运动(分)', value: summary.value.thisWeekMinutes ?? '...' },
])

onMounted(() => { loadData() })

async function loadData() {
  try { const r = await statsApi.summary(); if (r.data.code===200) summary.value = r.data.data } catch {}
  await loadTrend()
}
async function loadTrend() {
  try { const r = await statsApi.trend(period.value); if (r.data.code===200) trend.value = r.data.data; drawChart() } catch {}
}

function drawChart() {
  if (!chartRef.value) return
  const c = echarts.init(chartRef.value)
  c.setOption({
    grid: { top: 10, right: 10, bottom: 20, left: 40 },
    xAxis: { type: 'category', data: trend.value.labels?.map((l:string) => l.slice(5)) || [], axisLabel: { color: '#666', fontSize: 10 } },
    yAxis: { type: 'value', axisLabel: { color: '#666', fontSize: 10 }, splitLine: { lineStyle: { color: '#1a1a20' } } },
    series: [{ data: trend.value.values || [], type: 'line', smooth: true, symbol: 'none',
      lineStyle: { color: '#FF6B6B', width: 2 }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[
        { offset: 0, color: 'rgba(255,107,107,0.3)' }, { offset: 1, color: 'rgba(255,107,107,0)' }]) } }]
  })
  window.addEventListener('resize', () => c.resize())
}

watch(period, drawChart)
</script>

<style scoped>
.page { @apply min-h-screen; background: #0a0a0f; font-family: 'Noto Sans SC', sans-serif; }
.page-title { font-family: 'Bebas Neue', sans-serif; @apply text-3xl tracking-wider mb-4; color: #fff; }
.tab-row { @apply flex gap-2 mb-4; }
.tab { @apply px-4 py-2 rounded-lg text-sm; background: rgba(255,255,255,0.03); color: rgba(255,255,255,0.4); border: 1px solid rgba(255,255,255,0.05); cursor: pointer; transition: all 0.2s; }
.tab.on { background: rgba(255,107,107,0.1); border-color: rgba(255,107,107,0.3); color: #FF6B6B; }
.summary-grid { @apply grid grid-cols-2 gap-3 mb-4; }
.s-card { @apply p-4 rounded-xl; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { @apply block text-2xl; font-family: 'Bebas Neue', sans-serif; color: #FF6B6B; }
.s-lbl { @apply text-xs; color: rgba(255,255,255,0.3); }
.chart-card { @apply rounded-2xl p-4 mb-4; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.chart-box { width: 100%; height: 200px; }
.chart-pie { height: 250px; }
.section-title { @apply text-xs uppercase tracking-wider mb-3; color: rgba(255,255,255,0.2); }
</style>
