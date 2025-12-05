import request from '../index'

/**
 * 控制面板API
 */

// 获取核心指标
export function getMetrics() {
  return request({
    url: '/api/dashboard/metrics',
    method: 'get'
  })
}

// 获取趋势数据
export function getTrendData(days = 7) {
  return request({
    url: '/api/dashboard/trend',
    method: 'get',
    params: { days }
  })
}

// 获取热门资源
export function getHotResources(limit = 10) {
  return request({
    url: '/api/dashboard/hot-resources',
    method: 'get',
    params: { limit }
  })
}

// 获取最新资源
export function getLatestResources(limit = 10) {
  return request({
    url: '/api/dashboard/latest-resources',
    method: 'get',
    params: { limit }
  })
}

// 获取待处理事项
export function getPendingTasks() {
  return request({
    url: '/api/dashboard/pending-tasks',
    method: 'get'
  })
}

// 获取系统状态
export function getSystemStatus() {
  return request({
    url: '/api/dashboard/system-status',
    method: 'get'
  })
}
