import type { PageQuery } from './api'

/** 消息类型 */
type MessageType = 'text' | 'image' | 'markdown' | 'task_card'

/** 发送方式 */
type SendMethod = 'immediate' | 'scheduled'

/** 任务状态 */
type TaskStatus = 'pending' | 'sending' | 'completed' | 'cancelled' | 'failed'

/** 消息任务 */
interface MessageTask {
  id: number
  /** 任务名称 */
  name: string
  /** 消息类型 */
  type: MessageType
  /** 发送方式 */
  method: SendMethod
  /** 接收人总数 */
  recipientCount: number
  /** 成功数 */
  successCount: number
  /** 失败数 */
  failCount: number
  /** 任务状态 */
  status: TaskStatus
  /** 进度百分比 0-100 */
  progress: number
  /** 创建时间 */
  createTime: string
  /** 定时发送时间 */
  sendTime?: string
  /** 消息内容摘要 */
  content?: string
}

/** 消息任务查询参数 */
interface MessageTaskQuery extends PageQuery {
  /** 关键词 */
  keyword?: string
  /** 任务状态 */
  status?: TaskStatus | ''
  /** 开始日期 */
  startDate?: string
  /** 结束日期 */
  endDate?: string
}

/** 发送消息表单 */
interface SendMessageForm {
  /** 消息类型 */
  type: MessageType
  /** 消息内容 */
  content: string
  /** 接收人类型：按部门 / 按标签 / 手动选择 */
  recipientType: 'department' | 'tag' | 'manual'
  /** 接收人 ID 列表 */
  recipientIds: number[]
  /** 定时发送时间 */
  scheduleTime?: string
}

export {
  type MessageType,
  type SendMethod,
  type TaskStatus,
  type MessageTask,
  type MessageTaskQuery,
  type SendMessageForm,
}
