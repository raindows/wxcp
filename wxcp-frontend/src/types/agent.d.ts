import type { PageQuery } from './api'

/** 联系方式类型：1=单人  2=多人 */
type ContactWayType = 1 | 2

/** 使用场景：1=小程序  2=二维码 */
type ContactWayScene = 1 | 2

/** 坐席二维码记录 */
export interface AgentQRCode {
  id: number
  /** 企微返回的 config_id */
  configId: string
  /** 备注名称 */
  name: string
  /** 联系方式类型 1=单人 2=多人 */
  type: ContactWayType
  /** 使用场景 1=小程序 2=二维码 */
  scene: ContactWayScene
  /** 关联用户 userId 列表 */
  users: string[]
  /** 关联用户名称列表（展示用） */
  userNames: string[]
  /** 二维码 URL（scene=2 时有值） */
  qrCode: string
  /** 渠道标识 */
  state: string
  /** 外部客户添加时是否无需验证 */
  skipVerify: boolean
  /** 是否临时会话模式 */
  isTemp: boolean
  /** 临时会话二维码有效秒数 */
  expiresIn: number
  /** 启用/停用 0=停用 1=启用 */
  status: 0 | 1
  /** 创建时间 */
  createTime: string
}

/** 新增/编辑坐席二维码表单 */
export interface AgentQRCodeForm {
  id?: number
  /** 备注名称 */
  name: string
  /** 联系方式类型 */
  type: ContactWayType
  /** 使用场景 */
  scene: ContactWayScene
  /** 关联用户 userId 列表 */
  users: string[]
  /** 渠道标识 */
  state: string
  /** 是否无需验证 */
  skipVerify: boolean
  /** 是否临时会话 */
  isTemp: boolean
  /** 临时二维码有效期（秒） */
  expiresIn: number
}

/** 查询参数 */
export interface AgentQRCodeQuery extends PageQuery {
  keyword?: string
  type?: ContactWayType
  status?: number
}
