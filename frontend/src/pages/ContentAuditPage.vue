<template>
  <div class="page-container">
    <h1 class="page-title">内容审核</h1>

    <div v-if="postList.length === 0" class="empty">暂无动态</div>
    <div v-for="p in postList" :key="p.id" class="user-card">
      <div class="user-info">
        <span class="user-name">{{ p.username }}</span>
        <span class="user-meta">{{ p.content }}</span>
      </div>
      <div class="user-stats">
        <span class="user-stat">👍 {{ p.likeCount }} 💬 {{ p.commentCount }}</span>
      </div>
      <button v-if="userRole >= 2" class="remind-btn" @click="deletePost(p)">删除</button>
      <span v-else class="tag-green">正常</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role ?? 0)
const postList = ref<any[]>([])

onMounted(async () => {
  try { const r = await request.get('/admin/posts?page=1&size=100'); if (r.data.code===200) postList.value = r.data.data.records } catch {}
})

async function deletePost(p: any) {
  if (!confirm('确定删除这条动态吗？')) return
  try {
    const r = await request.delete(`/admin/post/${p.id}`)
    if (r.data.code === 200) postList.value = postList.value.filter(item => item.id !== p.id)
  } catch (e: any) { alert('删除失败: ' + (e?.response?.data?.message || e.message)) }
}
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: var(--text); margin-bottom: 1.5rem; }
.empty { font-size: 0.875rem; text-align: center; padding: 3rem 0; color: var(--text-muted); }
.user-card { display: flex; flex-wrap: wrap; align-items: center; gap: 0.75rem; padding: 1rem; border-radius: 0.75rem; margin-bottom: 0.5rem; background: var(--card); border: 1px solid var(--border); }
.user-info { flex: 1; min-width: 150px; }
.user-name { display: block; font-size: 0.875rem; color: var(--text); }
.user-meta { font-size: 0.75rem; color: var(--text-secondary); }
.user-stats { display: flex; align-items: center; gap: 0.5rem; }
.user-stat { font-size: 0.75rem; color: var(--text-secondary); }
.tag-green { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(0,245,160,0.1); color: #00F5A0; }
.tag-red { padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); color: #FF6B6B; }
.remind-btn { padding: 0.25rem 0.75rem; border-radius: 0.5rem; font-size: 0.75rem; background: rgba(255,107,107,0.1); border: 1px solid rgba(255,107,107,0.2); color: #FF6B6B; cursor: pointer; }
.remind-btn:hover { background: rgba(255,107,107,0.2); }
</style>
