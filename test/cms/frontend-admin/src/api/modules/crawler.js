/**
 * 爬虫任务管理模块API接口
 * 提供爬虫任务的完整生命周期管理，包括创建、执行、监控等功能
 */
import request from '../request'

/**
 * 创建爬虫任务
 * @param {Object} data - 任务数据
 * @param {string} data.name - 任务名称
 * @param {string} data.url - 目标URL
 * @param {string} data.schedule - 调度表达式
 * @param {Object} data.config - 爬虫配置
 * @returns {Promise} 返回创建结果
 */
export const createTask = (data) => {
  return request.post('/api/crawler/tasks', data)
}

/**
 * 更新爬虫任务
 * @param {number} id - 任务ID
 * @param {Object} data - 更新的任务数据
 * @returns {Promise} 返回更新结果
 */
export const updateTask = (id, data) => {
  return request.put(`/api/crawler/tasks/${id}`, data)
}

/**
 * 删除爬虫任务
 * @param {number} id - 任务ID
 * @param {boolean} deleteResources - 是否同时删除相关资源
 * @returns {Promise} 返回删除结果
 */
export const deleteTask = (id, deleteResources = false) => {
  return request.delete(`/api/crawler/tasks/${id}`, {
    params: { deleteResources }
  })
}

/**
 * 切换任务状态（启用/禁用）
 * @param {number} id - 任务ID
 * @returns {Promise} 返回状态切换结果
 */
export const toggleTaskStatus = (id) => {
  return request.put(`/api/crawler/tasks/${id}/toggle`)
}

/**
 * 查询爬虫任务列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.keyword - 搜索关键词
 * @param {string} params.status - 任务状态
 * @returns {Promise} 返回任务列表数据
 */
export const queryTasks = (params) => {
  return request.get('/api/crawler/tasks', { params })
}

/**
 * 获取任务详情
 * @param {number} id - 任务ID
 * @returns {Promise} 返回任务详细信息
 */
export const getTaskDetail = (id) => {
  return request.get(`/api/crawler/tasks/${id}`)
}

/**
 * 立即触发任务执行
 * @param {number} id - 任务ID
 * @returns {Promise} 返回触发结果
 */
export const triggerTask = (id) => {
  return request.post(`/api/crawler/tasks/${id}/trigger`)
}

/**
 * 停止任务执行
 * @param {number} id - 任务ID
 * @returns {Promise} 返回停止结果
 */
export const stopTask = (id) => {
  return request.post(`/api/crawler/tasks/${id}/stop`)
}

/**
 * 验证URL是否可访问
 * @param {string} url - 待验证的URL
 * @returns {Promise} 返回URL验证结果
 */
export const validateUrl = (url) => {
  return request.post('/api/crawler/validate-url', null, {
    params: { url }
  })
}

/**
 * 查询执行日志列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {number} params.taskId - 任务ID
 * @param {string} params.status - 执行状态
 * @returns {Promise} 返回执行日志列表
 */
export const queryLogs = (params) => {
  return request.get('/api/crawler/logs', { params })
}

/**
 * 获取执行日志详情
 * @param {number} id - 日志ID
 * @returns {Promise} 返回日志详细信息
 */
export const getLogDetail = (id) => {
  return request.get(`/api/crawler/logs/${id}`)
}

/**
 * 检查任务是否正在执行
 * @param {number} id - 任务ID
 * @returns {Promise} 返回任务运行状态
 */
export const isTaskRunning = (id) => {
  return request.get(`/api/crawler/tasks/${id}/running`)
}
