<template>
  <div class="page-container">
    <h1 class="page-title">学员管理</h1>

    <!-- 统计卡片 -->
    <div class="summary-grid">
      <div class="s-card"><span class="s-val">{{ stats.totalStudents }}</span><span class="s-lbl">学员总数</span></div>
      <div class="s-card"><span class="s-val">{{ stats.todayCheckins }}</span><span class="s-lbl">今日打卡</span></div>
      <div class="s-card"><span class="s-val">{{ stats.needsRemind }}</span><span class="s-lbl">需提醒</span></div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-row">
      <button :class="['tab-btn', { active: activeTab === 'assigned' }]" @click="activeTab = 'assigned'">已分配学员</button>
      <button :class="['tab-btn', { active: activeTab === 'unassigned' }]" @click="activeTab = 'unassigned'">未分配学员</button>
    </div>

    <!-- 搜索框 -->
    <div class="search-bar">
      <input v-model="searchQuery" placeholder="搜索学员用户名..." class="search-input" />
    </div>

    <!-- 已分配学员 -->
    <div v-if="activeTab === 'assigned'">
      <div v-if="filteredMyStudents.length === 0" class="empty">暂无已分配学员</div>
      <div v-for="u in filteredMyStudents" :key="u.id" :class="['user-card', { warn: u.needsRemind }]">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">{{ u.fitnessGoal || '未设置目标' }} · 连续{{ u.streak }}天</span>
        </div>
        <div class="user-stats">
          <span class="user-stat">打卡{{ u.checkInDays }}天</span>
          <span :class="u.hasPlan ? 'tag-green' : 'tag-red'">{{ u.hasPlan ? '有计划' : '无计划' }}</span>
          <button v-if="u.needsRemind" class="remind-btn" @click="remindOne(u)">提醒</button>
          <button class="toggle-btn" @click="unassign(u)">移除</button>
        </div>
      </div>
    </div>

    <!-- 未分配学员 -->
    <div v-if="activeTab === 'unassigned'">
      <div v-if="filteredUnassigned.length === 0" class="empty">暂无未分配学员</div>
      <div v-for="u in filteredUnassigned" :key="u.id" class="user-card">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">{{ u.fitnessGoal || '未设置目标' }} · {{ u.fitnessLevel || '未知等级' }}</span>
        </div>
        <div class="user-stats">
          <button class="remind-btn" @click="assignStudent(u)">收为学员</button>
        </div>
      </div>
    </div>

    <!-- 提醒弹窗 -->
    <div v-if="remindTarget" class="modal-overlay" @click.self="remindTarget = null">
      <div class="modal-box">
        <h3 class="modal-title">发送提醒给 {{ remindTarget.username }}</h3>
        <textarea v-model="remindMsg" class="modal-textarea" rows="3" placeholder="请输入提醒内容..."></textarea>
        <div class="modal-actions">
          <button class="toggle-btn" @click="remindTarget = null">取消</button>
          <button class="remind-btn" @click="doRemind">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const activeTab = ref('assigned')
const searchQuery = ref('')
const myStudents = ref<any[]>([])
const unassignedStudents = ref<any[]>([])
const stats = ref({ totalStudents: 0, todayCheckins: 0, needsRemind: 0 })
const remindTarget = ref<any>(null)
const remindMsg = ref('请记得今日打卡哦！')

const filteredMyStudents = computed(() => {
  if (!searchQuery.value) return myStudents.value
  const q = searchQuery.value.toLowerCase()
  return myStudents.value.filter((u: any) => u.username.toLowerCase().includes(q))
})

const filteredUnassigned = computed(() => {
  if (!searchQuery.value) return unassignedStudents.value
  const q = searchQuery.value.toLowerCase()
  return unassignedStudents.value.filter((u: any) => u.username.toLowerCase().includes(q))
})

onMounted(async () => {
  await Promise.all([loadStudents(), loadUnassigned(), loadDashboard()])
})

async function loadDashboard() {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code === 200) { const d = r.data.data; stats.value = { totalStudents: d.totalUsers || 0, todayCheckins: d.todayCheckIns || 0, needsRemind: d.anomalyCount || 0 } } } catch {}
}

async function loadStudents() {
  try { const r = await request.get('/admin/my-users'); if (r.data.code === 200) myStudents.value = r.data.data } catch {}
}

async function loadUnassigned() {
  try { const r = await request.get('/admin/unassigned-students'); if (r.data.code === 200) unassignedStudents.value = r.data.data } catch {}
}

async function assignStudent(u: any) {
  try {
    const r = await request.put(`/admin/assign-student/${u.id}`, {})
    if (r.data.code === 200) {
      unassignedStudents.value = unassignedStudents.value.filter(s => s.id !== u.id)
      await Promise.all([loadStudents(), loadDashboard()])
    }
  } catch (e: any) { alert('分配失败: ' + (e?.response?.data?.message || e.message)) }
}

async function unassign(u: any) {
  try {
    const r = await request.put(`/admin/unassign-student/${u.id}`, {})
    if (r.data.code === 200) {
      myStudents.value = myStudents.value.filter(s => s.id !== u.id)
      await Promise.all([loadUnassigned(), loadDashboard()])
    }
  } catch (e: any) { alert('移除失败: ' + (e?.response?.data?.message || e.message)) }
}

function remindOne(u: any) {
  remindTarget.value = u
  remindMsg.value = '请记得今日打卡哦！'
}

async function doRemind() {
  if (!remindTarget.value) return
  try {
    const r = await request.post('/admin/remind', { userIds: [remindTarget.value.id], message: remindMsg.value })
    if (r.data.code === 200) {
      const idx = myStudents.value.findIndex(s => s.id === remindTarget.value.id)
      if (idx >= 0) myStudents.value[idx].needsRemind = false
      remindTarget.value = null
    }
  } catch (e: any) { alert('提醒失败: ' + (e?.response?.data?.message || e.message)) }
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: #fff; margin-bottom: 1.5rem; }

.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
.s-card { padding: 1.25rem; border-radius: 1rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { display: block; font-size: 1.75rem; font-family: 'Bebas Neue', sans-serif; color: #FF3B5C; }
.s-lbl { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.tab-row { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
.tab-btn { padding: 0.5rem 1.25rem; border-radius: 0.5rem; font-size: 0.875rem; background: none; border: 1px solid rgba(255,255,255,0.06); color: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.2s; }
.tab-btn:hover { color: rgba(255,255,255,0.7); }
.tab-btn.active { background: rgba(255,59,92,0.15); border-color: transparent; color: #FF3B5C; font-weight: 600; }

.search-bar { margin-bottom: 1rem; }
.search-input { width: 100%; max-width: 320px; padding: 0.5rem 1rem; border-radius: 0.5rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; font-size: 0.875rem; outline: none; transition: border-color 0.2s; }
.search-input::placeholder { color: rgba(255,255,255,0.2); }
.search-input:focus { border-color: rgba(255,59,92,0.3); }

.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: rgba(255,255,255,0.15); }

.user-card { display: flex; flex-wrap: wrap; align-items: center; gap: 0.75rem; padding: 1rem; border-radius: 0.75rem; margin-bottom: 0.5rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.user-card.warn { border-color: rgba(255,107,107,0.2); }
.user-info { flex: 1; min-width: 150px; }
.user-name { display: block; font-size: 0.875rem; color: #fff; }
.user-meta { font-size: 0.75rem; color: rgba(255,255,255,0.3); }
.user-stats { display: flex; align-items: center; gap: 0.5rem; }
.user-stat { font-size: 0.75rem; color: rgba(255,255,255,0.5); }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }

.remind-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); border: 1px solid rgba(255,107,107,0.2); color: #FF6B6B; cursor: pointer; }
.remind-btn:hover { background: rgba(255,107,107,0.2); }
.toggle-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; cursor: pointer; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); }
.toggle-btn:hover { color: #fff; }

.modal-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.6); }
.modal-box { width: 90%; max-width: 400px; padding: 1.5rem; border-radius: 1rem; background: #151520; border: 1px solid rgba(255,255,255,0.08); }
.modal-title { font-size: 1rem; color: #fff; margin-bottom: 1rem; }
.modal-textarea { width: 100%; padding: 0.75rem; border-radius: 0.5rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; font-size: 0.875rem; resize: vertical; outline: none; }
.modal-textarea:focus { border-color: rgba(255,59,92,0.3); }
.modal-actions { display: flex; gap: 0.5rem; margin-top: 1rem; justify-content: flex-end; }
</style>
