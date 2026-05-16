import request from './request'

export const planApi = {
  create(data: any) { return request.post('/plan', data) },
  list(params: { page?: number; size?: number; status?: number }) {
    return request.get('/plan/list', { params })
  },
  getById(id: number) { return request.get(`/plan/${id}`) },
  update(id: number, data: any) { return request.put(`/plan/${id}`, data) },
  toggleStatus(id: number) { return request.put(`/plan/${id}/status`) },
  templates(params: { page?: number; size?: number }) {
    return request.get('/plan/templates', { params })
  }
}
