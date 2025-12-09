/**
 * 系统配置模块API接口
 * 提供系统配置的完整管理功能，包括获取、更新、测试等
 */
import request from '../index'

/**
 * 获取所有系统配置
 * @returns {Promise} 返回所有配置项数据
 */
export function getAllConfigs() {
  return request({
    url: '/api/config/all',
    method: 'get'
  })
}

/**
 * 获取配置分类列表
 * @returns {Promise} 返回配置分类数据
 */
export function getConfigCategories() {
  return request({
    url: '/api/config/categories',
    method: 'get'
  })
}

/**
 * 根据类别获取配置项
 * @param {string} category - 配置类别
 * @returns {Promise} 返回指定类别的配置项
 */
export function getConfigsByCategory(category) {
  return request({
    url: `/api/config/category/${category}`,
    method: 'get'
  })
}

/**
 * 根据键获取单个配置项
 * @param {string} key - 配置键
 * @returns {Promise} 返回指定配置项的值
 */
export function getConfigByKey(key) {
  return request({
    url: `/api/config/${key}`,
    method: 'get'
  })
}

/**
 * 批量获取配置项
 * @param {Array<string>} keys - 配置键数组
 * @returns {Promise} 返回指定配置项的值集合
 */
export function getConfigsByKeys(keys) {
  return request({
    url: '/api/config/batch',
    method: 'post',
    data: keys
  })
}

/**
 * 更新单个配置项
 * @param {string} key - 配置键
 * @param {any} value - 配置值
 * @returns {Promise} 返回更新结果
 */
export function updateConfig(key, value) {
  return request({
    url: `/api/config/${key}`,
    method: 'put',
    data: { value }
  })
}

/**
 * 批量更新配置项
 * @param {Object} configs - 配置项对象，键值对形式
 * @returns {Promise} 返回批量更新结果
 */
export function updateConfigs(configs) {
  return request({
    url: '/api/config/batch',
    method: 'put',
    data: configs
  })
}

/**
 * 重置配置项为默认值
 * @param {string} key - 配置键
 * @returns {Promise} 返回重置结果
 */
export function resetConfig(key) {
  return request({
    url: `/api/config/${key}/reset`,
    method: 'post'
  })
}

/**
 * 测试邮件配置
 * @param {Object} emailConfig - 邮件配置对象
 * @param {string} emailConfig.host - SMTP服务器地址
 * @param {number} emailConfig.port - SMTP端口
 * @param {string} emailConfig.username - 用户名
 * @param {string} emailConfig.password - 密码
 * @returns {Promise} 返回邮件配置测试结果
 */
export function testEmailConfig(emailConfig) {
  return request({
    url: '/api/config/test/email',
    method: 'post',
    data: emailConfig
  })
}

/**
 * 测试存储配置
 * @param {Object} storageConfig - 存储配置对象
 * @returns {Promise} 返回存储配置测试结果
 */
export function testStorageConfig(storageConfig) {
  return request({
    url: '/api/config/test/storage',
    method: 'post',
    data: storageConfig
  })
}
