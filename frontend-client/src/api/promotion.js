/**
 * 客户端推广广告相关API接口
 * 提供广告展示和点击统计功能
 */
import request from './request'

/**
 * 获取指定位置的活跃广告
 * @param {string} position - 广告位置（header/sidebar/footer/content等）
 * @returns {Promise} 返回该位置的活跃广告列表
 */
export function getActiveAdvertisements(position) {
  return request({
    url: '/api/promotion/active',
    method: 'get',
    params: { position }
  })
}

/**
 * 记录广告点击事件
 * @param {number} id - 广告ID
 * @returns {Promise} 返回点击记录结果，用于统计分析
 */
export function recordClick(id) {
  return request({
    url: `/api/promotion/${id}/click`,
    method: 'post'
  })
}
