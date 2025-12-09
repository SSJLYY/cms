/**
 * 资源管理相关API接口
 * 提供资源的增删改查和状态切换功能
 */
import request from './request'

/**
 * 获取资源列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.categoryId - 分类ID
 * @returns {Promise} 返回资源列表数据
 */
export function getResourceList(params) {
  return request({
    url: '/api/resources/admin/list',
    method: 'get',
    params
  })
}

/**
 * 创建新资源
 * @param {Object} data - 资源数据
 * @param {string} data.title - 资源标题
 * @param {string} data.description - 资源描述
 * @param {number} data.categoryId - 分类ID
 * @param {Array} data.links - 下载链接数组
 * @returns {Promise} 返回创建结果
 */
export function createResource(data) {
  return request({
    url: '/api/resources/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新资源信息
 * @param {number} id - 资源ID
 * @param {Object} data - 更新的资源数据
 * @returns {Promise} 返回更新结果
 */
export function updateResource(id, data) {
  return request({
    url: `/api/resources/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除资源
 * @param {number} id - 资源ID
 * @returns {Promise} 返回删除结果
 */
export function deleteResource(id) {
  return request({
    url: `/api/resources/admin/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 切换资源状态（启用/禁用）
 * @param {number} id - 资源ID
 * @returns {Promise} 返回状态切换结果
 */
export function toggleResourceStatus(id) {
  return request({
    url: `/api/resources/admin/toggle-status/${id}`,
    method: 'put'
  })
}

/**
 * 批量发布资源
 * @param {Array} ids - 资源ID数组
 * @returns {Promise} 返回批量发布结果
 */
export function batchPublishResources(ids) {
  return request({
    url: '/api/resources/admin/batch-publish',
    method: 'put',
    data: { ids }
  })
}

/**
 * 批量下架资源
 * @param {Array} ids - 资源ID数组
 * @returns {Promise} 返回批量下架结果
 */
export function batchUnpublishResources(ids) {
  return request({
    url: '/api/resources/admin/batch-unpublish',
    method: 'put',
    data: { ids }
  })
}

/**
 * 批量删除资源
 * @param {Array} ids - 资源ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function batchDeleteResources(ids) {
  return request({
    url: '/api/resources/admin/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

/**
 * 批量移动资源到指定分类
 * @param {Array} ids - 资源ID数组
 * @param {number} categoryId - 目标分类ID
 * @returns {Promise} 返回批量移动结果
 */
export function batchMoveToCategory(ids, categoryId) {
  return request({
    url: '/api/resources/admin/batch-move-category',
    method: 'put',
    data: { ids, categoryId }
  })
}
