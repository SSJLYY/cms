/**
 * 分类管理模块API接口
 * 提供分类的完整管理功能，包括统计、查询、增删改查、排序等
 */
import request from '../index'

/**
 * 获取分类统计信息
 * @returns {Promise} 返回分类统计数据（总数、层级分布等）
 */
export function getCategoryStatistics() {
  return request({
    url: '/api/categories/statistics',
    method: 'get'
  })
}

/**
 * 获取分类树形结构
 * @returns {Promise} 返回完整的分类树形数据
 */
export function getCategoryTree() {
  return request({
    url: '/api/categories/tree',
    method: 'get'
  })
}

/**
 * 查询分类列表（支持分页和筛选）
 * @param {Object} data - 查询参数
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @param {string} data.keyword - 搜索关键词
 * @param {number} data.parentId - 父分类ID
 * @returns {Promise} 返回分类列表数据
 */
export function queryCategories(data) {
  return request({
    url: '/api/categories/query',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取分类详情
 * @param {number} id - 分类ID
 * @returns {Promise} 返回分类详细信息
 */
export function getCategoryById(id) {
  return request({
    url: `/api/categories/${id}`,
    method: 'get'
  })
}

/**
 * 创建新分类
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.description - 分类描述
 * @param {number} data.parentId - 父分类ID
 * @param {number} data.sortOrder - 排序顺序
 * @returns {Promise} 返回创建结果
 */
export function createCategory(data) {
  return request({
    url: '/api/categories',
    method: 'post',
    data
  })
}

/**
 * 更新分类信息
 * @param {number} id - 分类ID
 * @param {Object} data - 更新的分类数据
 * @returns {Promise} 返回更新结果
 */
export function updateCategory(id, data) {
  return request({
    url: `/api/categories/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除单个分类
 * @param {number} id - 分类ID
 * @returns {Promise} 返回删除结果
 */
export function deleteCategory(id) {
  return request({
    url: `/api/categories/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除分类
 * @param {Array<number>} ids - 分类ID数组
 * @returns {Promise} 返回批量删除结果
 */
export function deleteCategories(ids) {
  return request({
    url: '/api/categories/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 调整分类排序顺序
 * @param {number} id - 分类ID
 * @param {number} sortOrder - 新的排序顺序
 * @returns {Promise} 返回排序调整结果
 */
export function updateCategorySortOrder(id, sortOrder) {
  return request({
    url: `/api/categories/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

/**
 * 导出分类数据
 * @returns {Promise} 返回导出的分类数据
 */
export function exportCategories() {
  return request({
    url: '/api/categories/export',
    method: 'get'
  })
}
