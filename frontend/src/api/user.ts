import request from './request'

export const userApi = {
  getProfile() {
    return request.get('/user/profile')
  },
  updateProfile(data: { height?: number; weight?: number; fitnessGoal?: string; fitnessLevel?: string }) {
    return request.put('/user/profile', data)
  },
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.put('/user/password', data)
  }
}
