/**
 * 日志管理模块API接口
 * 提供系统日志和审计日志的完整管理功能
 */
import request from '../index'

/**
 * 获取系统日志统计信息
 * @returns {Promise} 返回日志统计数据（总数、级别分布等）
 */
export function getLogStatistics() {
  return request({
    url: '/api/logs/statistics',
    method: 'get'
  })
}

/**
 * 查询系统日志列表
 * @param {Object} data - 查询参数
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @param {string} data.level - 日志级别
 * @param {string} data.startTime - 开始时间
 * @param {string} data.endTime - 结束时间
 * @returns {Promise} 返回日志列表数据
 */
export function queryLogs(data) {
  return request({
    url: '/api/logs/query',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取日志详情
 * @param {number} id - 日志ID
 * @returns {Promise} 返回日志详细信息
 */
export function getLogById(id) {
  return request({
    url: `/api/logs/${id}`,
    method: 'get'
  })
}

/**
 * 清理指定时间之前的日志
 * @param {string} beforeTime - 清理时间点
 * @returns {Promise} 返回清理结果
 */
export function cleanLogs(beforeTime) {
  return request({
    url: '/api/logs/clean',
    method: 'delete',
    params: { beforeTime }
  })
}

/**
 * 导出日志数据
 * @param {Object} data - 导出参数
 * @returns {Promise} 返回导出结果
 */
export function exportLogs(data) {
  return request({
    url: '/api/logs/export',
    method: 'post',
    data
  })
}

/**
 * 获取审计日志统计信息
 * @returns {Promise} 返回审计日志统计数据
 */
export function getAuditStatistics() {
  return request({
    url: '/api/logs/audit/statistics',
    method: 'get'
  })
}

/**
 * 查询审计日志列表
 * @param {Object} data - 查询参数
 * @param {number} data.page - 页码
 * @param {number} data.size - 每页数量
 * @param {string} data.action - 操作类型
 * @param {string} data.startTime - 开始时间
 * @param {string} data.endTime - 结束时间
 * @returns {Promise} 返回审计日志列表数据
 */
export function queryAuditLogs(data) {
  return request({
    url: '/api/logs/audit/query',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取审计日志详情
 * @param {number} id - 审计日志ID
 * @returns {Promise} 返回审计日志详细信息
 */
export function getAuditLogById(id) {
  return request({
    url: `/api/logs/audit/${id}`,
    method: 'get'
  })
}
