// axios 的基础配置先写这里
import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
// 本地如果有 token，就顺手带上
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
// 如果后端走的是统一返回格式，这里就按那个格式处理
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return { ...response, data: res.data }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error?.response?.status
// 401 一般就是登录状态有问题，直接回登录页
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request

