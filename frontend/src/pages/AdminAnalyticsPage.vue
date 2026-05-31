<template>
  <div class="page-container">
    <h1 class="page-title">数据看板</h1>

    <!-- 概览卡片 -->
    <div class="summary-grid">
      <div class="s-card"><span class="s-val">{{ dash.totalUsers || 0 }}</span><span class="s-lbl">学员总数</span></div>
      <div class="s-card"><span class="s-val">{{ dash.todayCheckIns || 0 }}</span><span class="s-lbl">今日打卡</span></div>
      <div class="s-card"><span class="s-val">{{ dash.activePlans || 0 }}</span><span class="s-lbl">进行中计划</span></div>
      <div class="s-card"><span class="s-val">{{ dash.coachCount || 0 }}</span><span class="s-lbl">教练数</span></div>
      <div class="s-card"><span class="s-val">{{ dash.anomalyCount || 0 }}</span><span class="s-lbl">异常提醒</span></div>
    </div>

    <!-- 打卡率排行 -->
    <div class="section-wrap">
      <h2 class="section-title">学员打卡率排行</h2>
      <div v-if="userRates.length === 0" class="empty">暂无数据</div>
      <div class="rank-list">
        <div v-for="(u, idx) in userRates" :key="idx" class="rank-row">
          <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
          <span class="rank-name">{{ u.username || '用户' + (u.userId || idx) }}</span>
          <div class="rank-bar-wrap">
            <div class="rank-bar" :style="{ width: (u.rate || 0) * 100 + '%' }"></div>
          </div>
          <span class="rank-pct">{{ Math.round((u.rate || 0) * 100) }}%</span>
        </div>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="section-wrap">
      <h2 class="section-title">打卡趋势</h2>
      <div class="period-switch">
        <button :class="['period-btn', { active: period === 'week' }]" @click="period = 'week'; loadTrend()">本周</button>
        <button :class="['period-btn', { active: period === 'month' }]" @click="period = 'month'; loadTrend()">本月</button>
      </div>
      <div ref="chartRef" class="chart-box"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import request from '@/api/request'
import * as echarts from 'echarts'

const dash = ref<any>({})
const userRates = ref<any[]>([])
const period = ref<'week' | 'month'>('week')
const chartRef = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null

onMounted(async () => {
  await loadDashboard()
  await loadTrend()
})

async function loadDashboard() {
  try {
    const r = await request.get('/admin/dashboard')
    if (r.data.code === 200) { dash.value = r.data.data; userRates.value = (r.data.data.userRates || []).slice(0, 20) }
  } catch {}
}

async function loadTrend() {
  try {
    const r = await request.get(`/stats/trend?period=${period.value}`)
    if (r.data.code === 200) {
      await nextTick()
      renderChart(r.data.data)
    }
  } catch {}
}

function renderChart(data: any) {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const dates = data?.dates || data?.labels || []
  const values = data?.values || data?.counts || data?.checkIns || []

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#888', fontSize: 11 },
      axisLine: { lineStyle: { color: '#333' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#888', fontSize: 11 },
      splitLine: { lineStyle: { color: '#e0e0e0' } },
    },
    series: [{
      data: values,
      type: 'bar',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#FF3B5C' },
          { offset: 1, color: '#FF8C00' },
        ]),
        borderRadius: [4, 4, 0, 0],
      },
      barWidth: period.value === 'week' ? 40 : 16,
    }],
  })
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: var(--text); margin-bottom: 1.5rem; }

.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
.s-card { padding: 1.25rem; border-radius: 1rem; background: var(--card); border: 1px solid var(--border); }
.s-val { display: block; font-size: 1.75rem; font-family: 'Bebas Neue', sans-serif; color: #FF3B5C; }
.s-lbl { font-size: 0.75rem; color: var(--text-secondary); }

.section-wrap { margin-bottom: 2rem; }
.section-title { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 1rem; color: var(--text-muted); }

.empty { font-size: 0.875rem; text-align: center; padding: 2rem 0; color: var(--text-muted); }

.rank-list { display: flex; flex-direction: column; gap: 0.5rem; }
.rank-row { display: flex; align-items: center; gap: 0.75rem; padding: 0.5rem 0.75rem; border-radius: 0.5rem; background: var(--card); }
.rank-num { width: 24px; text-align: center; font-size: 0.875rem; font-weight: 600; color: var(--text-muted); }
.rank-1 { color: #FFD700; }
.rank-2 { color: #C0C0C0; }
.rank-3 { color: #CD7F32; }
.rank-name { width: 80px; font-size: 0.875rem; color: var(--text); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-bar-wrap { flex: 1; height: 6px; border-radius: 3px; background: var(--card); overflow: hidden; }
.rank-bar { height: 100%; border-radius: 3px; background: linear-gradient(90deg, #FF3B5C, #FF8C00); transition: width 0.5s; }
.rank-pct { width: 48px; text-align: right; font-size: 0.75rem; color: var(--text-secondary); }

.period-switch { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
.period-btn { padding: 0.4rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: var(--card); border: 1px solid var(--border); color: var(--text-secondary); cursor: pointer; transition: all 0.2s; }
.period-btn:hover { color: var(--text-secondary); }
.period-btn.active { background: rgba(255,59,92,0.15); border-color: transparent; color: #FF3B5C; font-weight: 600; }

.chart-box { width: 100%; height: 320px; }
</style>
