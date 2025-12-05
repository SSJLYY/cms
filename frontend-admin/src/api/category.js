import request from './request'

export function getCategoryList() {
  return request({
    url: '/api/categories/tree',
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/api/categories/admin/create',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/api/categories/admin/update/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/api/categories/admin/delete/${id}`,
    method: 'delete'
  })
}
