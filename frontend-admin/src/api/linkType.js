import request from './request'

/**
 * 获取所有启用的网盘类型
 */
export function getLinkTypes() {
  return request({
    url: '/api/link-types/public/list',
    method: 'get'
  })
}

/**
 * 获取所有网盘类型（包括禁用的）
 */
export function getAllLinkTypes() {
  return request({
    url: '/api/link-types/list',
    method: 'get'
  })
}

/**
 * 添加网盘类型
 */
export function createLinkType(data) {
  return request({
    url: '/api/link-types',
    method: 'post',
    data
  })
}

/**
 * 更新网盘类型
 */
export function updateLinkType(id, data) {
  return request({
    url: `/api/link-types/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除网盘类型
 */
export function deleteLinkType(id) {
  return request({
    url: `/api/link-types/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除网盘类型
 */
export function deleteLinkTypes(ids) {
  return request({
    url: '/api/link-types/batch',
    method: 'delete',
    data: ids
  })
}
