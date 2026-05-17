const BASE = '/api/ai'

function getToken(): string {
  return localStorage.getItem('token') || ''
}

export const aiApi = {
  async quota(): Promise<{ remaining: number; dailyLimit: number }> {
    const token = getToken()
    const res = await fetch(`${BASE}/quota`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (!res.ok) {
      throw new Error(`请求失败 (${res.status})`)
    }
    const json = await res.json()
    return json.data as { remaining: number; dailyLimit: number }
  }
}
