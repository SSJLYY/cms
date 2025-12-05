import request from '../index'

/**
 * 推广管理API
 */

// 获取广告列表
export function getAdvertisementList(params) {
  return request({
    url: '/api/promotion/list',
    method: 'get',
    params
  })
}

// 获取广告详情
export function getAdvertisementById(id) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'get'
  })
}

// 创建广告
export function createAdvertisement(data) {
  return request({
    url: '/api/promotion',
    method: 'post',
    data
  })
}

// 更新广告
export function updateAdvertisement(id, data) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'put',
    data
  })
}

// 删除广告
export function deleteAdvertisement(id) {
  return request({
    url: `/api/promotion/${id}`,
    method: 'delete'
  })
}

// 更新广告状态
export function updateStatus(id, status) {
  return request({
    url: `/api/promotion/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 更新广告排序
export function updateSortOrder(id, sortOrder) {
  return request({
    url: `/api/promotion/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

// 获取广告位置选项
export function getPositionOptions() {
  return request({
    url: '/api/promotion/positions',
    method: 'get'
  })
}

// 记录点击
export function recordClick(id) {
  return request({
    url: `/api/promotion/${id}/click`,
    method: 'post'
  })
}

// 获取用户端广告
export function getActiveAdvertisements(position) {
  return request({
    url: '/api/promotion/active',
    method: 'get',
    params: { position }
  })
}
