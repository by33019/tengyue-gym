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

    <!-- 快捷入口 -->
    <div class="quick-section">
      <h2 class="section-title">快捷操作</h2>
      <div class="quick-grid">
        <template v-if="userRole === 1">
          <router-link to="/coach/students" class="quick-card">
            <span class="quick-icon">👥</span>
            <span class="quick-label">管理学员</span>
            <span class="quick-desc">查看学员打卡进度、发送提醒</span>
          </router-link>
          <router-link to="/coach/alerts" class="quick-card">
            <span class="quick-icon">🔔</span>
            <span class="quick-label">查看提醒</span>
            <span class="quick-desc">预警记录与提醒日志</span>
          </router-link>
          <router-link to="/coach/templates" class="quick-card">
            <span class="quick-icon">📌</span>
            <span class="quick-label">模板管理</span>
            <span class="quick-desc">管理训练计划模板</span>
          </router-link>
        </template>
        <template v-if="userRole >= 2">
          <router-link to="/admin/users" class="quick-card">
            <span class="quick-icon">👥</span>
            <span class="quick-label">管理用户</span>
            <span class="quick-desc">用户列表、启用/禁用、删除</span>
          </router-link>
          <router-link to="/admin/coaches" class="quick-card">
            <span class="quick-icon">👨‍🏫</span>
            <span class="quick-label">教练管理</span>
            <span class="quick-desc">教练与学员分配关系</span>
          </router-link>
          <router-link to="/admin/analytics" class="quick-card">
            <span class="quick-icon">📊</span>
            <span class="quick-label">数据看板</span>
            <span class="quick-desc">全局数据统计与趋势</span>
          </router-link>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const userStore = useUserStore()
const userRole = computed(() => userStore.userInfo?.role ?? 0)
const roleText = computed(() => ['普通用户', '督导', '管理员'][userRole.value] || '')

const dash = ref<any>({})

onMounted(async () => {
  try { const r = await request.get('/admin/dashboard'); if (r.data.code === 200) dash.value = r.data.data } catch {}
})
</script>

<style scoped>
.page-title { font-family: 'Bebas Neue', sans-serif; font-size: 2rem; letter-spacing: 0.05em; color: #fff; margin-bottom: 1.5rem; }

.summary-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
.s-card { padding: 1.25rem; border-radius: 1rem; background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04); }
.s-val { display: block; font-size: 1.75rem; font-family: 'Bebas Neue', sans-serif; color: #FF3B5C; }
.s-lbl { font-size: 0.75rem; color: rgba(255,255,255,0.3); }

.quick-section { margin-top: 0.5rem; }
.section-title { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 1rem; color: rgba(255,255,255,0.2); }

.quick-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 0.75rem; }

.quick-card {
  display: flex; flex-direction: column; gap: 0.25rem;
  padding: 1.25rem; border-radius: 0.75rem;
  background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.04);
  text-decoration: none; transition: all 0.2s;
}
.quick-card:hover { background: rgba(255,59,92,0.06); border-color: rgba(255,59,92,0.15); }
.quick-icon { font-size: 1.5rem; }
.quick-label { font-size: 0.875rem; color: #fff; font-weight: 500; }
.quick-desc { font-size: 0.75rem; color: rgba(255,255,255,0.3); }
</style>
