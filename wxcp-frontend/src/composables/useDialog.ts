import { ref } from 'vue'

/**
 * 通用弹窗控制 Composable
 *
 * 管理弹窗的显示/隐藏状态及编辑 ID，
 * 适用于新增 / 编辑共用同一个弹窗的场景。
 */
export function useDialog<T extends string | number = number>() {
  /** 弹窗是否可见 */
  const visible = ref(false)

  /** 编辑目标 ID（undefined 表示新增模式） */
  const editId = ref<T | undefined>(undefined)

  /**
   * 打开弹窗
   *
   * @param id  传入则进入编辑模式，不传则进入新增模式
   */
  function open(id?: T) {
    editId.value = id
    visible.value = true
  }

  /** 关闭弹窗并清空编辑 ID */
  function close() {
    visible.value = false
    editId.value = undefined
  }

  return {
    visible,
    editId,
    open,
    close,
  }
}
