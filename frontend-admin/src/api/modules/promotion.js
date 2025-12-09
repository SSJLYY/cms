/**
 * 推广管理模块API接口
 * 提供广告推广的完整管理功能，包括增删改查、状态管理、点击统计等
 */
import request from '../index'

/**
 * 获取广告列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.position - 广告位置
 * @param {boolean} params.active - 是否启用
 * @returns {Promise} 返回广告列表数据
 */
export function getAdvertisementList(params) {
  return request({
    url: '/api/promotion/list',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取广告详情
 * @param {number} id - 广告ID
 * @returns {Promise} 返回广告详细信息
 */
export function getAdvertisementById(id) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'get'
  })
}

/**
 * 创建新广告
 * @param {Object} data - 广告数据
 * @param {string} data.title - 广告标题
 * @param {string} data.content - 广告内容
 * @param {string} data.position - 广告位置
 * @param {string} data.link - 广告链接
 * @returns {Promise} 返回创建结果
 */
export function createAdvertisement(data) {
  return request({
    url: '/api/promotion',
    method: 'post',
    data
  })
}

/**
 * 更新广告信息
 * @param {number} id - 广告ID
 * @param {Object} data - 更新的广告数据
 * @returns {Promise} 返回更新结果
 */
export function updateAdvertisement(id, data) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除广告
 * @param {number} id - 广告ID
 * @returns {Promise} 返回删除结果
 */
export function deleteAdvertisement(id) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'delete'
  })
}

/**
 * 更新广告状态
 * @param {number} id - 广告ID
 * @param {boolean} status - 新状态（true为启用，false为禁用）
 * @returns {Promise} 返回状态更新结果
 */
export function updateStatus(id, status) {
  return request({
    url: `/api/promotion/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 更新广告排序顺序
 * @param {number} id - 广告ID
 * @param {number} sortOrder - 新的排序顺序
 * @returns {Promise} 返回排序更新结果
 */
export function updateSortOrder(id, sortOrder) {
  return request({
    url: `/api/promotion/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

/**
 * 获取可用的广告位置选项
 * @returns {Promise} 返回广告位置选项列表
 */
export function getPositionOptions() {
  return request({
    url: '/api/promotion/positions',
    method: 'get'
  })
}

/**
 * 记录广告点击事件
 * @param {number} id - 广告ID
 * @returns {Promise} 返回点击记录结果
 */
export function recordClick(id) {
  return request({
    url: `/api/promotion/${id}/click`,
    method: 'post'
  })
}

/**
 * 获取指定位置的活跃广告（用户端）
 * @param {string} position - 广告位置
 * @returns {Promise} 返回该位置的活跃广告列表
 */
export function getActiveAdvertisements(position) {
  return request({
    url: '/api/promotion/active',
    method: 'get',
    params: { position }
  })
}
