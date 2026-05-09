import { get, post } from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type { CustomerQuery, Customer, CustomerDetail, CustomerStats } from '@/types/customer'

/** 获取客户列表（分页） */
export function getCustomerList(params: CustomerQuery) {
  return get<R<PageResult<Customer>>>('/customer/list', { params })
}

/** 获取客户详情（含跟进记录） */
export function getCustomerDetail(id: number) {
  return get<R<CustomerDetail>>(`/customer/detail/${id}`)
}

/** 获取客户统计概览 */
export function getCustomerStats() {
  return get<R<CustomerStats>>('/customer/stats')
}

/** 同步企微客户 */
export function syncCustomers() {
  return post<R<null>>('/customer/sync')
}

/** 导出客户列表 */
export function exportCustomers(params: CustomerQuery) {
  return get<Blob>('/customer/export', { params, responseType: 'blob' })
}

/** 批量添加标签 */
export function batchAddTags(customerIds: number[], tagIds: number[]) {
  return post<R<null>>('/customer/batch-tags', { customerIds, tagIds })
}
