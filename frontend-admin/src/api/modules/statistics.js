import request from '../index'

// 获取统计概览
export function getStatisticsOverview(period = 'today') {
  return request({
    url: '/api/statistics/overview',
    method: 'get',
    params: { period }
  })
}

// 获取下载分布数据
export function getDownloadDistribution(period = 'today') {
  return request({
    url: '/api/statistics/download-distribution',
    method: 'get',
    params: { period }
  })
}

// 获取访问统计详情
export function getVisitDetails(params) {
  return request({
    url: '/api/statistics/visit-details',
    method: 'get',
    params
  })
}

// 获取实时活动
export function getRealtimeActivities(limit = 10) {
  return request({
    url: '/api/statistics/realtime-activities',
    method: 'get',
    params: { limit }
  })
}
