import type { PageQuery } from './api'

/** 营销任务状态 */
type CampaignStatus = 'pending' | 'sending' | 'completed' | 'cancelled' | 'failed'

/** SOP 触发类型 */
type TriggerType = 'new_customer' | 'add_days' | 'manual'

/** 客户旅程阶段 */
type JourneyStage = 'new' | 'interested' | 'deal' | 'lost'

/** 群发任务 */
interface CampaignTask {
  /** 任务 ID */
  id: number
  /** 任务名称 */
  name: string
  /** 任务类型：群发/定时 */
  type: string
  /** 发送范围名称（标签/部门） */
  scopeName: string
  /** 接收人数量 */
  recipientCount: number
  /** 成功数量 */
  successCount: number
  /** 任务状态 */
  status: CampaignStatus
  /** 进度百分比 0-100 */
  progress: number
  /** 创建时间 */
  createTime: string
}

/** 群发任务查询参数 */
interface CampaignTaskQuery extends PageQuery {
  /** 关键词 */
  keyword?: string
  /** 任务状态 */
  status?: CampaignStatus | ''
}

/** SOP 话术模板 */
interface SopTemplate {
  /** 模板 ID */
  id: number
  /** 话术名称 */
  name: string
  /** 触发类型 */
  triggerType: TriggerType
  /** 适用范围类型 */
  scopeType: string
  /** 适用范围值 */
  scopeValue: string
  /** 话术内容 */
  content: string
  /** 状态：0 停用，1 启用 */
  status: 0 | 1
  /** 创建时间 */
  createTime: string
}

/** SOP 话术查询参数 */
interface SopTemplateQuery extends PageQuery {
  /** 关键词 */
  keyword?: string
}

/** 客户旅程客户 */
interface JourneyCustomer {
  /** 记录 ID */
  id: number
  /** 客户 ID */
  customerId: number
  /** 客户姓名 */
  customerName: string
  /** 企业名称 */
  company: string
  /** 旅程阶段 */
  stage: JourneyStage
  /** 进入该阶段的时间 */
  enterTime: string
  /** 头像 URL */
  avatar?: string
}

export {
  type CampaignStatus,
  type TriggerType,
  type JourneyStage,
  type CampaignTask,
  type CampaignTaskQuery,
  type SopTemplate,
  type SopTemplateQuery,
  type JourneyCustomer,
}
