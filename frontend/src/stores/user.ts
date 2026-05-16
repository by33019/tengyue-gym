import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<any>(null)

  function setToken(access: string, refresh: string) {
    token.value = access
    refreshToken.value = refresh
    localStorage.setItem('token', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return { token, refreshToken, userInfo, setToken, setUserInfo, logout }
})
