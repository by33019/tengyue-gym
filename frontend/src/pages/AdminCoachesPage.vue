<template>
  <div class="page-container">
    <h1 class="page-title">教练管理</h1>

    <!-- 统计 -->
    <div class="summary-grid">
      <div class="s-card"><span class="s-val">{{ coachList.length }}</span><span class="s-lbl">教练总数</span></div>
      <div class="s-card"><span class="s-val">{{ totalStudents }}</span><span class="s-lbl">已分配学员</span></div>
      <div class="s-card"><span class="s-val">{{ unassignedCount }}</span><span class="s-lbl">未分配学员</span></div>
    </div>

    <div v-if="coachList.length === 0" class="empty">暂无教练</div>

    <!-- 教练卡片列表 -->
    <div v-for="c in coachList" :key="c.id" class="coach-card-wrap">
      <div class="coach-card" @click="toggleExpand(c.id)">
        <div class="coach-info">
          <span class="coach-avatar">👨‍🏫</span>
          <div>
            <span class="coach-name">{{ c.username }}</span>
            <span class="coach-meta">{{ c.studentCount || 0 }} 名学员</span>
          </div>
        </div>
        <span class="expand-icon">{{ expandedId === c.id ? '▾' : '▸' }}</span>
      </div>

      <!-- 展开学员列表 -->
      <div v-if="expandedId === c.id" class="student-sublist">
        <div v-if="c.students.length === 0" class="empty-small">该教练暂无学员</div>
        <div v-for="s in c.students" :key="s.id" class="student-row">
          <div class="student-info">
            <span class="student-name">{{ s.username }}</span>
            <span class="student-meta">{{ s.fitnessGoal || '无目标' }} · 打卡{{ s.checkInDays || 0 }}天</span>
          </div>
          <button class="transfer-btn" @click="openTransfer(s)">转移</button>
        </div>
      </div>
    </div>

    <!-- 未分配学员 -->
    <div class="section-title">未分配学员</div>
    <div v-if="unassigned.length === 0" class="empty-small">所有学员已分配教练</div>
    <div v-for="u in unassigned" :key="u.id" class="student-row">
      <div class="student-info">
        <span class="student-name">{{ u.username }}</span>
        <span class="student-meta">{{ u.fitnessGoal || '无目标' }} · {{ u.fitnessLevel || '未知等级' }}</span>
      </div>
      <button class="assign-btn" @click="openAssign(u)">分配</button>
    </div>

    <!-- 转移/分配弹窗 -->
    <div v-if="transferTarget" class="modal-overlay" @click.self="transferTarget = null">
      <div class="modal-box">
        <h3 class="modal-title">{{ transferMode === 'assign' ? '分配学员' : '转移学员' }}：{{ transferTarget.username }}</h3>
        <label class="modal-label">目标教练</label>
        <select v-model="targetCoachId" class="modal-select">
          <option :value="null">请选择教练</option>
          <option v-for="c in coachList" :key="c.id" :value="c.id">{{ c.username }}（{{ c.studentCount || 0 }}名学员）</option>
        </select>
        <div class="modal-actions">
          <button class="cancel-btn" @click="transferTarget = null">取消</button>
          <button class="send-btn" @click="doTransfer">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const coachList = ref<any[]>([])
const unassigned = ref<any[]>([])
const expandedId = ref<number | null>(null)

const transferTarget = ref<any>(null)
const transferMode = ref<'assign' | 'transfer'>('assign')
const targetCoachId = ref<number | null>(null)

const totalStudents = computed(() => coachList.value.reduce((sum, c) => sum + (c.studentCount || 0), 0))
const unassignedCount = computed(() => unassigned.value.length)

onMounted(async () => { await loadData() })

async function loadData() {
  try {
    const [coachRes, userRes] = await Promise.all([
      request.get('/admin/coaches'),
      request.get('/admin/users?role=0&size=2000')
    ])
    const coaches = coachRes.data.code === 200 ? coachRes.data.data : []
    const allStudents = userRes.data.code === 200 ? userRes.data.data.records : []

    coachList.value = coaches.map((c: any) => {
      const students = allStudents.filter((s: any) => s.coachId === c.id)
      return { ...c, students, studentCount: students.length }
    })

    unassigned.value = allStudents.filter((s: any) => !s.coachId)
  } catch {}
}

function toggleExpand(id: number) {
  expandedId.value = expandedId.value === id ? null : id
}

function openTransfer(s: any) {
  transferTarget.value = s
  transferMode.value = 'transfer'
  targetCoachId.value = null
}

function openAssign(u: any) {
  transferTarget.value = u
  transferMode.value = 'assign'
  targetCoachId.value = null
}

async function doTransfer() {
  if (!transferTarget.value || !targetCoachId.value) return

  try {
    if (transferMode.value === 'transfer') {
      await request.put(`/admin/unassign-student/${transferTarget.value.id}`, {})
    }
    await request.put(`/admin/assign-student/${transferTarget.value.id}`, { coachId: targetCoachId.value })
    transferTarget.value = null
    expandedId.value = null
    await loadData()
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: #fff; margin-bottom: 1.5rem; }

.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
.s-card { padding: 1.25rem; border-radius: 1rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { display: block; font-size: 1.75rem; font-family: 'Bebas Neue', sans-serif; color: #FF3B5C; }
.s-lbl { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: rgba(255,255,255,0.15); }
.empty-small { font-size: 0.75rem; text-align: center; padding: 1.5rem 0; color: rgba(255,255,255,0.1); }

.coach-card-wrap { margin-bottom: 0.5rem; }
.coach-card { display: flex; justify-content: space-between; align-items: center; padding: 1rem; border-radius: 0.75rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); cursor: pointer; transition: all 0.2s; }
.coach-card:hover { background: rgba(255,255,255,0.04); }
.coach-info { display: flex; align-items: center; gap: 0.75rem; }
.coach-avatar { font-size: 1.5rem; }
.coach-name { display: block; font-size: 0.875rem; color: #fff; font-weight: 500; }
.coach-meta { font-size: 0.75rem; color: rgba(255,255,255,0.3); }
.expand-icon { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.section-title { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; margin: 2rem 0 0.75rem; color: rgba(255,255,255,0.2); }

.student-sublist { margin-left: 1.5rem; border-left: 1px solid rgba(255,255,255,0.04); padding-left: 1rem; }
.student-row { display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; border-radius: 0.5rem; margin-bottom: 0.25rem; background: rgba(255,255,255,0.01); }
.student-info { flex: 1; }
.student-name { font-size: 0.875rem; color: #fff; }
.student-meta { display: block; font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.transfer-btn, .assign-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; cursor: pointer; transition: all 0.2s; }
.transfer-btn { background: rgba(255,140,0,0.1); border: 1px solid rgba(255,140,0,0.2); color: #FF8C00; }
.transfer-btn:hover { background: rgba(255,140,0,0.2); }
.assign-btn { background: rgba(0,245,160,0.1); border: 1px solid rgba(0,245,160,0.2); color: #00F5A0; }
.assign-btn:hover { background: rgba(0,245,160,0.2); }

.modal-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.6); }
.modal-box { width: 90%; max-width: 400px; padding: 1.5rem; border-radius: 1rem; background: #151520; border: 1px solid rgba(255,255,255,0.08); }
.modal-title { font-size: 1rem; color: #fff; margin-bottom: 1rem; }
.modal-label { display: block; font-size: 0.75rem; color: rgba(255,255,255,0.3); margin-bottom: 0.25rem; }
.modal-select { width: 100%; padding: 0.5rem 0.75rem; border-radius: 0.5rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; font-size: 0.875rem; outline: none; }
.modal-select option { background: #151520; color: #fff; }
.modal-select:focus { border-color: rgba(255,59,92,0.3); }
.modal-actions { display: flex; gap: 0.5rem; margin-top: 1rem; justify-content: flex-end; }
.send-btn { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,59,92,0.15); border: 1px solid rgba(255,59,92,0.2); color: #FF3B5C; cursor: pointer; }
.send-btn:hover { background: rgba(255,59,92,0.25); }
.cancel-btn { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); cursor: pointer; }
</style>
