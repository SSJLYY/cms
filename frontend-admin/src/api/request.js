import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
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
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }

    if (!error.config?.skipBusinessErrorMessage) {
      ElMessage.error(error.response?.data?.message || error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default request
