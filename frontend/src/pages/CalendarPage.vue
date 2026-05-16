<template>
  <div class="calendar-page">
    <div class="max-w-lg mx-auto px-4 py-8 relative z-10">
      <!-- 月份切换 -->
      <div class="header-row">
        <button class="nav-arrow" @click="prevMonth">←</button>
        <h1 class="page-title">{{ year }}年 {{ month }}月</h1>
        <button class="nav-arrow" @click="nextMonth">→</button>
      </div>

      <!-- 统计摘要 -->
      <div class="summary-row">
        <div class="summary-item">
          <span class="sum-num">{{ totalCheckIns }}</span>
          <span class="sum-label">本月打卡</span>
        </div>
        <div class="summary-item">
          <span class="sum-num">{{ totalMinutes }}</span>
          <span class="sum-label">总时长(分)</span>
        </div>
        <div class="summary-item">
          <span class="sum-num">{{ streakDays }}</span>
          <span class="sum-label">连续天数</span>
        </div>
      </div>

      <!-- 日历网格 -->
      <div class="calendar-card">
        <!-- 星期头 -->
        <div class="weekday-row">
          <span v-for="d in weekDays" :key="d" class="weekday-label">{{ d }}</span>
        </div>
        <!-- 日期网格 -->
        <div class="date-grid">
          <div v-for="(cell, i) in calendarCells" :key="i"
            :class="['date-cell', {
              'other-month': !cell.currentMonth,
              'today': cell.isToday,
              'has-data': cell.count > 0,
              'level-1': cell.count === 1,
              'level-2': cell.count === 2,
              'level-3': cell.count >= 3,
            }]"
          >
            <span class="date-num">{{ cell.day }}</span>
            <span v-if="cell.count > 0" class="date-dot">{{ cell.count }}</span>
          </div>
        </div>
      </div>

      <!-- 图例 -->
      <div class="legend">
        <span class="legend-label">打卡次数</span>
        <div class="legend-blocks">
          <span class="legend-block l0"></span>
          <span class="legend-block l1"></span>
          <span class="legend-block l2"></span>
          <span class="legend-block l3"></span>
        </div>
        <span class="legend-label">1</span>
        <span class="legend-label">2</span>
        <span class="legend-label">3</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { checkinApi } from '@/api/checkin'

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const calendarData = ref<Record<string, number>>({})
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

onMounted(() => loadCalendar())

async function loadCalendar() {
  try {
    const { data: res } = await checkinApi.calendar(year.value, month.value)
    if (res.code === 200) calendarData.value = res.data
  } catch { /* */ }
}

function prevMonth() {
  if (month.value === 1) { month.value = 12; year.value-- }
  else month.value--
  loadCalendar()
}
function nextMonth() {
  if (month.value === 12) { month.value = 1; year.value++ }
  else month.value++
  loadCalendar()
}

const calendarCells = computed(() => {
  const cells: { day: number; currentMonth: boolean; isToday: boolean; count: number }[] = []
  const firstDay = new Date(year.value, month.value - 1, 1)
  const lastDay = new Date(year.value, month.value, 0)
  const today = new Date()

  // 上个月填充
  const startDayOfWeek = firstDay.getDay() || 7 // Mon=1 Sun=7
  if (startDayOfWeek > 1) {
    const prevLast = new Date(year.value, month.value - 1, 0).getDate()
    for (let d = prevLast - startDayOfWeek + 2; d <= prevLast; d++) {
      cells.push({ day: d, currentMonth: false, isToday: false, count: 0 })
    }
  }

  // 本月
  for (let d = 1; d <= lastDay.getDate(); d++) {
    const key = `${year.value}-${String(month.value).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const isToday = d === today.getDate() && month.value === today.getMonth() + 1 && year.value === today.getFullYear()
    cells.push({ day: d, currentMonth: true, isToday, count: calendarData.value[key] || 0 })
  }

  // 下个月填充
  const endDayOfWeek = lastDay.getDay() || 7
  if (endDayOfWeek < 7) {
    for (let d = 1; d <= 7 - endDayOfWeek; d++) {
      cells.push({ day: d, currentMonth: false, isToday: false, count: 0 })
    }
  }

  return cells
})

const totalCheckIns = computed(() => Object.values(calendarData.value).reduce((a, b) => a + b, 0))
const totalMinutes = computed(() => totalCheckIns.value * 30) // 估算
const streakDays = computed(() => {
  const today = new Date()
  let streak = 0
  for (let i = 0; i < 100; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    if (calendarData.value[key] && calendarData.value[key] > 0) streak++
    else break
  }
  return streak
})
</script>

<style scoped>
.calendar-page {
  @apply min-h-screen relative;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}

.header-row {
  @apply flex items-center justify-between mb-6;
  animation: fadeInUp 0.5s ease both;
}
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  @apply text-3xl tracking-wider;
  color: #fff;
}
.nav-arrow {
  @apply w-10 h-10 rounded-xl text-lg transition-all duration-200;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
}
.nav-arrow:hover { background: rgba(255, 255, 255, 0.08); color: #fff; }

.summary-row {
  @apply flex gap-4 mb-6;
  animation: fadeInUp 0.5s 0.1s ease both;
}
.summary-item {
  @apply flex-1 p-4 rounded-xl text-center;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.sum-num {
  font-family: 'Bebas Neue', sans-serif;
  @apply block text-2xl font-bold;
  color: #FF6B6B;
}
.sum-label { @apply text-xs mt-1; color: rgba(255, 255, 255, 0.3); }

.calendar-card {
  @apply rounded-2xl p-4;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  animation: fadeInUp 0.5s 0.15s ease both;
}
.weekday-row {
  @apply grid grid-cols-7 mb-2;
}
.weekday-label {
  @apply text-center text-xs py-2;
  color: rgba(255, 255, 255, 0.2);
}
.date-grid {
  @apply grid grid-cols-7 gap-1;
}
.date-cell {
  @apply aspect-square rounded-lg flex flex-col items-center justify-center text-sm relative transition-all duration-200;
  color: rgba(255, 255, 255, 0.3);
}
.date-cell.other-month { color: rgba(255, 255, 255, 0.05); }
.date-cell.today {
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.4);
}
.date-cell.has-data { color: #fff; }
.date-cell.level-1 { background: rgba(255, 107, 107, 0.15); }
.date-cell.level-2 { background: rgba(255, 107, 107, 0.3); }
.date-cell.level-3 { background: rgba(255, 107, 107, 0.5); }
.date-num { @apply relative z-10 text-xs; }
.date-dot {
  @apply absolute bottom-1 text-[10px] font-bold;
  color: rgba(255, 255, 255, 0.6);
}

.legend {
  @apply flex items-center gap-2 mt-4 justify-end;
  animation: fadeInUp 0.5s 0.2s ease both;
}
.legend-label { @apply text-xs; color: rgba(255, 255, 255, 0.2); }
.legend-blocks { @apply flex gap-1; }
.legend-block {
  @apply w-3 h-3 rounded;
}
.l0 { background: rgba(255, 255, 255, 0.05); }
.l1 { background: rgba(255, 107, 107, 0.15); }
.l2 { background: rgba(255, 107, 107, 0.3); }
.l3 { background: rgba(255, 107, 107, 0.5); }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
