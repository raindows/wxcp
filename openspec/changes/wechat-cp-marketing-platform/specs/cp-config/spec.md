## ADDED Requirements

### Requirement: 企业微信应用配置管理
系统 SHALL 提供企业微信应用配置的CRUD管理，包括corpId、corpSecret、agentId、token、aesKey等核心参数。

#### Scenario: 新增应用配置
- **WHEN** 管理员填写完整的企微应用配置信息并提交
- **THEN** 系统保存配置到数据库，corpSecret使用AES加密存储，并验证配置有效性（调用获取AccessToken接口）

#### Scenario: 配置有效性校验
- **WHEN** 管理员点击"测试连接"按钮
- **THEN** 系统使用当前配置尝试获取AccessToken，成功则提示配置有效，失败则提示具体错误原因

#### Scenario: 编辑应用配置
- **WHEN** 管理员修改已有配置并保存
- **THEN** 系统更新配置，同时刷新Redis中的WxCpService实例缓存

### Requirement: 多应用支持
系统 SHALL 支持在同一企业（corpId）下配置多个企业微信应用（不同agentId）。

#### Scenario: 新增同corpId下的第二个应用
- **WHEN** 管理员新增一个与已有配置相同corpId但不同agentId的配置
- **THEN** 系统保存新配置，并通过agentId区分不同WxCpService实例

#### Scenario: 切换活跃应用
- **WHEN** 管理员在后台选择某个应用设为"当前操作应用"
- **THEN** 系统后续的API调用（同步、群发等）使用该应用的WxCpService实例

### Requirement: WxCpService动态管理
系统 SHALL 在配置变更时动态创建或刷新WxCpService实例，无需重启服务。

#### Scenario: 新增配置后自动初始化Service
- **WHEN** 管理员新增一个应用配置并保存成功
- **THEN** 系统自动创建对应的WxCpServiceImpl实例（使用WxCpRedissonConfigImpl），注册到Service容器中

#### Scenario: 删除配置后清理Service
- **WHEN** 管理员删除一个应用配置
- **THEN** 系统从Service容器中移除对应的WxCpService实例
