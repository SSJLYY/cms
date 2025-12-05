import request from './request'

export function getConfig() {
  return request({
    url: '/api/config/admin',
    method: 'get'
  })
}

export function updateConfig(data) {
  return request({
    url: '/api/config/admin/update',
    method: 'put',
    data
  })
}
