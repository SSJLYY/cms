/**
 * 网盘类型管理相关API接口
 * 提供网盘类型的增删改查功能
 */
import request from './request'

/**
 * 获取所有启用的网盘类型
 * @returns {Promise} 返回启用的网盘类型列表
 */
export function getLinkTypes() {
  return request({
    url: '/api/link-types/public/list',
    method: 'get'
  })
}

/**
 * 获取所有网盘类型（包括禁用的）
 * @returns {Promise} 返回所有网盘类型列表
 */
export function getAllLinkTypes() {
  return request({
    url: '/api/link-types/list',
    method: 'get'
  })
}

/**
 * 添加网盘类型
 * @param {Object} data - 网盘类型数据
 * @param {string} data.name - 网盘类型名称
 * @param {string} data.icon - 网盘类型图标
 * @param {boolean} data.enabled - 是否启用
 * @returns {Promise} 返回创建结果
 */
export function createLinkType(data) {
  return request({
    url: '/api/link-types',
    method: 'post',
    data
  })
}

/**
 * 更新网盘类型
 * @param {number} id - 网盘类型ID
 * @param {Object} data - 更新的网盘类型数据
 * @returns {Promise} 返回更新结果
 */
export function updateLinkType(id, data) {
  return request({
    url: `/api/link-types/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除网盘类型
 * @param {number} id - 网盘类型ID
 * @returns {Promise} 返回删除结果
 */
export function deleteLinkType(id) {
  return request({
    url: `/api/link-types/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除网盘类型
 * @param {Array<number>} ids - 网盘类型ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function deleteLinkTypes(ids) {
  return request({
    url: '/api/link-types/batch',
    method: 'delete',
    data: ids
  })
}
