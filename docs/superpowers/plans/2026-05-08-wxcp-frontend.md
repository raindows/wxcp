# WXCP 前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现企业微信营销平台的前端 Vue 3 管理控制台，包含布局框架、路由权限、10个业务模块页面。

**Architecture:** Vue 3 + Vite + TypeScript + Element Plus，Composition API + Pinia 状态管理。Maven 多模块项目中的 wxcp-frontend 子目录，独立运行开发服务器，通过 Vite proxy 代理后端 API。

**Tech Stack:** Vue 3.5, Vite 6.x, TypeScript 5.x, Element Plus 2.9, Pinia 2.x, Vue Router 4.x, Axios 1.x, ECharts 5.x, @vueuse/core

---

## 文件结构

```
wxcp-frontend/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── .env.development
├── .env.production
├── .eslintrc.cjs
├── .prettierrc
├── src/
│   ├── main.ts                          # 应用入口
│   ├── App.vue                          # 根组件
│   ├── env.d.ts                         # 类型声明
│   ├── api/
│   │   ├── auth.ts                      # 登录、刷新 Token
│   │   ├── config.ts                    # 应用配置 CRUD
│   │   ├── contact.ts                   # 通讯录（部门、用户、标签）
│   │   ├── customer.ts                  # 客户管理
│   │   ├── message.ts                   # 消息推送
│   │   ├── autoReply.ts                # 自动回复
│   │   ├── material.ts                  # 素材管理
│   │   ├── campaign.ts                  # 营销活动
│   │   ├── dashboard.ts                 # 数据看板
│   │   └── system.ts                    # 系统管理
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppLayout.vue            # 主布局容器
│   │   │   ├── Sidebar.vue              # 左侧导航栏
│   │   │   ├── Header.vue               # 顶部栏
│   │   │   └── PageHeader.vue           # 页面标题栏
│   │   ├── common/
│   │   │   ├── StatCard.vue             # 统计指标卡片
│   │   │   ├── FilterBar.vue            # 通用筛选栏
│   │   │   ├── EmptyState.vue           # 空状态占位
│   │   │   └── FloatingBar.vue          # 批量操作浮动栏
│   │   └── charts/
│   │       ├── TrendChart.vue           # 趋势图（折线/柱状）
│   │       ├── PieChart.vue             # 饼图/环形图
│   │       └── RankList.vue             # 排行榜
│   ├── composables/
│   │   ├── useTable.ts                  # 表格通用逻辑
│   │   └── useDialog.ts                 # Dialog 弹窗控制
│   ├── router/
│   │   ├── index.ts                     # 路由实例 + 全局守卫
│   │   └── routes.ts                    # 路由定义
│   ├── stores/
│   │   ├── user.ts                      # 用户信息、Token
│   │   ├── app.ts                       # 全局状态（侧边栏折叠）
│   │   └── permission.ts                # 动态路由
│   ├── styles/
│   │   ├── variables.scss               # CSS 变量
│   │   ├── reset.scss                   # 样式重置
│   │   ├── global.scss                  # 全局通用样式
│   │   └── element-overrides.scss       # Element Plus 覆盖
│   ├── types/
│   │   ├── api.d.ts                     # R<T>, PageResult, PageQuery
│   │   ├── auth.d.ts
│   │   ├── config.d.ts
│   │   ├── contact.d.ts
│   │   ├── customer.d.ts
│   │   ├── message.d.ts
│   │   ├── material.d.ts
│   │   ├── campaign.d.ts
│   │   ├── dashboard.d.ts
│   │   └── system.d.ts
│   ├── utils/
│   │   ├── request.ts                   # Axios 封装 + 拦截器
│   │   └── auth.ts                      # Token 存取
│   └── views/
│       ├── login/index.vue
│       ├── dashboard/workbench/index.vue
│       ├── dashboard/data/index.vue
│       ├── customer/list/index.vue
│       ├── customer/components/CustomerDrawer.vue
│       ├── message/push/index.vue
│       ├── message/task/index.vue
│       ├── autoReply/welcome/index.vue
│       ├── autoReply/keyword/index.vue
│       ├── autoReply/default/index.vue
│       ├── material/index.vue
│       ├── campaign/task/index.vue
│       ├── campaign/sop/index.vue
│       ├── campaign/journey/index.vue
│       ├── config/app/index.vue
│       ├── contact/department/index.vue
│       ├── contact/user/index.vue
│       ├── contact/tag/index.vue
│       ├── system/user/index.vue
│       ├── system/role/index.vue
│       ├── system/menu/index.vue
│       ├── system/log/index.vue
│       └── system/config/index.vue
```

---

### Task 1: 项目初始化

**Files:**
- Create: `wxcp-frontend/` (Vite 脚手架)

- [ ] **Step 1: 创建 Vue 3 + TypeScript 项目**

```bash
cd /Users/wangxy/Downloads/codespace/aicoding/workspace-claude/wxcp
npm create vite@latest wxcp-frontend -- --template vue-ts
cd wxcp-frontend
```

- [ ] **Step 2: 安装依赖**

```bash
npm install element-plus @element-plus/icons-vue pinia vue-router@4 axios echarts vue-echarts @vueuse/core
npm install -D sass unplugin-auto-import unplugin-vue-components @types/node
```

- [ ] **Step 3: 配置 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
  },
  server: {
    port: 3000,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
  },
})
```

- [ ] **Step 4: 配置环境变量**

`.env.development`:
```
VITE_APP_TITLE=WXCP 营销平台
VITE_API_BASE_URL=/api
```

`.env.production`:
```
VITE_APP_TITLE=WXCP 营销平台
VITE_API_BASE_URL=/api
```

- [ ] **Step 5: 配置 .eslintrc.cjs 和 .prettierrc**

`.eslintrc.cjs`:
```javascript
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: ['eslint:recommended', 'plugin:@typescript-eslint/recommended', 'plugin:vue/vue3-recommended'],
  parser: 'vue-eslint-parser',
  parserOptions: { ecmaVersion: 'latest', parser: '@typescript-eslint/parser', sourceType: 'module' },
  rules: { 'vue/multi-word-component-names': 'off', '@typescript-eslint/no-explicit-any': 'off' },
}
```

`.prettierrc`:
```json
{ "semi": false, "singleQuote": true, "tabWidth": 2, "trailingComma": "all", "printWidth": 100 }
```

- [ ] **Step 6: 创建 src/env.d.ts**

```typescript
/// <reference types="vite/client" />
interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_API_BASE_URL: string
}
interface ImportMeta { readonly env: ImportMetaEnv }
```

- [ ] **Step 7: 验证启动**

```bash
npm run dev
```

- [ ] **Step 8: Commit**

```bash
git add wxcp-frontend/
git commit -m "feat: initialize Vue 3 + Vite + TypeScript frontend project"
```

---

### Task 2: 设计系统与全局样式

**Files:**
- Create: `src/styles/variables.scss`
- Create: `src/styles/reset.scss`
- Create: `src/styles/global.scss`
- Create: `src/styles/element-overrides.scss`
- Modify: `src/main.ts`

- [ ] **Step 1: 创建 CSS 变量**

`src/styles/variables.scss` — 包含完整色彩系统（主色 #165DFF、成功/警告/危险色、文字色、背景色、边框色）、侧边栏色彩、圆角、间距基准、字体定义。所有值与设计文档第2节完全对齐。

- [ ] **Step 2: 创建 reset.scss 和 global.scss**

reset.scss: `box-sizing: border-box` 重置、`html` 字体平滑、`body` 背景和字体。

global.scss: `.page-container`、`.page-title-bar`、`.stat-cards`、`.filter-bar`、`.table-wrapper`、`.stat-value/label/trend` 等通用 class。

- [ ] **Step 3: 创建 element-overrides.scss**

覆盖 Element Plus 组件样式：按钮圆角 6px、表格表头背景 #f7f8fa、输入框圆角 4px、Dialog 圆角 8px、Tag 圆角 4px。

- [ ] **Step 4: 在 main.ts 中引入样式**

```typescript
import './styles/variables.scss'
import './styles/reset.scss'
import './styles/global.scss'
import './styles/element-overrides.scss'
```

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add design system variables and global styles"
```

---

### Task 3: 路由、状态管理与布局框架

**Files:**
- Create: `src/types/api.d.ts`
- Create: `src/utils/auth.ts`
- Create: `src/utils/request.ts`
- Create: `src/api/auth.ts`
- Create: `src/stores/user.ts`
- Create: `src/stores/app.ts`
- Create: `src/stores/permission.ts`
- Create: `src/router/routes.ts`
- Create: `src/router/index.ts`
- Create: `src/components/layout/AppLayout.vue`
- Create: `src/components/layout/Sidebar.vue`
- Create: `src/components/layout/Header.vue`
- Create: `src/components/layout/PageHeader.vue`
- Create: `src/views/login/index.vue`
- Create: `src/views/dashboard/workbench/index.vue`
- Create: `src/views/dashboard/data/index.vue`
- Modify: `src/main.ts`
- Modify: `src/App.vue`

- [ ] **Step 1: 创建 api.d.ts — R<T>、PageQuery、PageResult**

- [ ] **Step 2: 创建 utils/auth.ts — localStorage Token 存取**

- [ ] **Step 3: 创建 utils/request.ts — Axios 实例 + 请求拦截(附加Token) + 响应拦截(401自动刷新+错误提示)**

- [ ] **Step 4: 创建 api/auth.ts — loginByCode、getUserInfo、logout、getOAuthUrl**

- [ ] **Step 5: 创建 stores/user.ts、app.ts、permission.ts**

- [ ] **Step 6: 创建 router/routes.ts — Login 路由 + Layout 路由（内含 Workbench 和 DataDashboard 子路由）+ 404 兜底**

- [ ] **Step 7: 创建 router/index.ts — 路由实例 + beforeEach 全局守卫（Token判断 → 白名单放行 → 获取用户信息 → 动态路由注入）**

- [ ] **Step 8: 创建 AppLayout.vue（flex 布局，侧边栏 fixed + main 区域）**

- [ ] **Step 9: 创建 Sidebar.vue（深色背景 #1d2129，4组分组菜单，选中项蓝色高亮，折叠动画，注册 Element Plus 图标）**

- [ ] **Step 10: 创建 Header.vue（折叠按钮 + 面包屑 + 通知徽标 + 用户下拉退出）**

- [ ] **Step 11: 创建 PageHeader.vue（标题 + 操作按钮 slot）**

- [ ] **Step 12: 创建 login/index.vue（企微 OAuth2 跳转逻辑）和两个 dashboard 占位页**

- [ ] **Step 13: 更新 main.ts（注册 Pinia、Router、ElementPlus、图标）和 App.vue（router-view）**

- [ ] **Step 14: 验证布局渲染，Commit**

```bash
git commit -m "feat: add layout framework, routing, auth stores, and login page"
```

---

### Task 4: 通用业务组件

**Files:**
- Create: `src/components/common/StatCard.vue`
- Create: `src/components/common/FilterBar.vue`
- Create: `src/components/common/EmptyState.vue`
- Create: `src/components/common/FloatingBar.vue`
- Create: `src/composables/useTable.ts`
- Create: `src/composables/useDialog.ts`

- [ ] **Step 1: StatCard — label/value/trend props，卡片样式**

- [ ] **Step 2: FilterBar — slot 插入筛选项 + 查询/重置按钮**

- [ ] **Step 3: EmptyState — el-empty 包装 + action slot**

- [ ] **Step 4: FloatingBar — 批量操作浮动条（固定底部，展示已选数量和操作按钮）**

- [ ] **Step 5: useTable — loading/list/total/selectedRows/query/loadData/resetQuery + 分页事件处理**

- [ ] **Step 6: useDialog — visible/editId/open/close**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add reusable components and composables"
```

---

### Task 5: 工作台首页

**Files:**
- Create: `src/types/dashboard.d.ts`
- Create: `src/api/dashboard.ts`
- Modify: `src/views/dashboard/workbench/index.vue`

- [ ] **Step 1: 实现 types + API**

WorkbenchStats、TodoItem、RecentAction 类型。getWorkbenchStats、getTodoList、getRecentActions 接口。

- [ ] **Step 2: 实现页面**

4个 StatCard → 待办事项表格 + 快捷操作网格（6个图标按钮） → 最近操作表格。Promise.all 加载数据，catch 时使用模拟数据兜底。

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: implement workbench page"
```

---

### Task 6: 客户管理页面

**Files:**
- Create: `src/types/customer.d.ts`
- Create: `src/api/customer.ts`
- Create: `src/views/customer/list/index.vue`
- Create: `src/views/customer/components/CustomerDrawer.vue`

- [ ] **Step 1: 实现 CustomerQuery/Customer/FollowRecord 类型和 API**

- [ ] **Step 2: 实现列表页**

StatCards → FilterBar（关键词/跟进人/标签/日期范围） → el-table（客户信息含头像+名称+企业、来源标签、标签、跟进人、状态、操作） → 分页。底部 FloatingBar 批量添加标签。

- [ ] **Step 3: 实现 CustomerDrawer（640px）**

基本信息网格、标签管理（可删除）、跟进时间线（el-timeline）、操作记录。

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: implement customer management with detail drawer"
```

---

### Task 7: 数据看板页面

**Files:**
- Create: `src/components/charts/TrendChart.vue`
- Create: `src/components/charts/PieChart.vue`
- Create: `src/components/charts/RankList.vue`
- Modify: `src/views/dashboard/data/index.vue`

- [ ] **Step 1: 创建三个 ECharts 组件**

TrendChart（柱状/折线切换、双系列）、PieChart（环形图+图例）、RankList（进度条列表）。

- [ ] **Step 2: 实现看板页面**

时间 Tab → 4 StatCard → 客户趋势柱状图 + 员工排行 → 消息类型饼图 + 群发效果摘要。

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: implement data dashboard with ECharts"
```

---

### Task 8: 应用配置页面

**Files:**
- Create: `src/types/config.d.ts`
- Create: `src/api/config.ts`
- Create: `src/views/config/app/index.vue`

- [ ] **Step 1: 实现卡片列表 + Dialog CRUD**

每个应用一张卡片（名称、AgentId、状态、操作）。Dialog 表单：corpId、corpSecret(密码框)、agentId、token、aesKey、测试连接按钮。

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement app configuration page"
```

---

### Task 9: 通讯录管理

**Files:**
- Create: `src/types/contact.d.ts`
- Create: `src/api/contact.ts`
- Create: `src/views/contact/department/index.vue`
- Create: `src/views/contact/user/index.vue`
- Create: `src/views/contact/tag/index.vue`

- [ ] **Step 1: 三个子页面**

部门管理：树形表格。用户管理：左侧部门树 + 右侧用户列表。标签管理：分组 + 标签列表。

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement contact management"
```

---

### Task 10: 消息推送页面

**Files:**
- Create: `src/types/message.d.ts`
- Create: `src/api/message.ts`
- Create: `src/views/message/push/index.vue`
- Create: `src/views/message/task/index.vue`

- [ ] **Step 1: 发送页 — 消息类型选择卡片 → 编辑区 → 左右分栏选范围 → 发送**

- [ ] **Step 2: 任务列表 — 筛选 + 表格（进度、状态、取消操作）**

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: implement message push and task pages"
```

---

### Task 11: 自动回复页面

**Files:**
- Create: `src/api/autoReply.ts`
- Create: `src/views/autoReply/welcome/index.vue`
- Create: `src/views/autoReply/keyword/index.vue`
- Create: `src/views/autoReply/default/index.vue`

- [ ] **Step 1: 三个 Tab 页面，共享规则表格 + Dialog 编辑。关键词回复支持拖拽排序。**

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement auto-reply management"
```

---

### Task 12: 素材管理页面

**Files:**
- Create: `src/types/material.d.ts`
- Create: `src/api/material.ts`
- Create: `src/views/material/index.vue`

- [ ] **Step 1: 左侧分类树(240px) + 右侧卡片网格(4列) + el-upload 拖拽上传**

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement material management"
```

---

### Task 13: 营销活动页面

**Files:**
- Create: `src/types/campaign.d.ts`
- Create: `src/api/campaign.ts`
- Create: `src/views/campaign/task/index.vue`
- Create: `src/views/campaign/sop/index.vue`
- Create: `src/views/campaign/journey/index.vue`

- [ ] **Step 1: 群发任务列表 + SOP话术列表 + 客户旅程看板（多列 grid 拖拽卡片）**

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement campaign management"
```

---

### Task 14: 系统管理页面

**Files:**
- Create: `src/types/system.d.ts`
- Create: `src/api/system.ts`
- Create: `src/views/system/user/index.vue`
- Create: `src/views/system/role/index.vue`
- Create: `src/views/system/menu/index.vue`
- Create: `src/views/system/log/index.vue`
- Create: `src/views/system/config/index.vue`

- [ ] **Step 1: el-tabs 切换五个子页面。角色管理含 el-tree 权限勾选。菜单管理为树形表格。**

- [ ] **Step 2: Commit**

```bash
git commit -m "feat: implement system management"
```

---

### Task 15: 路由完善与集成

**Files:**
- Modify: `src/router/routes.ts`

- [ ] **Step 1: 在 Layout children 中注册 Task 8-14 所有业务路由**

- [ ] **Step 2: 验证所有菜单跳转正常，页面无报错**

```bash
npm run dev
```

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: register all business routes"
```

---

### Task 16: 构建与部署

**Files:**
- Create: `wxcp-frontend/nginx.conf.example`

- [ ] **Step 1: 生产构建**

```bash
npm run build
```

- [ ] **Step 2: 创建 nginx.conf.example（SPA history 模式 + API 反向代理）**

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: add build config and nginx example"
```
