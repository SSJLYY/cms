/**
 * 系统配置管理相关API接口
 * 提供系统配置的获取和更新功能
 */
import request from './request'

/**
 * 获取所有系统配置
 * @returns {Promise} 返回系统配置数据
 */
export function getConfig() {
  return request({
    url: '/api/config/all',
    method: 'get'
  })
}

/**
 * 批量更新系统配置
 * @param {Object} data - 配置数据对象
 * @returns {Promise} 返回更新结果
 */
export function updateConfig(data) {
  return request({
    url: '/api/config/batch',
    method: 'put',
    data
  })
}
