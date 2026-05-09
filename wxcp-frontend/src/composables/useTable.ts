import { ref, reactive } from 'vue'
import type { PageQuery, PageResult, R } from '@/types/api'

/**
 * 通用表格分页 Composable
 *
 * @template T  表格行数据类型
 * @template Q  查询参数类型（必须继承 PageQuery）
 */
export function useTable<T = unknown, Q extends PageQuery = PageQuery>() {
  /** 加载状态 */
  const loading = ref(false)

  /** 表格数据列表 */
  const list = ref<T[]>([])

  /** 数据总条数 */
  const total = ref(0)

  /** 多选已选行 */
  const selectedRows = ref<T[]>([])

  /** 查询参数（带分页） */
  const query = reactive<Q>({
    pageNum: 1,
    pageSize: 20,
  } as Q)

  /**
   * 加载表格数据
   *
   * @param apiFn  返回分页结果的 API 函数
   */
  async function loadData(apiFn: (params: Q) => Promise<R<PageResult<T>>>) {
    loading.value = true
    try {
      const res = await apiFn({ ...query } as Q)
      list.value = res.data.list
      total.value = res.data.total
    } catch {
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 重置查询条件并重新加载（ pageNum 重置为 1 ） */
  async function resetQuery(apiFn?: (params: Q) => Promise<R<PageResult<T>>>) {
    query.pageNum = 1
    // 保留 pageNum 和 pageSize，清空其余查询字段
    const keys = Object.keys(query)
    for (const key of keys) {
      if (key !== 'pageNum' && key !== 'pageSize') {
        delete (query as Record<string, unknown>)[key]
      }
    }
    if (apiFn) {
      await loadData(apiFn)
    }
  }

  /** 处理多选变化 */
  function handleSelectionChange(rows: T[]) {
    selectedRows.value = rows
  }

  /** 处理每页条数变化 */
  function handleSizeChange(pageSize: number) {
    query.pageSize = pageSize
    query.pageNum = 1
  }

  /** 处理当前页码变化 */
  function handleCurrentChange(pageNum: number) {
    query.pageNum = pageNum
  }

  return {
    loading,
    list,
    total,
    selectedRows,
    query,
    loadData,
    resetQuery,
    handleSelectionChange,
    handleSizeChange,
    handleCurrentChange,
  }
}
