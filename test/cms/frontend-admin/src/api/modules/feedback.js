/**
 * 反馈管理模块API接口
 * 提供用户反馈的完整管理功能，包括查询、回复、状态管理等
 */
import request from '../index'

/**
 * 获取反馈统计信息
 * @returns {Promise} 返回反馈统计数据（总数、状态分布等）
 */
export function getFeedbackStatistics() {
  return request({
    url: '/api/feedback/statistics',
    method: 'get'
  })
}

/**
 * 查询反馈列表
 * @param {Object} data - 查询参数
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @param {string} data.keyword - 搜索关键词
 * @param {string} data.status - 反馈状态
 * @param {string} data.type - 反馈类型
 * @returns {Promise} 返回反馈列表数据
 */
export function queryFeedback(data) {
  return request({
    url: '/api/feedback/query',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取反馈详情
 * @param {number} id - 反馈ID
 * @returns {Promise} 返回反馈详细信息
 */
export function getFeedbackById(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'get'
  })
}

/**
 * 回复反馈
 * @param {number} id - 反馈ID
 * @param {string} reply - 回复内容
 * @returns {Promise} 返回回复结果
 */
export function replyFeedback(id, reply) {
  return request({
    url: `/api/feedback/${id}/reply`,
    method: 'post',
    data: { reply }
  })
}

/**
 * 修改反馈状态
 * @param {number} id - 反馈ID
 * @param {string} status - 新状态（pending/processing/resolved/closed）
 * @returns {Promise} 返回状态更新结果
 */
export function updateFeedbackStatus(id, status) {
  return request({
    url: `/api/feedback/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 删除单个反馈
 * @param {number} id - 反馈ID
 * @returns {Promise} 返回删除结果
 */
export function deleteFeedback(id) {
  return request({
    url: `/api/feedback/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除反馈
 * @param {Array<number>} ids - 反馈ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function deleteFeedbacks(ids) {
  return request({
    url: '/api/feedback/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 提交反馈（公开接口）
 * @param {Object} data - 反馈数据
 * @param {string} data.type - 反馈类型
 * @param {string} data.content - 反馈内容
 * @param {string} data.contact - 联系方式
 * @returns {Promise} 返回提交结果
 */
export function submitFeedback(data) {
  return request({
    url: '/api/feedback/submit',
    method: 'post',
    data
  })
}
