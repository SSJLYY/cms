/**
 * 客户端友情链接相关API接口
 * 提供友情链接的获取功能
 */
import request from './request'

/**
 * 获取所有启用的友情链接
 * @returns {Promise} 返回启用的友情链接列表，用于前台展示
 */
export function getEnabledFriendLinks() {
  return request({
    url: '/api/friendlinks/enabled',
    method: 'get'
  })
}
