/**
 * 客户端HTTP请求封装
 * 基于axios创建请求实例，提供基础的错误处理
 * 相比管理端，客户端不需要认证功能
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 创建axios请求实例
 * 配置基础URL和超时时间
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

/**
 * 请求拦截器
 * 客户端请求不需要添加认证token
 */
request.interceptors.request.use(
  config => {
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
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    ElMessage.error('网络错误，请检查网络连接')
    return Promise.reject(error)
  }
)

export default request
