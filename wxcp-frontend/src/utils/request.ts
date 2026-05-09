import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'

/** 创建 Axios 实例 */
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

/** 请求拦截器 -- 自动附加 Token */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

/** 是否正在处理 401 */
let isRefreshing = false

/** 响应拦截器 -- 错误处理 & 401 自动跳转 */
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 如果后端返回的 code 不是 0 / 200，则视为业务错误
    if (res.code !== undefined && res.code !== 0 && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized()
    } else if (status === 403) {
      ElMessage.error('没有权限访问该资源')
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (status === 500) {
      ElMessage.error('服务器内部错误')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

/** 处理未授权（401） */
function handleUnauthorized() {
  if (isRefreshing) return
  isRefreshing = true
  removeToken()
  // 延迟导入 router 避免循环依赖
  import('@/router').then(({ default: router }) => {
    router.push(`/login?redirect=${router.currentRoute.value.fullPath}`).finally(() => {
      isRefreshing = false
    })
  })
}

/** 封装 GET 请求 */
export function get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.get(url, config) as unknown as Promise<T>
}

/** 封装 POST 请求 */
export function post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.post(url, data, config) as unknown as Promise<T>
}

/** 封装 PUT 请求 */
export function put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return service.put(url, data, config) as unknown as Promise<T>
}

/** 封装 DELETE 请求 */
export function del<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return service.delete(url, config) as unknown as Promise<T>
}

export default service
