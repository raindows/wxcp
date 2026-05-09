import { get, post } from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type {
  CampaignTask,
  CampaignTaskQuery,
  SopTemplate,
  SopTemplateQuery,
  JourneyCustomer,
  JourneyStage,
} from '@/types/campaign'

/** 获取群发任务列表（分页） */
export function getCampaignTasks(params: CampaignTaskQuery) {
  return get<R<PageResult<CampaignTask>>>('/campaign/task/list', { params })
}

/** 创建群发任务 */
export function createCampaignTask(data: Partial<CampaignTask>) {
  return post<R<null>>('/campaign/task/create', data)
}

/** 更新群发任务 */
export function updateCampaignTask(data: Partial<CampaignTask>) {
  return post<R<null>>('/campaign/task/update', data)
}

/** 删除群发任务 */
export function deleteCampaignTask(id: number) {
  return post<R<null>>(`/campaign/task/delete/${id}`)
}

/** 取消群发任务 */
export function cancelCampaignTask(id: number) {
  return post<R<null>>(`/campaign/task/cancel/${id}`)
}

/** 获取 SOP 话术列表（分页） */
export function getSopTemplates(params: SopTemplateQuery) {
  return get<R<PageResult<SopTemplate>>>('/campaign/sop/list', { params })
}

/** 创建 SOP 话术 */
export function createSopTemplate(data: Partial<SopTemplate>) {
  return post<R<null>>('/campaign/sop/create', data)
}

/** 更新 SOP 话术 */
export function updateSopTemplate(data: Partial<SopTemplate>) {
  return post<R<null>>('/campaign/sop/update', data)
}

/** 删除 SOP 话术 */
export function deleteSopTemplate(id: number) {
  return post<R<null>>(`/campaign/sop/delete/${id}`)
}

/** 获取客户旅程看板数据 */
export function getJourneyBoard() {
  return get<R<JourneyCustomer[]>>('/campaign/journey/board')
}

/** 移动客户到指定旅程阶段 */
export function moveJourneyStage(customerId: number, stage: JourneyStage) {
  return post<R<null>>('/campaign/journey/move', { customerId, stage })
}
