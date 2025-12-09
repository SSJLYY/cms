/**
 * 客户端资源相关API接口
 * 提供资源浏览、下载、访问统计等功能
 */
import request from './request'

/**
 * 获取公开资源列表
 * @returns {Promise} 返回所有公开可访问的资源列表
 */
export function getResourceList() {
  return request({
    url: '/api/resources/public/list',
    method: 'get'
  })
}

/**
 * 获取资源详情
 * @param {string|number} id - 资源ID
 * @returns {Promise} 返回指定资源的详细信息
 */
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

/**
 * 记录资源下载事件
 * @param {number} id - 资源ID
 * @returns {Promise} 返回下载记录结果，用于统计分析
 */
export function recordDownload(id) {
  return request({
    url: `/api/resources/public/download/${id}`,
    method: 'post'
  })
}

/**
 * 记录资源访问事件
 * @param {number} id - 资源ID
 * @returns {Promise} 返回访问记录结果，用于统计分析
 */
export function recordVisit(id) {
  return request({
    url: `/api/resources/public/visit/${id}`,
    method: 'post'
  })
}

/**
 * 获取公开系统配置
 * @returns {Promise} 返回前台可见的系统配置信息
 */
export function getConfig() {
  return request({
    url: '/api/config/public',
    method: 'get'
  })
}

/**
 * 获取启用的网盘类型列表
 * @returns {Promise} 返回可用的网盘类型列表
 */
export function getLinkTypes() {
  return request({
    url: '/api/link-types/public/list',
    method: 'get'
  })
}

/**
 * 获取剩余下载次数
 * @returns {Promise} 返回当前用户的剩余下载次数
 */
export function getRemainingDownloads() {
  return request({
    url: '/api/resources/public/remaining-downloads',
    method: 'get'
  })
}

/**
 * 检查资源是否已下载
 * @param {number} id - 资源ID
 * @returns {Promise} 返回资源下载状态
 */
export function checkDownloaded(id) {
  return request({
    url: `/api/resources/public/check-downloaded/${id}`,
    method: 'get'
  })
}
