import { get, post, put, del } from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { AgentQRCode, AgentQRCodeForm, AgentQRCodeQuery } from '@/types/agent'

/** 获取坐席二维码列表 */
export function getAgentQRList(params: AgentQRCodeQuery) {
  return get<R<PageResult<AgentQRCode>>>('/agent/qr/list', { params })
}

/** 获取坐席二维码详情 */
export function getAgentQRDetail(id: number) {
  return get<R<AgentQRCode>>(`/agent/qr/${id}`)
}

/** 创建坐席二维码（调用企微 API 生成） */
export function createAgentQR(data: AgentQRCodeForm) {
  return post<R<AgentQRCode>>('/agent/qr/create', data)
}

/** 更新坐席二维码 */
export function updateAgentQR(data: AgentQRCodeForm & { id: number }) {
  return put<R<null>>('/agent/qr/update', data)
}

/** 删除坐席二维码 */
export function deleteAgentQR(id: number) {
  return del<R<null>>(`/agent/qr/${id}`)
}
