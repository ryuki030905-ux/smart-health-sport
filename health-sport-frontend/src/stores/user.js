// 状态管理文件：把登录态和用户信息集中处理
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token    = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin    = computed(() => userInfo.value?.role === 'ADMIN')

  // 这里处理登录请求，成功后保存登录态
  function login(data) {
    token.value    = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
  }

  // 这里处理退出登录，清理本地状态并调用后端 logout 接口
  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // 忽略 logout 接口错误，状态必须清掉
    }
    token.value    = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, logout }
})
