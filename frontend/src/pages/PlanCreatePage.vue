<template>
  <div class="create-page">
    <div class="max-w-lg mx-auto px-4 py-8 relative z-10">
      <div class="header-row">
        <button class="back-btn" @click="router.back()">← 返回</button>
        <h1 class="page-title">{{ editId ? '编辑计划' : '创建计划' }}</h1>
        <div></div>
      </div>

      <form class="form-card" @submit.prevent="handleSubmit">
        <!-- 基本信息 -->
        <div class="form-group">
          <label class="form-label">计划名称</label>
          <input v-model="form.planName" class="form-input" placeholder="例如：增肌三分化训练" />
        </div>
        <div class="form-row-3">
          <div class="form-group">
            <label class="form-label">目标</label>
            <select v-model="form.goal" class="form-input">
              <option v-for="g in goals" :key="g" :value="g">{{ g }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">难度</label>
            <select v-model="form.difficulty" class="form-input">
              <option v-for="l in levels" :key="l" :value="l">{{ l }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">来源</label>
            <select v-model="form.source" class="form-input">
              <option value="手动">手动</option>
              <option value="模板">模板</option>
            </select>
          </div>
        </div>
        <div class="form-row-2">
          <div class="form-group">
            <label class="form-label">开始日期</label>
            <input v-model="form.startDate" type="date" class="form-input" />
          </div>
          <div class="form-group">
            <label class="form-label">结束日期</label>
            <input v-model="form.endDate" type="date" class="form-input" />
          </div>
        </div>

        <!-- 周计划编辑 -->
        <div class="section-header">
          <span class="section-title">每周训练安排</span>
        </div>

        <div v-for="(day, di) in weekDays" :key="di" class="day-block">
          <div class="day-header" @click="toggleDay(di)">
            <span class="day-name">{{ day }}</span>
            <span class="day-count">{{ getDayDetails(di + 1).length }} 项</span>
            <span class="day-arrow" :class="{ open: expandedDay === di }">▾</span>
          </div>
          <div v-if="expandedDay === di" class="day-body">
            <div v-for="(ex, ei) in getDayDetails(di + 1)" :key="ei" class="exercise-row">
              <input v-model="ex.exerciseType" class="ex-input" placeholder="动作名称" />
              <input v-model.number="ex.sets" class="ex-num" placeholder="组" type="number" min="0" />
              <span class="ex-times">×</span>
              <input v-model.number="ex.reps" class="ex-num" placeholder="次" type="number" min="0" />
              <button type="button" class="ex-remove" @click="removeExercise(di + 1, ei)">✕</button>
            </div>
            <button type="button" class="add-ex-btn" @click="addExercise(di + 1)">+ 添加动作</button>
          </div>
        </div>

        <p v-if="msg" :class="msgType">{{ msg }}</p>
        <button type="submit" class="submit-btn" :disabled="submitting">保存计划</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { planApi } from '@/api/plan'

const router = useRouter()
const route = useRoute()
const editId = ref<number | null>(null)
const submitting = ref(false)
const msg = ref('')
const msgType = ref('msg-success')
const expandedDay = ref(0)

const goals = ['减脂', '增肌', '塑形', '保持健康']
const levels = ['入门', '进阶', '高级']
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const form = reactive({
  planName: '', goal: '增肌', difficulty: '进阶', source: '手动',
  startDate: new Date().toISOString().slice(0, 10),
  endDate: new Date(Date.now() + 30*86400000).toISOString().slice(0, 10),
  details: [] as any[]
})

onMounted(async () => {
  const id = route.query.edit
  if (id) {
    editId.value = Number(id)
    try {
      const { data: res } = await planApi.getById(editId.value)
      if (res.code === 200) {
        const d = res.data
        form.planName = d.planName
        form.goal = d.goal
        form.difficulty = d.difficulty
        form.source = d.source
        form.startDate = d.startDate
        form.endDate = d.endDate
        form.details = d.details || []
      }
    } catch { /* */ }
  }
})

function getDayDetails(day: number) {
  return form.details.filter((d: any) => d.dayOfWeek === day)
}
function toggleDay(di: number) {
  expandedDay.value = expandedDay.value === di ? -1 : di
}
function addExercise(day: number) {
  form.details.push({ dayOfWeek: day, exerciseType: '', sets: 3, reps: 12, duration: 0, note: '' })
}
function removeExercise(day: number, ei: number) {
  const dayDetails = getDayDetails(day)
  const realIndex = form.details.indexOf(dayDetails[ei])
  if (realIndex >= 0) form.details.splice(realIndex, 1)
}

async function handleSubmit() {
  msg.value = ''
  submitting.value = true
  try {
    const payload = { ...form }
    const { data: res } = editId.value
      ? await planApi.update(editId.value, payload)
      : await planApi.create(payload)

    if (res.code === 200) {
      router.push('/plan')
    } else {
      msg.value = res.message; msgType.value = 'msg-error'
    }
  } catch {
    msg.value = '网络错误'; msgType.value = 'msg-error'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-page {
  @apply min-h-screen;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
.header-row {
  @apply flex items-center justify-between mb-6;
  animation: fadeInUp 0.5s ease both;
}
.back-btn {
  @apply text-sm transition-all duration-200;
  background: none; border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
}
.back-btn:hover { color: #fff; }
.page-title {
  font-family: 'Bebas Neue', sans-serif;
  @apply text-3xl tracking-wider;
  color: #fff;
}

.form-card {
  animation: fadeInUp 0.5s 0.1s ease both;
}
.form-group { @apply mb-4 flex-1; }
.form-label {
  @apply block text-xs text-white/40 mb-2;
}
.form-input, select.form-input {
  @apply w-full px-4 py-3 rounded-xl text-sm text-white transition-all duration-300;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  outline: none;
  appearance: none;
}
.form-input:focus {
  border-color: rgba(255, 107, 107, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}
select.form-input { background-image: none; }
select.form-input option {
  background: #1a1a20;
  color: #fff;
}

.form-row-3 { @apply grid grid-cols-3 gap-3; }
.form-row-2 { @apply grid grid-cols-2 gap-3; }

.section-header { @apply mt-6 mb-4; }
.section-title { @apply text-sm; color: rgba(255, 255, 255, 0.5); }

.day-block { @apply mb-2; }
.day-header {
  @apply flex items-center p-3 rounded-xl cursor-pointer transition-all duration-200;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
}
.day-header:hover { background: rgba(255, 255, 255, 0.04); }
.day-name { @apply text-sm font-medium; color: #fff; width: 60px; }
.day-count { @apply flex-1 text-xs; color: rgba(255, 255, 255, 0.2); }
.day-arrow { @apply text-xs transition-transform duration-300; color: rgba(255, 255, 255, 0.3); }
.day-arrow.open { transform: rotate(180deg); }

.day-body {
  @apply p-3 rounded-b-xl;
  background: rgba(255, 255, 255, 0.01);
  border: 1px solid rgba(255, 255, 255, 0.03);
  border-top: none;
  animation: fadeIn 0.3s ease;
}
.exercise-row { @apply flex items-center gap-2 mb-2; }
.ex-input {
  @apply flex-1 px-3 py-2 rounded-lg text-xs;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: #fff;
  outline: none;
}
.ex-input:focus { border-color: rgba(255, 107, 107, 0.3); }
.ex-num {
  @apply w-14 px-2 py-2 rounded-lg text-xs text-center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: #fff;
  outline: none;
}
.ex-times { color: rgba(255, 255, 255, 0.2); font-size: 12px; }
.ex-remove {
  @apply w-6 h-6 rounded-full flex items-center justify-center text-xs transition-all;
  background: none; border: none;
  color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
}
.ex-remove:hover { color: #FF6B6B; background: rgba(255, 107, 107, 0.1); }

.add-ex-btn {
  @apply w-full py-2 rounded-lg text-xs transition-all duration-200 mt-1;
  background: rgba(255, 255, 255, 0.02);
  border: 1px dashed rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
}
.add-ex-btn:hover { background: rgba(255, 255, 255, 0.04); color: rgba(255, 255, 255, 0.5); }

.submit-btn {
  @apply w-full py-3 rounded-xl text-sm font-semibold tracking-widest transition-all duration-300 mt-6;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  cursor: pointer;
}
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.submit-btn:not(:disabled):hover { transform: translateY(-1px); }

.msg-success { @apply text-sm mt-3; color: #00F5A0; }
.msg-error { @apply text-sm mt-3; color: #FF6B6B; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
