import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000
})

request.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (!response.config?.skipBusinessErrorMessage) {
        ElMessage.error(res.message || '请求失败')
      }
      const businessError = new Error(res.message || 'Error')
      businessError.response = response
      businessError.businessCode = res.code
      businessError.businessData = res
      return Promise.reject(businessError)
    }
    return res
  },
  error => {
    if (!error.config?.skipBusinessErrorMessage) {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
