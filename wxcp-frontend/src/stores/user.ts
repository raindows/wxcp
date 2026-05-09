import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginByCode, getUserInfo, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import type { R } from '@/types/api'

export interface UserInfo {
  userId: number
  name: string
  avatar: string
  corpId: string
  corpName: string
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(null)

  /** 登录 */
  async function login(code: string) {
    const res = await loginByCode(code)
    const data = (res as unknown as R<{ token: string }>).data
    token.value = data.token
    setToken(data.token)
  }

  /** 模拟登录（开发环境无后端时使用） */
  function mockLogin() {
    const mockToken = 'mock_token_' + Date.now()
    token.value = mockToken
    setToken(mockToken)
    userInfo.value = {
      userId: 1,
      name: '开发测试用户',
      avatar: '',
      corpId: 'mock_corp_id',
      corpName: '测试企业',
      permissions: ['*'],
    }
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    const data = (res as unknown as R<UserInfo>).data
    userInfo.value = data
    return data
  }

  /** 退出登录 */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 即使 API 调用失败也要清理本地状态
    }
    token.value = null
    userInfo.value = null
    removeToken()
  }

  /** 重置状态 */
  function resetState() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    login,
    mockLogin,
    fetchUserInfo,
    logout,
    resetState,
  }
})
