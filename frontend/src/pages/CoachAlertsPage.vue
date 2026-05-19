<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">提醒日志</h1>
      <button class="send-btn" @click="showRemindModal = true">发送提醒</button>
    </div>

    <!-- 类型筛选 -->
    <div class="filter-bar">
      <button :class="['filter-chip', { active: filterType === null }]" @click="filterType = null; page = 1; loadAlerts()">全部</button>
      <button :class="['filter-chip', { active: filterType === 'remind' }]" @click="filterType = 'remind'; page = 1; loadAlerts()">提醒</button>
      <button :class="['filter-chip', { active: filterType === 'anomaly' }]" @click="filterType = 'anomaly'; page = 1; loadAlerts()">异常</button>
    </div>

    <!-- 列表 -->
    <div v-if="alerts.length === 0" class="empty">暂无记录</div>
    <div v-for="a in alerts" :key="a.id" class="alert-card">
      <div class="alert-info">
        <span class="alert-user">{{ a.username || '系统' }}</span>
        <span class="alert-msg">{{ a.message }}</span>
      </div>
      <div class="alert-meta">
        <span :class="a.type === 'remind' ? 'tag-green' : 'tag-red'">{{ a.type === 'remind' ? '提醒' : '异常' }}</span>
        <span class="alert-time">{{ formatTime(a.createdAt) }}</span>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pager" v-if="total > pageSize">
      <button :disabled="page <= 1" @click="page--; loadAlerts()">上一页</button>
      <span class="page-info">第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button :disabled="page >= totalPages" @click="page++; loadAlerts()">下一页</button>
    </div>

    <!-- 发送提醒弹窗 -->
    <div v-if="showRemindModal" class="modal-overlay" @click.self="showRemindModal = false">
      <div class="modal-box">
        <h3 class="modal-title">发送提醒</h3>
        <label class="modal-label">选择学员</label>
        <select v-model="selectedStudentId" class="modal-select">
          <option :value="null">请选择学员</option>
          <option v-for="s in myStudents" :key="s.id" :value="s.id">{{ s.username }}</option>
        </select>
        <label class="modal-label">提醒内容</label>
        <textarea v-model="remindMessage" class="modal-textarea" rows="3" placeholder="请输入提醒内容..."></textarea>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showRemindModal = false">取消</button>
          <button class="send-btn" @click="doSendRemind">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const alerts = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const filterType = ref<string | null>(null)
const myStudents = ref<any[]>([])

const showRemindModal = ref(false)
const selectedStudentId = ref<number | null>(null)
const remindMessage = ref('请记得今日打卡哦！')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

onMounted(async () => {
  await Promise.all([loadAlerts(), loadStudents()])
})

async function loadAlerts() {
  let url = `/admin/alerts?page=${page.value}&size=${pageSize}`
  if (filterType.value) url += `&type=${filterType.value}`
  try { const r = await request.get(url); if (r.data.code === 200) { alerts.value = r.data.data.records; total.value = r.data.data.total } } catch {}
}

async function loadStudents() {
  try { const r = await request.get('/admin/my-users'); if (r.data.code === 200) myStudents.value = r.data.data } catch {}
}

async function doSendRemind() {
  if (!selectedStudentId.value) return
  try {
    const r = await request.post('/admin/remind', { userIds: [selectedStudentId.value], message: remindMessage.value })
    if (r.data.code === 200) { showRemindModal.value = false; selectedStudentId.value = null; remindMessage.value = '请记得今日打卡哦！'; page.value = 1; await loadAlerts() }
  } catch (e: any) { alert('发送失败: ' + (e?.response?.data?.message || e.message)) }
}

function formatTime(t: string | null) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: #fff; }

.filter-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.filter-chip { padding: 0.4rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.2s; }
.filter-chip:hover { color: rgba(255,255,255,0.7); }
.filter-chip.active { background: rgba(255,59,92,0.15); border-color: transparent; color: #FF3B5C; font-weight: 600; }

.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: rgba(255,255,255,0.15); }

.alert-card { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding: 1rem; border-radius: 0.75rem; margin-bottom: 0.5rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); flex-wrap: wrap; }
.alert-info { flex: 1; min-width: 200px; }
.alert-user { display: block; font-size: 0.875rem; color: #fff; }
.alert-msg { font-size: 0.75rem; color: rgba(255,255,255,0.4); }
.alert-meta { display: flex; align-items: center; gap: 0.75rem; }
.alert-time { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }

.pager { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
.pager button { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.6); cursor: pointer; }
.pager button:hover:not(:disabled) { color: #fff; }
.pager button:disabled { opacity: 0.3; cursor: default; }
.page-info { font-size: 0.875rem; color: rgba(255,255,255,0.3); }

.send-btn { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,59,92,0.15); border: 1px solid rgba(255,59,92,0.2); color: #FF3B5C; cursor: pointer; transition: all 0.2s; }
.send-btn:hover { background: rgba(255,59,92,0.25); }
.cancel-btn { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); color: rgba(255,255,255,0.5); cursor: pointer; }

.modal-overlay { position: fixed; inset: 0; z-index: 50; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.6); }
.modal-box { width: 90%; max-width: 420px; padding: 1.5rem; border-radius: 1rem; background: #151520; border: 1px solid rgba(255,255,255,0.08); }
.modal-title { font-size: 1rem; color: #fff; margin-bottom: 1rem; }
.modal-label { display: block; font-size: 0.75rem; color: rgba(255,255,255,0.3); margin-bottom: 0.25rem; margin-top: 0.75rem; }
.modal-select { width: 100%; padding: 0.5rem 0.75rem; border-radius: 0.5rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; font-size: 0.875rem; outline: none; }
.modal-select option { background: #151520; color: #fff; }
.modal-textarea { width: 100%; padding: 0.75rem; border-radius: 0.5rem; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; font-size: 0.875rem; resize: vertical; outline: none; margin-top: 0.25rem; }
.modal-textarea:focus, .modal-select:focus { border-color: rgba(255,59,92,0.3); }
.modal-actions { display: flex; gap: 0.5rem; margin-top: 1rem; justify-content: flex-end; }
</style>
