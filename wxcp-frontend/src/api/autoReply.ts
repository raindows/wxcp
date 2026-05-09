import { get, post, put, del } from '@/utils/request'
import type { R } from '@/types/api'

/** 欢迎语 */
export interface WelcomeMessage {
  id: number
  name: string
  content: string
  userIds: number[]
  userNames: string[]
  status: 0 | 1
  createTime: string
}

/** 关键词规则 */
export interface KeywordRule {
  id: number
  keyword: string
  matchType: 'exact' | 'fuzzy'
  replyContent: string
  priority: number
  status: 0 | 1
  createTime: string
}

/** 默认回复 */
export interface DefaultReply {
  enabled: boolean
  content: string
}

// ======================== 欢迎语 ========================

/** 获取欢迎语列表 */
export function getWelcomeList() {
  return get<R<WelcomeMessage[]>>('/auto-reply/welcome/list')
}

/** 创建欢迎语 */
export function createWelcome(data: Partial<WelcomeMessage>) {
  return post<R<null>>('/auto-reply/welcome/create', data)
}

/** 更新欢迎语 */
export function updateWelcome(data: Partial<WelcomeMessage>) {
  return put<R<null>>('/auto-reply/welcome/update', data)
}

/** 删除欢迎语 */
export function deleteWelcome(id: number) {
  return del<R<null>>(`/auto-reply/welcome/delete/${id}`)
}

// ======================== 关键词回复 ========================

/** 获取关键词规则列表 */
export function getKeywordRules() {
  return get<R<KeywordRule[]>>('/auto-reply/keyword/list')
}

/** 创建关键词规则 */
export function createKeywordRule(data: Partial<KeywordRule>) {
  return post<R<null>>('/auto-reply/keyword/create', data)
}

/** 更新关键词规则 */
export function updateKeywordRule(data: Partial<KeywordRule>) {
  return put<R<null>>('/auto-reply/keyword/update', data)
}

/** 删除关键词规则 */
export function deleteKeywordRule(id: number) {
  return del<R<null>>(`/auto-reply/keyword/delete/${id}`)
}

/** 更新关键词优先级 */
export function updateKeywordPriority(id: number, priority: number) {
  return put<R<null>>('/auto-reply/keyword/priority', { id, priority })
}

// ======================== 默认回复 ========================

/** 获取默认回复配置 */
export function getDefaultReply() {
  return get<R<DefaultReply>>('/auto-reply/default/get')
}

/** 保存默认回复配置 */
export function saveDefaultReply(data: DefaultReply) {
  return post<R<null>>('/auto-reply/default/save', data)
}
