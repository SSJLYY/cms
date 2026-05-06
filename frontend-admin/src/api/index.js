import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
      if (!response.config?.skipBusinessErrorMessage) {
        ElMessage.error(res.message || '请求失败')
      }

      const businessError = new Error(res.message || '请求失败')
      businessError.response = response
      businessError.response.data = res
      businessError.businessCode = res.code
      businessError.businessData = res

      if (res.code === 401) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }

      return Promise.reject(businessError)
    }

    return res
  },
  error => {
    console.error('响应错误:', error)

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          if (!error.config?.skipBusinessErrorMessage) {
            ElMessage.error('未授权，请重新登录')
          }
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          if (!error.config?.skipBusinessErrorMessage) {
            ElMessage.error('拒绝访问')
          }
          break
        case 404:
          if (!error.config?.skipBusinessErrorMessage) {
            ElMessage.error('请求的资源不存在')
          }
          break
        case 500:
          if (!error.config?.skipBusinessErrorMessage) {
            ElMessage.error('服务器错误')
          }
          break
        default:
          if (!error.config?.skipBusinessErrorMessage) {
            ElMessage.error(data.message || '请求失败')
          }
      }
    } else if (!error.config?.skipBusinessErrorMessage) {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service
