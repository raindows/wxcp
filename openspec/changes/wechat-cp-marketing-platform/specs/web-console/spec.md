## ADDED Requirements

### Requirement: 前端项目初始化
系统 SHALL 基于Vue 3 + Vite + Element Plus搭建前端管理控制台项目。

#### Scenario: 项目启动
- **WHEN** 开发者执行npm install && npm run dev
- **THEN** 前端开发服务器启动，可通过浏览器访问管理后台页面

#### Scenario: 生产构建
- **WHEN** 开发者执行npm run build
- **THEN** 系统输出静态资源到dist目录，支持Nginx部署

### Requirement: 布局框架
系统 SHALL 提供经典管理后台布局，包含左侧导航菜单、顶部导航栏和内容区域。

#### Scenario: 左侧导航菜单
- **WHEN** 用户登录系统
- **THEN** 系统展示左侧导航菜单，菜单项根据用户权限动态渲染，支持菜单折叠/展开

#### Scenario: 顶部导航栏
- **WHEN** 用户登录系统
- **THEN** 系统展示顶部导航栏，包含面包屑导航、当前用户信息、消息通知入口、退出登录按钮

#### Scenario: 响应式布局
- **WHEN** 浏览器窗口宽度小于768px
- **THEN** 系统自动将左侧菜单收起为抽屉模式，点击汉堡按钮展开

### Requirement: 路由与权限控制
系统 SHALL 基于Vue Router实现前端路由，路由与后端菜单权限联动。

#### Scenario: 未登录跳转
- **WHEN** 用户访问任意页面且JWT Token不存在或已过期
- **THEN** 系统自动跳转到企业微信OAuth2登录页面

#### Scenario: 无权限访问
- **WHEN** 用户访问其角色权限之外的页面路由
- **THEN** 系统展示403无权限提示页面

#### Scenario: 动态路由加载
- **WHEN** 用户登录成功
- **THEN** 系统根据用户角色权限动态加载对应路由，未授权路由不可访问

### Requirement: 统一请求封装
系统 SHALL 封装统一的HTTP请求层（Axios），集成Token管理和错误处理。

#### Scenario: 请求自动携带Token
- **WHEN** 前端发起API请求
- **THEN** Axios拦截器自动在请求头中附加Authorization: Bearer {jwtToken}

#### Scenario: Token过期自动刷新
- **WHEN** API返回401未授权状态码
- **THEN** 系统尝试使用刷新Token获取新的JWT，成功则重试原请求，失败则跳转登录页

#### Scenario: 统一错误处理
- **WHEN** API返回错误响应
- **THEN** 系统统一展示错误提示消息（Element Plus Message），网络异常时提示"网络连接失败"

### Requirement: 首页工作台
系统 SHALL 提供登录后的首页工作台，展示关键数据概览和快捷操作入口。

#### Scenario: 查看工作台
- **WHEN** 用户登录后进入首页
- **THEN** 系统展示待办事项、关键数据指标（今日新增客户、待跟进客户、进行中的群发任务）、快捷操作入口
