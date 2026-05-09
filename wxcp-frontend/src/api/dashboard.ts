import { get } from '@/utils/request'
import type { R } from '@/types/api'
import type {
  WorkbenchStats,
  TodoItem,
  RecentAction,
  DashboardQuery,
  DashboardStats,
  CustomerTrend,
  EmployeeRank,
  MessageTypeDistribution,
  GroupSendSummary,
} from '@/types/dashboard'

/** 获取工作台统计概览 */
export function getWorkbenchStats() {
  return get<R<WorkbenchStats>>('/dashboard/stats')
}

/** 获取待办事项列表 */
export function getTodoList() {
  return get<R<TodoItem[]>>('/dashboard/todos')
}

/** 获取最近操作记录 */
export function getRecentActions() {
  return get<R<RecentAction[]>>('/dashboard/recent-actions')
}

/** 获取数据看板统计概览 */
export function getDashboardStats(params: DashboardQuery) {
  return get<R<DashboardStats>>('/dashboard/data/stats', { params })
}

/** 获取客户趋势数据 */
export function getCustomerTrend(params: DashboardQuery) {
  return get<R<CustomerTrend>>('/dashboard/data/customer-trend', { params })
}

/** 获取员工排行 */
export function getEmployeeRank(params: DashboardQuery) {
  return get<R<EmployeeRank[]>>('/dashboard/data/employee-rank', { params })
}

/** 获取消息类型分布 */
export function getMessageTypeDistribution(params: DashboardQuery) {
  return get<R<MessageTypeDistribution[]>>('/dashboard/data/message-type', { params })
}

/** 获取群发效果摘要 */
export function getGroupSendSummary(params: DashboardQuery) {
  return get<R<GroupSendSummary>>('/dashboard/data/group-send-summary', { params })
}
