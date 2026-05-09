import { get, post } from '@/utils/request'
import type { R } from '@/types/api'

/** 企微 OAuth2 登录（通过 code） */
export function loginByCode(code: string) {
  return post<R<{ token: string }>>('/auth/login', { code })
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return get<
    R<{
      userId: number
      name: string
      avatar: string
      corpId: string
      corpName: string
      permissions: string[]
    }>
  >('/auth/userinfo')
}

/** 退出登录 */
export function logout() {
  return post<R<null>>('/auth/logout')
}

/** 获取企微 OAuth2 授权 URL */
export function getOAuthUrl(redirectUri: string) {
  return get<R<{ url: string }>>('/auth/oauth-url', { params: { redirectUri } })
}
