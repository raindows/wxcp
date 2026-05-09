/** 通用 API 响应结构 */
export interface R<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页查询参数 */
export interface PageQuery {
  pageNum: number
  pageSize: number
  [key: string]: unknown
}

/** 分页结果 */
export interface PageResult<T = unknown> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}
