import request from '../index'

/**
 * 反馈管理API
 */

// 获取反馈统计
export function getFeedbackStatistics() {
  return request({
    url: '/api/feedback/statistics',
    method: 'get'
  })
}

// 查询反馈列表
export function queryFeedback(data) {
  return request({
    url: '/api/feedback/query',
    method: 'post',
    data
  })
}

// 获取反馈详情
export function getFeedbackById(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'get'
  })
}

// 回复反馈
export function replyFeedback(id, reply) {
  return request({
    url: `/api/feedback/${id}/reply`,
    method: 'post',
    data: { reply }
  })
}

// 修改反馈状态
export function updateFeedbackStatus(id, status) {
  return request({
    url: `/api/feedback/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 删除反馈
export function deleteFeedback(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'delete'
  })
}

// 批量删除反馈
export function deleteFeedbacks(ids) {
  return request({
    url: '/api/feedback/batch',
    method: 'delete',
    data: ids
  })
}

// 提交反馈（公开接口）
export function submitFeedback(data) {
  return request({
    url: '/api/feedback/submit',
    method: 'post',
    data
  })
}
