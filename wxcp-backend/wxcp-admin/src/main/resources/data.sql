-- WXCP Seed Data

-- Admin user (password: admin123, BCrypt hashed)
INSERT IGNORE INTO sys_user (id, username, password, real_name, status, role_id)
VALUES (1, 'admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1C9e', '系统管理员', 1, 1);

-- Super admin role
INSERT IGNORE INTO sys_role (id, name, role_key, description)
VALUES (1, '超级管理员', 'admin', '拥有系统全部权限');

-- Operator role
INSERT IGNORE INTO sys_role (id, name, role_key, description)
VALUES (2, '运营人员', 'operator', '营销运营相关权限');

-- User-role association
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Menu tree (matching frontend sidebarMenuGroups from routes.ts)
-- Overview group
INSERT IGNORE INTO sys_menu (id, parent_id, name, icon, path, permission, type, sort) VALUES
(1, 0, '概览', 'Monitor', '/dashboard/workbench', '', 'directory', 1),
(2, 1, '工作台', 'Monitor', '/dashboard/workbench', 'dashboard:workbench', 'menu', 1),
(3, 1, '数据看板', 'DataAnalysis', '/dashboard/data', 'dashboard:data', 'menu', 2);

-- Marketing center group
INSERT IGNORE INTO sys_menu (id, parent_id, name, icon, path, permission, type, sort) VALUES
(10, 0, '营销中心', 'Promotion', '/customer/list', '', 'directory', 2),
(11, 10, '客户管理', 'User', '/customer/list', 'customer:list', 'menu', 1),
(12, 10, '消息推送', 'Promotion', '/message/push', 'message:push', 'menu', 2),
(13, 10, '任务列表', 'List', '/message/task', 'message:task', 'menu', 3),
(14, 10, '欢迎语', 'ChatLineRound', '/auto-reply/welcome', 'autoReply:welcome', 'menu', 4),
(15, 10, '关键词回复', 'ChatLineRound', '/auto-reply/keyword', 'autoReply:keyword', 'menu', 5),
(16, 10, '默认回复', 'ChatLineRound', '/auto-reply/default', 'autoReply:default', 'menu', 6),
(17, 10, '素材管理', 'PictureFilled', '/material/list', 'material:list', 'menu', 7),
(18, 10, '坐席二维码', 'Link', '/agent/qr', 'agent:qr', 'menu', 8),
(19, 10, '群发任务', 'Trophy', '/campaign/task', 'campaign:task', 'menu', 9),
(20, 10, 'SOP话术', 'ChatLineRound', '/campaign/sop', 'campaign:sop', 'menu', 10),
(21, 10, '客户旅程', 'MapLocation', '/campaign/journey', 'campaign:journey', 'menu', 11);

-- Data center group
INSERT IGNORE INTO sys_menu (id, parent_id, name, icon, path, permission, type, sort) VALUES
(30, 0, '数据中心', 'DataAnalysis', '/dashboard/data', '', 'directory', 3);

-- System settings group
INSERT IGNORE INTO sys_menu (id, parent_id, name, icon, path, permission, type, sort) VALUES
(40, 0, '系统设置', 'Setting', '/config/app', '', 'directory', 4),
(41, 40, '应用配置', 'Setting', '/config/app', 'config:app', 'menu', 1),
(42, 40, '部门管理', 'Connection', '/contact/department', 'contact:department', 'menu', 2),
(43, 40, '用户管理(企微)', 'UserFilled', '/contact/user', 'contact:user', 'menu', 3),
(44, 40, '标签管理', 'PriceTag', '/contact/tag', 'contact:tag', 'menu', 4),
(45, 40, '用户管理(系统)', 'Lock', '/system/user', 'system:user', 'menu', 5),
(46, 40, '角色管理', 'Lock', '/system/role', 'system:role', 'menu', 6),
(47, 40, '菜单管理', 'Lock', '/system/menu', 'system:menu', 'menu', 7),
(48, 40, '操作日志', 'Lock', '/system/log', 'system:log', 'menu', 8),
(49, 40, '系统配置', 'Lock', '/system/config', 'system:config', 'menu', 9);

-- System config default values
INSERT IGNORE INTO sys_config (config_name, config_key, config_value, config_group, remark) VALUES
('系统名称', 'sys.name', '企微营销管理平台', 'basic', '平台显示名称'),
('消息发送间隔(ms)', 'message.send.interval', '200', 'message', '消息发送最小间隔毫秒数'),
('每日发送上限', 'message.daily.limit', '10000', 'message', '单日消息发送上限'),
('客户跟进提醒(天)', 'customer.followup.remind', '3', 'customer', '未跟进提醒天数');
