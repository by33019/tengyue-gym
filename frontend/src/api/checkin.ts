import request from './request'

export const checkinApi = {
  create(data: { exerciseType: string; durationMinutes: number; calories?: number; note?: string }) {
    return request.post('/checkin', data)
  },
  list(params: { page?: number; size?: number; exerciseType?: string; startDate?: string; endDate?: string }) {
    return request.get('/checkin/list', { params })
  },
  calendar(year: number, month: number) {
    return request.get('/checkin/calendar', { params: { year, month } })
  },
  todayCount() {
    return request.get('/checkin/today-count')
  },
  exportCSV() {
    return request.get('/checkin/export', { params: { format: 'csv' } })
  }
}
