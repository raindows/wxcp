/** 数据看板查询参数 */
export interface DashboardQuery {
  period: 'today' | 'week' | 'month' | 'quarter'
}

/** 数据看板统计概览 */
export interface DashboardStats {
  totalCustomers: number
  todayNew: number
  todayMessages: number
  groupSendRate: number
}

/** 客户趋势数据 */
export interface CustomerTrend {
  dates: string[]
  newCustomers: number[]
  followUps: number[]
}

/** 员工排行 */
export interface EmployeeRank {
  name: string
  customerCount: number
}

/** 消息类型分布 */
export interface MessageTypeDistribution {
  name: string
  value: number
}

/** 群发效果摘要 */
export interface GroupSendSummary {
  totalTasks: number
  successRate: number
  totalRecipients: number
}

/** 工作台统计概览 */
export interface WorkbenchStats {
  /** 今日新增客户数 */
  newCustomers: number
  /** 今日新增客户趋势（百分比，负数为下降） */
  newCustomersTrend: number
  /** 待跟进客户数 */
  pendingFollow: number
  /** 待跟进客户趋势 */
  pendingFollowTrend: number
  /** 进行中群发数 */
  activeGroupSends: number
  /** 进行中群发趋势 */
  activeGroupSendsTrend: number
  /** 今日消息发送量 */
  todayMessages: number
  /** 今日消息发送量趋势 */
  todayMessagesTrend: number
}

/** 待办事项类型 */
export type TodoItemType = 'follow_up' | 'task' | 'expired_sop'

/** 待办事项 */
export interface TodoItem {
  id: number
  type: TodoItemType
  title: string
  customerName?: string
  deadline?: string
  status: 'pending' | 'overdue'
}

/** 最近操作记录 */
export interface RecentAction {
  id: number
  operator: string
  action: string
  target: string
  time: string
}
