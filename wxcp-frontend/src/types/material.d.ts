/** 素材分类 */
export interface MaterialCategory {
  /** 分类 ID */
  id: number
  /** 父级分类 ID，0 表示根分类 */
  parentId: number
  /** 分类名称 */
  name: string
  /** 子分类列表 */
  children?: MaterialCategory[]
}

/** 素材类型 */
export type MaterialType = 'image' | 'article' | 'video' | 'file'

/** 素材 */
export interface Material {
  /** 素材 ID */
  id: number
  /** 所属分类 ID */
  categoryId: number
  /** 素材名称 */
  name: string
  /** 素材类型 */
  type: MaterialType
  /** 资源 URL */
  url: string
  /** 缩略图 URL */
  thumbnailUrl?: string
  /** 文件大小（字节） */
  fileSize: number
  /** MIME 类型 */
  mimeType: string
  /** 上传时间 */
  uploadTime: string
}
