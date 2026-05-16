<template>
  <div class="page">
    <div class="max-w-lg mx-auto px-4 py-6">
      <h1 class="page-title">社区</h1>
      <!-- 发帖 -->
      <div class="post-box">
        <textarea v-model="newPost" class="post-input" placeholder="分享你的健身动态..." rows="2"></textarea>
        <button class="post-btn" @click="publish" :disabled="!newPost.trim()">发布</button>
      </div>

      <!-- 动态流 -->
      <div v-if="posts.length === 0" class="empty">暂无动态</div>
      <div v-for="p in posts" :key="p.id" class="feed-card">
        <div class="feed-header">
          <span class="feed-user">用户 #{{ p.userId }}</span>
          <span class="feed-time">{{ fmt(p.createdAt) }}</span>
        </div>
        <p class="feed-content">{{ p.content }}</p>
        <div class="feed-actions">
          <button class="act-btn" @click="likePost(p)">❤ {{ p.likeCount }}</button>
          <button class="act-btn" @click="openComments(p)">💬 {{ p.commentCount }}</button>
        </div>
        <!-- 评论区 -->
        <div v-if="activePost === p.id" class="comment-section">
          <div v-for="c in comments" :key="c.id" :class="['comment-item', { reply: c.parentId }]">
            <span class="cmt-user">用户 #{{ c.userId }}</span>
            <span class="cmt-content">{{ c.content }}</span>
            <button class="reply-btn" @click="replyTo = replyTo === c.id ? null : c.id">回复</button>
            <!-- 回复输入框 -->
            <div v-if="replyTo === c.id" class="reply-box">
              <input v-model="replyText" class="reply-input" placeholder="输入回复..." />
              <button class="post-btn sm" @click="submitReply(p.id, c.id)">发送</button>
            </div>
            <!-- 子评论 -->
            <div v-for="sub in c.children" :key="sub.id" class="comment-item sub">
              <span class="cmt-user">用户 #{{ sub.userId }}</span>
              <span class="cmt-content">{{ sub.content }}</span>
            </div>
          </div>
          <div class="reply-box">
            <input v-model="commentText" class="reply-input" placeholder="写评论..." />
            <button class="post-btn sm" @click="submitComment(p.id)">评论</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { postApi, commentApi } from '@/api/post'

const newPost = ref(''); const posts = ref<any[]>([])
const activePost = ref<number | null>(null); const comments = ref<any[]>([])
const replyTo = ref<number | null>(null); const replyText = ref(''); const commentText = ref('')

onMounted(loadPosts)
async function loadPosts() {
  try { const r = await postApi.list({}); if (r.data.code===200) posts.value = r.data.data.records || [] } catch {}
}
async function publish() {
  if (!newPost.value.trim()) return
  try { await postApi.create({ content: newPost.value }); newPost.value = ''; loadPosts() } catch {}
}
async function likePost(p: any) {
  try { await postApi.like(p.id); p.likeCount++; } catch {}
}
async function openComments(p: any) {
  if (activePost.value === p.id) { activePost.value = null; return }
  activePost.value = p.id; replyTo.value = null
  try { const r = await commentApi.list(p.id); if (r.data.code===200) comments.value = r.data.data } catch {}
}
async function submitComment(postId: number) {
  if (!commentText.value.trim()) return
  try { await commentApi.create({ postId, content: commentText.value }); commentText.value = ''; openComments(posts.value.find(p => p.id === postId)) } catch {}
}
async function submitReply(postId: number, parentId: number) {
  if (!replyText.value.trim()) return
  try { await commentApi.create({ postId, parentId, content: replyText.value }); replyText.value = ''; replyTo.value = null; openComments(posts.value.find(p => p.id === postId)) } catch {}
}
function fmt(t: string) { if (!t) return ''; const d = new Date(t); return `${d.getMonth()+1}/${d.getDate()}` }
</script>

<style scoped>
.page { @apply min-h-screen; background: #0a0a0f; font-family: 'Noto Sans SC', sans-serif; }
.page-title { font-family: 'Bebas Neue', sans-serif; @apply text-3xl tracking-wider mb-4; color: #fff; }
.post-box { @apply mb-6; }
.post-input { @apply w-full p-4 rounded-xl text-sm resize-none; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06); color: #fff; outline: none; }
.post-input:focus { border-color: rgba(255,107,107,0.3); }
.post-btn { @apply mt-2 px-6 py-2 rounded-xl text-sm font-semibold transition-all; background: linear-gradient(135deg, #FF6B6B, #FF8E53); color: #fff; border: none; cursor: pointer; }
.post-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.post-btn.sm { @apply px-3 py-1.5 text-xs; }
.empty { @apply text-sm text-center py-12; color: rgba(255,255,255,0.15); }
.feed-card { @apply p-4 rounded-2xl mb-3; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.feed-header { @apply flex justify-between mb-2; }
.feed-user { @apply text-sm font-semibold; color: #FF6B6B; }
.feed-time { @apply text-xs; color: rgba(255,255,255,0.2); }
.feed-content { @apply text-sm leading-relaxed mb-3; color: rgba(255,255,255,0.7); }
.feed-actions { @apply flex gap-4; }
.act-btn { @apply text-xs px-3 py-1 rounded-lg; background: rgba(255,255,255,0.03); color: rgba(255,255,255,0.4); border: 1px solid rgba(255,255,255,0.05); cursor: pointer; transition: all 0.2s; }
.act-btn:hover { color: #FF6B6B; }
.comment-section { @apply mt-3 pt-3; border-top: 1px solid rgba(255,255,255,0.04); }
.comment-item { @apply py-2; }
.comment-item.reply { @apply ml-0; }
.comment-item.sub { @apply ml-6; }
.cmt-user { @apply text-xs mr-2; color: #FF6B6B; }
.cmt-content { @apply text-sm; color: rgba(255,255,255,0.6); }
.reply-btn { @apply text-xs ml-2; background: none; border: none; color: rgba(255,255,255,0.2); cursor: pointer; }
.reply-btn:hover { color: #00F5A0; }
.reply-box { @apply flex gap-2 mt-2; }
.reply-input { @apply flex-1 px-3 py-2 rounded-lg text-xs; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.06); color: #fff; outline: none; }
</style>
