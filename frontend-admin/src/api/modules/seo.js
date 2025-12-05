import request from '../index'

/**
 * SEO管理API
 */

// 获取SEO统计
export function getSEOStatistics() {
  return request({
    url: '/api/seo/statistics',
    method: 'get'
  })
}

// 生成网站地图
export function generateSitemap() {
  return request({
    url: '/api/seo/sitemap/generate',
    method: 'post'
  })
}

// 提交到百度
export function submitToBaidu() {
  return request({
    url: '/api/seo/submit/baidu',
    method: 'post'
  })
}

// 提交到必应
export function submitToBing() {
  return request({
    url: '/api/seo/submit/bing',
    method: 'post'
  })
}

// 批量提交
export function batchSubmit() {
  return request({
    url: '/api/seo/submit/batch',
    method: 'post'
  })
}

// 获取提交历史
export function getSubmissionHistory(params) {
  return request({
    url: '/api/seo/history',
    method: 'get',
    params
  })
}

// 重新提交
export function resubmit(id) {
  return request({
    url: `/api/seo/resubmit/${id}`,
    method: 'post'
  })
}

// 删除历史记录
export function deleteHistory(id) {
  return request({
    url: `/api/seo/history/${id}`,
    method: 'delete'
  })
}
