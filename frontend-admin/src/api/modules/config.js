import request from '../index'

/**
 * 系统配置API
 */

// 获取所有配置
export function getAllConfigs() {
  return request({
    url: '/api/config/all',
    method: 'get'
  })
}

// 获取配置分类列表
export function getConfigCategories() {
  return request({
    url: '/api/config/categories',
    method: 'get'
  })
}

// 根据类别获取配置
export function getConfigsByCategory(category) {
  return request({
    url: `/api/config/category/${category}`,
    method: 'get'
  })
}

// 根据键获取配置
export function getConfigByKey(key) {
  return request({
    url: `/api/config/${key}`,
    method: 'get'
  })
}

// 批量获取配置
export function getConfigsByKeys(keys) {
  return request({
    url: '/api/config/batch',
    method: 'post',
    data: keys
  })
}

// 更新配置
export function updateConfig(key, value) {
  return request({
    url: `/api/config/${key}`,
    method: 'put',
    data: { value }
  })
}

// 批量更新配置
export function updateConfigs(configs) {
  return request({
    url: '/api/config/batch',
    method: 'put',
    data: configs
  })
}

// 重置配置
export function resetConfig(key) {
  return request({
    url: `/api/config/${key}/reset`,
    method: 'post'
  })
}

// 测试邮件配置
export function testEmailConfig(emailConfig) {
  return request({
    url: '/api/config/test/email',
    method: 'post',
    data: emailConfig
  })
}

// 测试存储配置
export function testStorageConfig(storageConfig) {
  return request({
    url: '/api/config/test/storage',
    method: 'post',
    data: storageConfig
  })
}
