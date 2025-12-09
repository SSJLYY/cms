/**
 * 图片管理模块API接口
 * 提供图片的统计、查询、删除和使用情况检查功能
 */
import request from '../index'

/**
 * 获取图片统计信息
 * @returns {Promise} 返回图片统计数据（总数、大小、类型分布等）
 */
export function getImageStatistics() {
  return request({
    url: '/api/images/statistics',
    method: 'get'
  })
}

/**
 * 查询图片列表
 * @param {Object} data - 查询参数
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @param {string} data.keyword - 搜索关键词
 * @param {string} data.type - 图片类型
 * @returns {Promise} 返回图片列表数据
 */
export function queryImages(data) {
  return request({
    url: '/api/images/query',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取图片详情
 * @param {number} id - 图片ID
 * @returns {Promise} 返回图片详细信息
 */
export function getImageById(id) {
  return request({
    url: `/api/images/${id}`,
    method: 'get'
  })
}

/**
 * 删除单个图片
 * @param {number} id - 图片ID
 * @returns {Promise} 返回删除结果
 */
export function deleteImage(id) {
  return request({
    url: `/api/images/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除图片
 * @param {Array<number>} ids - 图片ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function deleteImages(ids) {
  return request({
    url: '/api/images/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 检查图片使用情况
 * @param {number} id - 图片ID
 * @returns {Promise} 返回图片在系统中的使用情况
 */
export function checkImageUsage(id) {
  return request({
    url: `/api/images/${id}/usage`,
    method: 'get'
  })
}
