import request from '../index'

/**
 * 分页查询友情链接
 */
export const getFriendLinkPage = (params) => {
  return request({
    url: '/api/friendlinks/page',
    method: 'get',
    params
  })
}

/**
 * 获取所有启用的友情链接
 */
export const getEnabledFriendLinks = () => {
  return request({
    url: '/api/friendlinks/enabled',
    method: 'get'
  })
}

/**
 * 根据ID获取友情链接
 */
export const getFriendLinkById = (id) => {
  return request({
    url: `/api/friendlinks/${id}`,
    method: 'get'
  })
}

/**
 * 创建友情链接
 */
export const createFriendLink = (data) => {
  return request({
    url: '/api/friendlinks',
    method: 'post',
    data
  })
}

/**
 * 更新友情链接
 */
export const updateFriendLink = (data) => {
  return request({
    url: '/api/friendlinks',
    method: 'put',
    data
  })
}

/**
 * 删除友情链接
 */
export const deleteFriendLink = (id) => {
  return request({
    url: `/api/friendlinks/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除友情链接
 */
export const batchDeleteFriendLinks = (ids) => {
  return request({
    url: '/api/friendlinks/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新友情链接状态
 */
export const updateFriendLinkStatus = (id, status) => {
  return request({
    url: `/api/friendlinks/${id}/status`,
    method: 'put',
    params: { status }
  })
}
