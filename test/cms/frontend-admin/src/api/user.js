/**
 * 用户认证相关API接口
 * 提供用户登录、登出和获取当前用户信息功能
 */
import request from './request'

/**
 * 用户登录
 * @param {Object} data - 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @returns {Promise} 返回登录结果和token
 */
export function login(data) {
  return request({
    url: '/api/users/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns {Promise} 返回登出结果
 */
export function logout() {
  return request({
    url: '/api/users/logout',
    method: 'post'
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise} 返回当前用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/api/users/current',
    method: 'get'
  })
}
