## Context

本项目为全新构建的企业微信营销管理平台（Web端），基于 `weixin-java-cp 4.8.0` SDK 对接企业微信API。平台面向企业运营人员，提供客户管理、消息群发、营销活动、数据看板等一站式营销能力。

当前企业微信官方仅提供基础管理后台，缺乏面向营销场景的专项工具（如SOP话术、客户旅程、群发任务调度等）。本平台填补这一空白。

## Goals / Non-Goals

**Goals:**

- 构建稳定可靠的企业微信营销Web管理平台
- 支持多应用配置，一套系统可对接多个企业微信应用
- 提供完整的客户生命周期管理（添加→跟进→标签→群发→分析）
- 支持消息群发、自动回复、SOP话术等营销自动化能力
- 提供可视化数据看板，辅助营销决策
- 前后端分离架构，前端Vue 3 + Element Plus，后端Spring Boot 3.x

**Non-Goals:**

- 不替代企业微信原生管理后台的核心功能（审批、打卡等）
- 不做移动端App，仅提供Web管理端
- 不涉及企业微信会话存档（合规敏感领域）
- 不涉及企业微信直播功能
- 首期不做多租户SaaS模式，采用单部署单企业模式（架构预留多企业扩展）

## Decisions

### D1: 技术栈选型

| 层级 | 技术 | 理由 |
|------|------|------|
| 后端框架 | Spring Boot 3.x + Java 17 | 生态成熟，与WxJava兼容性好 |
| ORM | MyBatis-Plus 3.5.x | 灵活可控，国内Java生态主流选择 |
| 数据库 | MySQL 8.0 | 成熟稳定，运维成本低 |
| 缓存 | Redis 7.x (Redisson) | WxJava推荐Redis存储Token，同时用于缓存和分布式锁 |
| 前端 | Vue 3 + Vite + Element Plus | 国内管理后台主流方案，组件丰富 |
| 企业微信SDK | weixin-java-cp 4.8.0 | 用户指定版本，API覆盖全面 |
| 认证 | 企业微信OAuth2 + JWT | 基于企微身份认证，JWT管理会话 |
| 定时任务 | XXL-JOB | 分布式任务调度，支持群发任务管理 |
| 文件存储 | MinIO / 阿里云OSS | 营销素材存储，可插拔 |

**备选方案**：后端考虑过Spring Cloud微服务架构，但首期业务体量不需要微服务拆分的复杂度，采用单体模块化架构（Maven多模块），后续可按需拆分。

### D2: 项目模块结构

```
wxcp-parent/
├── wxcp-common/          # 公共模块（工具类、基础实体、统一响应）
├── wxcp-auth/            # 认证授权模块（OAuth2、JWT、RBAC）
├── wxcp-cp/              # 企业微信对接模块（WxCpService封装、回调处理）
├── wxcp-contact/         # 通讯录模块（部门、用户、标签同步与管理）
├── wxcp-customer/        # 客户管理模块（外部联系人、客户群、跟进记录）
├── wxcp-message/         # 消息推送模块（主动发送、群发任务、消息记录）
├── wxcp-material/        # 素材管理模块（图文、图片、视频、文件）
├── wxcp-campaign/        # 营销活动模块（SOP话术、客户旅程、活动管理）
├── wxcp-dashboard/       # 数据看板模块（统计聚合、报表）
├── wxcp-system/          # 系统管理模块（用户、角色、菜单、日志、配置）
├── wxcp-admin/           # 后台管理Web模块（Spring Boot启动入口，Controller层）
└── wxcp-frontend/        # 前端Vue 3项目
```

### D3: 企业微信集成架构

```
┌─────────────┐     OAuth2      ┌──────────────┐
│  企业微信客户端 │ ◄────────────► │  Web管理后台   │
└──────┬──────┘                └──────┬───────┘
       │                              │
       │ 消息回调(POST)                │ WxCpService API调用
       │                              │
       ▼                              ▼
┌──────────────┐    Redis共享Token    ┌──────────────┐
│  WxMsgRouter  │ ◄──────────────────► │  MySQL        │
│  (消息路由)    │                     │  (业务数据)    │
└──────────────┘                     └──────────────┘
```

- 使用 `WxCpRedissonConfigImpl` 存储AccessToken，支持集群部署
- 回调消息通过 `WxCpMessageRouter` 路由到各Handler处理
- Handler异步处理消息事件，写入MySQL持久化

### D4: 数据库设计原则

- 所有表包含 `id`(BIGINT自增)、`create_time`、`update_time`、`create_by`、`update_by`、`deleted`(逻辑删除) 公共字段
- 外部联系人数据以企业微信 `external_userid` 为业务唯一键
- 企业微信回调事件写入事件日志表，支持幂等处理
- 群发任务使用任务表 + 任务明细表（一对多）设计

### D5: 前端架构

- 基于Vue 3 Composition API + TypeScript
- 使用Pinia状态管理，Vue Router路由管理
- 封装统一的HTTP请求层（Axios），集成JWT Token自动刷新
- 布局采用经典管理后台左侧菜单 + 顶部导航 + 内容区结构
- 页面按功能模块拆分为独立路由

### D6: 安全设计

- 管理后台通过企业微信OAuth2扫码登录，仅允许企业内部成员访问
- API接口基于JWT鉴权，支持RBAC权限控制
- 企业微信回调URL验证（签名校验）
- 敏感配置（corpSecret）加密存储，不落明文日志

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| 企业微信API调用频率限制导致消息发送失败 | 实现请求队列 + 限流控制，群发任务分批执行 |
| AccessToken过期导致API调用失败 | 使用Redis自动续期，WxJava内置Token管理 |
| 多实例部署Token竞争 | 使用Redisson分布式锁，WxCpRedissonConfigImpl |
| 大量客户数据同步性能 | 增量同步 + 分页处理，避免全量拉取 |
| 企业微信回调消息丢失 | 事件日志表幂等设计，支持重试 |
| 单体架构后期扩展瓶颈 | Maven多模块设计，核心模块低耦合，可按需拆分为微服务 |

## Open Questions

- 文件存储方案：MinIO私有化部署 vs 阿里云OSS，需根据部署环境决定
- 定时任务方案：XXL-JOB vs Spring @Scheduled，需评估运维成本
- 是否需要对接企业微信客服（kf）功能
- 首期是否需要数据导出（Excel）能力
