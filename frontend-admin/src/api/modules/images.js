import request from '../index'

/**
 * 图片管理API
 */

// 获取图片统计
export function getImageStatistics() {
  return request({
    url: '/api/images/statistics',
    method: 'get'
  })
}

// 查询图片列表
export function queryImages(data) {
  return request({
    url: '/api/images/query',
    method: 'post',
    data
  })
}

// 获取图片详情
export function getImageById(id) {
  return request({
    url: `/api/images/${id}`,
    method: 'get'
  })
}

// 删除图片
export function deleteImage(id) {
  return request({
    url: `/api/images/${id}`,
    method: 'delete'
  })
}

// 批量删除图片
export function deleteImages(ids) {
  return request({
    url: '/api/images/batch',
    method: 'delete',
    data: ids
  })
}

// 检查图片使用情况
export function checkImageUsage(id) {
  return request({
    url: `/api/images/${id}/usage`,
    method: 'get'
  })
}
