import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'

export const usePermissionStore = defineStore('permission', () => {
  /** 动态路由是否已加载 */
  const isRoutesLoaded = ref(false)

  /** 动态添加的路由记录 */
  const dynamicRoutes = ref<RouteRecordRaw[]>([])

  /** 设置路由已加载 */
  function setRoutesLoaded(loaded: boolean) {
    isRoutesLoaded.value = loaded
  }

  /** 添加动态路由 — Task 15 将在此调用 router.addRoute() 注册业务路由 */
  function addDynamicRoutes(routes: RouteRecordRaw[]) {
    dynamicRoutes.value = [...dynamicRoutes.value, ...routes]
    isRoutesLoaded.value = true
  }

  /** 重置权限状态 */
  function resetRoutes() {
    dynamicRoutes.value = []
    isRoutesLoaded.value = false
  }

  return {
    isRoutesLoaded,
    dynamicRoutes,
    setRoutesLoaded,
    addDynamicRoutes,
    resetRoutes,
  }
})
