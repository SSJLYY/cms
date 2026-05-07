import request from '../request'

/**
 * 获取收益概览
 */
export function getRevenueOverview(period = 'today') {
  return request({
    url: '/api/revenue/overview',
    method: 'get',
    params: { period }
  })
}

/**
 * 按类型获取收益统计
 */
export function getRevenueByType(period = 'today') {
  return request({
    url: '/api/revenue/by-type',
    method: 'get',
    params: { period }
  })
}

/**
 * 获取收益明细列表
 */
export function getRevenueList(params) {
  return request({
    url: '/api/revenue/list',
    method: 'get',
    params
  })
}

/**
 * 删除收益记录
 */
export function deleteRevenue(id, config = {}) {
  return request({
    url: `/api/revenue/${id}`,
    method: 'delete',
    ...config
  })
}

/**
 * 批量删除收益记录
 */
export function batchDeleteRevenue(ids, config = {}) {
  return request({
    url: '/api/revenue/batch',
    method: 'delete',
    data: ids,
    ...config
  })
}
