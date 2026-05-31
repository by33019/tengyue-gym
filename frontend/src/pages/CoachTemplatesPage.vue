<template>
  <div class="page-container">
    <h1 class="page-title">模板管理</h1>

    <!-- Tab 切换 -->
    <div class="tab-row">
      <button :class="['tab-btn', { active: activeTab === 'published' }]" @click="activeTab = 'published'">已发布模板</button>
      <button :class="['tab-btn', { active: activeTab === 'myplans' }]" @click="activeTab = 'myplans'">我的计划</button>
    </div>

    <!-- 已发布模板 -->
    <div v-if="activeTab === 'published'">
      <div v-if="templates.length === 0" class="empty">暂无已发布模板</div>
      <div class="card-grid">
        <div v-for="t in templates" :key="t.id" class="plan-card">
          <div class="card-header">
            <span class="card-name">{{ t.planName }}</span>
            <span :class="['diff-tag', diffClass(t.difficulty)]">{{ t.difficulty }}</span>
          </div>
          <div class="card-meta">
            <span>目标：{{ t.goal }}</span>
            <span>{{ t.startDate }} ~ {{ t.endDate }}</span>
          </div>
          <div class="card-footer">
            <span class="card-stat">{{ t.detailCount || 0 }} 个训练日</span>
            <button v-if="t.userId === currentUserId" class="unpublish-btn" @click="unpublish(t)">取消发布</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的计划 -->
    <div v-if="activeTab === 'myplans'">
      <div v-if="myPlans.length === 0" class="empty">暂无个人计划，请先去训练计划页面创建</div>
      <div class="card-grid">
        <div v-for="p in myPlans" :key="p.id" class="plan-card">
          <div class="card-header">
            <span class="card-name">{{ p.planName }}</span>
            <span :class="['diff-tag', diffClass(p.difficulty)]">{{ p.difficulty }}</span>
          </div>
          <div class="card-meta">
            <span>目标：{{ p.goal }}</span>
            <span>{{ p.startDate }} ~ {{ p.endDate }}</span>
          </div>
          <div class="card-footer">
            <span :class="p.status ? 'tag-green' : 'tag-red'">{{ p.status ? '进行中' : '已停用' }}</span>
            <button v-if="p.status" class="publish-btn" @click="publish(p)">发布为模板</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id ?? 0)

const activeTab = ref('published')
const templates = ref<any[]>([])
const myPlans = ref<any[]>([])

onMounted(async () => {
  await Promise.all([loadTemplates(), loadMyPlans()])
})

async function loadTemplates() {
  try { const r = await request.get('/plan/templates?page=1&size=100'); if (r.data.code === 200) templates.value = r.data.data.records } catch {}
}

async function loadMyPlans() {
  try { const r = await request.get('/plan/list?page=1&size=100'); if (r.data.code === 200) myPlans.value = r.data.data.records.filter((p: any) => !p.isTemplate) } catch {}
}

async function publish(p: any) {
  try {
    const r = await request.put(`/plan/${p.id}/publish`, {})
    if (r.data.code === 200) { p.isTemplate = true; await loadTemplates(); await loadMyPlans() }
  } catch (e: any) { alert('发布失败: ' + (e?.response?.data?.message || e.message)) }
}

async function unpublish(t: any) {
  try {
    const r = await request.put(`/plan/${t.id}/unpublish`, {})
    if (r.data.code === 200) { t.isTemplate = false; await loadTemplates(); await loadMyPlans() }
  } catch (e: any) { alert('取消发布失败: ' + (e?.response?.data?.message || e.message)) }
}

function diffClass(d: string) {
  if (d === '入门') return 'diff-beginner'
  if (d === '进阶') return 'diff-intermediate'
  return 'diff-advanced'
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: var(--text); margin-bottom: 1.5rem; }

.tab-row { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; }
.tab-btn { padding: 0.5rem 1.25rem; border-radius: 0.5rem; font-size: 0.875rem; background: none; border: 1px solid var(--border); color: var(--text-secondary); cursor: pointer; transition: all 0.2s; }
.tab-btn:hover { color: var(--text-secondary); }
.tab-btn.active { background: rgba(255,59,92,0.15); border-color: transparent; color: #FF3B5C; font-weight: 600; }

.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: var(--text-muted); }

.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; }

.plan-card { padding: 1.25rem; border-radius: 0.75rem; background: var(--card); border: 1px solid var(--border); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
.card-name { font-size: 1rem; color: var(--text); font-weight: 500; }
.card-meta { display: flex; gap: 0.75rem; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.75rem; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-stat { font-size: 0.75rem; color: var(--text-secondary); }

.diff-tag { padding: 0.2rem 0.5rem; border-radius: 0.25rem; font-size: 0.7rem; }
.diff-beginner { background: rgba(0,245,160,0.1); color: #00F5A0; }
.diff-intermediate { background: rgba(255,140,0,0.1); color: #FF8C00; }
.diff-advanced { background: rgba(255,59,92,0.1); color: #FF3B5C; }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }

.publish-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); border: 1px solid rgba(0,245,160,0.2); color: #00F5A0; cursor: pointer; transition: all 0.2s; }
.publish-btn:hover { background: rgba(0,245,160,0.2); }
.unpublish-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: var(--card); border: 1px solid var(--border); color: var(--text-secondary); cursor: pointer; transition: all 0.2s; }
.unpublish-btn:hover { color: #FF3B5C; border-color: rgba(255,59,92,0.2); }
</style>
