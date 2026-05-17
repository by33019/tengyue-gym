<template>
  <div class="flex flex-col h-[calc(100vh-3.5rem)] max-w-2xl mx-auto">
    <!-- 额度提示 -->
    <div class="px-4 py-2 text-xs text-gray-500 bg-gray-50 dark:bg-gray-800 flex justify-between items-center">
      <span>今日剩余 <b class="text-emerald-600">{{ quota.remaining }}</b>/{{ quota.dailyLimit }} 次</span>
      <button @click="clearChat" class="text-gray-400 hover:text-gray-600">清空对话</button>
    </div>

    <!-- 功能快捷卡片 -->
    <div class="flex gap-2 px-4 py-3 overflow-x-auto border-b border-gray-100 dark:border-gray-700">
      <button
        v-for="fn in functions"
        :key="fn.type"
        @click="selectFunction(fn)"
        :class="[
          'shrink-0 px-3 py-2 rounded-xl text-sm font-medium transition-colors',
          callType === fn.type
            ? 'bg-emerald-500 text-white'
            : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200'
        ]"
      >
        <span class="mr-1">{{ fn.icon }}</span>{{ fn.label }}
      </button>
    </div>

    <!-- 对话流 -->
    <div ref="chatContainer" class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
      <div v-if="messages.length === 0" class="text-center text-gray-400 mt-20">
        <div class="text-4xl mb-4">🤖</div>
        <p class="text-sm">选择功能后输入你的需求</p>
        <p class="text-xs mt-1">AI 将为你提供个性化健身指导</p>
      </div>

      <div
        v-for="(msg, i) in messages"
        :key="i"
        :class="[
          'max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed',
          msg.role === 'user'
            ? 'ml-auto bg-emerald-500 text-white'
            : 'mr-auto bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-200'
        ]"
      >
        <div v-if="msg.role === 'user'">{{ msg.content }}</div>
        <div v-else>
          <span>{{ msg.content }}</span>
          <span v-if="msg.streaming" class="inline-block w-1.5 h-4 bg-emerald-500 animate-pulse align-middle ml-0.5"></span>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="p-4 border-t border-gray-100 dark:border-gray-700">
      <div class="flex items-center gap-2">
        <input
          v-model="input"
          @keydown.enter="send"
          :placeholder="inputPlaceholder"
          :disabled="sending || quota.remaining <= 0"
          class="flex-1 px-4 py-2.5 rounded-xl border border-gray-200 dark:border-gray-600 bg-white dark:bg-gray-800 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-400 disabled:opacity-50"
        />
        <button
          @click="send"
          :disabled="!input.trim() || sending || quota.remaining <= 0"
          class="px-5 py-2.5 bg-emerald-500 text-white rounded-xl text-sm font-medium hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {{ sending ? '思考中' : '发送' }}
        </button>
      </div>
      <p v-if="quota.remaining <= 0" class="text-xs text-red-500 mt-1">今日次数已用完</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { aiApi } from '@/api/ai'

interface Message {
  role: 'user' | 'ai'
  content: string
  streaming: boolean
}

interface FunctionCard {
  type: string
  label: string
  icon: string
  placeholder: string
}

const functions: FunctionCard[] = [
  { type: 'chat', label: '通用', icon: '💬', placeholder: '输入你的健身问题...' },
  { type: 'plan', label: '计划', icon: '📋', placeholder: '描述你想要的健身计划...' },
  { type: 'analyze', label: '分析', icon: '📊', placeholder: '请 AI 分析你的运动数据...' },
  { type: 'recipe', label: '食谱', icon: '🍽️', placeholder: '告诉我你的饮食偏好...' },
  { type: 'exercise_guide', label: '动作', icon: '🏋️', placeholder: '想了解哪个动作的要领...' },
  { type: 'recovery', label: '恢复', icon: '🩹', placeholder: '描述你的不适症状...' }
]

const callType = ref('chat')
const input = ref('')
const sending = ref(false)
const messages = ref<Message[]>([])
const chatContainer = ref<HTMLElement>()
const quota = ref({ remaining: 10, dailyLimit: 10 })
let abortController: AbortController | null = null

const inputPlaceholder = computed(() => {
  const fn = functions.find(f => f.type === callType.value)
  return fn ? fn.placeholder : '输入消息...'
})

function selectFunction(fn: FunctionCard) {
  callType.value = fn.type
}

function clearChat() {
  messages.value = []
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text, streaming: false })
  input.value = ''
  sending.value = true

  const aiMsg: Message = { role: 'ai', content: '', streaming: true }
  messages.value.push(aiMsg)

  await nextTick()
  scrollToBottom()

  // recovery 复用 chat 端点
  const apiType = callType.value === 'recovery' ? 'chat' : callType.value

  abortController = new AbortController()
  const token = localStorage.getItem('user')
  let authToken = ''
  if (token) {
    try { authToken = JSON.parse(token).token || '' } catch { /* */ }
  }

  try {
    const response = await fetch(`/api/ai/${apiType}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({ message: text }),
      signal: abortController.signal
    })

    if (!response.ok) {
      aiMsg.content = `请求失败 (${response.status})`
      aiMsg.streaming = false
      sending.value = false
      await refreshQuota()
      return
    }

    const reader = response.body?.getReader()
    if (!reader) {
      aiMsg.content = '浏览器不支持流式读取'
      aiMsg.streaming = false
      sending.value = false
      return
    }

    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed.startsWith('data:')) continue

        const data = trimmed.substring(5).trim()
        if (!data) continue

        if (data.startsWith('{')) {
          try {
            const json = JSON.parse(data)
            if (json.message) {
              aiMsg.content += json.message
            }
          } catch {
            aiMsg.content += data
          }
        } else {
          aiMsg.content += data
        }

        await nextTick()
        scrollToBottom()
      }
    }

    aiMsg.streaming = false
    await refreshQuota()
  } catch (err: any) {
    if (err.name !== 'AbortError') {
      aiMsg.content = aiMsg.content || `网络异常: ${err.message || '未知错误'}`
      aiMsg.streaming = false
    }
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

async function refreshQuota() {
  try {
    quota.value = await aiApi.quota()
  } catch { /* ignore */ }
}

onMounted(() => {
  refreshQuota()
})
</script>
