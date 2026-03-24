// 这个文件主要放接口请求，后面找起来更方便
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',  // ← 关键改这里！改成 '/api' （相对路径）
  timeout: 130000,
})
request.interceptors.request.use(config => {
  const store = useUserStore()
  // 这里判断一下边界，避免页面逻辑走偏
  if (store.token) config.headers.Authorization = `Bearer ${store.token}`
  return config
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code === 200) return data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  err => {
    // 这里判断一下边界，避免页面逻辑走偏
    if (err.response?.status === 401) {
      const store = useUserStore()
      store.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (err.code === 'ECONNABORTED' || err.message?.toLowerCase().includes('timeout')) {
      ElMessage.error('AI 响应超时，请稍后重试')
    } else {
      ElMessage.error(err.response?.data?.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
