import type { PageQuery } from './api'

/** 客户查询参数 */
export interface CustomerQuery extends PageQuery {
  /** 关键词（姓名/企业） */
  keyword?: string
  /** 跟进人 ID */
  followUserId?: number
  /** 标签 ID */
  tagId?: number
  /** 开始日期 */
  startDate?: string
  /** 结束日期 */
  endDate?: string
}

/** 客户来源渠道类型 */
export type CustomerSource = '扫描二维码' | '搜索手机号' | '分享名片' | '群聊添加' | '其他'

/** 客户状态 */
export type CustomerStatus = 'active' | 'lost' | 'unfollow'

/** 客户信息 */
export interface Customer {
  id: number
  /** 企微外部联系人 ID */
  externalUserId: string
  /** 客户姓名 */
  name: string
  /** 头像 URL */
  avatar: string
  /** 企业名称 */
  company: string
  /** 职位 */
  position: string
  /** 来源渠道 */
  source: CustomerSource
  /** 标签列表 */
  tags: string[]
  /** 跟进人 */
  followUser: string
  /** 客户状态 */
  status: CustomerStatus
  /** 添加时间 */
  addTime: string
  /** 备注 */
  remark?: string
}

/** 跟进记录类型 */
export type FollowRecordType = 'follow' | 'note' | 'call'

/** 跟进记录 */
export interface FollowRecord {
  id: number
  /** 客户 ID */
  customerId: number
  /** 跟进内容 */
  content: string
  /** 操作人 */
  operator: string
  /** 操作时间 */
  time: string
  /** 跟进类型 */
  type: FollowRecordType
}

/** 客户详情（含跟进记录） */
export interface CustomerDetail extends Customer {
  /** 跟进记录列表 */
  followRecords: FollowRecord[]
}

/** 客户统计概览 */
export interface CustomerStats {
  /** 客户总数 */
  total: number
  /** 今日新增 */
  todayNew: number
  /** 待跟进 */
  pendingFollow: number
  /** 客户群数 */
  groupCount: number
}
