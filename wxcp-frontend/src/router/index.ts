import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { constantRoutes } from './routes'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes as RouteRecordRaw[],
  scrollBehavior() {
    return { top: 0 }
  },
})

// 404 兜底路由 — 必须在所有动态路由注册之后再添加，否则会吞掉后续的异步路由
router.addRoute({
  path: '/:pathMatch(.*)*',
  name: 'NotFound',
  component: () => import('@/views/error/404.vue'),
  meta: { title: '404', hidden: true },
})

/** 白名单路由（不需要登录即可访问） */
const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  const title = to.meta?.title as string | undefined
  if (title) {
    document.title = `${title} - ${import.meta.env.VITE_APP_TITLE}`
  }

  const token = getToken()

  if (token) {
    // 已登录
    if (to.path === '/login') {
      // 已登录状态访问登录页，重定向到工作台
      next({ path: '/dashboard/workbench' })
    } else {
      const permissionStore = usePermissionStore()
      if (permissionStore.isRoutesLoaded) {
        // 路由已加载，直接放行
        next()
      } else {
        try {
          const userStore = useUserStore()
          // 如果已有用户信息（如模拟登录设置），直接使用，不再请求 API
          if (!userStore.userInfo) {
            await userStore.fetchUserInfo()
          }
          // 标记路由已加载
          permissionStore.setRoutesLoaded(true)
          // 放行当前导航
          next({ ...to, replace: true })
        } catch {
          // 获取用户信息失败，清除 token 并跳转登录
          const userStore = useUserStore()
          userStore.resetState()
          permissionStore.resetRoutes()
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 白名单路由，直接放行
      next()
    } else {
      // 重定向到登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
