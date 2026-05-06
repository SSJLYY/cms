/**
 * 客户端反馈相关API接口
 * 提供用户反馈提交功能
 */
import request from './request'

/**
 * 提交用户反馈
 * @param {Object} data - 反馈数据
 * @param {string} data.type - 反馈类型（bug/suggestion/complaint等）
 * @param {string} data.content - 反馈内容
 * @param {string} data.contact - 联系方式（可选）
 * @param {string} data.resourceId - 相关资源ID（可选）
 * @returns {Promise} 返回提交结果
 */
export function submitFeedback(data) {
  return request({
    url: '/api/feedback/public/submit',
    method: 'post',
    data
  })
}
