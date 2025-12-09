/**
 * 控制面板模块API接口
 * 提供管理后台首页的各种统计数据和概览信息
 */
import request from '../index'

/**
 * 获取核心指标数据
 * @returns {Promise} 返回核心业务指标（用户数、资源数、访问量等）
 */
export function getMetrics() {
  return request({
    url: '/api/dashboard/metrics',
    method: 'get'
  })
}

/**
 * 获取趋势数据
 * @param {number} days - 统计天数，默认7天
 * @returns {Promise} 返回指定天数内的趋势数据
 */
export function getTrendData(days = 7) {
  return request({
    url: '/api/dashboard/trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取热门资源列表
 * @param {number} limit - 返回数量限制，默认10条
 * @returns {Promise} 返回热门资源列表
 */
export function getHotResources(limit = 10) {
  return request({
    url: '/api/dashboard/hot-resources',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取最新资源列表
 * @param {number} limit - 返回数量限制，默认10条
 * @returns {Promise} 返回最新资源列表
 */
export function getLatestResources(limit = 10) {
  return request({
    url: '/api/dashboard/latest-resources',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取待处理事项列表
 * @returns {Promise} 返回需要管理员处理的事项列表
 */
export function getPendingTasks() {
  return request({
    url: '/api/dashboard/pending-tasks',
    method: 'get'
  })
}

/**
 * 获取系统状态信息
 * @returns {Promise} 返回系统运行状态（CPU、内存、磁盘等）
 */
export function getSystemStatus() {
  return request({
    url: '/api/dashboard/system-status',
    method: 'get'
  })
}
