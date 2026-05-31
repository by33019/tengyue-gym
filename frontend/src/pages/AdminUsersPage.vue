<template>
  <div class="page-container">
    <h1 class="page-title">用户管理</h1>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <select v-model="filterRole" class="filter-select">
        <option :value="null">全部角色</option>
        <option :value="0">学员</option>
        <option :value="1">教练</option>
        <option :value="2">管理员</option>
      </select>
      <select v-model="filterStatus" class="filter-select">
        <option :value="null">全部状态</option>
        <option :value="1">正常</option>
        <option :value="0">禁用</option>
      </select>
      <input v-model="searchQuery" placeholder="搜索用户名..." class="filter-search" />
      <button class="refresh-btn" @click="loadUsers">刷新</button>
    </div>

    <!-- 用户表格 -->
    <div v-if="users.length === 0" class="empty">暂无用户</div>
    <div v-else class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>角色</th>
            <th>健身目标</th>
            <th>等级</th>
            <th>教练</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" :class="{ 'row-disabled': !u.status }">
            <td>{{ u.id }}</td>
            <td class="td-name">{{ u.username }}</td>
            <td><span :class="roleTagClass(u.role)">{{ roleLabel(u.role) }}</span></td>
            <td>{{ u.fitnessGoal || '-' }}</td>
            <td>{{ u.fitnessLevel || '-' }}</td>
            <td>{{ u.coachName || '-' }}</td>
            <td><span :class="u.status ? 'tag-green' : 'tag-red'">{{ u.status ? '正常' : '禁用' }}</span></td>
            <td class="td-actions" v-if="u.id !== currentUserId">
              <button :class="['action-btn', u.status ? 'btn-warn' : 'btn-green']" @click="toggleUser(u)">{{ u.status ? '禁用' : '启用' }}</button>
              <button class="action-btn btn-danger" @click="deleteUser(u)">删除</button>
            </td>
            <td v-else class="td-actions"><span class="tag-green">当前账号</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pager">
      <button :disabled="page <= 1" @click="page--; loadUsers()">上一页</button>
      <span class="page-info">第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 人）</span>
      <button :disabled="page >= totalPages" @click="page++; loadUsers()">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id || 0)

const users = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 15
const filterRole = ref<number | null>(null)
const filterStatus = ref<number | null>(null)
const searchQuery = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

onMounted(() => { loadUsers() })

async function loadUsers() {
  let url = `/admin/users?page=${page.value}&size=${pageSize}`
  if (filterRole.value !== null) url += `&role=${filterRole.value}`
  if (filterStatus.value !== null) url += `&status=${filterStatus.value}`
  try { const r = await request.get(url); if (r.data.code === 200) { users.value = r.data.data.records; total.value = r.data.data.total } } catch {}
}

function roleLabel(r: number) { return ['学员', '教练', '管理员'][r] || '' }

function roleTagClass(r: number) {
  return r === 2 ? 'tag-red' : r === 1 ? 'tag-green' : 'tag-default'
}

async function toggleUser(u: any) {
  try {
    const r = await request.put(`/admin/user/${u.id}/status`, {})
    if (r.data.code === 200) u.status = u.status ? 0 : 1
  } catch (e: any) { alert('操作失败: ' + (e?.response?.data?.message || e.message)) }
}

async function deleteUser(u: any) {
  if (!confirm(`确定要永久删除用户 "${u.username}" 吗？此操作不可恢复！`)) return
  try {
    const r = await request.delete(`/admin/user/${u.id}`)
    if (r.data.code === 200) { loadUsers() }
  } catch (e: any) { alert('删除失败: ' + (e?.response?.data?.message || e.message)) }
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: var(--text); margin-bottom: 1.5rem; }

.filter-bar { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; flex-wrap: wrap; }
.filter-select { padding: 0.5rem 0.75rem; border-radius: 0.5rem; background: var(--card); border: 1px solid var(--border); color: var(--text); font-size: 0.875rem; outline: none; cursor: pointer; }
.filter-select option { background: #151520; color: var(--text); }
.filter-search { padding: 0.5rem 0.75rem; border-radius: 0.5rem; background: var(--card); border: 1px solid var(--border); color: var(--text); font-size: 0.875rem; outline: none; min-width: 160px; }
.filter-search::placeholder { color: var(--text-muted); }
.filter-search:focus { border-color: rgba(255,59,92,0.3); }
.refresh-btn { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: var(--card); border: 1px solid var(--border); color: var(--text); cursor: pointer; }
.refresh-btn:hover { color: var(--text); }

.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: var(--text-muted); }

.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; font-size: 0.875rem; }
.data-table th { text-align: left; padding: 0.75rem 1rem; color: var(--text-muted); font-weight: 500; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; border-bottom: 1px solid var(--border); }
.data-table td { padding: 0.75rem 1rem; color: var(--text); border-bottom: 1px solid var(--border); }
.data-table tr:hover td { background: var(--card); }
.row-disabled td { opacity: 0.4; }
.td-name { color: var(--text); font-weight: 500; }
.td-actions { display: flex; gap: 0.25rem; }

.action-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; border: none; cursor: pointer; transition: all 0.15s; }
.btn-warn { background: rgba(255,107,107,0.1); color: #FF6B6B; }
.btn-warn:hover { background: rgba(255,107,107,0.2); }
.btn-green { background: rgba(0,245,160,0.1); color: #00F5A0; }
.btn-green:hover { background: rgba(0,245,160,0.2); }
.btn-danger { background: rgba(255,59,92,0.1); color: #FF3B5C; }
.btn-danger:hover { background: rgba(255,59,92,0.2); }

.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; white-space: nowrap; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; white-space: nowrap; }
.tag-default { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: var(--card); color: var(--text-secondary); white-space: nowrap; }

.pager { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.5rem; }
.pager button { padding: 0.5rem 1rem; border-radius: 0.5rem; font-size: 0.875rem; background: var(--card); border: 1px solid var(--border); color: var(--text); cursor: pointer; }
.pager button:hover:not(:disabled) { color: var(--text); }
.pager button:disabled { opacity: 0.3; cursor: default; }
.page-info { font-size: 0.875rem; color: var(--text-secondary); }
</style>
