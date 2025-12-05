import request from './request'

export function getResourceList(params) {
  return request({
    url: '/api/resources/admin/list',
    method: 'get',
    params
  })
}

export function createResource(data) {
  return request({
    url: '/api/resources/admin/create',
    method: 'post',
    data
  })
}

export function updateResource(id, data) {
  return request({
    url: `/api/resources/admin/update/${id}`,
    method: 'put',
    data
  })
}

export function deleteResource(id) {
  return request({
    url: `/api/resources/admin/delete/${id}`,
    method: 'delete'
  })
}

export function toggleResourceStatus(id) {
  return request({
    url: `/api/resources/admin/toggle-status/${id}`,
    method: 'put'
  })
}
