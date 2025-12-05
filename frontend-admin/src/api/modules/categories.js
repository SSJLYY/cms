import request from '../index'

/**
 * 分类管理API
 */

// 获取分类统计
export function getCategoryStatistics() {
  return request({
    url: '/api/categories/statistics',
    method: 'get'
  })
}

// 获取分类树
export function getCategoryTree() {
  return request({
    url: '/api/categories/tree',
    method: 'get'
  })
}

// 查询分类列表
export function queryCategories(data) {
  return request({
    url: '/api/categories/query',
    method: 'post',
    data
  })
}

// 获取分类详情
export function getCategoryById(id) {
  return request({
    url: `/api/categories/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/api/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/api/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/api/categories/${id}`,
    method: 'delete'
  })
}

// 批量删除分类
export function deleteCategories(ids) {
  return request({
    url: '/api/categories/batch',
    method: 'delete',
    data: ids
  })
}

// 调整分类排序
export function updateCategorySortOrder(id, sortOrder) {
  return request({
    url: `/api/categories/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

// 导出分类
export function exportCategories() {
  return request({
    url: '/api/categories/export',
    method: 'get'
  })
}
