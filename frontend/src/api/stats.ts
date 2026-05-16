import request from './request'

export const statsApi = {
  summary() { return request.get('/stats/summary') },
  trend(period: string) { return request.get('/stats/trend', { params: { period } }) },
  ranking(type: string, period: string) { return request.get('/stats/ranking', { params: { type, period } }) }
}
