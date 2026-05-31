<template>
  <div class="checkin-page">
    <div class="page-container relative z-10">
      <!-- 头部 -->
      <div class="hero-section">
        <h1 class="page-title">今日打卡</h1>
        <div class="quota-badge" :class="{ full: todayCount >= 3 }">
          {{ todayCount }}/3 次
        </div>
      </div>

      <!-- 打卡表单 -->
      <form class="form-card" @submit.prevent="handleSubmit">
        <!-- 运动类型 -->
        <div class="form-section">
          <label class="form-label">运动类型</label>
          <div class="type-grid">
            <button type="button" v-for="t in exerciseTypes" :key="t.value"
              :class="['type-chip', { on: form.exerciseType === t.value }]"
              @click="form.exerciseType = t.value"
            >
              <span class="type-icon">{{ t.icon }}</span>
              <span class="type-name">{{ t.label }}</span>
            </button>
          </div>
          <!-- 自定义输入 -->
          <input v-if="form.exerciseType === '其他'"
            v-model="customType"
            class="form-input mt-3" placeholder="输入运动名称"
          />
        </div>

        <!-- 时长 -->
        <div class="form-section">
          <label class="form-label">运动时长（分钟）</label>
          <div class="duration-row">
            <button type="button" v-for="d in quickDurations" :key="d"
              :class="['dur-btn', { on: form.durationMinutes === d }]"
              @click="form.durationMinutes = d"
            >{{ d }}'</button>
          </div>
          <input v-model.number="form.durationMinutes" type="number"
            class="form-input mt-3" placeholder="自定义时长" min="1"
          />
        </div>

        <!-- 消耗 -->
        <div class="form-section">
          <label class="form-label">消耗卡路里（估算）</label>
          <input v-model.number="form.calories" type="number"
            class="form-input" placeholder="输入消耗卡路里" min="0"
          />
        </div>

        <!-- 备注 -->
        <div class="form-section">
          <label class="form-label">备注</label>
          <textarea v-model="form.note" class="form-input form-textarea"
            placeholder="记录今天的训练感受..."
            rows="3"
          ></textarea>
        </div>

        <p v-if="msg" :class="msgType">{{ msg }}</p>

        <button type="submit" class="submit-btn" :disabled="submitting || todayCount >= 3">
          <span v-if="submitting" class="spinner"></span>
          <span v-else-if="todayCount >= 3">今日已达上限</span>
          <span v-else>记录打卡</span>
        </button>
      </form>

      <!-- 历史记录 -->
      <div class="history-section">
        <h2 class="section-title">最近记录</h2>
        <div v-if="records.length === 0" class="empty-text">暂无打卡记录</div>
        <div v-for="r in records" :key="r.id" class="record-card">
          <div class="record-left">
            <span class="record-type">{{ r.exerciseType }}</span>
            <span class="record-note" v-if="r.note">{{ r.note }}</span>
          </div>
          <div class="record-right">
            <span class="record-dur">{{ r.durationMinutes }}分钟</span>
            <span class="record-cal">{{ r.calories }} kcal</span>
            <span class="record-time">{{ formatTime(r.checkInTime) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { checkinApi } from '@/api/checkin'

const form = reactive({ exerciseType: '力量训练', durationMinutes: 45, calories: null as number | null, note: '' })
const customType = ref('')
const submitting = ref(false)
const msg = ref('')
const msgType = ref('msg-success')
const todayCount = ref(0)
const records = ref<any[]>([])

const exerciseTypes = [
  { value: '力量训练', label: '力量训练', icon: '🏋️' },
  { value: '跑步', label: '跑步', icon: '🏃' },
  { value: 'HIIT', label: 'HIIT', icon: '⚡' },
  { value: '瑜伽', label: '瑜伽', icon: '🧘' },
  { value: '游泳', label: '游泳', icon: '🏊' },
  { value: '其他', label: '其他', icon: '💪' },
]
const quickDurations = [15, 30, 45, 60, 90, 120]

onMounted(async () => {
  await Promise.all([loadTodayCount(), loadRecords()])
})

async function loadTodayCount() {
  try {
    const { data: res } = await checkinApi.todayCount()
    if (res.code === 200) todayCount.value = res.data
  } catch { /* */ }
}

async function loadRecords() {
  try {
    const { data: res } = await checkinApi.list({ page: 1, size: 10 })
    if (res.code === 200) records.value = res.data.records || []
  } catch { /* */ }
}

async function handleSubmit() {
  msg.value = ''
  submitting.value = true
  try {
    const payload: any = { ...form }
    if (payload.exerciseType === '其他' && customType.value) {
      payload.exerciseType = customType.value
    }
    const { data: res } = await checkinApi.create(payload)
    if (res.code === 200) {
      msg.value = '打卡成功！'
      msgType.value = 'msg-success'
      form.note = ''
      form.calories = null
      customType.value = ''
      await Promise.all([loadTodayCount(), loadRecords()])
    } else {
      msg.value = res.message
      msgType.value = 'msg-error'
    }
  } catch {
    msg.value = '网络错误'
    msgType.value = 'msg-error'
  } finally {
    submitting.value = false
  }
}

function formatTime(t: string) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.checkin-page {
  @apply min-h-screen relative;
  background: var(--bg);
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}

.hero-section {
  @apply flex items-center justify-between mb-6;
  animation: fadeInUp 0.5s ease both;
}
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  @apply text-4xl tracking-wider;
  color: var(--text);
}
.quota-badge {
  @apply px-3 py-1 rounded-full text-sm font-bold;
  background: rgba(0, 245, 160, 0.1);
  color: #00F5A0;
  border: 1px solid rgba(0, 245, 160, 0.2);
}
.quota-badge.full {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  border-color: rgba(255, 107, 107, 0.2);
}

.form-card {
  @apply rounded-2xl p-6 mb-6;
  background: var(--card);
  border: 1px solid var(--border);
  animation: fadeInUp 0.5s 0.1s ease both;
}
.form-section { @apply mb-5; }
.form-label {
  @apply block text-xs mb-2 tracking-wider uppercase;
  color: var(--text-muted);
}
.form-input {
  @apply w-full px-4 py-3 rounded-xl text-sm transition-all duration-300;
  color: var(--text);
  background: var(--card);
  border: 1px solid var(--border);
  outline: none;
}
.form-input:focus {
  border-color: rgba(255, 107, 107, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}
.form-textarea { resize: vertical; }

.type-grid {
  @apply grid grid-cols-3 gap-2;
}
.type-chip {
  @apply py-3 rounded-xl text-center transition-all duration-200;
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  cursor: pointer;
}
.type-chip:hover { background: var(--card); }
.type-chip.on {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.4);
  color: #FF6B6B;
}
.type-icon { @apply block text-lg mb-1; }
.type-name { @apply text-xs; }

.duration-row { @apply flex flex-wrap gap-2; }
.dur-btn {
  @apply px-4 py-2 rounded-lg text-sm transition-all duration-200;
  background: var(--card);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  cursor: pointer;
}
.dur-btn.on {
  background: rgba(255, 107, 107, 0.1);
  border-color: rgba(255, 107, 107, 0.4);
  color: #FF6B6B;
}

.submit-btn {
  @apply w-full py-3 rounded-xl text-sm font-semibold tracking-widest transition-all duration-300 mt-4;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: var(--text);
  cursor: pointer;
}
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.submit-btn:not(:disabled):hover { transform: translateY(-1px); }
.submit-btn:not(:disabled):active { transform: scale(0.98); }

.msg-success { @apply text-sm mb-3; color: #00F5A0; }
.msg-error { @apply text-sm mb-3; color: #FF6B6B; }

.spinner {
  @apply inline-block w-5 h-5 border-2 border-white/30 border-t-white rounded-full;
  animation: spin 0.6s linear infinite;
}

.history-section {
  animation: fadeInUp 0.5s 0.2s ease both;
}
.section-title {
  @apply text-xs uppercase tracking-widest mb-3;
  color: var(--text-muted);
}
.empty-text {
  @apply text-sm text-center py-8;
  color: var(--text-muted);
}
.record-card {
  @apply flex justify-between items-start p-4 rounded-xl mb-2;
  background: var(--card);
  border: 1px solid var(--border);
}
.record-left { @apply flex flex-col; }
.record-type { @apply text-sm font-medium; color: var(--text); }
.record-note { @apply text-xs mt-1; color: var(--text-secondary); }
.record-right { @apply flex flex-col items-end gap-0.5; }
.record-dur { @apply text-sm font-bold; color: #FF6B6B; }
.record-cal { @apply text-xs; color: var(--text-secondary); }
.record-time { @apply text-xs; color: var(--text-muted); }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
