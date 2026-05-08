## ADDED Requirements

### Requirement: 部门同步与管理
系统 SHALL 支持从企业微信同步部门树结构，并在本地维护部门数据。

#### Scenario: 全量同步部门
- **WHEN** 管理员触发"同步部门"操作
- **THEN** 系统调用WxCpDepartmentService获取完整部门列表，同步到本地数据库，以企业微信departmentId为唯一键进行upsert

#### Scenario: 增量同步部门（回调触发）
- **WHEN** 企业微信推送部门变更回调事件（创建/修改/删除）
- **THEN** 系统根据回调中的departmentId更新本地对应部门数据

#### Scenario: 查看部门树
- **WHEN** 用户打开部门管理页面
- **THEN** 系统以树形结构展示所有部门，支持展开/折叠和搜索

### Requirement: 用户同步与管理
系统 SHALL 支持从企业微信同步用户列表，关联用户与部门关系。

#### Scenario: 全量同步用户
- **WHEN** 管理员触发"同步用户"操作
- **THEN** 系统调用WxCpUserService按部门分页拉取用户列表，同步到本地数据库，以企业微信userid为唯一键

#### Scenario: 增量同步用户（回调触发）
- **WHEN** 企业微信推送用户变更回调事件（创建/修改/删除）
- **THEN** 系统根据回调中的userid更新本地对应用户数据

#### Scenario: 查看部门下的用户列表
- **WHEN** 用户选择某个部门节点
- **THEN** 系统展示该部门下的所有用户列表，包含姓名、职位、手机号、状态等信息

### Requirement: 标签同步与管理
系统 SHALL 支持从企业微信同步标签数据，并允许本地创建和管理标签分组。

#### Scenario: 同步标签
- **WHEN** 管理员触发"同步标签"操作
- **THEN** 系统调用WxCpTagService获取所有标签和标签分组，同步到本地数据库

#### Scenario: 创建标签
- **WHEN** 管理员在本地新建一个标签并指定所属标签组
- **THEN** 系统保存到本地数据库，同时调用企业微信API创建标签，并保存返回的tagId

#### Scenario: 为用户打标签
- **WHEN** 管理员选择多个用户并添加标签
- **THEN** 系统调用企业微信API批量打标签，本地记录标签关联关系
