/** 应用配置信息 */
export interface CpConfig {
  id: number
  /** 应用名称 */
  name: string
  /** 企业 ID */
  corpId: string
  /** 应用密钥 */
  corpSecret: string
  /** 企微 AgentId */
  agentId: number
  /** 回调 Token */
  token: string
  /** 回调 EncodingAESKey */
  aesKey: string
  /** 状态：0=禁用，1=启用 */
  status: 0 | 1
  /** 创建时间 */
  createTime: string
}

/** 应用配置表单 */
export interface CpConfigForm {
  /** 编辑时传入 */
  id?: number
  /** 应用名称 */
  name: string
  /** 企业 ID */
  corpId: string
  /** 应用密钥 */
  corpSecret: string
  /** 企微 AgentId */
  agentId: string
  /** 回调 Token */
  token: string
  /** 回调 EncodingAESKey */
  aesKey: string
}
