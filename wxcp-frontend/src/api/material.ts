import { get, post, put, del } from '@/utils/request'
import type { R, PageResult, PageQuery } from '@/types/api'
import type { MaterialCategory, MaterialType, Material } from '@/types/material'

/** 获取素材分类树 */
export function getMaterialCategories() {
  return get<R<MaterialCategory[]>>('/material/categories')
}

/** 创建素材分类 */
export function createMaterialCategory(data: { parentId: number; name: string }) {
  return post<R<null>>('/material/category', data)
}

/** 更新素材分类 */
export function updateMaterialCategory(data: { id: number; name: string }) {
  return put<R<null>>('/material/category', data)
}

/** 删除素材分类 */
export function deleteMaterialCategory(id: number) {
  return del<R<null>>(`/material/category/${id}`)
}

/** 获取素材列表（分页） */
export function getMaterialList(params: { categoryId?: number; type?: MaterialType } & PageQuery) {
  return get<R<PageResult<Material>>>('/material/list', { params })
}

/** 删除素材 */
export function deleteMaterial(id: number) {
  return del<R<null>>(`/material/${id}`)
}

/** 上传素材 */
export function uploadMaterial(formData: FormData) {
  return post<R<Material>>('/material/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
