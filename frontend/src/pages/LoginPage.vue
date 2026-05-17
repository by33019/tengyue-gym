<template>
  <div class="login-page">
    <!-- 背景层 -->
    <div class="bg-layer">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="grid-pattern"></div>
    </div>

    <!-- 主内容 -->
    <div class="content-wrapper">
      <!-- 品牌区域 -->
      <div class="brand-section">
        <div class="brand-inner">
          <div class="logo-mark">T</div>
          <h1 class="brand-name">腾跃</h1>
          <p class="brand-tagline">
            <span class="tagline-slash">/</span>
            每一次打卡，都离更好的自己更近一步
          </p>
          <div class="stat-row">
            <div class="stat-item" v-for="s in stats" :key="s.label">
              <span class="stat-num">{{ s.num }}</span>
              <span class="stat-label">{{ s.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 表单区域 -->
      <div class="form-section">
        <div class="form-card">
          <!-- Tab 切换 -->
          <div class="tab-bar">
            <button
              :class="['tab', { active: activeTab === 'login' }]"
              @click="switchTab('login')"
            >登录</button>
            <button
              :class="['tab', { active: activeTab === 'register' }]"
              @click="switchTab('register')"
            >注册</button>
          </div>

          <!-- 登录表单 -->
          <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="form-body">
            <div class="input-group">
              <label class="input-label">用户名</label>
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <input v-model="loginForm.username" class="input" placeholder="输入用户名" autocomplete="username" />
              </div>
            </div>
            <div class="input-group">
              <label class="input-label">密码</label>
              <div class="input-wrapper">
                <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                <input v-model="loginForm.password" class="input" type="password" placeholder="输入密码" autocomplete="current-password" />
              </div>
            </div>
            <p v-if="loginError" class="error-text">{{ loginError }}</p>
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              <span v-else>登 录</span>
            </button>
          </form>

          <!-- 注册表单 -->
          <form v-else @submit.prevent="handleRegister" class="form-body">
            <div class="input-group">
              <label class="input-label">用户名</label>
              <input v-model="registerForm.username" class="input simple" placeholder="输入用户名" />
            </div>
            <div class="input-group">
              <label class="input-label">密码</label>
              <input v-model="registerForm.password" class="input simple" type="password" placeholder="输入密码" />
            </div>
            <div class="input-group">
              <label class="input-label">注册身份</label>
              <div class="chip-row">
                <button type="button" v-for="r in roles" :key="r.value"
                  :class="['chip', { on: registerForm.role === r.value }]"
                  @click="registerForm.role = r.value"
                >{{ r.label }}</button>
              </div>
            </div>
            <div class="input-group">
              <label class="input-label">健身目标</label>
              <div class="chip-row">
                <button type="button" v-for="g in goals" :key="g"
                  :class="['chip', { on: registerForm.fitnessGoal === g }]"
                  @click="registerForm.fitnessGoal = g"
                >{{ g }}</button>
              </div>
            </div>
            <div class="input-group">
              <label class="input-label">运动基础</label>
              <div class="chip-row">
                <button type="button" v-for="l in levels" :key="l"
                  :class="['chip', { on: registerForm.fitnessLevel === l }]"
                  @click="registerForm.fitnessLevel = l"
                >{{ l }}</button>
              </div>
            </div>
            <p v-if="registerError" class="error-text">{{ registerError }}</p>
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              <span v-else>注 册</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref<'login' | 'register'>('login')
const loading = ref(false)
const loginError = ref('')
const registerError = ref('')

function switchTab(tab: 'login' | 'register') {
  activeTab.value = tab
  loginError.value = ''
  registerError.value = ''
}

const loginForm = reactive({ username: '', password: '' })
const roles = [
  { value: 0, label: '普通用户' },
  { value: 1, label: '教练' },
]

const registerForm = reactive({
  role: 0,
  username: '', password: '',
  fitnessGoal: '增肌', fitnessLevel: '入门'
})

const goals = ['减脂', '增肌', '塑形', '保持健康']
const levels = ['入门', '进阶', '高级']

const stats = [
  { num: '3次', label: '每日打卡上限' },
  { num: '5项', label: 'AI 智能功能' },
  { num: '100天', label: '最高成就徽章' },
]

async function handleLogin() {
  loginError.value = ''
  if (!loginForm.username.trim()) { loginError.value = '请输入用户名'; return }
  if (!loginForm.password) { loginError.value = '请输入密码'; return }
  loading.value = true
  try {
    const { data: res } = await request.post('/auth/login', loginForm)
    if (res.code === 200) {
      userStore.setToken(res.data.token, res.data.refreshToken)
      userStore.setUserInfo(res.data)
      router.push((res.data.role || 0) >= 1 ? '/admin' : '/')
    } else {
      loginError.value = res.message
    }
  } catch {
    loginError.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  registerError.value = ''
  if (!registerForm.username.trim()) { registerError.value = '请输入用户名'; return }
  if (!registerForm.password) { registerError.value = '请输入密码'; return }
  loading.value = true
  try {
    const { data: res } = await request.post('/auth/register', registerForm)
    if (res.code === 200) {
      const { data: loginRes } = await request.post('/auth/login', {
        username: registerForm.username,
        password: registerForm.password
      })
      if (loginRes.code === 200) {
        userStore.setToken(loginRes.data.token, loginRes.data.refreshToken)
        userStore.setUserInfo(loginRes.data)
        router.push((loginRes.data.role || 0) >= 1 ? '/admin' : '/')
      }
    } else {
      registerError.value = res.message
    }
  } catch {
    registerError.value = '网络错误，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  @apply relative min-h-screen flex items-center justify-center overflow-hidden;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
  background: #0a0a0f;
}

/* 背景层 */
.bg-layer {
  @apply absolute inset-0 pointer-events-none;
}
.gradient-orb {
  @apply absolute rounded-full blur-[120px] opacity-40;
  animation: orb-drift 20s ease-in-out infinite;
}
.orb-1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.5), transparent);
  top: -200px; left: -100px;
  animation-delay: 0s;
}
.orb-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(0, 245, 160, 0.3), transparent);
  bottom: -150px; right: -100px;
  animation-delay: -10s;
}
@keyframes orb-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(60px, -40px) scale(1.1); }
  50% { transform: translate(-30px, 50px) scale(0.95); }
  75% { transform: translate(-50px, -30px) scale(1.05); }
}
.grid-pattern {
  @apply absolute inset-0 opacity-[0.03];
  background-image:
    linear-gradient(rgba(255,255,255,1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,1) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* 内容布局 */
.content-wrapper {
  @apply relative z-10 flex w-full max-w-5xl mx-4;
  gap: 0;
}
@media (max-width: 768px) {
  .content-wrapper {
    @apply flex-col max-w-md;
  }
}

/* 品牌区 */
.brand-section {
  @apply flex-1 flex items-center p-8 lg:p-12;
}
.brand-inner {
  animation: fadeInUp 0.8s ease both;
}
.logo-mark {
  font-family: 'Bebas Neue', 'Noto Sans SC', sans-serif;
  @apply w-16 h-16 flex items-center justify-center text-3xl font-bold rounded-2xl mb-6;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  box-shadow: 0 8px 32px rgba(255, 107, 107, 0.3);
}
.brand-name {
  font-family: 'Bebas Neue', 'Noto Sans SC', sans-serif;
  @apply text-6xl lg:text-7xl font-bold tracking-wider mb-4;
  color: #fff;
  letter-spacing: 0.15em;
}
.brand-tagline {
  @apply text-base text-white/60 mb-10 leading-relaxed;
}
.tagline-slash {
  color: #FF6B6B;
  font-weight: bold;
}
.stat-row {
  @apply flex gap-8;
}
.stat-item {
  @apply flex flex-col;
}
.stat-num {
  font-family: 'Bebas Neue', sans-serif;
  @apply text-3xl font-bold;
  color: #FF6B6B;
}
.stat-label {
  @apply text-xs text-white/40 mt-1 tracking-wide;
}

/* 表单卡片 */
.form-section {
  @apply flex-1 flex items-center p-4 lg:p-8;
}
.form-card {
  @apply w-full rounded-3xl p-8;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  animation: fadeInUp 0.8s 0.15s ease both;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.5);
}

/* Tab */
.tab-bar {
  @apply flex mb-8 p-1 rounded-xl;
  background: rgba(255, 255, 255, 0.04);
}
.tab {
  @apply flex-1 py-2.5 text-sm rounded-lg transition-all duration-300;
  color: rgba(255, 255, 255, 0.5);
}
.tab.active {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* 表单 */
.form-body {
  animation: fadeIn 0.4s ease;
}
.input-group {
  @apply mb-5;
}
.input-label {
  @apply block text-xs text-white/50 mb-2 tracking-wider uppercase;
}
.input-wrapper {
  @apply relative flex items-center;
}
.input-icon {
  @apply absolute left-3 w-5 h-5 text-white/20 pointer-events-none;
}
.input {
  @apply w-full px-4 py-3 rounded-xl text-sm text-white transition-all duration-300;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  outline: none;
}
.input:focus {
  border-color: rgba(255, 107, 107, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}
.input.simple {
  @apply pl-4;
}
.input-wrapper .input {
  @apply pl-11;
}

/* 选项标签 */
.chip-row {
  @apply flex flex-wrap gap-2;
}
.chip {
  @apply px-4 py-2 rounded-lg text-sm transition-all duration-200;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
}
.chip:hover {
  background: rgba(255, 255, 255, 0.08);
}
.chip.on {
  background: rgba(255, 107, 107, 0.15);
  border-color: rgba(255, 107, 107, 0.4);
  color: #FF6B6B;
}

/* 错误 */
.error-text {
  @apply text-sm mb-4;
  color: #FF6B6B;
}

/* 提交按钮 */
.submit-btn {
  @apply w-full py-3 rounded-xl text-sm font-semibold tracking-widest transition-all duration-300 mt-2;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.3);
  cursor: pointer;
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 30px rgba(255, 107, 107, 0.4);
}
.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.submit-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.spinner {
  @apply inline-block w-5 h-5 border-2 border-white/30 border-t-white rounded-full;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
