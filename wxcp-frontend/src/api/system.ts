import { get, post, put, del } from '@/utils/request'
import type { R } from '@/types/api'
import type { PageQuery, PageResult } from '@/types/api'
import type { SysUser, SysRole, SysMenu, SysLog, SysConfig } from '@/types/system'

// ==================== 用户管理 ====================

/** 获取用户列表（分页） */
export function getSysUserList(params: PageQuery & { keyword?: string }) {
  return get<R<PageResult<SysUser>>>('/system/user/list', { params })
}

/** 新增用户 */
export function createSysUser(data: Partial<SysUser> & { password: string }) {
  return post<R<null>>('/system/user/create', data)
}

/** 更新用户 */
export function updateSysUser(data: Partial<SysUser>) {
  return put<R<null>>('/system/user/update', data)
}

/** 删除用户 */
export function deleteSysUser(id: number) {
  return del<R<null>>(`/system/user/delete/${id}`)
}

/** 重置密码 */
export function resetPassword(id: number) {
  return post<R<null>>(`/system/user/reset-password/${id}`)
}

// ==================== 角色管理 ====================

/** 获取角色列表 */
export function getSysRoleList() {
  return get<R<SysRole[]>>('/system/role/list')
}

/** 新增角色 */
export function createSysRole(data: Partial<SysRole>) {
  return post<R<null>>('/system/role/create', data)
}

/** 更新角色 */
export function updateSysRole(data: Partial<SysRole>) {
  return put<R<null>>('/system/role/update', data)
}

/** 删除角色 */
export function deleteSysRole(id: number) {
  return del<R<null>>(`/system/role/delete/${id}`)
}

/** 获取菜单树 */
export function getMenuTree() {
  return get<R<SysMenu[]>>('/system/menu/tree')
}

/** 分配角色菜单权限 */
export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return post<R<null>>(`/system/role/assign-menus/${roleId}`, { menuIds })
}

// ==================== 菜单管理 ====================

/** 获取菜单树 */
export function getSysMenuTree() {
  return get<R<SysMenu[]>>('/system/menu/tree')
}

/** 新增菜单 */
export function createSysMenu(data: Partial<SysMenu>) {
  return post<R<null>>('/system/menu/create', data)
}

/** 更新菜单 */
export function updateSysMenu(data: Partial<SysMenu>) {
  return put<R<null>>('/system/menu/update', data)
}

/** 删除菜单 */
export function deleteSysMenu(id: number) {
  return del<R<null>>(`/system/menu/delete/${id}`)
}

// ==================== 操作日志 ====================

/** 获取日志列表（分页） */
export function getSysLogList(
  params: PageQuery & {
    keyword?: string
    operator?: string
    startDate?: string
    endDate?: string
  },
) {
  return get<R<PageResult<SysLog>>>('/system/log/list', { params })
}

// ==================== 系统配置 ====================

/** 获取配置列表（分页） */
export function getSysConfigList(params: PageQuery & { configGroup?: string }) {
  return get<R<PageResult<SysConfig>>>('/system/config/list', { params })
}

/** 更新配置 */
export function updateSysConfig(data: Partial<SysConfig>) {
  return put<R<null>>('/system/config/update', data)
}
