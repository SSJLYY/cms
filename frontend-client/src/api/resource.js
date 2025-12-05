import request from './request'

export function getResourceList() {
  return request({
    url: '/api/resources/public/list',
    method: 'get'
  })
}

export function getResourceDetail(id) {
  return request({
    url: `/api/resources/public/list`,
    method: 'get'
  }).then(res => {
    // 从列表中找到对应的资源
    const resource = res.data?.find(r => r.id === parseInt(id))
    return { data: resource }
  })
}

export function recordDownload(id) {
  return request({
    url: `/api/resources/public/download/${id}`,
    method: 'post'
  })
}

export function recordVisit(id) {
  return request({
    url: `/api/resources/public/visit/${id}`,
    method: 'post'
  })
}

export function getConfig() {
  return request({
    url: '/api/config/public',
    method: 'get'
  })
}

export function getLinkTypes() {
  return request({
    url: '/api/link-types/public/list',
    method: 'get'
  })
}
