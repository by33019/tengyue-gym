<template>
  <div class="page-container">
    <h1 class="page-title">{{ roleText }}工作台</h1>

    <!-- 概览卡片 -->
    <div class="summary-grid">
      <div class="s-card"><span class="s-val">{{ dash.totalUsers }}</span><span class="s-lbl">健身用户</span></div>
      <div class="s-card"><span class="s-val">{{ dash.todayCheckIns }}</span><span class="s-lbl">今日打卡</span></div>
      <div class="s-card"><span class="s-val">{{ dash.activePlans }}</span><span class="s-lbl">进行中计划</span></div>
      <div class="s-card"><span class="s-val">{{ dash.anomalyCount || 0 }}</span><span class="s-lbl">异常提醒</span></div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-row">
      <button v-for="t in tabs" :key="t.key" :class="['tab-btn', { active: activeTab === t.key }]" @click="activeTab = t.key">
        {{ t.label }}
      </button>
    </div>

    <!-- Tab 1: 用户打卡 -->
    <div v-if="activeTab === 'users'">
      <div v-if="!dash.userRates || dash.userRates.length === 0" class="empty">暂无数据</div>
      <div v-for="u in dash.userRates" :key="u.userId" :class="['user-card', { warn: u.needsRemind }]">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">{{ u.fitnessGoal || '未设置目标' }} · 连续{{ u.streak }}天</span>
        </div>
        <div class="user-stats">
          <span class="user-stat">打卡{{ u.checkInDays }}天</span>
          <span :class="u.hasPlan ? 'tag-green' : 'tag-red'">{{ u.hasPlan ? '有计划' : '无计划' }}</span>
        </div>
        <button v-if="u.needsRemind" class="remind-btn" @click="remindOne(u)">提醒</button>
      </div>

      <!-- 管理员：用户列表 -->
      <div v-if="userRole >= 2" class="section-title">全部用户</div>
      <div v-if="userRole >= 2" v-for="u in userList" :key="u.id" class="user-card">
        <div class="user-info">
          <span class="user-name">{{ u.username }}</span>
          <span class="user-meta">{{ ['普通','督导','管理员'][u.role] }} · {{ u.fitnessGoal || '无目标' }}</span>
        </div>
        <button :class="['toggle-btn', u.status ? '' : 'disabled']" @click="toggleUser(u)">
          {{ u.status ? '禁用' : '启用' }}
        </button>
      </div>
    </div>

    <!-- Tab 2: 内容审核 -->
    <div v-if="activeTab === 'posts'">
      <div v-if="postList.length === 0" class="empty">暂无动态</div>
      <div v-for="p in postList" :key="p.id" :class="['user-card', { deleted: p.status === 0 }]">
        <div class="user-info">
          <span class="user-name">{{ p.username }}</span>
          <span class="user-meta">{{ p.content }}</span>
        </div>
        <div class="user-stats">
          <span class="user-stat">👍 {{ p.likeCount }}</span>
          <span class="user-stat">💬 {{ p.commentCount }}</span>
          <span v-if="p.status === 0" class="tag-red">已删除</span>
        </div>
        <button v-if="p.status !== 0" class="remind-btn" @click="deletePost(p)">删除</button>
      </div>
    </div>

    <!-- Tab 3: 提醒记录 -->
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
import { ref, watch, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role ?? 0)
const roleText = computed(() => ['普通用户', '督导', '管理员'][userRole.value] || '')

const tabs = [
  { key: 'users', label: '用户打卡' },
  { key: 'posts', label: '内容审核' },
  { key: 'alerts', label: '提醒记录' },
]
const activeTab = ref('users')
const dash = ref<any>({})
const userList = ref<any[]>([])
const postList = ref<any[]>([])
const alertList = ref<any[]>([])

watch(activeTab, () => { loadCurrentTab() })

onMounted(async () => {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code===200) dash.value = r.data.data } catch {}
  loadCurrentTab()
})

async function loadCurrentTab() {
  if (activeTab.value === 'users' && userRole.value >= 2) {
    try { const r = await request.get('/admin/users?page=1&size=50'); if (r.data.code===200) userList.value = r.data.data.records } catch {}
  }
  if (activeTab.value === 'posts') {
    try { const r = await request.get('/admin/posts?page=1&size=50'); if (r.data.code===200) postList.value = r.data.data.records } catch {}
  }
  if (activeTab.value === 'alerts') {
    try { const r = await request.get('/admin/alerts?page=1&size=50'); if (r.data.code===200) alertList.value = r.data.data.records } catch {}
  }
}

async function remindOne(u: any) {
  await request.post('/admin/remind', { userIds: [u.userId], message: '请记得今日打卡哦！' })
  u.needsRemind = false
  alert(`已向 ${u.username} 发送提醒`)
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

.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 1.5rem; }
@media (max-width: 767px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } }
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
.user-stat { font-size: 0.75rem; color: rgba(255,255,255,0.5); }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }

.remind-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); border: 1px solid rgba(255,107,107,0.2); color: #FF6B6B; cursor: pointer; }
.remind-btn:hover { background: rgba(255,107,107,0.2); }

.toggle-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; cursor: pointer; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); }
.toggle-btn.disabled { background: rgba(0,245,160,0.1); border-color: rgba(0,245,160,0.2); color: #00F5A0; }
.toggle-btn:hover { color: #fff; }
</style>
