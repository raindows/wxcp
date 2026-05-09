/** 部门信息 */
export interface Department {
  /** 部门 ID */
  id: number
  /** 父部门 ID */
  parentId: number
  /** 部门名称 */
  name: string
  /** 部门负责人 */
  leader: string
  /** 成员数量 */
  memberCount: number
  /** 排序 */
  sort: number
  /** 状态 0=禁用 1=启用 */
  status: 0 | 1
  /** 子部门 */
  children?: Department[]
}

/** 部门表单 */
export interface DepartmentForm {
  id?: number
  parentId?: number
  name: string
  leader: string
  sort: number
  status: 0 | 1
}

/** 通讯录用户 */
export interface ContactUser {
  /** 用户 ID */
  id: number
  /** 姓名 */
  name: string
  /** 部门 ID */
  departmentId: number
  /** 部门名称 */
  departmentName: string
  /** 职位 */
  position: string
  /** 手机号 */
  mobile: string
  /** 企微 userId */
  userId: string
  /** 状态 0=禁用 1=启用 */
  status: 0 | 1
}

/** 用户表单 */
export interface ContactUserForm {
  id?: number
  name: string
  departmentId: number | undefined
  position: string
  mobile: string
  userId: string
  status: 0 | 1
}

/** 用户查询参数 */
export interface ContactUserQuery {
  keyword?: string
  departmentId?: number
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

/** 标签分组 */
export interface TagGroup {
  /** 分组 ID */
  id: number
  /** 分组名称 */
  name: string
  /** 排序 */
  sortOrder: number
  /** 标签数量 */
  tagCount: number
}

/** 标签 */
export interface ContactTag {
  /** 标签 ID */
  id: number
  /** 所属分组 ID */
  groupId: number
  /** 标签名称 */
  name: string
  /** 排序 */
  sortOrder: number
}
