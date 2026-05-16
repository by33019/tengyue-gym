<template>
  <div class="profile-page">
    <!-- 顶部渐变条 -->
    <div class="top-accent"></div>

    <div class="max-w-2xl mx-auto px-4 py-8 relative z-10">
      <!-- 头像 & 基础信息 -->
      <div class="hero-card">
        <div class="avatar-wrap" @click="triggerUpload" title="点击更换头像">
          <img v-if="profile.avatar" :src="profile.avatar" class="avatar-img" />
          <div v-else class="avatar">{{ (profile.username || '?')[0]?.toUpperCase() }}</div>
          <div class="avatar-glow"></div>
          <div class="avatar-overlay">📷</div>
        </div>
        <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif" hidden @change="onFileChange" />
        <h1 class="text-2xl font-bold text-white mt-4">{{ profile.username }}</h1>
        <span :class="['role-badge', roleClass]">{{ roleText }}</span>
      </div>

      <!-- 身体数据卡片 -->
      <div class="section-title">身体数据</div>
      <div class="data-card">
        <div class="data-grid">
          <div class="data-item" v-for="item in bodyData" :key="item.label">
            <span class="data-value">{{ item.value ?? '--' }}</span>
            <span class="data-label">{{ item.label }}</span>
          </div>
        </div>
        <button class="edit-btn" @click="toggleEdit">{{ editing ? '取消' : '编辑资料' }}</button>
      </div>

      <!-- 编辑表单 -->
      <form v-if="editing" class="edit-card" @submit.prevent="saveProfile">
        <div class="form-row">
          <label class="form-label">身高 (cm)</label>
          <input v-model.number="editForm.height" type="number" class="form-input" placeholder="175" step="0.1" />
        </div>
        <div class="form-row">
          <label class="form-label">体重 (kg)</label>
          <input v-model.number="editForm.weight" type="number" class="form-input" placeholder="70" step="0.1" />
        </div>
        <div class="form-row">
          <label class="form-label">健身目标</label>
          <div class="chip-row">
            <button type="button" v-for="g in goals" :key="g"
              :class="['chip', { on: editForm.fitnessGoal === g }]"
              @click="editForm.fitnessGoal = g"
            >{{ g }}</button>
          </div>
        </div>
        <div class="form-row">
          <label class="form-label">运动等级</label>
          <div class="chip-row">
            <button type="button" v-for="l in levels" :key="l"
              :class="['chip', { on: editForm.fitnessLevel === l }]"
              @click="editForm.fitnessLevel = l"
            >{{ l }}</button>
          </div>
        </div>
        <p v-if="msg" :class="msgType">{{ msg }}</p>
        <button type="submit" class="submit-btn" :disabled="saving">保存修改</button>
      </form>

      <!-- 修改密码 -->
      <div class="section-title mt-8">账号安全</div>
      <form class="edit-card" @submit.prevent="changePwd">
        <div class="form-row">
          <label class="form-label">原密码</label>
          <input v-model="pwdForm.oldPassword" type="password" class="form-input" />
        </div>
        <div class="form-row">
          <label class="form-label">新密码</label>
          <input v-model="pwdForm.newPassword" type="password" class="form-input" />
        </div>
        <p v-if="pwdMsg" :class="pwdMsgType">{{ pwdMsg }}</p>
        <button type="submit" class="submit-btn secondary" :disabled="pwdSaving">修改密码</button>
      </form>

      <!-- 退出 -->
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const fileInput = ref<HTMLInputElement>()
const profile = ref<any>({})
const editing = ref(false)
const saving = ref(false)
const msg = ref('')
const msgType = ref('msg-success')
const pwdSaving = ref(false)
const pwdMsg = ref('')
const pwdMsgType = ref('msg-success')

const editForm = reactive({ height: null as number | null, weight: null as number | null, fitnessGoal: '增肌', fitnessLevel: '入门' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

const goals = ['减脂', '增肌', '塑形', '保持健康']
const levels = ['入门', '进阶', '高级']

const roleText = computed(() => ['普通用户', '督导', '管理员'][profile.value.role] || '普通用户')
const roleClass = computed(() => ['role-user', 'role-coach', 'role-admin'][profile.value.role] || 'role-user')

const bodyData = computed(() => [
  { label: '身高 cm', value: profile.value.height },
  { label: '体重 kg', value: profile.value.weight },
  { label: '健身目标', value: profile.value.fitnessGoal },
  { label: '运动等级', value: profile.value.fitnessLevel },
])

onMounted(async () => {
  try {
    const { data: res } = await userApi.getProfile()
    if (res.code === 200) profile.value = res.data
  } catch { /* 网络错误 */ }
})

function toggleEdit() {
  if (!editing.value) {
    editForm.height = profile.value.height
    editForm.weight = profile.value.weight
    editForm.fitnessGoal = profile.value.fitnessGoal || '增肌'
    editForm.fitnessLevel = profile.value.fitnessLevel || '入门'
  }
  editing.value = !editing.value
  msg.value = ''
}

async function saveProfile() {
  saving.value = true
  msg.value = ''
  try {
    const { data: res } = await userApi.updateProfile(editForm)
    if (res.code === 200) {
      profile.value = { ...profile.value, ...editForm }
      editing.value = false
      msg.value = '保存成功'
      msgType.value = 'msg-success'
    } else {
      msg.value = res.message
      msgType.value = 'msg-error'
    }
  } catch {
    msg.value = '网络错误'
    msgType.value = 'msg-error'
  } finally {
    saving.value = false
  }
}

async function changePwd() {
  pwdSaving.value = true
  pwdMsg.value = ''
  try {
    const { data: res } = await userApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (res.code === 200) {
      pwdMsg.value = '密码修改成功'
      pwdMsgType.value = 'msg-success'
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
    } else {
      pwdMsg.value = res.message
      pwdMsgType.value = 'msg-error'
    }
  } catch {
    pwdMsg.value = '网络错误'
    pwdMsgType.value = 'msg-error'
  } finally {
    pwdSaving.value = false
  }
}

function triggerUpload() { fileInput.value?.click() }

async function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const { data: res } = await userApi.uploadAvatar(file)
    if (res.code === 200) profile.value.avatar = res.data
  } catch { /* */ }
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  @apply min-h-screen relative;
  background: #0a0a0f;
  font-family: 'Noto Sans SC', system-ui, sans-serif;
}
.top-accent {
  height: 4px;
  background: linear-gradient(90deg, #FF6B6B, #FF8E53, #00F5A0);
}

/* 头像卡片 */
.hero-card {
  @apply text-center py-8;
  animation: fadeInUp 0.6s ease both;
}
.avatar-wrap {
  @apply relative inline-block cursor-pointer group;
}
.avatar, .avatar-img {
  @apply w-20 h-20 rounded-full flex items-center justify-center text-3xl font-bold relative z-10;
  font-family: 'Bebas Neue', sans-serif;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
  object-fit: cover;
}
.avatar-glow {
  @apply absolute inset-0 rounded-full blur-xl opacity-30 -z-0;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
}
.avatar-overlay {
  @apply absolute inset-0 rounded-full flex items-center justify-center text-lg z-20 opacity-0 group-hover:opacity-100 transition-opacity duration-200;
  background: rgba(0, 0, 0, 0.5);
}
.role-badge {
  @apply inline-block px-3 py-1 rounded-full text-xs mt-2;
}
.role-user { background: rgba(0, 245, 160, 0.1); color: #00F5A0; }
.role-coach { background: rgba(0, 210, 255, 0.1); color: #00D2FF; }
.role-admin { background: rgba(255, 107, 107, 0.1); color: #FF6B6B; }

/* 数据卡片 */
.section-title {
  @apply text-xs text-white/30 uppercase tracking-widest mt-8 mb-3;
}
.data-card {
  @apply rounded-2xl p-6;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  animation: fadeInUp 0.6s 0.1s ease both;
}
.data-grid {
  @apply grid grid-cols-2 gap-4;
}
.data-item {
  @apply flex flex-col;
}
.data-value {
  @apply text-xl font-bold text-white;
  font-family: 'Bebas Neue', sans-serif;
}
.data-label {
  @apply text-xs text-white/30 mt-0.5;
}
.edit-btn {
  @apply mt-4 px-4 py-2 rounded-lg text-sm transition-all duration-200;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
}
.edit-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* 编辑卡片 */
.edit-card {
  @apply rounded-2xl p-6 mt-4;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  animation: fadeIn 0.4s ease;
}
.form-row { @apply mb-4; }
.form-label {
  @apply block text-xs text-white/40 mb-2;
}
.form-input {
  @apply w-full px-4 py-3 rounded-xl text-sm text-white transition-all duration-300;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  outline: none;
}
.form-input:focus {
  border-color: rgba(255, 107, 107, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.1);
}
.chip-row { @apply flex flex-wrap gap-2; }
.chip {
  @apply px-4 py-2 rounded-lg text-sm transition-all duration-200;
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
}
.chip.on {
  background: rgba(255, 107, 107, 0.15);
  border-color: rgba(255, 107, 107, 0.4);
  color: #FF6B6B;
}

.submit-btn {
  @apply w-full py-3 rounded-xl text-sm font-semibold tracking-widest transition-all duration-300 mt-2 cursor-pointer;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: #fff;
}
.submit-btn.secondary {
  background: linear-gradient(135deg, rgba(255,255,255,0.08), rgba(255,255,255,0.04));
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.submit-btn:hover:not(:disabled) { transform: translateY(-1px); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.msg-success { @apply text-sm mt-2; color: #00F5A0; }
.msg-error { @apply text-sm mt-2; color: #FF6B6B; }

/* 退出 */
.logout-btn {
  @apply w-full mt-8 py-3 rounded-xl text-sm text-white/30 transition-all duration-200 text-center;
  cursor: pointer;
}
.logout-btn:hover { color: #FF6B6B; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
