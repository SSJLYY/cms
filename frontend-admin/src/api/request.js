/**
 * HTTP请求封装（简化版）
 * 基于axios创建请求实例，提供基础的认证和错误处理
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建axios请求实例
 * 配置基础URL和超时时间
 */
const request = axios.create({
  baseURL: '',
  timeout: 10000
})

/**
 * 请求拦截器
 * 自动添加认证token到请求头
 */
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理响应结果和错误
 */
request.interceptors.response.use(
  response => {
    const res = response.data
    // 检查业务状态码
    if (res.code !== 200) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default request
