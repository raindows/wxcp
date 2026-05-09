import type { PageQuery } from './api'

/** 系统用户 */
export interface SysUser {
  id: number
  username: string
  realName: string
  deptId: number
  deptName: string
  phone: string
  roleId: number
  roleName: string
  status: 0 | 1
  createTime: string
}

/** 系统角色 */
export interface SysRole {
  id: number
  name: string
  roleKey: string
  description: string
  status: 0 | 1
  menuIds: number[]
  createTime: string
}

/** 系统菜单 */
export interface SysMenu {
  id: number
  parentId: number
  name: string
  icon: string
  path: string
  component: string
  permission: string
  type: 'directory' | 'menu' | 'button'
  sort: number
  status: 0 | 1
  children?: SysMenu[]
}

/** 操作日志 */
export interface SysLog {
  id: number
  module: string
  type: 'INFO' | 'WARN' | 'ERROR'
  operator: string
  ip: string
  content: string
  createTime: string
}

/** 系统配置 */
export interface SysConfig {
  id: number
  configName: string
  configKey: string
  configValue: string
  configGroup: string
  remark: string
}

/** 用户查询参数 */
export interface SysUserQuery extends PageQuery {
  keyword?: string
}
