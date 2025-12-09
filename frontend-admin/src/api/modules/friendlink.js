/**
 * 友情链接管理模块API接口
 * 提供友情链接的完整管理功能，包括增删改查、状态管理等
 */
import request from '../index'

/**
 * 分页查询友情链接
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {boolean} params.enabled - 是否启用
 * @returns {Promise} 返回友情链接分页数据
 */
export const getFriendLinkPage = (params) => {
  return request({
    url: '/api/friendlinks/page',
    method: 'get',
    params
  })
}

/**
 * 获取所有启用的友情链接
 * @returns {Promise} 返回启用的友情链接列表
 */
export const getEnabledFriendLinks = () => {
  return request({
    url: '/api/friendlinks/enabled',
    method: 'get'
  })
}

/**
 * 根据ID获取友情链接详情
 * @param {number} id - 友情链接ID
 * @returns {Promise} 返回友情链接详细信息
 */
export const getFriendLinkById = (id) => {
  return request({
    url: `/api/friendlinks/${id}`,
    method: 'get'
  })
}

/**
 * 创建友情链接
 * @param {Object} data - 友情链接数据
 * @param {string} data.name - 链接名称
 * @param {string} data.url - 链接地址
 * @param {string} data.description - 链接描述
 * @param {string} data.logo - 链接图标
 * @returns {Promise} 返回创建结果
 */
export const createFriendLink = (data) => {
  return request({
    url: '/api/friendlinks',
    method: 'post',
    data
  })
}

/**
 * 更新友情链接
 * @param {Object} data - 更新的友情链接数据
 * @param {number} data.id - 友情链接ID
 * @returns {Promise} 返回更新结果
 */
export const updateFriendLink = (data) => {
  return request({
    url: '/api/friendlinks',
    method: 'put',
    data
  })
}

/**
 * 删除友情链接
 * @param {number} id - 友情链接ID
 * @returns {Promise} 返回删除结果
 */
export const deleteFriendLink = (id) => {
  return request({
    url: `/api/friendlinks/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除友情链接
 * @param {Array<number>} ids - 友情链接ID数组
 * @returns {Promise} 返回批量删除结果
 */
export const batchDeleteFriendLinks = (ids) => {
  return request({
    url: '/api/friendlinks/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新友情链接状态
 * @param {number} id - 友情链接ID
 * @param {boolean} status - 新状态（true为启用，false为禁用）
 * @returns {Promise} 返回状态更新结果
 */
export const updateFriendLinkStatus = (id, status) => {
  return request({
    url: `/api/friendlinks/${id}/status`,
    method: 'put',
    params: { status }
  })
}
