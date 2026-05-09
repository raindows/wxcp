import type { RouteRecordRaw } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'

/** 公开路由（无需认证） */
export const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true },
  },
]

/** 需要认证的路由 */
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard/workbench',
    children: [
      {
        path: 'dashboard/workbench',
        name: 'Workbench',
        component: () => import('@/views/dashboard/workbench/index.vue'),
        meta: { title: '工作台', icon: 'Monitor', group: '概览' },
      },
      {
        path: 'dashboard/data',
        name: 'DataDashboard',
        component: () => import('@/views/dashboard/data/index.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis', group: '数据中心' },
      },
      {
        path: 'customer/list',
        name: 'CustomerList',
        component: () => import('@/views/customer/list/index.vue'),
        meta: { title: '客户管理', icon: 'User', group: '营销中心' },
      },
      {
        path: 'message/push',
        name: 'MessagePush',
        component: () => import('@/views/message/push/index.vue'),
        meta: { title: '消息推送', icon: 'Promotion', group: '营销中心' },
      },
      {
        path: 'message/task',
        name: 'MessageTask',
        component: () => import('@/views/message/task/index.vue'),
        meta: { title: '任务列表', icon: 'List', group: '营销中心' },
      },
      {
        path: 'auto-reply/welcome',
        name: 'AutoReplyWelcome',
        component: () => import('@/views/autoReply/welcome/index.vue'),
        meta: { title: '欢迎语', icon: 'ChatLineRound', group: '营销中心' },
      },
      {
        path: 'auto-reply/keyword',
        name: 'AutoReplyKeyword',
        component: () => import('@/views/autoReply/keyword/index.vue'),
        meta: { title: '关键词回复', icon: 'ChatLineRound', group: '营销中心' },
      },
      {
        path: 'auto-reply/default',
        name: 'AutoReplyDefault',
        component: () => import('@/views/autoReply/default/index.vue'),
        meta: { title: '默认回复', icon: 'ChatLineRound', group: '营销中心' },
      },
      {
        path: 'campaign/task',
        name: 'CampaignTask',
        component: () => import('@/views/campaign/task/index.vue'),
        meta: { title: '群发任务', icon: 'Trophy', group: '营销中心' },
      },
      {
        path: 'campaign/sop',
        name: 'CampaignSop',
        component: () => import('@/views/campaign/sop/index.vue'),
        meta: { title: 'SOP话术', icon: 'ChatLineRound', group: '营销中心' },
      },
      {
        path: 'campaign/journey',
        name: 'CampaignJourney',
        component: () => import('@/views/campaign/journey/index.vue'),
        meta: { title: '客户旅程', icon: 'MapLocation', group: '营销中心' },
      },
      {
        path: 'material/list',
        name: 'MaterialList',
        component: () => import('@/views/material/index.vue'),
        meta: { title: '素材管理', icon: 'PictureFilled', group: '营销中心' },
      },
      {
        path: 'agent/qr',
        name: 'AgentQR',
        component: () => import('@/views/agent/qr/index.vue'),
        meta: { title: '坐席二维码', icon: 'Link', group: '营销中心' },
      },
      {
        path: 'contact/department',
        name: 'ContactDepartment',
        component: () => import('@/views/contact/department/index.vue'),
        meta: { title: '部门管理', icon: 'Connection', group: '系统设置' },
      },
      {
        path: 'contact/user',
        name: 'ContactUser',
        component: () => import('@/views/contact/user/index.vue'),
        meta: { title: '用户管理', icon: 'Connection', group: '系统设置' },
      },
      {
        path: 'contact/tag',
        name: 'ContactTag',
        component: () => import('@/views/contact/tag/index.vue'),
        meta: { title: '标签管理', icon: 'Connection', group: '系统设置' },
      },
      {
        path: 'config/app',
        name: 'ConfigApp',
        component: () => import('@/views/config/app/index.vue'),
        meta: { title: '应用配置', icon: 'Setting', group: '系统设置' },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'Lock', group: '系统设置' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Lock', group: '系统设置' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Lock', group: '系统设置' },
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志', icon: 'Lock', group: '系统设置' },
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统配置', icon: 'Lock', group: '系统设置' },
      },
    ],
  },
]

/** 静态路由（常驻路由表） — 注意：404 兜底路由在 router/index.ts 中动态添加，避免吞掉后续注册的异步路由 */
export const constantRoutes: RouteRecordRaw[] = [...publicRoutes, ...asyncRoutes]

/** 侧边栏菜单分组定义（用于渲染菜单） */
export interface MenuGroup {
  title: string
  items: {
    path: string
    title: string
    icon: string
  }[]
}

/** 完整侧边栏菜单配置 */
export const sidebarMenuGroups: MenuGroup[] = [
  {
    title: '概览',
    items: [{ path: '/dashboard/workbench', title: '工作台', icon: 'Monitor' }],
  },
  {
    title: '营销中心',
    items: [
      { path: '/customer/list', title: '客户管理', icon: 'User' },
      { path: '/message/push', title: '消息推送', icon: 'Promotion' },
      { path: '/message/task', title: '任务列表', icon: 'List' },
      { path: '/auto-reply/welcome', title: '欢迎语', icon: 'ChatLineRound' },
      { path: '/auto-reply/keyword', title: '关键词回复', icon: 'ChatLineRound' },
      { path: '/auto-reply/default', title: '默认回复', icon: 'ChatLineRound' },
      { path: '/material/list', title: '素材管理', icon: 'PictureFilled' },
      { path: '/agent/qr', title: '坐席二维码', icon: 'Link' },
      { path: '/campaign/task', title: '群发任务', icon: 'Trophy' },
      { path: '/campaign/sop', title: 'SOP话术', icon: 'ChatLineRound' },
      { path: '/campaign/journey', title: '客户旅程', icon: 'MapLocation' },
    ],
  },
  {
    title: '数据中心',
    items: [{ path: '/dashboard/data', title: '数据看板', icon: 'DataAnalysis' }],
  },
  {
    title: '系统设置',
    items: [
      { path: '/config/app', title: '应用配置', icon: 'Setting' },
      { path: '/contact/department', title: '部门管理', icon: 'Connection' },
      { path: '/contact/user', title: '用户管理', icon: 'UserFilled' },
      { path: '/contact/tag', title: '标签管理', icon: 'PriceTag' },
      { path: '/system/user', title: '用户管理', icon: 'Lock' },
      { path: '/system/role', title: '角色管理', icon: 'Lock' },
      { path: '/system/log', title: '操作日志', icon: 'Lock' },
    ],
  },
]
