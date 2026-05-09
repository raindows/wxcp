import { get, post, put, del } from '@/utils/request'
import type { R } from '@/types/api'
import type { CpConfig, CpConfigForm } from '@/types/config'

/** 获取应用配置列表 */
export function getConfigList() {
  return get<R<CpConfig[]>>('/config/list')
}

/** 获取应用配置详情 */
export function getConfigDetail(id: number) {
  return get<R<CpConfig>>(`/config/detail/${id}`)
}

/** 新增应用配置 */
export function createConfig(data: CpConfigForm) {
  return post<R<null>>('/config/create', data)
}

/** 更新应用配置 */
export function updateConfig(data: CpConfigForm) {
  return put<R<null>>('/config/update', data)
}

/** 删除应用配置 */
export function deleteConfig(id: number) {
  return del<R<null>>(`/config/delete/${id}`)
}

/** 测试连接 */
export function testConnection(id: number) {
  return post<R<{ success: boolean; message: string }>>(`/config/test/${id}`)
}
