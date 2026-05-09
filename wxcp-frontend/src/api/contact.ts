import { get, post, put, del } from '@/utils/request'
import type { R, PageResult } from '@/types/api'
import type {
  Department,
  ContactUser,
  TagGroup,
  ContactTag,
  ContactUserQuery,
} from '@/types/contact'

// ==================== 部门管理 ====================

/** 获取部门树 */
export function getDepartmentTree() {
  return get<R<Department[]>>('/contact/department/tree')
}

/** 创建部门 */
export function createDepartment(data: {
  parentId?: number
  name: string
  leader: string
  sort: number
  status: 0 | 1
}) {
  return post<R<null>>('/contact/department/create', data)
}

/** 更新部门 */
export function updateDepartment(data: {
  id: number
  parentId?: number
  name?: string
  leader?: string
  sort?: number
  status?: 0 | 1
}) {
  return put<R<null>>('/contact/department/update', data)
}

/** 删除部门 */
export function deleteDepartment(id: number) {
  return del<R<null>>(`/contact/department/delete/${id}`)
}

/** 同步企微部门 */
export function syncDepartments() {
  return post<R<null>>('/contact/department/sync')
}

// ==================== 用户管理 ====================

/** 获取用户列表（分页） */
export function getUserList(params: ContactUserQuery) {
  return get<R<PageResult<ContactUser>>>('/contact/user/list', { params })
}

/** 创建用户 */
export function createUser(data: {
  name: string
  departmentId: number
  position: string
  mobile: string
  userId: string
  status: 0 | 1
}) {
  return post<R<null>>('/contact/user/create', data)
}

/** 更新用户 */
export function updateUser(data: {
  id: number
  name?: string
  departmentId?: number
  position?: string
  mobile?: string
  userId?: string
  status?: 0 | 1
}) {
  return put<R<null>>('/contact/user/update', data)
}

/** 删除用户 */
export function deleteUser(id: number) {
  return del<R<null>>(`/contact/user/delete/${id}`)
}

/** 同步企微用户 */
export function syncUsers() {
  return post<R<null>>('/contact/user/sync')
}

// ==================== 标签管理 ====================

/** 获取标签分组列表 */
export function getTagGroups() {
  return get<R<TagGroup[]>>('/contact/tag/groups')
}

/** 获取分组下的标签 */
export function getTagsByGroup(groupId: number) {
  return get<R<ContactTag[]>>(`/contact/tag/list/${groupId}`)
}

/** 创建标签分组 */
export function createTagGroup(data: { name: string }) {
  return post<R<null>>('/contact/tag/group/create', data)
}

/** 创建标签 */
export function createTag(data: { groupId: number; name: string }) {
  return post<R<null>>('/contact/tag/create', data)
}

/** 删除标签 */
export function deleteTag(id: number) {
  return del<R<null>>(`/contact/tag/delete/${id}`)
}

/** 同步企微标签 */
export function syncTags() {
  return post<R<null>>('/contact/tag/sync')
}
