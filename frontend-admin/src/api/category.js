/**
 * 分类管理相关API接口
 * 提供分类的增删改查功能
 */
import request from './request'

/**
 * 获取分类树形列表
 * @returns {Promise} 返回分类树形结构数据
 */
export function getCategoryList() {
  return request({
    url: '/api/categories/tree',
    method: 'get'
  })
}

/**
 * 创建新分类
 * @param {Object} data - 分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.description - 分类描述
 * @param {number} data.parentId - 父分类ID
 * @returns {Promise} 返回创建结果
 */
export function createCategory(data) {
  return request({
    url: '/api/categories/admin/create',
    method: 'post',
    data
  })
}

/**
 * 更新分类信息
 * @param {number} id - 分类ID
 * @param {Object} data - 更新的分类数据
 * @param {string} data.name - 分类名称
 * @param {string} data.description - 分类描述
 * @returns {Promise} 返回更新结果
 */
export function updateCategory(id, data) {
  return request({
    url: `/api/categories/admin/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 * @param {number} id - 分类ID
 * @returns {Promise} 返回删除结果
 */
export function deleteCategory(id) {
  return request({
    url: `/api/categories/admin/delete/${id}`,
    method: 'delete'
  })
}
