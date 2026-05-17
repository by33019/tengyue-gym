<template>
  <div class="page-container">
    <h1 class="page-title">{{ roleText }}工作台</h1>

    <!-- 概览卡片 -->
    <div class="summary-grid">
      <div class="s-card"><span class="s-val">{{ dash.totalUsers || 0 }}</span><span class="s-lbl">{{ userRole >= 2 ? '学员总数' : '我的学员' }}</span></div>
      <div class="s-card"><span class="s-val">{{ dash.todayCheckIns || 0 }}</span><span class="s-lbl">今日打卡</span></div>
      <div class="s-card"><span class="s-val">{{ dash.activePlans || 0 }}</span><span class="s-lbl">进行中计划</span></div>
      <div class="s-card"><span class="s-val">{{ dash.anomalyCount || 0 }}</span><span class="s-lbl">异常提醒</span></div>
      <div v-if="userRole >= 2" class="s-card"><span class="s-val">{{ dash.coachCount || 0 }}</span><span class="s-lbl">教练数</span></div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-row">
      <button v-for="t in tabs" :key="t.key" :class="['tab-btn', { active: activeTab === t.key }]" @click="activeTab = t.key">{{ t.label }}</button>
    </div>

    <!-- Tab: 学员管理 / 全部用户 -->
    <div v-if="activeTab === 'users'">
      <!-- 教练：学员分配 -->
      <div v-if="userRole === 1">
        <div class="section-title">我的学员</div>
        <div v-if="myStudents.length === 0" class="empty">暂无学员，从下方未分配列表中添加</div>
        <div v-for="u in myStudents" :key="u.id" :class="['user-card', { warn: u.needsRemind }]">
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

        <div class="section-title" v-if="unassignedStudents.length > 0">未分配学员</div>
        <div v-for="u in unassignedStudents" :key="u.id" class="user-card">
          <div class="user-info">
            <span class="user-name">{{ u.username }}</span>
            <span class="user-meta">{{ u.fitnessGoal || '未设置目标' }} · {{ u.fitnessLevel || '未知等级' }}</span>
          </div>
          <button class="remind-btn" @click="assignStudent(u)">收为学员</button>
        </div>
      </div>

      <!-- 管理员：全部用户管理 -->
      <div v-if="userRole >= 2">
        <div class="section-title">全部用户</div>
        <div v-if="userList.length === 0" class="empty">暂无用户</div>
        <div v-for="u in userList" :key="u.id" class="user-card">
          <div class="user-info">
            <span class="user-name">{{ u.username }}</span>
            <span class="user-meta">{{ ['学员','教练','管理员'][u.role] }} · {{ u.fitnessGoal || '无目标' }} · {{ u.coachName ? '教练: ' + u.coachName : '无教练' }}</span>
          </div>
          <button v-if="u.id !== currentUserId" :class="['toggle-btn', u.status ? '' : 'disabled']" @click="toggleUser(u)">
            {{ u.status ? '禁用' : '启用' }}
          </button>
          <span v-else class="tag-green">当前账号</span>
        </div>
      </div>
    </div>

    <!-- Tab: 内容审核 -->
    <div v-if="activeTab === 'posts'">
      <div v-if="postList.length === 0" class="empty">暂无动态</div>
      <div v-for="p in postList" :key="p.id" :class="['user-card', { deleted: p.status === 0 }]">
        <div class="user-info">
          <span class="user-name">{{ p.username }}</span>
          <span class="user-meta">{{ p.content }}</span>
        </div>
        <div class="user-stats">
          <span class="user-stat">👍 {{ p.likeCount }} 💬 {{ p.commentCount }}</span>
          <span v-if="p.status === 0" class="tag-red">已删除</span>
        </div>
        <button v-if="p.status !== 0 && userRole >= 2" class="remind-btn" @click="deletePost(p)">删除</button>
        <span v-if="p.status !== 0 && userRole < 2" class="tag-green">正常</span>
      </div>
    </div>

    <!-- Tab: 提醒记录 -->
    <div v-if="activeTab === 'alerts'">
      <div v-if="alertList.length === 0" class="empty">暂无记录</div>
      <div v-for="a in alertList" :key="a.id" class="user-card">
        <div class="user-info">
          <span class="user-name">{{ a.username }}</span>
          <span class="user-meta">{{ a.message }}</span>
        </div>
        <span :class="a.type === 'remind' ? 'tag-green' : 'tag-red'">{{ a.type === 'remind' ? '提醒' : '异常' }}</span>
        <span class="user-stat">{{ formatTime(a.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role ?? 0)
const currentUserId = computed(() => userStore.userInfo?.id || 0)
const roleText = computed(() => ['普通用户', '督导', '管理员'][userRole.value] || '')

const coachTabs = [
  { key: 'users', label: '学员管理' },
  { key: 'alerts', label: '提醒记录' },
]
const adminTabs = [
  { key: 'users', label: '用户管理' },
  { key: 'posts', label: '内容审核' },
  { key: 'alerts', label: '提醒记录' },
]
const tabs = computed(() => userRole.value >= 2 ? adminTabs : coachTabs)
const activeTab = ref('users')
const dash = ref<any>({})
const userList = ref<any[]>([])
const postList = ref<any[]>([])
const alertList = ref<any[]>([])
const myStudents = ref<any[]>([])
const unassignedStudents = ref<any[]>([])

watch(activeTab, () => { loadCurrentTab() })

onMounted(async () => {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code===200) dash.value = r.data.data } catch {}
  loadCurrentTab()
})

async function loadCurrentTab() {
  if (activeTab.value === 'users') {
    if (userRole.value === 1) {
      try { const r = await request.get('/admin/my-users'); if (r.data.code===200) myStudents.value = r.data.data } catch {}
      try { const r = await request.get('/admin/unassigned-students'); if (r.data.code===200) unassignedStudents.value = r.data.data } catch {}
    }
    if (userRole.value >= 2) {
      try { const r = await request.get('/admin/users?page=1&size=100'); if (r.data.code===200) userList.value = r.data.data.records } catch {}
    }
  }
  if (activeTab.value === 'posts') {
    try { const r = await request.get('/admin/posts?page=1&size=50'); if (r.data.code===200) postList.value = r.data.data.records } catch {}
  }
  if (activeTab.value === 'alerts') {
    try { const r = await request.get('/admin/alerts?page=1&size=50'); if (r.data.code===200) alertList.value = r.data.data.records } catch {}
  }
}

async function refreshDash() {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code===200) dash.value = r.data.data } catch {}
}

async function assignStudent(u: any) {
  try { const r = await request.put(`/admin/assign-student/${u.id}`); if (r.data.code===200) { unassignedStudents.value = unassignedStudents.value.filter(s => s.id !== u.id); loadCurrentTab(); refreshDash() } } catch {}
}

async function unassign(u: any) {
  try { const r = await request.put(`/admin/unassign-student/${u.id}`); if (r.data.code===200) { myStudents.value = myStudents.value.filter(s => s.id !== u.id); loadCurrentTab(); refreshDash() } } catch {}
}

async function remindOne(u: any) {
  await request.post('/admin/remind', { userIds: [u.id], message: '请记得今日打卡哦！' })
  u.needsRemind = false
}

async function toggleUser(u: any) {
  await request.put(`/admin/user/${u.id}/status`)
  u.status = u.status ? 0 : 1
}

async function deletePost(p: any) {
  if (!confirm('确定删除这条动态吗？')) return
  await request.delete(`/admin/post/${p.id}`)
  p.status = 0
}

function formatTime(t: string | null) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: #fff; margin-bottom: 1.5rem; }

.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
.s-card { padding: 1.25rem; border-radius: 1rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { display: block; font-size: 1.75rem; font-family: 'Bebas Neue', sans-serif; color: #FF3B5C; }
.s-lbl { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.tab-row { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; border-bottom: 1px solid rgba(255,255,255,0.06); padding-bottom: 0.75rem; }
.tab-btn { padding: 0.5rem 1.25rem; border-radius: 0.5rem; font-size: 0.875rem; background: none; border: none; color: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.2s; }
.tab-btn:hover { color: rgba(255,255,255,0.7); }
.tab-btn.active { background: rgba(255,59,92,0.15); color: #FF3B5C; font-weight: 600; }

.section-title { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; margin: 1.5rem 0 0.75rem; color: rgba(255,255,255,0.2); }
.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: rgba(255,255,255,0.15); }

.user-card { display: flex; flex-wrap: wrap; align-items: center; gap: 0.75rem; padding: 1rem; border-radius: 0.75rem; margin-bottom: 0.5rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.user-card.warn { border-color: rgba(255,107,107,0.2); }
.user-card.deleted { opacity: 0.5; }
.user-info { flex: 1; min-width: 150px; }
.user-name { display: block; font-size: 0.875rem; color: #fff; }
.user-meta { font-size: 0.75rem; color: rgba(255,255,255,0.3); }
.user-stats { display: flex; align-items: center; gap: 0.5rem; }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }
.user-stat { font-size: 0.75rem; color: rgba(255,255,255,0.5); }

.remind-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); border: 1px solid rgba(255,107,107,0.2); color: #FF6B6B; cursor: pointer; }
.remind-btn:hover { background: rgba(255,107,107,0.2); }
.toggle-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; cursor: pointer; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); }
.toggle-btn.disabled { background: rgba(0,245,160,0.1); border-color: rgba(0,245,160,0.2); color: #00F5A0; }
.toggle-btn:hover { color: #fff; }
</style>
