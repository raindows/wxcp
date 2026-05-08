## Why

企业微信已成为企业内部沟通和客户运营的核心渠道，但当前缺乏一个统一的Web管理平台来支撑营销场景。通过搭建基于WxJava SDK的企业微信营销平台，企业可以实现消息群发、客户管理、内容营销、数据看板等核心营销能力，提升企业微信生态下的运营效率和客户触达能力。

## What Changes

- 新建一个基于Spring Boot 3.x + weixin-java-cp 4.8.0的Web应用，提供企业微信营销管理后台
- 实现企业微信应用配置管理（多应用支持、多企业支持）
- 实现通讯录同步与管理（部门、用户、标签）
- 实现客户联系管理（外部联系人、客户群、跟进记录）
- 实现消息推送与群发能力（文本、图文、markdown、任务卡片等消息类型）
- 实现欢迎语与自动回复管理
- 实现营销素材管理（图文、图片、视频、文件等）
- 实现营销活动管理（群发任务、SOP话术、客户旅程）
- 实现数据统计分析与可视化看板
- 实现系统管理（权限、操作日志、配置）

## Capabilities

### New Capabilities

- `cp-config`: 企业微信应用配置管理（corpId/secret/agentId等配置，多应用、多企业支持）
- `contact-sync`: 通讯录同步与管理（部门树、用户列表、标签管理的同步与维护）
- `customer-management`: 客户联系管理（外部联系人列表、客户群管理、客户跟进记录、客户标签）
- `message-push`: 消息推送与群发（主动消息发送、按标签/部门群发、多种消息类型支持）
- `auto-reply`: 欢迎语与自动回复（新客户欢迎语、关键词自动回复、收到消息自动回复）
- `material-management`: 营销素材管理（图文素材、图片、视频、文件的CRUD与分类管理）
- `campaign-management`: 营销活动管理（群发任务调度、SOP话术模板、客户旅程规则引擎）
- `data-dashboard`: 数据统计与可视化（客户数据、消息发送数据、员工活跃度等多维统计看板）
- `system-admin`: 系统管理（RBAC权限、操作审计日志、系统参数配置）
- `web-console`: Web管理控制台（前端页面、路由、布局、鉴权拦截）

### Modified Capabilities

<!-- 本项目为新建项目，无现有能力需要修改 -->

## Impact

- **新建项目**：基于Spring Boot 3.x的全新Java Web应用
- **核心依赖**：`com.github.binarywang:weixin-java-cp:4.8.0`、Spring Boot 3.x、MyBatis-Plus、Redis、MySQL
- **前端技术**：Vue 3 + Element Plus（管理后台前端）
- **API影响**：新建RESTful API，通过企业微信OAuth2进行用户认证
- **部署影响**：需配置企业微信回调URL，需外网可访问地址，需Redis集群部署支持
- **数据影响**：新建MySQL数据库，存储营销数据、配置数据、操作日志等
