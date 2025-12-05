import request from '../index'

/**
 * 日志管理API
 */

// 获取日志统计
export function getLogStatistics() {
  return request({
    url: '/api/logs/statistics',
    method: 'get'
  })
}

// 查询日志列表
export function queryLogs(data) {
  return request({
    url: '/api/logs/query',
    method: 'post',
    data
  })
}

// 获取日志详情
export function getLogById(id) {
  return request({
    url: `/api/logs/${id}`,
    method: 'get'
  })
}

// 清理日志
export function cleanLogs(beforeTime) {
  return request({
    url: '/api/logs/clean',
    method: 'delete',
    params: { beforeTime }
  })
}

// 导出日志
export function exportLogs(data) {
  return request({
    url: '/api/logs/export',
    method: 'post',
    data
  })
}

// 获取审计日志统计
export function getAuditStatistics() {
  return request({
    url: '/api/logs/audit/statistics',
    method: 'get'
  })
}

// 查询审计日志列表
export function queryAuditLogs(data) {
  return request({
    url: '/api/logs/audit/query',
    method: 'post',
    data
  })
}

// 获取审计日志详情
export function getAuditLogById(id) {
  return request({
    url: `/api/logs/audit/${id}`,
    method: 'get'
  })
}
