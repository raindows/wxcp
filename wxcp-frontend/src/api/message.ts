import { get, post } from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { MessageTaskQuery, MessageTask, SendMessageForm } from '@/types/message'

/** 获取消息任务列表（分页） */
export function getMessageTasks(params: MessageTaskQuery) {
  return get<R<PageResult<MessageTask>>>('/message/task/list', { params })
}

/** 获取消息任务详情 */
export function getMessageTaskDetail(id: number) {
  return get<R<MessageTask>>(`/message/task/detail/${id}`)
}

/** 取消消息任务 */
export function cancelMessageTask(id: number) {
  return post<R<null>>(`/message/task/cancel/${id}`)
}

/** 发送消息 */
export function sendMessage(data: SendMessageForm) {
  return post<R<{ taskId: number }>>('/message/send', data)
}

/** 获取任务接收人列表 */
export function getMessageRecipients(taskId: number) {
  return get<R<{ name: string; userId: string }[]>>(`/message/task/recipients/${taskId}`)
}
