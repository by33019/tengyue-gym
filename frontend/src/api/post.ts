import request from './request'

export const postApi = {
  create(data: { content: string }) { return request.post('/post', data) },
  list(params: { page?: number; size?: number }) { return request.get('/post/list', { params }) },
  delete(id: number) { return request.delete(`/post/${id}`) },
  like(id: number) { return request.post(`/post/${id}/like`) }
}

export const commentApi = {
  create(data: { postId: number; parentId?: number; content: string }) { return request.post('/comment', data) },
  list(postId: number) { return request.get('/comment/list', { params: { postId } }) },
  delete(id: number) { return request.delete(`/comment/${id}`) }
}
