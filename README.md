# WXCP - 企业微信营销平台

基于 [WxJava](https://github.com/binarywang/WxJava)（weixin-java-cp 4.8.0）构建的企业微信营销管理平台，提供客户管理、消息群发、营销活动、数据看板等一站式营销能力。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.x / Java 17 |
| 企业微信SDK | [weixin-java-cp 4.8.0](https://github.com/binarywang/WxJava) |
| ORM | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x (Redisson) |
| 前端 | Vue 3 + Vite + Element Plus + TypeScript |
| 认证 | 企业微信 OAuth2 + JWT |
| 定时任务 | XXL-JOB |
| 文件存储 | MinIO / 阿里云 OSS |

## 功能模块

```
wxcp-parent/
├── wxcp-common/          # 公共模块（工具类、统一响应、全局异常）
├── wxcp-auth/            # 认证授权（OAuth2、JWT、RBAC）
├── wxcp-cp/              # 企业微信对接（WxCpService封装、回调处理）
├── wxcp-contact/         # 通讯录（部门、用户、标签同步与管理）
├── wxcp-customer/        # 客户管理（外部联系人、客户群、跟进记录）
├── wxcp-message/         # 消息推送（主动发送、群发任务、发送记录）
├── wxcp-material/        # 素材管理（图文、图片、视频、文件）
├── wxcp-campaign/        # 营销活动（SOP话术、客户旅程、活动管理）
├── wxcp-dashboard/       # 数据看板（统计聚合、报表、导出）
├── wxcp-system/          # 系统管理（用户、角色、菜单、日志、配置）
├── wxcp-admin/           # 后台管理Web模块（启动入口、Controller层）
└── wxcp-frontend/        # 前端 Vue 3 项目
```

### 核心能力

- **应用配置管理** — 多应用、多企业支持，配置热更新无需重启
- **通讯录同步** — 部门树、用户列表、标签的全量/增量同步
- **客户联系管理** — 外部联系人、客户群、跟进记录、客户标签
- **消息推送与群发** — 文本/图文/Markdown/任务卡片，按标签或部门群发，API限流
- **自动回复** — 欢迎语、关键词匹配回复、兜底回复
- **营销素材** — 图片/图文/视频/文件分类管理
- **营销活动** — 群发任务调度、SOP话术模板、客户旅程规则引擎
- **数据看板** — 客户统计、消息统计、员工活跃度、Excel导出
- **系统管理** — RBAC权限、操作审计日志、系统参数配置

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x+
- Node.js 18+

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/raindows/wxcp.git
cd wxcp

# 2. 创建数据库并导入初始化脚本
mysql -u root -p < doc/sql/init.sql

# 3. 修改配置文件
vi wxcp-admin/src/main/resources/application.yml
# 配置数据库连接、Redis连接、企业微信 corpId/secret/agentId 等

# 4. 编译并启动
mvn clean install -DskipTests
java -jar wxcp-admin/target/wxcp-admin.jar
```

### 前端启动

```bash
cd wxcp-frontend

# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```

### 企业微信配置

1. 登录 [企业微信管理后台](https://work.weixin.qq.com/)
2. 创建自建应用，获取 `AgentId` 和 `Secret`
3. 设置应用回调 URL：`http://{your-domain}/wx/cp/portal/{agentId}`
4. 在本平台管理后台填入 `corpId`、`corpSecret`、`agentId`、`token`、`aesKey`

## 架构概览

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

## 参考资料

- [WxJava Wiki](https://github.com/binarywang/WxJava/wiki)
- [weixin-java-cp Demo](https://github.com/binarywang/weixin-java-cp-demo)
- [企业微信开发文档](https://developer.work.weixin.qq.com/document/)

## License

MIT
